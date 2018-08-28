package com.ywl5320.wlivefm.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.bumptech.glide.Glide;
import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.adapter.ViewPagerAdapter;
import com.ywl5320.wlivefm.base.BaseActivity;
import com.ywl5320.wlivefm.http.beans.LiveChannelPlaceBean;
import com.ywl5320.wlivefm.http.beans.PlaceBean;
import com.ywl5320.wlivefm.http.serviceapi.RadioApi;
import com.ywl5320.wlivefm.http.subscribers.HttpSubscriber;
import com.ywl5320.wlivefm.http.subscribers.SubscriberOnListener;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.widget.bar.NavitationFollowScrollLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by ywl5320 on 2017/8/15.
 */

public class LocalActivity extends BaseActivity{

    @BindView(R.id.nfsl_bar)
    NavitationFollowScrollLayout navitationFollowScrollLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;
    private List<Fragment> fragments;

    private List<String> titles;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_layout);
        setTitle("省市台");
        setBackView();
        getLivePlace(FMApplication.getInstance().getToken());
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

    public static void startActivity(Context context)
    {
        Intent intent = new Intent(context, LocalActivity.class);
        context.startActivity(intent);
    }

    private void getLivePlace(String token)
    {
        RadioApi.getInstance().getLivePlace(token, new HttpSubscriber<LiveChannelPlaceBean>(new SubscriberOnListener() {
            @Override
            public void onSucceed(Object data) {
                LiveChannelPlaceBean liveChannelPlaceBean = (LiveChannelPlaceBean) data;
                if(data != null && liveChannelPlaceBean.getLiveChannelPlace() != null)
                {
                    initVp(liveChannelPlaceBean.getLiveChannelPlace());
                }
            }

            @Override
            public void onError(int code, String msg) {

            }
        }, this));
    }

    private void initVp(List<PlaceBean> liveChannelPlace)
    {
        titles = new ArrayList<>();
        fragments = new ArrayList<>();
        for(PlaceBean placeBean : liveChannelPlace)
        {
            titles.add(placeBean.getName());
            PlaceFragment placeFragment = new PlaceFragment();
            placeFragment.setChannelId(placeBean.getId());
            fragments.add(placeFragment);

        }
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(viewPagerAdapter);
//        viewPager.setOffscreenPageLimit(titles.size());
        navitationFollowScrollLayout.setViewPager(this, titles, viewPager, R.color.color_666666, R.color.color_ce3e3a, 16, 16, 12, true, R.color.color_333333, 0f, 15f, 15f, 80);
        navitationFollowScrollLayout.setBgLine(this, 1, R.color.color_ec4c48);
        navitationFollowScrollLayout.setNavLine(this, 3, R.color.color_ce3e3a);

        navitationFollowScrollLayout.setOnNaPageChangeListener(new NavitationFollowScrollLayout.OnNaPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((PlaceFragment)fragments.get(position)).loadData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }
}
