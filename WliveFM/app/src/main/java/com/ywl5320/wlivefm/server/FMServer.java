package com.ywl5320.wlivefm.server;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.MainActivity;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.mvp.LiveActivity;
import com.ywl5320.wlivefm.util.GlideApp;
import com.ywl5320.wlivefm.util.NotificationBarUtil;
import com.ywl5320.wlivefm.widget.GlideRoundTransform;
import com.ywl5320.wlsdk.player.WlPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl5320 on 2017/7/20.
 */

public class FMServer extends Service{

    private FmBinder binder = new FmBinder();
    private List<OnFmServerPlayListener> onFmServerPlayListeners;
    private boolean isFirstPlay = true;
    private boolean sendNotification = false;
    private int notificationId = 0x5320;
    private NotificationBarUtil notificationBarUtil;
    private int playStatus = -1;//0：准备 1：加载 2：播放 3：暂停 4：完成 5：出错
    private String url = "";
    private String name;
    private String subname;
    private boolean isInitPlayer = false;
    private LiveChannelBean liveChannelBean;

    private RemoteViews remoteViews;
    private Notification notification;

//    private NotificationBroadCast notificationBroadCast;

    public class FmBinder extends Binder
    {
        public FMServer getServer()
        {
            return FMServer.this;
        }
    }

    public void setLiveChannelBean(LiveChannelBean liveChannelBean) {
        this.liveChannelBean = liveChannelBean;
    }

    public LiveChannelBean getLiveChannelBean() {
        return liveChannelBean;
    }

    public void registerFmserverPlayListener(OnFmServerPlayListener onFmServerPlayListener) {
        if(onFmServerPlayListeners != null && onFmServerPlayListener != null)
        {
            if(!onFmServerPlayListeners.contains(onFmServerPlayListener))
            {
                onFmServerPlayListeners.add(onFmServerPlayListener);
            }
        }
    }

    public void unRegusterFmserverPlayListener(OnFmServerPlayListener onFmServerPlayListener)
    {
        if(onFmServerPlayListeners != null && onFmServerPlayListener != null)
        {
            if(onFmServerPlayListeners.contains(onFmServerPlayListener))
            {
                onFmServerPlayListeners.remove(onFmServerPlayListener);
            }
        }
    }

    public boolean isPlaying()
    {
        if(playStatus == 2)
        {
            return true;
        }
        return false;
    }

    public String getUrl()
    {
        if(TextUtils.isEmpty(url))
            return "";
        return url;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        notificationBarUtil = new NotificationBarUtil();
        onFmServerPlayListeners = new ArrayList<>();
        MyLog.d("fmserver oncreate...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d("binder server...");
        handler.postDelayed(runnable, 200);
        WlPlayer.setPrepardListener(new WlPlayer.OnPlayerPrepard() {
            @Override
            public void onPrepard() {
                isFirstPlay = false;
                WlPlayer.wlStart();
                playStatus = 0;
                for(OnFmServerPlayListener listener : onFmServerPlayListeners)
                {
                    if(listener != null)
                    {
                        listener.onPrepard();
                    }
                }
            }
        });
        WlPlayer.setOnCompleteListener(new WlPlayer.OnCompleteListener() {
            @Override
            public void onConplete() {
                playStatus = 4;
                if(FMApplication.getInstance().getFmServer() != null) {
                    for (OnFmServerPlayListener listener : onFmServerPlayListeners) {
                        if (listener != null) {
                            listener.onConplete();
                        }
                    }
                }
            }
        });
        WlPlayer.setOnErrorListener(new WlPlayer.OnErrorListener() {
            @Override
            public void onError(int i, String s) {
                WlPlayer.release();
//                playStatus = 5;
                MyLog.d("url is wrong......");
//                if(FMApplication.getInstance().getFmServer() != null) {
//                    for (OnFmServerPlayListener listener : onFmServerPlayListeners) {
//                        if (listener != null) {
//                            listener.onError(i, s);
//                        }
//                    }
//                }
            }
        });
        WlPlayer.setOnPlayerInfoListener(new WlPlayer.OnPlayerInfoListener() {
            @Override
            public void onLoad() {
                if(playStatus != 1) {
                    playStatus = 1;
                }
                for(OnFmServerPlayListener listener : onFmServerPlayListeners)
                {
                    if(listener != null)
                    {
                        listener.onLoad();
                    }
                }
            }

            @Override
            public void onPlay() {
                if(playStatus != 2) {
                    playStatus = 2;
                }
                for(OnFmServerPlayListener listener : onFmServerPlayListeners)
                {
                    if(listener != null)
                    {
                        listener.onPlay();
                    }
                }
            }
        });
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.d("onUnbind");
        onRelease();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.d("onDestroy");
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(runnable, 200);
            for(OnFmServerPlayListener listener : onFmServerPlayListeners)
            {
                try {
                    if(FMApplication.getInstance().getFmServer() != null)
                        listener.onInfo(WlPlayer.wlDuration(), WlPlayer.wlNowTime(), playStatus, name, subname);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    };

    public void sendNotify()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if(notificationBarUtil.isDarkNotificationBar(this))
        {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.notify_dark_layout);
        }
        else
        {
            remoteViews = new RemoteViews(this.getPackageName(), R.layout.notify_light_layout);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            remoteViews.setViewVisibility(R.id.iv_status, View.GONE);
        }
        else
        {
//            notificationBroadCast = new NotificationBroadCast();
//            IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction("CLICK_NOTIFICATION_ACTION");
//            this.registerReceiver(notificationBroadCast, intentFilter);

            Intent click_intent_status = new Intent();
            click_intent_status.setAction("CLICK_NOTIFICATION_ACTION");
            Bundle bundle_status = new Bundle();
            bundle_status.putInt("flag", 0);
            click_intent_status.putExtras(bundle_status);
            PendingIntent resultPendingIntent_status = PendingIntent.getBroadcast(this, 0x2001, click_intent_status, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.iv_status, resultPendingIntent_status);

            Intent click_intent_next = new Intent();
            click_intent_next.setAction("CLICK_NOTIFICATION_ACTION");
            Bundle bundle_next = new Bundle();
            bundle_next.putInt("flag", 1);
            click_intent_next.putExtras(bundle_next);
            PendingIntent resultPendingIntent_next = PendingIntent.getBroadcast(this, 0x2002, click_intent_next, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.iv_next, resultPendingIntent_next);
        }

        builder.setCustomContentView(remoteViews);
        Intent intent = new Intent(this, LiveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(Notification.FLAG_FOREGROUND_SERVICE);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setOngoing(true);
        notification = builder.build();

        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notificationId, notification);

    }

    public void initPlayer(Activity activity)
    {
        if(!isInitPlayer) {
            isInitPlayer = true;
            WlPlayer.initPlayer(activity);
            MyLog.d("init native player....");
        }
    }

    public void onPlay(String url, String name, String subname, String imgurl)
    {
        this.url = url;
        this.name = name;
        this.subname = subname;
        playStatus = 0;
        if(isFirstPlay) {
            isFirstPlay = false;
            WlPlayer.setDataSource(url);
            WlPlayer.prePard();
            if(!sendNotification) {
                sendNotification = true;
                sendNotify();
            }
        }
        else
        {
            WlPlayer.next(url);
        }
        if(!TextUtils.isEmpty(subname))
        {
            remoteViews.setTextViewText(R.id.tv_subtitle, subname);
        }
        remoteViews.setTextViewText(R.id.tv_title, name);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notificationId, notification);

        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                if(resource != null)
                {
                    remoteViews.setImageViewBitmap(R.id.iv_logo, resource);
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notificationId, notification);
                }
            }
        };
        GlideApp.with(this).asBitmap().load(imgurl).transform(new GlideRoundTransform(this, 40)).into(simpleTarget);
    }


    public void pause()
    {
        WlPlayer.wlPause();
        playStatus = 3;
        if(notificationBarUtil.isDarkNotificationBar(this))
        {
            remoteViews.setImageViewResource(R.id.iv_status, R.drawable.nofication_white_pause_selector);
        }
        else
        {
            remoteViews.setImageViewResource(R.id.iv_status, R.drawable.nofication_black_pause_selector);
        }
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notificationId, notification);
    }

    public void play()
    {
        WlPlayer.wlPlay();
        if(notificationBarUtil.isDarkNotificationBar(this))
        {
            remoteViews.setImageViewResource(R.id.iv_status, R.drawable.nofication_white_play_selector);
        }
        else
        {
            remoteViews.setImageViewResource(R.id.iv_status, R.drawable.nofication_black_play_selector);
        }
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(notificationId, notification);
    }

    public void setFirstPlay(boolean firstPlay) {
        isFirstPlay = firstPlay;
    }

    public void seekTo(int secds)
    {
        WlPlayer.wlSeekTo(secds);
    }

    public void btnPlay()
    {
        if(playStatus == 2)
        {
            pause();
        }
        else if(playStatus == 3)
        {
            play();
        }
        else
        {
            if(!TextUtils.isEmpty(url)) {
                WlPlayer.next(url);
            }
            Toast.makeText(this, "playstatus:" + playStatus, Toast.LENGTH_SHORT).show();
        }
    }

    public void onRelease()
    {
        playStatus = -1;
        handler.removeCallbacks(runnable);
        WlPlayer.release();
        if(sendNotification)
        {
            sendNotification = false;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    public interface OnFmServerPlayListener
    {
        void onPrepard();
        void onConplete();
        void onError(int i, String s);
        void onLoad();
        void onPlay();
        void onInfo(int totaltime, int nowtime, int status, String name, String subname);
    }

    public class NotificationBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(FMServer.this, "playstatus:" + playStatus, Toast.LENGTH_SHORT).show();
            if("CLICK_NOTIFICATION_ACTION".equals(intent.getAction()))
            {
                Bundle bundle = intent.getExtras();
                int flag = bundle.getInt("flag", -1);
                if(flag == 0)//播放按钮
                {
                    MyLog.d("play");
                    btnPlay();
                }
                else if(flag == 1)//下一个
                {
                    MyLog.d("next");
                }
                else
                {
//                    Toast.makeText(FMServer.this, "playstatus:" + playStatus, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
