package com.worldtech.camera2video

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.worldtech.camera2video.activity.RecordVideoActivity
import com.worldtech.camera2video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var currentBinding: ActivityMainBinding
    private var rxPermissions: RxPermissions? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(currentBinding.root)

        rxPermissions = RxPermissions(this)
        currentBinding.tvRecordVideo.setOnClickListener {
            rxPermissions!!.request(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                .subscribe { granted ->
                    if (granted) {
                        startRecordVideoActivity()
                    }
                }
        }
    }

    private fun startRecordVideoActivity() {
        startActivity(Intent(this, RecordVideoActivity::class.java))
    }

}
