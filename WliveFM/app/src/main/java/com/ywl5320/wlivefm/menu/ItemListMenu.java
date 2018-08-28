package com.ywl5320.wlivefm.menu;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.ywl5320.wlivefm.R;

/**
 * Created by ywl5320 on 2017/8/17.
 */

public class ItemListMenu extends PopupWindow{

    private Activity activity;
    private View popView;

    public ItemListMenu(Activity activity) {
        super(activity);
        this.activity = activity;
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popView = inflater.inflate(R.layout.popupmenu_itemlist_layout, null);// 加载菜单布局文件
        this.setContentView(popView);// 把布局文件添加到popupwindow中
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置菜单的宽度（需要和菜单于右边距的距离搭配，可以自己调到合适的位置）
        this.setHeight(dip2px(activity, 200));
        this.setFocusable(true);// 获取焦点
        this.setTouchable(true); // 设置PopupWindow可触摸
        this.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
        this.setAnimationStyle(R.style.PopWindw);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

    }

    public void showLocation(View v) {
//        showAsDropDown(activity.findViewById(resourId), dip2px(activity, 0),
//                dip2px(activity, 0));
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - getHeight() - 200);
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


}
