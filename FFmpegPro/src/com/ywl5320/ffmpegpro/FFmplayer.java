package com.ywl5320.ffmpegpro;

public class FFmplayer {
	
	static
	{
		System.loadLibrary("avutil-55");
		System.loadLibrary("swresample-2");
		System.loadLibrary("avcodec-57");
		System.loadLibrary("avformat-57");
		System.loadLibrary("swscale-4");
		System.loadLibrary("postproc-54");
		System.loadLibrary("avfilter-6");
		System.loadLibrary("avdevice-57");
		System.loadLibrary("myffmpeg");
	}
	
	public native void showFFmpegInfo();
	
}
