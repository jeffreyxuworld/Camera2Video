package com.worldtech.camera2video

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.worldtech.camera2video.activity.RecordVideoActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var rxPermissions: RxPermissions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxPermissions = RxPermissions(this)
        tv_test1.setOnClickListener {
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
