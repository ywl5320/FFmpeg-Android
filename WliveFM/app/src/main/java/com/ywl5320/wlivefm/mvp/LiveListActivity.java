package com.ywl5320.wlivefm.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.MainActivity;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.adapter.MyAdapter;
import com.ywl5320.wlivefm.adapter.WapHeaderAndFooterAdapter;
import com.ywl5320.wlivefm.base.BaseActivity;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.http.beans.LiveListBean;
import com.ywl5320.wlivefm.http.serviceapi.RadioApi;
import com.ywl5320.wlivefm.http.subscribers.HttpSubscriber;
import com.ywl5320.wlivefm.http.subscribers.SubscriberOnListener;
import com.ywl5320.wlivefm.log.MyLog;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by ywl5320 on 2017/8/14.
 */

public class LiveListActivity extends BaseActivity{

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.swipRefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private MyAdapter myAdapter;
    private WapHeaderAndFooterAdapter headerAndFooterAdapter;
    private List<LiveChannelBean> datas;
    private TextView tvLoadMsg;

    private String title;
    private String channelId;
    private int pageSize = 10;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isOver = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getIntent().getStringExtra("title");
        channelId = getIntent().getStringExtra("channelId");
        setContentView(R.layout.activity_live_list_layout);

        setBackView();
        setTitle(title);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_ec4c48));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isLoading) {
                    isLoading = true;
                    isOver = false;
                    currentPage = 1;
                    getLiveByParam(FMApplication.getInstance().getToken(), channelId, pageSize, currentPage);
                }
            }
        });

        datas = new ArrayList<>();
        myAdapter = new MyAdapter(this, datas);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LiveChannelBean liveChannelBean, int position) {
                LiveActivity.startActivity(LiveListActivity.this, liveChannelBean);
            }
        });
        headerAndFooterAdapter = new WapHeaderAndFooterAdapter(myAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        View footerView = LayoutInflater.from(this).inflate(R.layout.footer_layout, recyclerView, false);
        AutoUtils.auto(footerView);
        tvLoadMsg = (TextView) footerView.findViewById(R.id.tv_loadmsg);
        headerAndFooterAdapter.addFooter(footerView);

        recyclerView.setAdapter(headerAndFooterAdapter);

        headerAndFooterAdapter.setOnloadMoreListener(recyclerView, new WapHeaderAndFooterAdapter.OnLoadMoreListener(){

            @Override
            public void onLoadMore() {
                if(!isOver)
                {
                    if(!isLoading)
                    {
                        getLiveByParam(FMApplication.getInstance().getToken(), channelId, pageSize, currentPage);
                    }
                }
            }
        });
        getLiveByParam(FMApplication.getInstance().getToken(), channelId, pageSize, currentPage);
    }

    @Override
    public void onClickBack() {
        super.onClickBack();
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public static void startActivity(Context context, String title, String channelId)
    {
        Intent intent = new Intent(context, LiveListActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("channelId", channelId);
        context.startActivity(intent);
    }

    private void getLiveByParam(String token, String channelPlaceId, int limit, int offset)
    {
        RadioApi.getInstance().getLiveByParam(token, channelPlaceId, limit, offset, new HttpSubscriber<LiveListBean>(new SubscriberOnListener<LiveListBean>() {
            @Override
            public void onSucceed(LiveListBean data) {
                MyLog.d(data);
                if(data != null && data.getLiveChannel()!= null)
                {
                    if(currentPage == 1) {
                        datas.clear();
                    }
                    if(data.getLiveChannel().size() < pageSize)
                    {
                        tvLoadMsg.setText("没有更多了");
                        isOver = true;
                    }
                    else {
                        tvLoadMsg.setText("加载更多");
                        isOver = false;
                        currentPage++;
                    }
                    if(data.getLiveChannel().size() > 0) {
                        datas.addAll(data.getLiveChannel());
                    }
                    headerAndFooterAdapter.notifyDataSetChanged();
                }
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void onError(int code, String msg) {
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
            }
        }, LiveListActivity.this));
    }
}
