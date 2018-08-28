package com.ywl5320.wlivefm.util;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl5320 on 2017/7/19.
 */

public class NotificationBarUtil {
    private static final double COLOR_THRESHOLD = 180.0;

    private String DUMMY_TITLE = "WLIVEFM_TITLE";
    private int titleColor;

    public NotificationBarUtil() {
    }

    //判断是否Notification背景是否为黑色
    public boolean isDarkNotificationBar(Context context) {
        return !isColorSimilar(Color.BLACK, getNotificationTitleColor(context));
    }



    //获取Notification 标题的颜色
    private int getNotificationTitleColor(Context context) {
        int color = 0;
        if (context instanceof AppCompatActivity) {
            color = getNotificationColorCompat(context);
        } else {
            color = getNotificationColorInternal(context);
        }
        return color;
    }

    //判断颜色是否相似
    public boolean isColorSimilar(int baseColor, int color) {
        int simpleBaseColor = baseColor | 0xff000000;
        int simpleColor = color | 0xff000000;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);

        double value = Math.sqrt(baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue);
        return value < COLOR_THRESHOLD;

    }

    //获取标题颜色
    private int getNotificationColorInternal(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(DUMMY_TITLE);
        Notification notification = builder.build();

        ViewGroup notificationRoot = (ViewGroup) notification.contentView.apply(context, new FrameLayout(context));
        TextView title = (TextView) notificationRoot.findViewById(android.R.id.title);
        if (title == null) { //如果ROM厂商更改了默认的id
            iteratorView(notificationRoot, new Filter() {
                @Override
                public void filter(View view) {
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        if (DUMMY_TITLE.equals(textView.getText().toString())) {

                            titleColor = textView.getCurrentTextColor();
                        }
                    }
                }
            });
            return titleColor;
        } else {

            return title.getCurrentTextColor();
        }
    }


    private int getNotificationColorCompat(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.build();
        int layoutId = notification.contentView.getLayoutId();
        ViewGroup notificationRoot = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        TextView title = (TextView) notificationRoot.findViewById(android.R.id.title);
        if (title == null) {
            final List<TextView> textViews = new ArrayList<>();
            iteratorView(notificationRoot, new Filter() {
                @Override
                public void filter(View view) {
                    if (view instanceof TextView) {
                        textViews.add((TextView) view);
                    }
                }
            });
            float minTextSize = Integer.MIN_VALUE;
            int index = 0;
            for (int i = 0, j = textViews.size(); i < j; i++) {
                float currentSize = textViews.get(i).getTextSize();
                if (currentSize > minTextSize) {
                    minTextSize = currentSize;
                    index = i;
                }
            }
            textViews.get(index).setText(DUMMY_TITLE);
            return textViews.get(index).getCurrentTextColor();
        } else {
            return title.getCurrentTextColor();
        }

    }


    private void iteratorView(View view, Filter filter) {
        if (view == null || filter == null) {
            return;
        }
        filter.filter(view);
        if (view instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) view;
            for (int i = 0, j = container.getChildCount(); i < j; i++) {
                View child = container.getChildAt(i);
                iteratorView(child, filter);
            }
        }

    }

    interface Filter {
        void filter(View view);
    }

}
