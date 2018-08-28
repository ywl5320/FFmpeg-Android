package com.ywl5320.ffmpegas;

/**
 * Created by ywl on 2017-7-14.
 */

public class FFmpegPlayer {

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
        System.loadLibrary("wlffmpeg");
    }
    public native void playMyMedia(String url);

}
