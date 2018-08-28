package com.ywl5320.wlivefm.base;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ywl5320.wlivefm.FMApplication;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.log.MyLog;
import com.ywl5320.wlivefm.server.FMServer;
import com.ywl5320.wlivefm.util.CommonUtil;
import com.zhy.autolayout.AutoLayoutActivity;


import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.http.Body;

/**
 * Created by ywl on 2017/2/5.
 */

public abstract class BaseActivity extends AutoLayoutActivity implements FMServer.OnFmServerPlayListener{

    @Nullable
    @BindView(R.id.ly_system_parent)
    LinearLayout lySystemParent;

    @Nullable
    @BindView(R.id.iv_line)
    ImageView ivLine;

    @Nullable
    @BindView(R.id.ly_system_bar)
    LinearLayout lySystemBar;

    @Nullable
    @BindView(R.id.tv_title)
    TextView mtvTitle;

    @Nullable
    @BindView(R.id.iv_back)
    ImageView mivBack;

    @Nullable
    @BindView(R.id.iv_right)
    ImageView mivRight;

    @Nullable
    @BindView(R.id.tv_right)
    TextView tvRight;

    @Nullable
    @BindView(R.id.iv_status)
    ImageView ivStatus;

    @Nullable
    @BindView(R.id.pb_load)
    ProgressBar pbLoad;

    @Nullable
    @BindView(R.id.tv_mini_name)
    TextView tvMiniName;

    @Nullable
    @BindView(R.id.tv_mini_subname)
    TextView tvMiniSubName;

    @Nullable
    @BindView(R.id.iv_menu)
    ImageView ivMenu;

    @Nullable
    @BindView(R.id.rl_content)
    RelativeLayout rlContent;

    @Nullable
    @BindView(R.id.v_bg)
    View bg;

    private int nowStatus = -1;
    private int menuHeight = 0;
    private boolean isShowMenu = false;
    private boolean isLoadAnimal = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        //透明状态栏
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4全透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if(FMApplication.getInstance().getFmServer() != null)
        {
            FMApplication.getInstance().getFmServer().registerFmserverPlayListener(BaseActivity.this);
        }

    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        lySystemBar = (LinearLayout) findViewById(R.id.ly_system_bar);
        if(lySystemBar != null) {
            initSystembar(lySystemBar);
        }
        if(mivBack != null)
        {
            mivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBack();
                }
            });
        }
        if(mivRight != null)
        {
            mivRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickMenu();
                }
            });
        }

        if(tvRight != null)
        {
            tvRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickTxtMenu();
                }
            });
        }
        if(ivStatus != null)
        {
            ivStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FMApplication.getInstance().getFmServer().btnPlay();
                }
            });
        }
        if(ivMenu != null)
        {
            ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickPlayerMenu(v);
                }
            });
        }

        if(rlContent != null && bg != null) {
            menuHeight = CommonUtil.getScreenHeight(this) * 1 / 2;

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) rlContent.getLayoutParams();
            lp.width = CommonUtil.getScreenWidth(this);
            lp.height = menuHeight;
            rlContent.setLayoutParams(lp);
            rlContent.setTranslationY(menuHeight);
            hideMenu(rlContent);
        }

        if(bg != null)
        {
            bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isShowMenu = false;
                    hideMenu(rlContent);
                }
            });
        }

    }

    public void setTitleTrans(int color)
    {
        if(lySystemParent != null)
        {
            lySystemParent.setBackgroundColor(getResources().getColor(color));
        }
    }

    public void setTitleLine(int color)
    {
        if(ivLine != null)
        {
            ivLine.setBackgroundColor(getResources().getColor(color));
        }
    }

    public void onClickMenu(){}

    public void onClickBack(){}

    public void onClickTxtMenu(){}

    public void onClickPlayerMenu(View view){
        if(rlContent != null && bg != null) {
            if(isLoadAnimal)
                return;
            isLoadAnimal = true;
            isShowMenu = !isShowMenu;
            if (isShowMenu) {
                if(rlContent.getVisibility() == View.GONE)
                {
                    rlContent.setVisibility(View.VISIBLE);
                }
                if(bg.getVisibility() == View.GONE)
                {
                    bg.setVisibility(View.VISIBLE);
                }
                showMenu(rlContent);
            } else {
                hideMenu(rlContent);
            }
        }
    }


    public void initSystembar(View lySystemBar)
    {
        if (Build.VERSION.SDK_INT >= 19) {
            if (lySystemBar != null) {
                lySystemBar.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lySystemBar.getLayoutParams();
                lp.height = CommonUtil.getStatusHeight(this);
                lySystemBar.requestLayout();
            }
        } else {
            if (lySystemBar != null) {
                lySystemBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPrepard() {

    }

    @Override
    public void onConplete() {

    }

    @Override
    public void onError(int i, String s) {
//        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        MyLog.d("error:" + s);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onPlay() {

    }

    /**
     * 0：准备 1：加载 2：播放 3：暂停 4：完成 5：出错
     * @param totaltime
     * @param nowtime
     * @param status
     */
    @Override
    public void onInfo(int totaltime, int nowtime, int status, String name, String subname) {
//        MyLog.d(CommonUtil.millisToDateFormat(totaltime) + "--" + CommonUtil.millisToDateFormat(nowtime) + "--" + status);
        if(tvMiniName != null) {
            if (!TextUtils.isEmpty(name)) {
                tvMiniName.setText(name);
            } else {
                tvMiniName.setText("未播放");
            }
        }
        if(tvMiniSubName != null) {
            if (!TextUtils.isEmpty(subname)) {
                tvMiniSubName.setText(subname);
                tvMiniSubName.setVisibility(View.VISIBLE);
            } else {
                tvMiniSubName.setText("");
                tvMiniSubName.setVisibility(View.GONE);
            }
        }
        if(ivStatus != null) {
            if (status == 2) {
                if(nowStatus != 2) {
                    nowStatus = status;
                    pbLoad.setVisibility(View.GONE);
                    ivStatus.setVisibility(View.VISIBLE);
                    ivStatus.setImageResource(R.drawable.miniplay_pause_selector);
                }
            }
            else if(status == 3)
            {
                if(nowStatus != 3) {
                    nowStatus = status;
                    pbLoad.setVisibility(View.GONE);
                    ivStatus.setVisibility(View.VISIBLE);
                    ivStatus.setImageResource(R.drawable.miniplay_play_selector);
                }
            }
            else if(status == 1)
            {
//                MyLog.d("loading...");
                nowStatus = 1;
                pbLoad.setVisibility(View.VISIBLE);
                ivStatus.setVisibility(View.GONE);
            }
            else
            {
                nowStatus = -1;
                pbLoad.setVisibility(View.GONE);
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.miniplay_play_selector);
            }
        }

    }

    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title)
    {
        if(mtvTitle != null)
        {
            mtvTitle.setText(title);
        }
    }

    /**
     * 显示返回图标
     */
    public void setBackView()
    {
        if(mivBack != null)
        {
            mivBack.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示返回图标
     */
    public void setBackView(int resId)
    {
        if(mivBack != null)
        {
            mivBack.setVisibility(View.VISIBLE);
            mivBack.setImageResource(resId);
        }
    }

    /**
     * 显示返回图标
     */
    public void setRightView(int resId)
    {
        if(mivRight != null)
        {
            mivRight.setVisibility(View.VISIBLE);
            mivRight.setImageResource(resId);
        }
    }

    public void setRightTxtMenu(String menu)
    {
        if(tvRight != null)
        {
            tvRight.setVisibility(View.VISIBLE);
            tvRight.setText(menu);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(FMApplication.getInstance().getFmServer() != null) {
            FMApplication.getInstance().getFmServer().unRegusterFmserverPlayListener(this);
        }
    }

    public LinearLayout getLySystemBar()
    {
        return lySystemBar;
    }

    public void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showMenu(final View v) {
        ValueAnimator animator;
        animator = ValueAnimator.ofFloat(menuHeight, 0);
        animator.setTarget(v);
        animator.setDuration(200).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                v.setTranslationY(value);
                bg.setAlpha(1 - value / menuHeight);
                if(value == 0)
                {
                    isLoadAnimal = false;
                }
            }
        });
    }

    public void hideMenu(final View v) {
        ValueAnimator animator;
        animator = ValueAnimator.ofFloat(0, menuHeight);
        animator.setTarget(v);
        animator.setDuration(200).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                v.setTranslationY(value);
                bg.setAlpha(1 - value / menuHeight);
                if(value == menuHeight)
                {
                    bg.setVisibility(View.GONE);
                    isLoadAnimal = false;
                }
            }
        });
    }
}
