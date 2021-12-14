package com.worldtech.camera2video.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.worldtech.camera2video.App;

import java.io.File;

public class FileUtils {

    public static final String VIDEO_PATH_NAME = "videorecord";

    /**
     * 根目录下创建文件夹
     * @param dirName（文件夹名称）
     * @return
     */
    public static File getDiskCacheDir(String dirName) {
        File file = null;
        String cachePath = getCachePath();
        file = new File(cachePath, dirName);
        if (file.exists()) {
            return file;
        } else {
            if (file.mkdir()) {
                return file;
            }
        }
        return file;
    }

    /**
     * app 缓存根目录
     * @return
     */
    public static String getCachePath(){
        Context context = App.context;
        String cachePath = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            cachePath = App.context.getExternalFilesDir(null).getAbsolutePath();
        }else {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                if (context.getExternalCacheDir() != null) {
                    cachePath = context.getExternalCacheDir().getPath(); //路径为:/mnt/sdcard//Android/data/< package name >/cach/…
                } else {
                    try {
                        cachePath = Environment.getExternalStorageDirectory().getPath();   //SD根目录:/mnt/sdcard/ (6.0后写入需要用户授权)
                    } catch (Exception e) {
                        cachePath = context.getCacheDir().getPath();    //路径是:/data/data/< package name >/cach/…
                    }
                }
            } else {
                cachePath = context.getCacheDir().getPath();    //路径是:/data/data/< package name >/cach/…
            }
        }

        return cachePath;
    }

    public static void deleteFile(String filePath) {
        if(TextUtils.isEmpty(filePath)){
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String[] filePaths = file.list();
                for (String path : filePaths) {
                    deleteFile(filePath + File.separator + path);
                }
                file.delete();
            }
        }
    }

}
