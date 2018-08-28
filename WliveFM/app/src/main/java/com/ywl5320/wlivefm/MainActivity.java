package com.ywl5320.wlivefm;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.ywl5320.wlivefm.adapter.MyAdapter;
import com.ywl5320.wlivefm.adapter.TypeAdapter;
import com.ywl5320.wlivefm.adapter.WapHeaderAndFooterAdapter;
import com.ywl5320.wlivefm.base.BaseActivity;
import com.ywl5320.wlivefm.http.beans.HomePageBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelTypeBean;
import com.ywl5320.wlivefm.http.beans.LiveListBean;
import com.ywl5320.wlivefm.http.beans.LivePageBean;
import com.ywl5320.wlivefm.http.beans.ScrollImgBean;
import com.ywl5320.wlivefm.http.serviceapi.RadioApi;
import com.ywl5320.wlivefm.http.subscribers.HttpSubscriber;
import com.ywl5320.wlivefm.http.subscribers.SubscriberOnListener;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.menu.ItemListMenu;
import com.ywl5320.wlivefm.mvp.LiveActivity;
import com.ywl5320.wlivefm.mvp.LiveListActivity;
import com.ywl5320.wlivefm.mvp.LocalActivity;
import com.ywl5320.wlivefm.util.CommonUtil;
import com.ywl5320.wlivefm.util.widget.adviewpager.adutils.AdViewpagerUtil;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.swipRefresh)
    SwipeRefreshLayout swipeRefreshLayout;




    private View headerView;
    private ViewPager viewPager;
    private LinearLayout lyDots;
    private RecyclerView recyclerView_type;
    private List<ScrollImgBean> urls;
    private AdViewpagerUtil adViewpagerUtil;
    private TypeAdapter typeAdapter;
    private List<LiveChannelTypeBean> types;
    private LinearLayout lyCountry;
    private LinearLayout lyProvence;

    private MyAdapter myAdapter;
    private WapHeaderAndFooterAdapter headerAndFooterAdapter;
    private List<LiveChannelBean> datas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        if(FMApplication.getInstance().getFmServer() != null)
        {
            FMApplication.getInstance().initPlayer(this);
        }
        handler.postDelayed(runnable, 1000);
        setTitle("电台");
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_ec4c48));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHomePage(FMApplication.getInstance().getToken());
                getLivePageBean(FMApplication.getInstance().getToken());
                getLiveByParam(FMApplication.getInstance().getToken(), "3225", 10, 1);
            }
        });

        urls = new ArrayList<>();

        datas = new ArrayList<>();
        myAdapter = new MyAdapter(this, datas);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LiveChannelBean liveChannelBean, int position) {
                LiveActivity.startActivity(MainActivity.this, liveChannelBean);
            }
        });
        headerAndFooterAdapter = new WapHeaderAndFooterAdapter(myAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        headerView = LayoutInflater.from(this).inflate(R.layout.header_home_layout, recyclerView, false);
        AutoUtils.auto(headerView);
        viewPager = (ViewPager) headerView.findViewById(R.id.viewpager);
        lyDots = (LinearLayout) headerView.findViewById(R.id.ly_dots);
        recyclerView_type = (RecyclerView) headerView.findViewById(R.id.recyclerView_type);
        lyCountry = (LinearLayout) headerView.findViewById(R.id.ly_country);
        lyProvence = (LinearLayout) headerView.findViewById(R.id.ly_provence);
        types = new ArrayList<>();
        headerAndFooterAdapter.addHeader(headerView);
        recyclerView.setAdapter(headerAndFooterAdapter);

        typeAdapter = new TypeAdapter(this, types);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_type.setLayoutManager(layoutManager);
        recyclerView_type.setAdapter(typeAdapter);

        adViewpagerUtil = new AdViewpagerUtil(this, viewPager, lyDots, 8, 4, urls);
        adViewpagerUtil.initVps();

        getHomePage(FMApplication.getInstance().getToken());
        getLivePageBean(FMApplication.getInstance().getToken());
        getLiveByParam(FMApplication.getInstance().getToken(), "3225", 10, 1);

        lyCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveListActivity.startActivity(MainActivity.this, "央视台", "3225");
            }
        });

        lyProvence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalActivity.startActivity(MainActivity.this);
            }
        });

    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onPlay() {
        super.onPlay();
    }

    @Override
    public void onInfo(int totaltime, int nowtime, int status, String name, String subname) {
        super.onInfo(totaltime, nowtime, status, name, subname);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
        FMApplication.getInstance().getFmServer().onRelease();
        FMApplication.getInstance().unBindFmServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }

    public static void startActivity(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private void getHomePage(String token)
    {
        RadioApi.getInstance().getHomePage(token, new HttpSubscriber<HomePageBean>(new SubscriberOnListener<HomePageBean>() {
            @Override
            public void onSucceed(HomePageBean data) {
                if(data != null && data.getScrollImg() != null && data.getScrollImg().size() > 0)
                {
                    urls = data.getScrollImg();
                    adViewpagerUtil.updateAdImgs(urls);
                }
            }

            @Override
            public void onError(int code, String msg) {

            }
        }, MainActivity.this));
    }

    private void getLivePageBean(String token)
    {
        RadioApi.getInstance().getLivePage(token, new HttpSubscriber<LivePageBean>(new SubscriberOnListener<LivePageBean>() {
            @Override
            public void onSucceed(LivePageBean data) {
                if(data != null && data.getLiveChannelType() != null && data.getLiveChannelType().size() > 0)
                {
                    types.clear();
                    types.addAll(data.getLiveChannelType());
                    typeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String msg) {

            }
        }, MainActivity.this));
    }

    private void getLiveByParam(String token, String channelPlaceId, int limit, int offset)
    {
        RadioApi.getInstance().getLiveByParam(token, channelPlaceId, limit, offset, new HttpSubscriber<LiveListBean>(new SubscriberOnListener<LiveListBean>() {
            @Override
            public void onSucceed(LiveListBean data) {
                MyLog.d(data);
                if(data != null && data.getLiveChannel()!= null && data.getLiveChannel().size() > 0)
                {
                    datas.clear();
                    datas.addAll(data.getLiveChannel());
                    headerAndFooterAdapter.notifyDataSetChanged();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(int code, String msg) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, MainActivity.this));
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(CommonUtil.isServiceExisted(MainActivity.this, "com.ywl5320.wlivefm.server.FMServer"))
            {
                MyLog.d("service is running");
            }
            else
            {
                MyLog.d("service is death");
            }
            handler.postDelayed(runnable, 1000);
        }
    };

}
