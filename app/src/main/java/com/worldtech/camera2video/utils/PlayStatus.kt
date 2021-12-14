package com.worldtech.camera2video.utils

object PlayStatus {
    const val TYPE_DEFAULT = 0
    const val TYPE_START_PLAYING = 1
    const val TYPE_START_PLAYING_RES = 2
    const val TYPE_PAUSE_PLAYING = 3
    const val TYPE_RESUME_PLAYING = 4
    const val TYPE_STOP_PLAYING = 5
    const val FULL_SCREEN = 0
    const val SMALL_SCREEN = 1
    const val SPLIDE_PAUSE = 0 //列表滑动过程中暂停之前播放的视频
    const val AUTO_PLAYING = 1 //播放中的视频回到列表页自动播放
}