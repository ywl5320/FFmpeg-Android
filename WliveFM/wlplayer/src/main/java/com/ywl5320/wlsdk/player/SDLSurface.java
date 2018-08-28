package com.ywl5320.wlsdk.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;

/**
 * @author ywl
 *
 */
public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback{

	private OnSurfacePrepard onSurfacePrepard;

	// Sensors
	protected static Display mDisplay;

	// Keep track of the surface size to normalize touch events
	protected static float mWidth, mHeight;

	// Startup
	public SDLSurface(Context context) {
		this(context, null);

	}

	public SDLSurface(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		getHolder().addCallback(this);

		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();

		mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		// Some arbitrary defaults to avoid a potential division by zero
		mWidth = 1.0f;
		mHeight = 1.0f;
	}



	public void setOnSurfacePrepard(OnSurfacePrepard onSurfacePrepard) {
		this.onSurfacePrepard = onSurfacePrepard;
	}

	public Surface getNativeSurface() {
		return getHolder().getSurface();
	}

	// Called when we have a valid drawing surface
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v("SDL", "surfaceCreated()");
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}

	// Called when we lose the surface
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("SDL", "surfaceDestroyed()");
		// Call this *before* setting mIsSurfaceReady to 'false'
		WlPlayer.handlePause();
		WlPlayer.mIsSurfaceReady = false;
		WlPlayer.onNativeSurfaceDestroyed();
	}

	// Called when the surface is resized
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v("SDL", "surfaceChanged()");

		int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
			case PixelFormat.A_8:
				Log.v("SDL", "pixel format A_8");
				break;
			case PixelFormat.LA_88:
				Log.v("SDL", "pixel format LA_88");
				break;
			case PixelFormat.L_8:
				Log.v("SDL", "pixel format L_8");
				break;
			case PixelFormat.RGBA_4444:
				Log.v("SDL", "pixel format RGBA_4444");
				sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
				break;
			case PixelFormat.RGBA_5551:
				Log.v("SDL", "pixel format RGBA_5551");
				sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
				break;
			case PixelFormat.RGBA_8888:
				Log.v("SDL", "pixel format RGBA_8888");
				sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
				break;
			case PixelFormat.RGBX_8888:
				Log.v("SDL", "pixel format RGBX_8888");
				sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
				break;
			case PixelFormat.RGB_332:
				Log.v("SDL", "pixel format RGB_332");
				sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
				break;
			case PixelFormat.RGB_565:
				Log.v("SDL", "pixel format RGB_565");
				sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
				break;
			case PixelFormat.RGB_888:
				Log.v("SDL", "pixel format RGB_888");
				// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
				sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
				break;
			default:
				Log.v("SDL", "pixel format unknown " + format);
				break;
		}

		mWidth = width;
		mHeight = height;
		WlPlayer.onNativeResize(width, height, sdlFormat, mDisplay.getRefreshRate());
		Log.v("ywl5320", "Window size: " + width + "x" + height);

		boolean skip = false;
		int requestedOrientation = WlPlayer.mSingleton.getRequestedOrientation();

		if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			// Accept any
		} else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			if (mWidth > mHeight) {
				skip = true;
			}
		} else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			if (mWidth < mHeight) {
				skip = true;
			}
		}

		// Special Patch for Square Resolution: Black Berry Passport
		if (skip) {
			double min = Math.min(mWidth, mHeight);
			double max = Math.max(mWidth, mHeight);

			if (max / min < 1.20) {
				Log.v("SDL", "Don't skip on such aspect-ratio. Could be a square resolution.");
				skip = false;
			}
		}

		if (skip) {
			Log.v("SDL", "Skip .. Surface is not ready.");
			return;
		}

		// Set mIsSurfaceReady to 'true' *before* making a call to handleResume
		WlPlayer.mIsSurfaceReady = true;
		WlPlayer.onNativeSurfaceChanged();

		if (WlPlayer.mHasFocus) {
			WlPlayer.handleResume();
		}

		if(onSurfacePrepard != null)
		{
			onSurfacePrepard.onPrepard();
		}
	}

	public interface OnSurfacePrepard
	{
		void onPrepard();
	}
}


