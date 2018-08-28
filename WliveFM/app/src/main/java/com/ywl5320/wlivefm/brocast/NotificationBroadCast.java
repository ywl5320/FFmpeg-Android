package com.ywl5320.wlivefm.brocast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.mvp.LiveActivity;
import com.ywl5320.wlivefm.util.CommonUtil;


/**
 * Created by ywl on 2017/2/28.
 */

public class NotificationBroadCast extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if("CLICK_NOTIFICATION_ACTION".equals(intent.getAction()))
        {
            Bundle bundle = intent.getExtras();
            int flag = bundle.getInt("flag", -1);
            if(flag == 0)//播放按钮
            {
                MyLog.d("play");
                if(FMApplication.getInstance().getFmServer() != null) {
                    FMApplication.getInstance().getFmServer().btnPlay();
                }
                else
                {
                    FMApplication.getInstance().bindFmServer();
                    LiveActivity.startActivity(context, null);
                }

            }
            else if(flag == 1)//下一个
            {
                MyLog.d("next");
            }
        }
    }
}
