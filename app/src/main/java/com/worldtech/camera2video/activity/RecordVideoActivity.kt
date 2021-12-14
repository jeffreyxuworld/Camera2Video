package com.worldtech.camera2video.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.worldtech.camera2video.R
import com.worldtech.camera2video.databinding.ActivityRecordVideoBinding
import com.worldtech.camera2video.fragment.RecordVideo2Fragment
import com.worldtech.camera2video.fragment.RecordVideoFragment

class RecordVideoActivity : FragmentActivity() {

    private lateinit var currentBinding: ActivityRecordVideoBinding
    private var videoFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBinding = ActivityRecordVideoBinding.inflate(layoutInflater)
        setContentView(currentBinding.root)

        currentBinding.cancel.setOnClickListener(View.OnClickListener { v: View? -> finishFragment() })
        videoFragment = recordVideoFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, videoFragment!!, "RecordVideoFragment")
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        finishFragment()
    }

    private val recordVideoFragment: Fragment
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            RecordVideoFragment()
        } else {
            RecordVideo2Fragment()
        }

    private fun finishFragment() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            ((RecordVideoFragment)videoFragment).exit();
        } else {
            finish()
        }
    }
}