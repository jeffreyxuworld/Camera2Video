package com.worldtech.camera2video.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.worldtech.camera2video.R;

public class ComposeRecordBtn extends RelativeLayout {
    private Context mContext;
    private RecordStartView mIvRecordRing;
    private ImageView mIvRecordStart;
    private ImageView mIvRecordPause;
    private ObjectAnimator recordRingZoomOutXAn, recordRingZoomOutYAn, recordStartZoomOutXAn, recordStartZoomOutYAn;
    private AnimatorSet animatorSet;

    public ComposeRecordBtn(Context context) {
        super(context);
        init(context);
    }

    public ComposeRecordBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ComposeRecordBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.compose_record_btn, this);
        mIvRecordRing = findViewById(R.id.iv_record_ring);
        mIvRecordStart = findViewById(R.id.iv_record);
        mIvRecordPause = findViewById(R.id.iv_record_pause);
    }

    public void startRecord() {
        gotoRecordMode();
    }

    public void resumeRecord() {
        gotoRecordMode();
        mIvRecordRing.resumeRecord();
    }

    public void pauseRecord() {
        gotoPauseMode();
    }

    public void stopRecord() {
        gotoNormalMode();
        mIvRecordRing.stopRecord();
    }

    public void releaseRecord() {
        mIvRecordRing.stopRecord();
        gotoNormalMode();
    }

    public void deleteLast() {
        clear();
        mIvRecordRing.deleteLast();
        gotoNormalMode();
    }

    private void gotoPauseMode() {
        mIvRecordPause.setVisibility(View.GONE);
        mIvRecordStart.setBackgroundResource(R.drawable.ugc_round_pause_record);
        mIvRecordStart.setVisibility(VISIBLE);
    }

    private void gotoRecordMode() {
        mIvRecordPause.setVisibility(View.VISIBLE);
        mIvRecordStart.setVisibility(GONE);
    }

    public void gotoNormalMode() {
        mIvRecordPause.setVisibility(View.GONE);
        mIvRecordStart.setVisibility(VISIBLE);
    }

    public void setProgress(int milliSecond) {
        mIvRecordRing.setProgress(milliSecond);
    }

    public int getProcess() {
        return mIvRecordRing.getProgress();
    }

    public void clear() {
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
        if (recordRingZoomOutXAn != null) {
            recordRingZoomOutXAn.cancel();
            recordRingZoomOutXAn = null;
        }
        if (recordRingZoomOutYAn != null) {
            recordRingZoomOutYAn.cancel();
            recordRingZoomOutYAn = null;
        }
        if (recordStartZoomOutXAn != null) {
            recordStartZoomOutXAn.cancel();
            recordStartZoomOutXAn = null;
        }
        if (recordStartZoomOutYAn != null) {
            recordStartZoomOutYAn.cancel();
            recordStartZoomOutYAn = null;
        }
    }

}
