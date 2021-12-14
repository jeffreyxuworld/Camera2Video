package com.worldtech.camera2video.utils

object CommonUtil {
    private var lastClickTime: Long = 0

    /**
     * 是否过快点击
     */
    fun fastClick(): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - lastClickTime
        if (timeD in 1..799) {
            return true
        }
        lastClickTime = time
        return false
    }
}