package com.worldtech.camera2video.view

import android.view.LayoutInflater
import com.worldtech.camera2video.App
import android.widget.TextView
import android.widget.Toast
import android.view.WindowManager
import android.content.Context
import android.view.Gravity
import com.worldtech.camera2video.R
import java.lang.Exception

object CustomToast {
    fun showToast(txtId: Int?) {
        try {
            val toastRoot = LayoutInflater.from(App.context).inflate(R.layout.custom_toast, null)
            val mTextView = toastRoot.findViewById<TextView>(R.id.message)
            mTextView.setText(txtId!!)
            val toastStart = Toast(App.context)
            val wm = App.context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val height = wm.defaultDisplay.height
            toastStart.setGravity(Gravity.TOP, 0, height / 2)
            toastStart.duration = Toast.LENGTH_SHORT
            toastStart.view = toastRoot
            toastStart.show()
        } catch (e: Exception) {
        }
    }

    @JvmStatic
    fun showToast(name: String?) {
        try {
            val toastRoot = LayoutInflater.from(App.context).inflate(R.layout.custom_toast, null)
            val mTextView = toastRoot.findViewById<TextView>(R.id.message)
            mTextView.text = name
            val toastStart = Toast(App.context)
            val wm = App.context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val height = wm.defaultDisplay.height
            toastStart.setGravity(Gravity.TOP, 0, height / 2)
            toastStart.duration = Toast.LENGTH_SHORT
            toastStart.view = toastRoot
            toastStart.show()
        } catch (e: Exception) {
        }
    }
}