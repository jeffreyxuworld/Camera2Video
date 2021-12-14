package com.worldtech.camera2video.utils

import java.io.File
import com.worldtech.camera2video.App
import android.os.Build
import android.os.Environment
import java.lang.Exception
import android.text.TextUtils

object FileUtils {
    const val VIDEO_PATH_NAME = "videorecord"

    /**
     * 根目录下创建文件夹
     * @param dirName（文件夹名称）
     * @return
     */
    @JvmStatic
    fun getDiskCacheDir(dirName: String?): File {
        var file: File? = null
        val cachePath = cachePath
        file = File(cachePath, dirName)
        if (file.exists()) {
            return file
        } else {
            if (file.mkdir()) {
                return file
            }
        }
        return file
    }//路径是:/data/data/< package name >/cach/…//路径是:/data/data/< package name >/cach/…//SD根目录:/mnt/sdcard/ (6.0后写入需要用户授权)//路径为:/mnt/sdcard//Android/data/< package name >/cach/…

    /**
     * app 缓存根目录
     * @return
     */
    val cachePath: String?
        get() {
            val context = App.context
            var cachePath: String? = null
            cachePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                App.context!!.getExternalFilesDir(null)!!.absolutePath
            } else {
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                    if (context!!.externalCacheDir != null) {
                        context.externalCacheDir!!.path //路径为:/mnt/sdcard//Android/data/< package name >/cach/…
                    } else {
                        try {
                            Environment.getExternalStorageDirectory().path //SD根目录:/mnt/sdcard/ (6.0后写入需要用户授权)
                        } catch (e: Exception) {
                            context.cacheDir.path //路径是:/data/data/< package name >/cach/…
                        }
                    }
                } else {
                    context!!.cacheDir.path //路径是:/data/data/< package name >/cach/…
                }
            }
            return cachePath
        }

    fun deleteFile(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        val file = File(filePath)
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            } else {
                val filePaths = file.list()
                for (path in filePaths) {
                    deleteFile(filePath + File.separator + path)
                }
                file.delete()
            }
        }
    }
}