package com.worldtech.camera2video.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.util.HashMap;

public class VideoUtils {

    //获取网络视频第一帧
    public static Bitmap getNetVideoBitmap(String videoUrl) {
        Bitmap bitmap = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 获取本地视频第一帧
     * @param path
     * @return
     */
    public  static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        return media.getFrameAtTime();
    }

}
