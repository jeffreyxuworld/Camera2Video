package com.worldtech.camera2video.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import com.worldtech.camera2video.R;
import com.worldtech.camera2video.utils.CameraControl;
import com.worldtech.camera2video.utils.CommonUtil;
import com.worldtech.camera2video.utils.FileUtils;
import com.worldtech.camera2video.utils.RecordStatus;
import com.worldtech.camera2video.utils.RecordVideoInterface;
import com.worldtech.camera2video.view.ComposeRecordBtn;
import com.worldtech.camera2video.view.CustomToast;
import com.worldtech.camera2video.view.TCConfigPlayerView;

import java.text.DecimalFormat;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RecordVideo2Fragment extends Fragment implements RecordVideoInterface, View.OnClickListener {

    private TextureView mTextureView;
    private ImageView recorder_facing;
    private TCConfigPlayerView playerView;
    private RelativeLayout record_layout, rl_record_tip, rl_result;
    private TextView progress_time, tv_record_again, tv_upload;
    private ComposeRecordBtn compose_record_btn;
    private int record_status = RecordStatus.TYPE_DEFAULT;
    private int mMinDuration = 5000;
    private String record_file;
    private int time;
    private CameraControl cameraControl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initView(inflater, container);
        return view;
    }



    public View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_record_video2, container, false);
        mTextureView = view.findViewById(R.id.textureView);
        recorder_facing = view.findViewById(R.id.recorder_facing);
        playerView = view.findViewById(R.id.playerView);
        record_layout = view.findViewById(R.id.record_layout);
        rl_record_tip = view.findViewById(R.id.rl_record_tip);
        rl_result = view.findViewById(R.id.rl_result);
        progress_time = view.findViewById(R.id.process_time);
        tv_record_again = view.findViewById(R.id.tv_record_again);
        tv_upload = view.findViewById(R.id.tv_upload);
        compose_record_btn = view.findViewById(R.id.compose_record_btn);

        compose_record_btn.setOnClickListener(this);
        tv_record_again.setOnClickListener(this);
        tv_upload.setOnClickListener(this);
        recorder_facing.setOnClickListener(this);

        initDataFragment();
        return view;
    }

    private void initDataFragment() {
        cameraControl = new CameraControl(getActivity(), mTextureView);
        cameraControl.setRecordVideoInterface(this);
    }


    public void startRecord() {
        cameraControl.prepareMediaRecorder();
        cameraControl.startMediaRecorder();//开始录制
    }

    private void stopRecord() {
        cameraControl.stopRecording(true);
    }

    @Override
    public void startRecordRes() {
        updateRecordStatus(RecordStatus.TYPE_START_RECORD);
    }


    @Override
    public void onRecording(long milliSecond) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    compose_record_btn.setProgress((int) milliSecond);
                    time = (int) (milliSecond / 1000f);
                    DecimalFormat fnum = new DecimalFormat("##0.0");
                    String timeFormat = fnum.format(time);
                    progress_time.setText(timeFormat);
                    if (time < mMinDuration / 1000f) {
                        compose_record_btn.setTag("false");
                    } else {
                        compose_record_btn.setTag("true");
                    }
                }
            });
        }
    }

    @Override
    public void onRecordFinish(String videoPath) {
        record_file = videoPath;
        playerView.setDataBean(videoPath, time);
        updateRecordStatus(RecordStatus.TYPE_STOP_RECORD);

    }

    @Override
    public void onRecordError() {
        CustomToast.showToast(R.string.record_video_error);
        updateRecordStatus(RecordStatus.TYPE_STOP_RECORD);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.compose_record_btn:
                switchRecord();
                break;
            case R.id.tv_upload:
                //todo 上传视频
                cameraControl.finishControl();
                compose_record_btn.releaseRecord();
                break;
            case R.id.tv_record_again:
                recordAgain();
                break;
            case R.id.recorder_facing:
                switchFacing();
                break;
        }
    }


    private void switchFacing() {
        if (!CommonUtil.fastClick()) {
            cameraControl.switchCamera();
        }
    }

    private void recordAgain() {
        FileUtils.deleteFile(record_file);
        updateRecordStatus(RecordStatus.TYPE_DEFAULT);
    }

    private void switchRecord() {
        switch (record_status) {
            case RecordStatus.TYPE_DEFAULT:
            case RecordStatus.TYPE_STOP_RECORD:
                startRecord();
                break;
            case RecordStatus.TYPE_START_RECORD:
                if (compose_record_btn.getTag().equals("false")) {
                    CustomToast.showToast(R.string.aleast_record_5s);
                } else {
                    stopRecord();
                }

                break;

        }
    }


    private void updateRecordStatus(int type) {
        record_status = type;
        if (type == RecordStatus.TYPE_DEFAULT) {
            mTextureView.setVisibility(View.VISIBLE);
            compose_record_btn.setVisibility(View.VISIBLE);
            compose_record_btn.releaseRecord();
            playerView.setVisibility(View.GONE);
            record_layout.setBackgroundResource(R.color.black);
            rl_record_tip.setVisibility(View.GONE);
            rl_result.setVisibility(View.GONE);
            recorder_facing.setVisibility(View.VISIBLE);
        } else if (type == RecordStatus.TYPE_START_RECORD) {
            mTextureView.setVisibility(View.VISIBLE);
            compose_record_btn.setVisibility(View.VISIBLE);
            compose_record_btn.startRecord();
            playerView.setVisibility(View.GONE);
            record_layout.setBackgroundResource(R.drawable.tran);
            rl_record_tip.setVisibility(View.VISIBLE);
            rl_result.setVisibility(View.GONE);
            recorder_facing.setVisibility(View.GONE);
        } else if (type == RecordStatus.TYPE_STOP_RECORD) {
            mTextureView.setVisibility(View.GONE);
            compose_record_btn.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            record_layout.setBackgroundResource(R.color.black);
            rl_record_tip.setVisibility(View.GONE);
            rl_result.setVisibility(View.VISIBLE);
            recorder_facing.setVisibility(View.GONE);
        }
    }

}
