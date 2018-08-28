#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <jni.h>

#include "SDL.h"
#include "SDL_thread.h"

#include <android/log.h>
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"ywl5320",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"ywl5320",FORMAT,##__VA_ARGS__);

#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/mathematics.h"
#include "libavutil/samplefmt.h"

#define SDL_AUDIO_BUFFER_SIZE 1024
#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000

int quit = 0;//0:播放 1：暂停 -1：结束
int play = 0;//0:init 1：播放
int isOver = 0;//0：没有 1：完了
int errorStep = -1;

void release();
void onErrorMsg(int code, char *msg);

typedef struct PacketQueue
{
	AVPacketList *first_pkt, *last_pkt;
	int nb_packets;
	int size;
	SDL_mutex *mutex;
	SDL_cond *cond;
}PacketQueue;

void packet_queue_init(PacketQueue *q) {
	memset(q, 0, sizeof(PacketQueue));
	q->mutex = SDL_CreateMutex();
	q->cond = SDL_CreateCond();
}


int packet_queue_put(PacketQueue *q, AVPacket *pkt) {

	if(quit == -1)
		return -1;
	AVPacketList *pkt1;
	if (av_dup_packet(pkt) < 0) {
		return -1;
	}
	pkt1 = av_malloc(sizeof(AVPacketList));
	if (!pkt1)
		return -1;
	pkt1->pkt = *pkt;
	pkt1->next = NULL;

	SDL_LockMutex(q->mutex);

	if (!q->last_pkt)
		q->first_pkt = pkt1;
	else
		q->last_pkt->next = pkt1;
	q->last_pkt = pkt1;
	q->nb_packets++;
	q->size += pkt1->pkt.size;
	SDL_CondSignal(q->cond);

	SDL_UnlockMutex(q->mutex);
	return 0;
}

static int packet_queue_get(PacketQueue *q, AVPacket *pkt) {
	if(quit == -1)
		return -1;
	AVPacketList *pkt1;
	int ret;
	SDL_LockMutex(q->mutex);

	for (;;) {
		pkt1 = q->first_pkt;
		if (pkt1) {
			q->first_pkt = pkt1->next;
			if (!q->first_pkt)
				q->last_pkt = NULL;
			q->nb_packets--;
			q->size -= pkt1->pkt.size;
			*pkt = pkt1->pkt;
			av_free(pkt1);
			ret = 1;
			break;
		}else if(quit == -1){
			ret = -1;
			break;
		}
		else {
			SDL_CondWait(q->cond, q->mutex);
		}
	}
	SDL_UnlockMutex(q->mutex);
	return ret;
}

int getQueueSize(PacketQueue *q)
{
	return q->nb_packets;
}

typedef struct PlayerState
{
	char *url;
	SDL_Thread *decodeThread;
	AVFormatContext *pFormatCtx;//封装格式上下文
	int audioStreamIndex;//音频流索引（暂时处理一个音频流）
	AVStream *audioStream;//音频流
	int audioDuration;//时长
	int audioPts;
	SDL_AudioSpec wanted_spec, spec;
	AVCodecContext *audioCodecCtx;//音频解码器上下文
	AVCodec *audioCodec;//音频解码器
	PacketQueue audioq;//音频队列


}PlayerState;


PlayerState *playerState;


int audio_decode_frame(AVCodecContext *aCodecCtx, uint8_t *audio_buf, int buf_) {

	if(quit == -1)
	{
		return -1;
	}
	AVFrame *frame = av_frame_alloc();
	int data_size = 0;
	AVPacket pkt;
	int got_frame_ptr;

	SwrContext *swr_ctx;

	if (packet_queue_get(&playerState->audioq, &pkt) < 0)
		return -1;

	int ret = avcodec_send_packet(aCodecCtx, &pkt);
	if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF)
		return -1;

	ret = avcodec_receive_frame(aCodecCtx, frame);
	if (ret < 0 && ret != AVERROR_EOF)
		return -1;

	// 设置通道数或channel_layout
	if (frame->channels > 0 && frame->channel_layout == 0)
		frame->channel_layout = av_get_default_channel_layout(frame->channels);
	else if (frame->channels == 0 && frame->channel_layout > 0)
		frame->channels = av_get_channel_layout_nb_channels(frame->channel_layout);

	enum AVSampleFormat dst_format = AV_SAMPLE_FMT_S16;//av_get_packed_sample_fmt((AVSampleFormat)frame->format);

	//重采样为立体声
	Uint64 dst_layout = AV_CH_LAYOUT_STEREO;
	// 设置转换参数
	swr_ctx = swr_alloc_set_opts(NULL, dst_layout, dst_format, frame->sample_rate,
		frame->channel_layout, (enum AVSampleFormat)frame->format, frame->sample_rate, 0, NULL);
	if (!swr_ctx || swr_init(swr_ctx) < 0)
		return -1;

	// 计算转换后的sample个数 a * b / c
	int dst_nb_samples = av_rescale_rnd(swr_get_delay(swr_ctx, frame->sample_rate) + frame->nb_samples, frame->sample_rate, frame->sample_rate, AV_ROUND_INF);
	// 转换，返回值为转换后的sample个数
	int nb = swr_convert(swr_ctx, &audio_buf, dst_nb_samples, (const uint8_t**)frame->data, frame->nb_samples);

	//根据布局获取声道数
	int out_channels = av_get_channel_layout_nb_channels(dst_layout);
	data_size = out_channels * nb * av_get_bytes_per_sample(dst_format);
	playerState->audioPts = pkt.pts;
	av_packet_unref(&pkt);
	av_frame_free(&frame);
	swr_free(&swr_ctx);
	return data_size;
}


void audio_callback(void *userdata, Uint8 *stream, int len) {


	AVCodecContext *aCodecCtx = (AVCodecContext *)userdata;

	int len1, audio_size;

	static uint8_t audio_buff[(AVCODEC_MAX_AUDIO_FRAME_SIZE * 3) / 2];
	static unsigned int audio_buf_size = 0;
	static unsigned int audio_buf_index = 0;

	SDL_memset(stream, 0, len);

	if(quit == 1 || quit == -1)
	{
		SDL_PauseAudio(0);
		memset(audio_buff, 0, audio_buf_size);
		SDL_MixAudio(stream, audio_buff + audio_buf_index, len, 0);
		return;
	}

//	LOGI("pkt nums: %d    queue size: %d\n", playerState->audioq.nb_packets, playerState->audioq.size);
	while (len > 0)// 想设备发送长度为len的数据
	{
		if (audio_buf_index >= audio_buf_size) // 缓冲区中无数据
		{
			// 从packet中解码数据
			audio_size = audio_decode_frame(aCodecCtx, audio_buff, sizeof(audio_buff));
			if (audio_size < 0) // 没有解码到数据或出错，填充0
			{
				audio_buf_size = 0;
				memset(audio_buff, 0, audio_buf_size);
			}
			else
				audio_buf_size = audio_size;

			audio_buf_index = 0;
		}
		len1 = audio_buf_size - audio_buf_index; // 缓冲区中剩下的数据长度
		if (len1 > len) // 向设备发送的数据长度为len
			len1 = len;

		SDL_MixAudio(stream, audio_buff + audio_buf_index, len, SDL_MIX_MAXVOLUME);

		len -= len1;
		stream += len1;
		audio_buf_index += len1;
	}
}

int decodeFile(void *args)
{
//	LOGI("decode ...");
	if (avformat_open_input(&playerState->pFormatCtx, playerState->url, NULL, NULL) != 0)
	{
//		LOGE("can not open url:%s", playerState->url);
		onErrorMsg(0x1002, "can not open the source url!");
        errorStep = 1;
		return -1;
	}
//	LOGI("here ...");
	if (avformat_find_stream_info(playerState->pFormatCtx, NULL) < 0)
	{
//		LOGE("can not find streams from %s", playerState->url);
		onErrorMsg(0x1003, "can not find streams from the source url!");
        errorStep = 2;
		return -1;
	}
//	LOGI("here2 ...");
	int i = 0;
	for (; i < playerState->pFormatCtx->nb_streams; i++)
	{
		if (playerState->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			playerState->audioStreamIndex = i;
			break;
		}
	}
//	LOGI("here3 ...");
	if (playerState->audioStreamIndex == -1)
	{
//		LOGE("can not find audio streams from %s", playerState->url);
		onErrorMsg(0x1004, "can not find audio streams from the source url!");
        errorStep = 3;
		return -1;
	}

	playerState->audioStream = playerState->pFormatCtx->streams[playerState->audioStreamIndex];

	playerState->audioDuration = playerState->pFormatCtx->duration / 1000000;
//	LOGI("duration:%d", playerState->audioDuration);
//	int h = playerState->audioDuration / 3600;
//	int m = (playerState->audioDuration - 3600 * h) / 60;
//	int s = playerState->audioDuration - 3600 * h - m * 60;
//	LOGI("%02d:%02d:%02d", h, m, s);

	AVCodecContext* pCodecCtxOrg;
	pCodecCtxOrg = playerState->pFormatCtx->streams[playerState->audioStreamIndex]->codec; // codec context
	playerState->audioCodec = avcodec_find_decoder(pCodecCtxOrg->codec_id);
	if (!playerState->audioCodec)
	{
//		LOGE("can not find audio %d codecctx!", playerState->audioStreamIndex);
		onErrorMsg(0x1005, "can not find audio codecctx!");
		play = 1;
        errorStep = 4;
		return -1;
	}
//	LOGI("here4 ...");
	// 不直接使用从AVFormatContext得到的CodecContext，要复制一个
	playerState->audioCodecCtx = avcodec_alloc_context3(playerState->audioCodec);
	if (avcodec_copy_context(playerState->audioCodecCtx, pCodecCtxOrg) != 0)
	{
//		LOGE("Could not copy codec context!");
		onErrorMsg(0x1006, "Could not copy codec context!");
        errorStep = 5;
		return -1;
	}
	avcodec_free_context(&pCodecCtxOrg);

	//initaudio sdl

	playerState->wanted_spec.freq = playerState->audioCodecCtx->sample_rate;
	playerState->wanted_spec.format = AUDIO_S16SYS;
	playerState->wanted_spec.channels = 2;
	playerState->wanted_spec.silence = 0;
	playerState->wanted_spec.samples = SDL_AUDIO_BUFFER_SIZE;
	playerState->wanted_spec.callback = audio_callback;
	playerState->wanted_spec.userdata = playerState->audioCodecCtx;
	if(SDL_OpenAudio(&playerState->wanted_spec, &playerState->spec) < 0)
	{
//		LOGE("sdl open audio failed:");
		onErrorMsg(0x1007, "sdl open audio failed!");
        errorStep = 6;
		return -1;
	}
	SDL_PauseAudio(0);

	if(avcodec_open2(playerState->audioCodecCtx, playerState->audioCodec, NULL) != 0)
	{
//		LOGE("open audio codec fail");
		onErrorMsg(0x1008, "open audio codec fail!");
        errorStep = 7;
		return -1;
	}
	onParpred();
	AVPacket packet;
	int index = 0;
	while (1)
	{
		if(quit == -1)
		{
			break;
		}
		if(play == 0)
		{
			continue;
		}
		if(playerState)
		{
			if (getQueueSize(&playerState->audioq) < 50)
			{
				int ret = av_read_frame(playerState->pFormatCtx, &packet);
				if (ret == 0)
				{
					isOver = 0;
					if (packet.stream_index == playerState->audioStreamIndex)
					{
						packet_queue_put(&playerState->audioq, &packet);
	//					LOGI("code %d", index++);
					}
					else
					{
						av_packet_unref(&packet);
					}
				}
				else if(ret == AVERROR_EOF)
				{
					isOver = 1;
					if(getQueueSize(&playerState->audioq) == 0)
					{
						quit = 1;
						onComplete();
						return 0;
					}
//					LOGE("right av_read_frame finished return %d", ret);
				}
				else
				{
//					LOGE("av_read_frame finished return %d", ret);
				}
			}
		}
	}
//	LOGI("here7 ...");

	return 0;
}

int avformat_interrupt_cb(void *ctx)
{
	if(quit == -1)
		return 1;
	return 0;
}


int main(int argc, char* args[])
{
	LOGI(".............come from main............");
//	LOGI("input url: %s", args[1]);
	quit = 0;
	play = 0;
	errorStep = -1;
	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER))
	{
		onErrorMsg(0x1001, "init sdl error!");
        errorStep = 0;
		return -1;
	}
	av_register_all();
	avformat_network_init();
	playerState = malloc(sizeof(PlayerState));
	packet_queue_init(&playerState->audioq);
	playerState->url = args[1];
	playerState->audioStreamIndex = -1;
	playerState->pFormatCtx = avformat_alloc_context();
	playerState->decodeThread = SDL_CreateThread(decodeFile, "decodeThread", NULL);
	playerState->pFormatCtx->interrupt_callback.callback = avformat_interrupt_cb;

	for(;;)
	{
		if(playerState)
		{
			if(quit == 0)
			{
				if(getQueueSize(&playerState->audioq) == 0)
				{
					if(isOver != 1)//退出
					{
//						LOGI("loading....");
						onLoad();
					}
				}
				else
				{
//					LOGI("plalying....");
					onPlay();
				}
			}
		}
		else{
			play = 1;
			return 0;
		}
		SDL_Delay(10);
	}
}

void JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlStart(JNIEnv* env, jclass jcls)
{
	if(play == 0)
	{
		play = 1;
	}
}

void JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlPause(JNIEnv* env, jclass jcls)
{
//	LOGI("pause");
	if(quit != 1 && isOver != 1)
	{
		quit = 1;
		if(playerState && playerState->pFormatCtx)
		{
			av_read_pause(playerState->pFormatCtx);
		}
	}
}
void JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlPlay(JNIEnv* env, jclass jcls)
{
//	LOGI("play");
	if(quit != 0 && isOver != 1)
	{
		quit = 0;
		if(playerState && playerState->pFormatCtx)
		{
			av_read_play(playerState->pFormatCtx);
		}
	}
}

jint JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlDuration(JNIEnv *env, jclass jcls)
{
	if(playerState)
	{
		if(playerState->audioDuration > 0)
		{
			return playerState->audioDuration;
		}
	}
	return 0;
}

//
void JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlRealease(JNIEnv* env, jclass jcls)
{
//	LOGI("release");
	release();

}

jint JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlNowTime(JNIEnv* env, jclass jcls)
{
	if(playerState && playerState->audioStream && getQueueSize(&playerState->audioq) > 0 && playerState->audioStream->time_base.den > 0)
	{
		return playerState->audioPts / playerState->audioStream->time_base.den;
	}
	return 0;
}

int JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlSeekTo(JNIEnv* env, jclass jcls, jint secds)
{
//	LOGI("wlSeekTo%d", secds);
	if(playerState && playerState->audioStream && secds < playerState->audioDuration && isOver != 1)
	{
		quit = 1;
		if(av_seek_frame(playerState->pFormatCtx, playerState->audioStreamIndex, secds * playerState->audioStream->time_base.den, AVSEEK_FLAG_ANY) >= 0)
		{
			playerState->audioq.first_pkt = NULL;
			playerState->audioq.last_pkt = NULL;
			playerState->audioq.nb_packets = 0;
			playerState->audioq.size = 0;
		}
		quit = 0;
		return 0;
	}
	return -1;
}

jint JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlIsInit(JNIEnv* env, jclass jcls)
{
	if(play == 0)
	{
		return -1;
	}
	return 0;
}

jint JNICALL Java_com_ywl5320_wlsdk_player_WlPlayer_wlIsRelease(JNIEnv* env, jclass jcls)
{
	return quit;
}

void release()
{
    quit = -1;
    play = 1;
    if(errorStep == 0 || errorStep == -1) {
        SDL_Quit();
        return;
    }
	SDL_CloseAudio();
	if(playerState != NULL) {

        if(errorStep == 1 || errorStep == 2 || errorStep == 3 || errorStep == 4) {

        } else{
            if (playerState->audioCodecCtx != NULL) {
                avcodec_free_context(playerState->audioCodecCtx);
                playerState->audioCodecCtx = NULL;
            }
			while (1) {
				AVPacket pkt;
				if (packet_queue_get(&playerState->audioq, &pkt) < 0) {
					break;
				}
				av_packet_unref(&pkt);
			}
        }
		if(playerState->pFormatCtx != NULL) {
			av_free(playerState->pFormatCtx);
			playerState->pFormatCtx = NULL;
		}
		av_free(playerState);
		playerState = NULL;
	}
	SDL_Quit();
}


void onErrorMsg(int code, char *msg)
{
	quit = -1;
	onError(code, msg);
	release();
}











