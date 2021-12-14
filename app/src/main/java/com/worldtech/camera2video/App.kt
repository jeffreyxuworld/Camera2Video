package com.worldtech.camera2video

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmField
        var context: Context? = null
    }
}