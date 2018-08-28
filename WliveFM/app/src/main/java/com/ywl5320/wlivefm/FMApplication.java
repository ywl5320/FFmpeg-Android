package com.ywl5320.wlivefm;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.squareup.leakcanary.LeakCanary;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.server.FMServer;

/**
 * Created by ywl on 2017-7-19.
 */

public class FMApplication extends Application{

    private static FMApplication instance;
    private String token;
    private FMServer fmServer;
    private FMServer.FmBinder fmBinder;
    private ServiceConnection conn;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        conn = new FmServiceConnection();
        bindFmServer();
        LeakCanary.install(this);
    }

    public static FMApplication getInstance()
    {
        return instance;
    }


    private class FmServiceConnection implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                fmBinder = (FMServer.FmBinder) service;
                fmServer = fmBinder.getServer();
            }
            catch(ClassCastException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyLog.d("onServiceDisconnected:" + name.toString());
        }
    }

    public void bindFmServer()
    {
        Intent intent=new Intent(getApplicationContext(), FMServer.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public void initPlayer(Activity activity)
    {
        if(fmServer != null)
        {
            MyLog.d("init service...");
            fmServer.initPlayer(activity);
        }
    }


    public void unBindFmServer()
    {
        unbindService(conn);
        fmBinder = null;
        fmServer = null;
    }

    public FMServer getFmServer() {
        return fmServer;
    }

    public void setFmServer(FMServer fmServer) {
        this.fmServer = fmServer;
    }

    public String getToken() {
        if(TextUtils.isEmpty(token))
        {
            token = (System.currentTimeMillis() / 1000) + "";
        }
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
