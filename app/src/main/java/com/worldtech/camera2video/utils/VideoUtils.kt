package com.worldtech.camera2video.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.util.HashMap
import java.lang.IllegalArgumentException

object VideoUtils {
    //获取网络视频第一帧
    fun getNetVideoBitmap(videoUrl: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, HashMap<String, String>())
            //获得第一帧图片
            bitmap = retriever.frameAtTime
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return bitmap
    }

    /**
     * 获取本地视频第一帧
     * @param path
     * @return
     */
    @JvmStatic
    fun getVideoThumb(path: String?): Bitmap? {
        val media = MediaMetadataRetriever()
        media.setDataSource(path)
        return media.frameAtTime
    }
}