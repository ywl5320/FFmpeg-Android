package com.ywl5320.wlivefm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.ywl5320.wlivefm.base.BaseActivity;
import com.ywl5320.wlivefm.http.beans.HomePageBean;
import com.ywl5320.wlivefm.http.beans.TokenBean;
import com.ywl5320.wlivefm.http.serviceapi.RadioApi;
import com.ywl5320.wlivefm.http.subscribers.HttpSubscriber;
import com.ywl5320.wlivefm.http.subscribers.SubscriberOnListener;
import com.ywl5320.wlivefm.log.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ywl on 2017-7-20.
 */

public class SplashActivity extends BaseActivity{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_layout);

        if(FMApplication.getInstance().getFmServer() == null)
        {
            FMApplication.getInstance().bindFmServer();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getToken();
            }
        }, 1500);

    }


    private void getToken()
    {
        RadioApi.getInstance().getToken(new HttpSubscriber<TokenBean>(new SubscriberOnListener<TokenBean>() {
            @Override
            public void onSucceed(TokenBean data) {
                FMApplication.getInstance().setToken(data.getToken());
                MainActivity.startActivity(SplashActivity.this);
                SplashActivity.this.finish();
            }

            @Override
            public void onError(int code, String msg) {
                showToast("加载数据失败，请稍后再试");
            }
        }, this));
    }

    public static void startActivity(Context context)
    {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

}
