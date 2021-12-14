package com.worldtech.camera2video.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.worldtech.camera2video.R

class ComposeRecordBtn : RelativeLayout {
    private var mContext: Context? = null
    private var mIvRecordRing: RecordStartView? = null
    private var mIvRecordStart: ImageView? = null
    private var mIvRecordPause: ImageView? = null
    private var recordRingZoomOutXAn: ObjectAnimator? = null
    private var recordRingZoomOutYAn: ObjectAnimator? = null
    private var recordStartZoomOutXAn: ObjectAnimator? = null
    private var recordStartZoomOutYAn: ObjectAnimator? = null
    private var animatorSet: AnimatorSet? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.compose_record_btn, this)
        mIvRecordRing = findViewById(R.id.iv_record_ring)
        mIvRecordStart = findViewById(R.id.iv_record)
        mIvRecordPause = findViewById(R.id.iv_record_pause)
    }

    fun startRecord() {
        gotoRecordMode()
    }

    fun resumeRecord() {
        gotoRecordMode()
        mIvRecordRing!!.resumeRecord()
    }

    fun pauseRecord() {
        gotoPauseMode()
    }

    fun stopRecord() {
        gotoNormalMode()
        mIvRecordRing!!.stopRecord()
    }

    fun releaseRecord() {
        mIvRecordRing!!.stopRecord()
        gotoNormalMode()
    }

    fun deleteLast() {
        clear()
        mIvRecordRing!!.deleteLast()
        gotoNormalMode()
    }

    private fun gotoPauseMode() {
        mIvRecordPause!!.visibility = GONE
        mIvRecordStart!!.setBackgroundResource(R.drawable.ugc_round_pause_record)
        mIvRecordStart!!.visibility = VISIBLE
    }

    private fun gotoRecordMode() {
        mIvRecordPause!!.visibility = VISIBLE
        mIvRecordStart!!.visibility = GONE
    }

    fun gotoNormalMode() {
        mIvRecordPause!!.visibility = GONE
        mIvRecordStart!!.visibility = VISIBLE
    }

    fun setProgress(milliSecond: Int) {
        mIvRecordRing!!.progress = milliSecond
    }

    val process: Int
        get() = mIvRecordRing!!.progress

    fun clear() {
        if (animatorSet != null) {
            animatorSet!!.cancel()
            animatorSet = null
        }
        if (recordRingZoomOutXAn != null) {
            recordRingZoomOutXAn!!.cancel()
            recordRingZoomOutXAn = null
        }
        if (recordRingZoomOutYAn != null) {
            recordRingZoomOutYAn!!.cancel()
            recordRingZoomOutYAn = null
        }
        if (recordStartZoomOutXAn != null) {
            recordStartZoomOutXAn!!.cancel()
            recordStartZoomOutXAn = null
        }
        if (recordStartZoomOutYAn != null) {
            recordStartZoomOutYAn!!.cancel()
            recordStartZoomOutYAn = null
        }
    }
}