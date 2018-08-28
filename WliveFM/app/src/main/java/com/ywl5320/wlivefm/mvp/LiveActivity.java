package com.ywl5320.wlivefm.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.MainActivity;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.base.BaseActivity;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.util.CommonUtil;
import com.ywl5320.wlivefm.widget.SquareImageView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ywl on 2017-7-26.
 */

public class LiveActivity extends BaseActivity{

    @BindView(R.id.iv_bg)
    ImageView ivBg;
    @BindView(R.id.siv_logo)
    SquareImageView sivLogo;
    @BindView(R.id.tv_live_name)
    TextView tvLiveName;
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.pb_load)
    ProgressBar pbLoad;

    private LiveChannelBean liveChannelBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getExtras() != null && FMApplication.getInstance().getFmServer() != null) {
            liveChannelBean = (LiveChannelBean) getIntent().getExtras().getSerializable("liveChannelBean");
            FMApplication.getInstance().getFmServer().setLiveChannelBean(liveChannelBean);
        }
        else if(FMApplication.getInstance().getFmServer() != null)
        {
            liveChannelBean =  FMApplication.getInstance().getFmServer().getLiveChannelBean();
        }

        if(liveChannelBean == null)
        {
            MainActivity.startActivity(this);
            this.finish();
            return;
        }

        FMApplication.getInstance().initPlayer(this);
        setContentView(R.layout.activity_play_layout);
        setTitle(liveChannelBean.getName());
        setBackView();
        setTitleTrans(R.color.colorTrans);
        setTitleLine(R.color.color_666666);
        tvLiveName.setText(liveChannelBean.getLiveSectionName());
        Glide.with(this).load(liveChannelBean.getImg()).into(sivLogo);
//        liveChannelBean.getStreams().get(0).setUrl("http://satellitepull.cnr.cn/live/wxscjtgb/playlist.m3u8");
        if(FMApplication.getInstance().getFmServer().getUrl().equals(liveChannelBean.getStreams().get(0).getUrl())) {
            if (!FMApplication.getInstance().getFmServer().isPlaying()) {
                FMApplication.getInstance().getFmServer().onPlay(liveChannelBean.getStreams().get(0).getUrl(), liveChannelBean.getName(), liveChannelBean.getLiveSectionName(), liveChannelBean.getImg());
            }
        }
        else
        {
            CommonUtil.saveLiveBeanInfo(this, liveChannelBean);
            FMApplication.getInstance().getFmServer().onPlay(liveChannelBean.getStreams().get(0).getUrl(), liveChannelBean.getName(), liveChannelBean.getLiveSectionName(), liveChannelBean.getImg());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(this).load(liveChannelBean.getImg()).into(sivLogo);
    }

    @Override
    public void onClickBack() {
        super.onClickBack();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
        liveChannelBean = null;
    }

    @OnClick(R.id.iv_play)
    public void onClickPlay(View view)
    {
        FMApplication.getInstance().getFmServer().btnPlay();
    }

    //0：准备 1：加载 2：播放 3：暂停 4：完成 5：出错
    @Override
    public void onInfo(int totaltime, int nowtime, int status, String name, String subname) {
        super.onInfo(totaltime, nowtime, status, name, subname);
        if(status == 2)
        {
            pbLoad.setVisibility(View.GONE);
            ivPlay.setVisibility(View.VISIBLE);
            ivPlay.setImageResource(R.mipmap.ic_play_pause);
        }
        else if(status == 1 || status == 0)
        {
            pbLoad.setVisibility(View.VISIBLE);
            ivPlay.setVisibility(View.INVISIBLE);
        }
        else if(status == 3 || status == 4)
        {
            pbLoad.setVisibility(View.GONE);
            ivPlay.setVisibility(View.VISIBLE);
            ivPlay.setImageResource(R.mipmap.ic_play_play);
        }
        else if(status == 5)
        {
            pbLoad.setVisibility(View.GONE);
            ivPlay.setVisibility(View.VISIBLE);
            ivPlay.setImageResource(R.mipmap.ic_play_play);
        }
    }

    public static void startActivity(Context context, LiveChannelBean liveChannelBean)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable("liveChannelBean", liveChannelBean);
        Intent intent = new Intent(context, LiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
