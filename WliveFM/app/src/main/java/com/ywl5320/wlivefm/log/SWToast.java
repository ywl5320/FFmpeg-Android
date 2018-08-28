package com.ywl5320.wlivefm.log;



import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2014/9/23.
 */
public class SWToast {

    private static SWToast mInstance = null;

    private Toast mToast = null;
    private TextView mInfo = null;
    private Handler mHandler = null;

    private Context mContext = null;
    
    private static final int TEXTCOLOR = 0xffffffff;
    
    private static final int BGCOLOR = 0xc0222222;
    
    private int mTextColor;
    private int mBgColor;
    
    private SWToast(){

    }

    public void init(Context appContext){
    	mContext = appContext.getApplicationContext();
        mInfo = new TextView(appContext);
        float density = appContext.getResources().getDisplayMetrics().density;
        mInfo.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16 * density);
        int padding = (int) (density * 8);
        mInfo.setPadding(padding, padding/2, padding, padding/2);
        mTextColor = 0;
        mBgColor = 0;
        setColor(TEXTCOLOR, BGCOLOR);
        mToast = new Toast(appContext);
        mToast.setGravity(Gravity.CENTER, 0, 200);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(mInfo);
        mHandler = new Handler(Looper.myLooper());
    }
    
    private void setColor(int textColor, int bgColor) {
    	if(0 == textColor) {
    		textColor = TEXTCOLOR;
    	}
    	if(0 == bgColor) {
    		bgColor = BGCOLOR;
    	}
    	if(mTextColor != textColor) {
    		mTextColor = textColor;
    		mInfo.setTextColor(mTextColor);
    		mInfo.invalidate();
    	}
    	if(mBgColor != bgColor) {
    		mBgColor = bgColor;
    		float density = mInfo.getResources().getDisplayMetrics().density;
            int padding = (int) (density * 8);
            mInfo.setPadding(padding, padding/2, padding, padding/2);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(mBgColor);
            gd.setStroke((int)(0.5*density), mInfo.getTextColors().getDefaultColor());
            gd.setCornerRadius(15 * density);
            mInfo.setBackgroundDrawable(gd);
            mInfo.invalidate();
    	}
    }
    
    private void show(String info, int time) {
	     mInfo.setText(info);
	     if(time == Toast.LENGTH_LONG) {
	     	mToast.setDuration(Toast.LENGTH_LONG);
	     } else {
	     	mToast.setDuration(Toast.LENGTH_SHORT);
	     }
	     mToast.show();
    }

    private void show(int resId, int time){
        mInfo.setText(resId);
        if(time == Toast.LENGTH_LONG) {
        	mToast.setDuration(Toast.LENGTH_LONG);
        } else {
        	mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
    
    public void toast(final String info, final int time){
       mHandler.post(new Runnable() {
           @Override
           public void run() {
        	   setColor(TEXTCOLOR, BGCOLOR);
        	   show(info, time);
           }
       });
    }
    
    public void toast(final int resId, final int time){
    	mHandler.post(new Runnable() {
            @Override
            public void run() {
         	   setColor(TEXTCOLOR, BGCOLOR);
         	   show(resId, time);
            }
        });
    }
    
    /**
     * 
     * @param info
     * @param s if short
     */
    public void toast(final String info, boolean s){
    	toast(info, s ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
    }
    /**
     *
     * @param info
     * @param
     */
    public void toast(final String info){
        toast(info, true ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
    }
    /**
     * 
     * @param resId
     * @param s if short
     */
    public void toast(final int resId, boolean s){
    	toast(resId, s ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
    }
    
    /**
     * 
     * @param info
     * @param s
     * @param textcolor ==0 使用默认的
     * @param bgColor ==0 使用默认的
     */
    public void toast(final String info, final boolean s, final int textcolor, final int bgColor){
    	mHandler.post(new Runnable() {
            @Override
            public void run() {
         	   setColor(textcolor, bgColor);
         	   show(info, s ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
            }
        });
    }
    
    /**
     * 
     * @param resId
     * @param s
     * @param textcolor ==0 使用默认的
     * @param bgColor ==0 使用默认的
     */
    public void toast(final int resId, final boolean s, final int textcolor, final int bgColor){
    	mHandler.post(new Runnable() {
            @Override
            public void run() {
         	   setColor(textcolor, bgColor);
         	   show(resId, s ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
            }
        });
    }

    public static SWToast getToast(){
        if(mInstance == null){
            mInstance = new SWToast();
        }

        return mInstance;
    }
    
    public Handler getHandler() {
    	return mHandler;
    }
    
    public Context getAppContext() {
    	return mContext;
    }
}
