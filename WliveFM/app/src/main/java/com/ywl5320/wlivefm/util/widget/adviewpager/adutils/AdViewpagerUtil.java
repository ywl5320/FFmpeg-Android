package com.ywl5320.wlivefm.util.widget.adviewpager.adutils;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.http.beans.ScrollImgBean;
import com.ywl5320.wlivefm.util.widget.adviewpager.adapter.ImageViewPagerAdapter;

import java.util.List;


/**
 * Created by ywl on 2016/6/24.
 */
public class AdViewpagerUtil {

    private Context context;
    private ViewPager viewPager;
    private ImageViewPagerAdapter mimageViewPagerAdapter;
    private ImageView[] mImageViews;
    private List<ScrollImgBean> urls;
    private LinearLayout dotlayout;
    private ImageView[] dotViews;

    private boolean isRight = true; // 判断viewpager是不是向右滑动
    private int lastPosition = 1; // 记录viewpager从哪个页面滑动到当前页面，从而区分滑动方向
    private int autoIndex = 1; // 自动轮询时自增坐标，能确定导航圆点的位置
    private int currentIndex = 0; //当前item
    private int delayTime = 5000; //自动轮播的时间间隔
    private int imgsize = 0; //图片的数量，item的数量
    private boolean isLoop = false;//轮播开关

    private OnAdPageChangeListener onAdPageChangeListener; //pagechange回调
    private OnAdItemClickListener onAdItemClickListener; //点击事件回调

    private int dotsize = 8; //小圆点的大小宽度
    private int dotoffset = 4; //小圆点的间距

    /**
     * 不带小圆点
     * @param context
     * @param viewPager
     * @param urls
     */
    public AdViewpagerUtil(Context context, ViewPager viewPager, List<ScrollImgBean> urls) {
        this.context = context;
        this.viewPager = viewPager;
        this.urls = urls;
    }

    /**
     * 有小圆点
     * @param context
     * @param viewPager
     * @param dotlayout
     * @param dotsize
     * @param dotoffset
     * @param urls
     */
    public AdViewpagerUtil(Context context, ViewPager viewPager, LinearLayout dotlayout, int dotsize, int dotoffset, List<ScrollImgBean> urls) {
        this.context = context;
        this.viewPager = viewPager;
        this.dotlayout = dotlayout;
        this.dotsize = dotsize;
        this.urls = urls;
    }

    public void setOnAdPageChangeListener(OnAdPageChangeListener onAdPageChangeListener) {
        this.onAdPageChangeListener = onAdPageChangeListener;
    }

    public void setOnAdItemClickListener(OnAdItemClickListener onAdItemClickListener) {
        this.onAdItemClickListener = onAdItemClickListener;
    }

    public void updateAdImgs(List<ScrollImgBean> urls)
    {
        this.urls = urls;
        initVps();
    }

    private void initAdimgs(List<ScrollImgBean> urls)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int length = urls.size() + 2;
        mImageViews = new ImageView[length];
        for (int i = 0; i <length; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageViews[i] = imageView;
        }
        setImg(length, urls);
    }

    private void setImg(int length, List<ScrollImgBean> urls)
    {
        if (urls.size() > 0) {
            imgsize = length;
            for (int i = 0; i < length; i++) {
                if (i < length - 2) {
                    final int index = i;
                    Glide.with(context) .load(urls.get(i).getImg()).into(mImageViews[i + 1]);
                    mImageViews[i + 1].setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if(onAdItemClickListener != null)
                            {
                                onAdItemClickListener.onItemClick(mImageViews[index + 1], index);
                            }
                        }
                    });
                }
            }
            Glide.with(context) .load(urls.get(urls.size() - 1).getImg()).into(mImageViews[0]);
            Glide.with(context) .load(urls.get(0).getImg()).into(mImageViews[length - 1]);
        }
    }

    public void initVps()
    {
        if(urls.size() == 0)
            return;
        initAdimgs(urls);
        initDots(urls.size());
        mimageViewPagerAdapter = new ImageViewPagerAdapter(context, mImageViews);
        viewPager.setAdapter(mimageViewPagerAdapter);
        viewPager.setOffscreenPageLimit(mImageViews.length);
        startLoopViewPager();
        viewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        isLoop = true;
                        stopLoopViewPager();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isLoop = false;
                        startLoopViewPager();
                    default:
                        break;
                }
                return false;
            }
        });
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
                if (isRight) {
                    if (arg0 != 1) {
                        if (lastPosition == 0) {
                            viewPager.setCurrentItem(imgsize - 2, false);
                        } else if (lastPosition == imgsize - 1) {
                            viewPager.setCurrentItem(1, false);
                        }
                    }
                }

                if(onAdPageChangeListener != null)
                {
                    onAdPageChangeListener.onPageScrollStateChanged(arg0);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
                if (!isRight) {
                    if (arg1 < 0.01) {
                        if (arg0 == 0) {
                            viewPager.setCurrentItem(imgsize - 2, false);
                        } else if (arg0 == imgsize - 1) {
                            viewPager.setCurrentItem(1, false);
                        }
                    }
                }
                if(onAdPageChangeListener != null)
                {
                    onAdPageChangeListener.onPageScrolled(arg0, arg1, arg2);
                }
            }

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                autoIndex = arg0;
                if (lastPosition < arg0 && lastPosition != 0) {
                    isRight = true;
                } else if (lastPosition == imgsize - 1) {
                    isRight = true;
                }
                if (lastPosition > arg0 && lastPosition != imgsize - 1) {
                    isRight = false;
                } else if (lastPosition == 0) {
                    isRight = false;
                }
                lastPosition = arg0;

                if (arg0 == 0) {
                    currentIndex = imgsize - 2;
                } else if (arg0 == imgsize - 1) {
                    currentIndex = 1;
                } else {
                    currentIndex = arg0;
                }

                for (int i = 0; i < dotViews.length; i++) {
                    if (i == currentIndex - 1) {
                        dotViews[i].setSelected(true);
                    } else {
                        dotViews[i].setSelected(false);
                    }
                }

                if(onAdPageChangeListener != null)
                {
                    onAdPageChangeListener.onPageSelected(arg0);
                }
            }

        });
        viewPager.setCurrentItem(1);// 初始化时设置显示第一页（ViewPager中索引为1）
    }

    public void initDots(int length)
    {
        if(dotlayout == null)
            return;
        dotlayout.removeAllViews();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(dip2px(context, dotsize), dip2px(context, dotsize));
        mParams.setMargins(dip2px(context, dotoffset), 0, dip2px(context, dotoffset), 0);//设置小圆点左右之间的间隔

        dotViews = new ImageView[length];

        for(int i = 0; i < length; i++)
        {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(mParams);
            imageView.setImageResource(R.drawable.dot_selector);
            if(i== 0)
            {
                imageView.setSelected(true);//默认启动时，选中第一个小圆点
            }
            else {
                imageView.setSelected(false);
            }
            dotViews[i] = imageView;//得到每个小圆点的引用，用于滑动页面时，（onPageSelected方法中）更改它们的状态。
            dotlayout.addView(imageView);//添加到布局里面显示
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 要做的事情
            if (viewPager.getChildCount() > 0) {
                handler.postDelayed(this, delayTime);
                autoIndex++;
                viewPager.setCurrentItem(autoIndex % imgsize, true);
            }
        }
    };

    private void startLoopViewPager() {
        if (!isLoop && viewPager != null) {
            handler.postDelayed(runnable, delayTime);// 每两秒执行一次runnable.
            isLoop = true;
        }

    }

    private void stopLoopViewPager() {
        if (isLoop && viewPager != null) {
            handler.removeCallbacks(runnable);
            isLoop = false;
        }
    }

    public interface OnAdItemClickListener
    {
        void onItemClick(View v, int flag);
    }

    public interface OnAdPageChangeListener
    {
        void onPageScrollStateChanged(int arg0);
        void onPageScrolled(int arg0, float arg1, int arg2);
        void onPageSelected(int arg0);
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
