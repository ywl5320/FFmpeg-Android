package com.ywl5320.wlivefm.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.gson.Gson;
import com.ywl5320.wlivefm.appconfig.Config;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by ywl on 2017-7-19.
 */

public class CommonUtil {

    /**
     * 获取状态栏高度
     * @param activity
     * @return
     */
    public static int getStatusHeight(Activity activity)
    {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            statusBarHeight = frame.top;
        }
        return statusBarHeight;
    }

    /**
     * 是否有导航栏
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean haveNavgtion(Activity activity)
    {
        //屏幕的高度  真实物理的屏幕
        Display display=activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics=new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int heightDisplay=displayMetrics.heightPixels;
        //为了防止横屏
        int widthDisplay=displayMetrics.widthPixels;
        DisplayMetrics contentDisplaymetrics=new DisplayMetrics();
        display.getMetrics(contentDisplaymetrics);
        int contentDisplay=contentDisplaymetrics.heightPixels;
        int contentDisplayWidth=contentDisplaymetrics.widthPixels;
        //屏幕内容高度  显示内容的屏幕
        int w=widthDisplay-contentDisplayWidth;
        //哪一方大于0   就有导航栏
        int h=heightDisplay-contentDisplay;
        return w>0||h>0;
    }

    /**
     * 导航栏高度
     * @param activity
     * @return
     */
    public static int getNavigationHeight(Activity activity) {
        int height=-1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object  object = clazz.newInstance();
            String heightStr = clazz.getField("navigation_bar_height").get(object).toString();
            height = Integer.parseInt(heightStr);
            //dp--px
            height = activity.getResources().getDimensionPixelSize(height);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return height;
    }

    public static String millisToDateFormat(int sends) {
        if(sends <= 0)
        {
            return "00:00:00";
        }
        long hours = sends / (60 * 60);
        long minutes = (sends % (60 * 60)) / (60);
        long seconds = sends % (60);

        String sh = "00";
        if (hours > 0) {
            if (hours < 10) {
                sh = "0" + hours;
            } else {
                sh = hours + "";
            }
        }
        String sm = "00";
        if (minutes > 0) {
            if (minutes < 10) {
                sm = "0" + minutes;
            } else {
                sm = minutes + "";
            }
        }

        String ss = "00";
        if (seconds > 0) {
            if (seconds < 10) {
                ss = "0" + seconds;
            } else {
                ss = seconds + "";
            }
        }
        return sh + ":" + sm + ":" + ss;
    }

    public static boolean isServiceExisted(Activity activity, String className) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = am.getRunningServices(Integer.MAX_VALUE);
        int myUid = android.os.Process.myUid();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
            if (runningServiceInfo.uid == myUid && runningServiceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    private boolean isServiceRunning(Activity activity, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);

        if (!(serviceList.isEmpty())) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void saveLiveBeanInfo(Context context, LiveChannelBean liveChannelBean)
    {
        Gson gson = new Gson();
        String str = gson.toJson(liveChannelBean);
        SharedpreferencesUtil.write(context, Config.SP_USER_DATA, Config.SP_LIVECHANNEL_KEY, str);
    }

    public static LiveChannelBean getLiveChannelInfo(Context context)
    {
        Gson gson = new Gson();
        String str = SharedpreferencesUtil.readString(context, Config.SP_USER_DATA, Config.SP_LIVECHANNEL_KEY);
        return gson.fromJson(str, LiveChannelBean.class);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int getScreenWidth(Activity context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        return  mScreenWidth;
    }

    public static int getScreenHeight(Activity context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenHeight = dm.heightPixels;
        return  mScreenHeight;
    }



}
