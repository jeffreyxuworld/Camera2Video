package com.worldtech.camera2video.utils;

public class PlayStatus {
    public static final int TYPE_DEFAULT= 0;
    public static final int TYPE_START_PLAYING = 1;
    public static final int TYPE_START_PLAYING_RES = 2;
    public static final int TYPE_PAUSE_PLAYING = 3;
    public static final int TYPE_RESUME_PLAYING = 4;
    public static final int TYPE_STOP_PLAYING = 5;


    public static final int FULL_SCREEN = 0;
    public static final int SMALL_SCREEN = 1;


    public static final int SPLIDE_PAUSE = 0;//列表滑动过程中暂停之前播放的视频
    public static final int AUTO_PLAYING = 1;//播放中的视频回到列表页自动播放
}
