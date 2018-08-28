package com.ywl5320.wlsdk.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ywl5320.wlsdk.player.SDLSurface.OnSurfacePrepard;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

public class WlPlayer {

	private static OnPlayerPrepard mOnPlayerPrepard;
	private static OnPlayerInfoListener onPlayerInfoListener;
	private static OnErrorListener onErrorListener;
	private static OnCompleteListener onCompleteListener;

	private static String url;

	public static Activity mSingleton;

	// Keep track of the paused state
	public static boolean mIsPaused, mIsSurfaceReady, mHasFocus;
	public static boolean mExitCalledFromJava;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	protected static Thread mSDLThread;

	// Audio
	protected static AudioTrack mAudioTrack;
	protected static AudioRecord mAudioRecord;

	/**
	 * If shared libraries (e.g. SDL or the native application) could not be
	 * loaded.
	 */
	public static boolean mBrokenLibraries;

	// If we want to separate mouse and touch events.
	// This is only toggled in native code when a hint is set!
	public static boolean mSeparateMouseAndTouch;

	public static SDLSurface mSurface;
	protected static SDLJoystickHandler mJoystickHandler;

	public static void initPlayer(Activity activity)
	{
		loadLibraries();
		initialize();
		mSingleton = activity;

		if (Build.VERSION.SDK_INT >= 12) {
			mJoystickHandler = new SDLJoystickHandler_API12();
		} else {
			mJoystickHandler = new SDLJoystickHandler();
		}
		System.out.println("init player ............................................");
	}

	/**
	 * This method is called by SDL before loading the native shared libraries.
	 * It can be overridden to provide names of shared libraries to be loaded.
	 * The default implementation returns the defaults. It never returns null.
	 * An array returned by a new implementation must at least contain "SDL2".
	 * Also keep in mind that the order the libraries are loaded may matter.
	 *
	 * @return names of shared libraries to be loaded (e.g. "SDL2", "main").
	 */
	protected static String[] getLibraries() {
		return new String[] {
				"SDL2",
				"avutil-55",
				"swresample-2",
				"avcodec-57",
				"avformat-57",
				"swscale-4",
				"postproc-54",
				"avfilter-6",
				"avdevice-57",
				"wlplayer" };
	}

	// Load the .so
	public static void loadLibraries() {
		for (String lib : getLibraries()) {
			System.loadLibrary(lib);
		}
	}

	public static void initialize() {
		// The static nature of the singleton and Android quirkyness force us to
		// initialize everything here
		// Otherwise, when exiting the app and returning to it, these variables
		// *keep* their pre exit values
		mSingleton = null;
		mSurface = null;
		// mTextEdit = null;
		// mLayout = null;
		mJoystickHandler = null;
		mSDLThread = null;
		mAudioTrack = null;
		mAudioRecord = null;
		mExitCalledFromJava = false;
		mBrokenLibraries = false;
		mIsPaused = false;
		mIsSurfaceReady = false;
		mHasFocus = true;
	}

	public static void initSurface(SDLSurface surface)
	{
		mSurface = surface;
	}

	public static void setPrepardListener(OnPlayerPrepard onPlayerPrepard) {
		mOnPlayerPrepard = onPlayerPrepard;
	}

	public static void setOnErrorListener(OnErrorListener error)
	{
		onErrorListener = error;
	}


	public static void setOnCompleteListener(OnCompleteListener onComplete)
	{
		onCompleteListener = onComplete;
	}

	public static void setDataSource(String source)
	{
		url = source;
	}

	/**
	 * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed is the
	 * first to be called, mIsSurfaceReady should still be set to 'true' during
	 * the call to onPause (in a usual scenario).
	 */
	public static void handlePause() {
		if (!mIsPaused && mIsSurfaceReady) {
			mIsPaused = true;
			nativePause();
		}
	}

	/**
	 * Called by onResume or surfaceCreated. An actual resume should be done
	 * only when the surface is ready. Note: Some Android variants may send
	 * multiple surfaceChanged events, so we don't need to resume every time we
	 * get one of those events, only if it comes after surfaceDestroyed
	 */
	public static void handleResume() {
		if (WlPlayer.mIsPaused && WlPlayer.mIsSurfaceReady && WlPlayer.mHasFocus) {
			WlPlayer.mIsPaused = false;
			WlPlayer.nativeResume();
		}
	}

	/* The native thread has finished */
	public static void handleNativeExit() {
		WlPlayer.mSDLThread = null;
		mSingleton.finish();
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static void pollInputDevices() {
		if (WlPlayer.mSDLThread != null) {
			mJoystickHandler.pollInputDevices();
		}
	}

	// Check if a given device is considered a possible SDL joystick
	public static boolean isDeviceSDLJoystick(int deviceId) {
		InputDevice device = InputDevice.getDevice(deviceId);
		// We cannot use InputDevice.isVirtual before API 16, so let's accept
		// only nonnegative device ids (VIRTUAL_KEYBOARD equals -1)
		if ((device == null) || (deviceId < 0)) {
			return false;
		}
		int sources = device.getSources();
		return (((sources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK)
				|| ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD)
				|| ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD));
	}

	// Joystick glue code, just a series of stubs that redirect to the
	// SDLJoystickHandler instance
	public static boolean handleJoystickMotionEvent(MotionEvent event) {
		return mJoystickHandler.handleMotionEvent(event);
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static Surface getNativeSurface() {
		return WlPlayer.mSurface.getNativeSurface();
	}

	// Audio

	/**
	 * This method is called by SDL using JNI.
	 */
	public static int audioOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("wlfm", "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " "
				+ (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

		if (mAudioTrack == null) {
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat,
					desiredFrames * frameSize, AudioTrack.MODE_STREAM);

			// Instantiating AudioTrack can "succeed" without an exception and
			// the track may still be invalid
			// Ref:
			// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
			// Ref:
			// http://developer.android.com/reference/android/media/AudioTrack.html#getState()

			if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
				Log.e("wlfm", "Failed during initialization of Audio Track");
				mAudioTrack = null;
				return -1;
			}

			mAudioTrack.play();
		}

		Log.v("wlfm",
				"SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono") + " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " "
						+ (mAudioTrack.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");

		return 0;
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static void audioWriteShortBuffer(short[] buffer) {
		if(buffer == null || mAudioTrack == null)
			return;
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("wlfm", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static void audioWriteByteBuffer(byte[] buffer) {
		if(buffer == null || mAudioTrack == null)
			return;
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("wlfm", "SDL audio: error return from write(byte)");
				return;
			}
		}
	}

	/** This method is called by SDL using JNI. */
	public static void audioClose() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}

	/** This method is called by SDL using JNI. */
	public static void captureClose() {
		if (mAudioRecord != null) {
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
		}
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static boolean sendMessage(int command, int param) {
		return false;
	}

	/**
	 * This method is called by SDL using JNI.
	 */
	public static int captureOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("wlfm", "SDL capture: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit")
				+ " " + (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(desiredFrames,
				(AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

		if (mAudioRecord == null) {
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, channelConfig, audioFormat,
					desiredFrames * frameSize);

			// see notes about AudioTrack state in audioOpen(), above. Probably
			// also applies here.
			if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
				Log.e("wlfm", "Failed during initialization of AudioRecord");
				mAudioRecord.release();
				mAudioRecord = null;
				return -1;
			}

			mAudioRecord.startRecording();
		}

		Log.v("wlfm",
				"SDL capture: got " + ((mAudioRecord.getChannelCount() >= 2) ? "stereo" : "mono") + " "
						+ ((mAudioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " "
						+ (mAudioRecord.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");

		return 0;
	}

	/**
	 * This method is called by SDL using JNI.
	 * @return an array which may be empty but is never null.
	 */
	public static int[] inputGetInputDeviceIds(int sources) {
		int[] ids = InputDevice.getDeviceIds();
		int[] filtered = new int[ids.length];
		int used = 0;
		for (int i = 0; i < ids.length; ++i) {
			InputDevice device = InputDevice.getDevice(ids[i]);
			if ((device != null) && ((device.getSources() & sources) != 0)) {
				filtered[used++] = device.getId();
			}
		}
		return Arrays.copyOf(filtered, used);
	}

	/** This method is called by SDL using JNI. */
	public static int captureReadShortBuffer(short[] buffer, boolean blocking) {
		// !!! FIXME: this is available in API Level 23. Until then, we always
		// block. :(
		// return mAudioRecord.read(buffer, 0, buffer.length, blocking ?
		// AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
		return mAudioRecord.read(buffer, 0, buffer.length);
	}

	/** This method is called by SDL using JNI. */
	public static int captureReadByteBuffer(byte[] buffer, boolean blocking) {
		// !!! FIXME: this is available in API Level 23. Until then, we always
		// block. :(
		// return mAudioRecord.read(buffer, 0, buffer.length, blocking ?
		// AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
		return mAudioRecord.read(buffer, 0, buffer.length);
	}

	public static void setOnPlayerInfoListener(OnPlayerInfoListener onInfoListener)
	{
		onPlayerInfoListener = onInfoListener;
	}

	// C functions we call
	public static native int nativeInit(String arguments);

	public static native void nativeLowMemory();

	public static native void nativeQuit();

	public static native void nativePause();

	public static native void nativeResume();

	public static native void onNativeDropFile(String filename);

	public static native void onNativeResize(int x, int y, int format, float rate);

	public static native int onNativePadDown(int device_id, int keycode);

	public static native int onNativePadUp(int device_id, int keycode);

	public static native void onNativeJoy(int device_id, int axis, float value);

	public static native void onNativeHat(int device_id, int hat_id, int x, int y);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeKeyboardFocusLost();

	public static native void onNativeMouse(int button, int action, float x, float y);

	public static native void onNativeTouch(int touchDevId, int pointerFingerId, int action, float x, float y, float p);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void onNativeSurfaceChanged();

	public static native void onNativeSurfaceDestroyed();

	public static native int nativeAddJoystick(int device_id, String name, int is_accelerometer, int nbuttons,
											   int naxes, int nhats, int nballs);

	public static native int nativeRemoveJoystick(int device_id);

	public static native String nativeGetHint(String name);

	/**my jni call method */
	public static native void wlStart();
	//暂停
	public static native void wlPause();
	//播放
	public static native void wlPlay();
	//得到总的时长
	public static native int wlDuration();
	//seek 到几秒 0:成功，1：失败
	public static native int wlSeekTo(int sec);
	//释放空间
	public static native void wlRealease();
	//得到当前播放的
	public static native int wlNowTime();
	//是否加载中
	public static native int wlIsInit();
	//是否停止
	public static native int wlIsRelease();


	//准备开始回调
	public static void onPrepard()
	{
		if(mOnPlayerPrepard != null)
		{
			mOnPlayerPrepard.onPrepard();
		}
	}



	//准备播放回调
	public interface OnPlayerPrepard
	{
		void onPrepard();
	}
	//开始播放
	public static void prePard()
	{
		if(mSDLThread != null)
			mSDLThread = null;
		mSDLThread = new Thread(runnable, "mainThread");
		mSDLThread.start();
	}

	private static Runnable runnable = new Runnable() {
		@Override
		public void run() {
			System.out.println("url:" + url);
			WlPlayer.nativeInit(url);
		}
	};

	public static void next(String u)
	{
		if(wlIsInit() == -1)
		{
			if(onErrorListener != null)
			{
				onErrorListener.onError(0x2001, "player is initing, please try later!");
			}
			return;
		}
		url = u;
		if(mSDLThread != null)
		{
			mSDLThread = null;
		}
		mSDLThread = new Thread(releaseRunnable, "mainThread");
		mSDLThread.start();
	}

	private static Runnable releaseRunnable = new Runnable() {
		@Override
		public void run() {
			WlPlayer.wlRealease();
			WlPlayer.nativeInit(url);
		}
	};

	private static Runnable onlyReleaseRunable = new Runnable() {
		@Override
		public void run() {
			WlPlayer.wlRealease();
		}
	};

	public static void release()
	{
		if(wlIsRelease() != -1)
		{
			if(mSDLThread != null)
			{
				mSDLThread = null;
			}
			mSDLThread = new Thread(onlyReleaseRunable, "mainThread");
			mSDLThread.start();
		}
		else
		{
			if(onErrorListener != null)
			{
				onErrorListener.onError(0x2002, "player is already release!");
			}
		}
		initialize();
	}
	//
	public interface OnPlayerInfoListener
	{
		void onLoad();
		void onPlay();
	}

	public interface OnCompleteListener
	{
		void onConplete();
	}

	public interface OnErrorListener
	{
		void onError(int code, String msg);
	}

	//播放完成
	public static void onCompleted()
	{
		if(onCompleteListener != null)
		{
			onCompleteListener.onConplete();
		}
	}

	//播放出错
	public static void onError(int code, String msg)
	{
		if(onErrorListener != null)
		{
			onErrorListener.onError(code, msg);
		}
	}

	//加载中
	public static void onLoad()
	{
		if(onPlayerInfoListener != null)
		{
			onPlayerInfoListener.onLoad();
		}
	}

	//播放中
	public static void onPlay()
	{
		if(onPlayerInfoListener != null)
		{
			onPlayerInfoListener.onPlay();
		}
	}

}



/*
 * A null joystick handler for API level < 12 devices (the accelerometer is
 * handled separately)
 */
class SDLJoystickHandler {

	/**
	 * Handles given MotionEvent.
	 *
	 * @param event
	 *            the event to be handled.
	 * @return if given event was processed.
	 */
	public boolean handleMotionEvent(MotionEvent event) {
		return false;
	}

	/**
	 * Handles adding and removing of input devices.
	 */
	public void pollInputDevices() {
	}
}

/* Actual joystick functionality available for API >= 12 devices */
class SDLJoystickHandler_API12 extends SDLJoystickHandler {

	static class SDLJoystick {
		public int device_id;
		public String name;
		public ArrayList<InputDevice.MotionRange> axes;
		public ArrayList<InputDevice.MotionRange> hats;
	}

	static class RangeComparator implements Comparator<InputDevice.MotionRange> {
		@Override
		public int compare(InputDevice.MotionRange arg0, InputDevice.MotionRange arg1) {
			return arg0.getAxis() - arg1.getAxis();
		}
	}

	private ArrayList<SDLJoystick> mJoysticks;

	public SDLJoystickHandler_API12() {

		mJoysticks = new ArrayList<SDLJoystick>();
	}

	@Override
	public void pollInputDevices() {
		int[] deviceIds = InputDevice.getDeviceIds();
		// It helps processing the device ids in reverse order
		// For example, in the case of the XBox 360 wireless dongle,
		// so the first controller seen by SDL matches what the receiver
		// considers to be the first controller

		for (int i = deviceIds.length - 1; i > -1; i--) {
			SDLJoystick joystick = getJoystick(deviceIds[i]);
			if (joystick == null) {
				joystick = new SDLJoystick();
				InputDevice joystickDevice = InputDevice.getDevice(deviceIds[i]);
				if (WlPlayer.isDeviceSDLJoystick(deviceIds[i])) {
					joystick.device_id = deviceIds[i];
					joystick.name = joystickDevice.getName();
					joystick.axes = new ArrayList<InputDevice.MotionRange>();
					joystick.hats = new ArrayList<InputDevice.MotionRange>();

					List<InputDevice.MotionRange> ranges = joystickDevice.getMotionRanges();
					Collections.sort(ranges, new RangeComparator());
					for (InputDevice.MotionRange range : ranges) {
						if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
							if (range.getAxis() == MotionEvent.AXIS_HAT_X
									|| range.getAxis() == MotionEvent.AXIS_HAT_Y) {
								joystick.hats.add(range);
							} else {
								joystick.axes.add(range);
							}
						}
					}

					mJoysticks.add(joystick);
					WlPlayer.nativeAddJoystick(joystick.device_id, joystick.name, 0, -1, joystick.axes.size(),
							joystick.hats.size() / 2, 0);
				}
			}
		}

		/* Check removed devices */
		ArrayList<Integer> removedDevices = new ArrayList<Integer>();
		for (int i = 0; i < mJoysticks.size(); i++) {
			int device_id = mJoysticks.get(i).device_id;
			int j;
			for (j = 0; j < deviceIds.length; j++) {
				if (device_id == deviceIds[j])
					break;
			}
			if (j == deviceIds.length) {
				removedDevices.add(Integer.valueOf(device_id));
			}
		}

		for (int i = 0; i < removedDevices.size(); i++) {
			int device_id = removedDevices.get(i).intValue();
			WlPlayer.nativeRemoveJoystick(device_id);
			for (int j = 0; j < mJoysticks.size(); j++) {
				if (mJoysticks.get(j).device_id == device_id) {
					mJoysticks.remove(j);
					break;
				}
			}
		}
	}

	protected SDLJoystick getJoystick(int device_id) {
		for (int i = 0; i < mJoysticks.size(); i++) {
			if (mJoysticks.get(i).device_id == device_id) {
				return mJoysticks.get(i);
			}
		}
		return null;
	}

	@Override
	public boolean handleMotionEvent(MotionEvent event) {
		if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0) {
			int actionPointerIndex = event.getActionIndex();
			int action = event.getActionMasked();
			switch (action) {
				case MotionEvent.ACTION_MOVE:
					SDLJoystick joystick = getJoystick(event.getDeviceId());
					if (joystick != null) {
						for (int i = 0; i < joystick.axes.size(); i++) {
							InputDevice.MotionRange range = joystick.axes.get(i);
						/* Normalize the value to -1...1 */
							float value = (event.getAxisValue(range.getAxis(), actionPointerIndex) - range.getMin())
									/ range.getRange() * 2.0f - 1.0f;
							WlPlayer.onNativeJoy(joystick.device_id, i, value);
						}
						for (int i = 0; i < joystick.hats.size(); i += 2) {
							int hatX = Math.round(event.getAxisValue(joystick.hats.get(i).getAxis(), actionPointerIndex));
							int hatY = Math
									.round(event.getAxisValue(joystick.hats.get(i + 1).getAxis(), actionPointerIndex));
							WlPlayer.onNativeHat(joystick.device_id, i / 2, hatX, hatY);
						}
					}
					break;
				default:
					break;
			}
		}
		return true;
	}
}

class SDLGenericMotionListener_API12 implements View.OnGenericMotionListener {
	// Generic Motion (mouse hover, joystick...) events go here
	@Override
	public boolean onGenericMotion(View v, MotionEvent event) {
		float x, y;
		int action;

		switch (event.getSource()) {
			case InputDevice.SOURCE_JOYSTICK:
			case InputDevice.SOURCE_GAMEPAD:
			case InputDevice.SOURCE_DPAD:
				return WlPlayer.handleJoystickMotionEvent(event);

			case InputDevice.SOURCE_MOUSE:
				action = event.getActionMasked();
				switch (action) {
					case MotionEvent.ACTION_SCROLL:
						x = event.getAxisValue(MotionEvent.AXIS_HSCROLL, 0);
						y = event.getAxisValue(MotionEvent.AXIS_VSCROLL, 0);
						WlPlayer.onNativeMouse(0, action, x, y);
						return true;

					case MotionEvent.ACTION_HOVER_MOVE:
						x = event.getX(0);
						y = event.getY(0);

						WlPlayer.onNativeMouse(0, action, x, y);
						return true;

					default:
						break;
				}
				break;

			default:
				break;
		}

		// Event was not managed
		return false;
	}

}

