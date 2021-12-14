package com.worldtech.camera2video.utils;

public class CommonUtil {

    private static long lastClickTime;

    /**
     * 是否过快点击
     */
    public static boolean fastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}
