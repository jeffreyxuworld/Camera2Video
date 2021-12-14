package com.worldtech.camera2video.fragment

import androidx.annotation.RequiresApi
import android.os.Build
import android.view.View
import android.view.TextureView
import android.widget.ImageView
import com.worldtech.camera2video.view.TCConfigPlayerView
import android.widget.RelativeLayout
import android.widget.TextView
import com.worldtech.camera2video.view.ComposeRecordBtn
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.worldtech.camera2video.R
import com.worldtech.camera2video.utils.*
import com.worldtech.camera2video.view.CustomToast
import java.text.DecimalFormat

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class RecordVideo2Fragment : Fragment(), RecordVideoInterface, View.OnClickListener {
    private var mTextureView: TextureView? = null
    private var recorder_facing: ImageView? = null
    private var playerView: TCConfigPlayerView? = null
    private var record_layout: RelativeLayout? = null
    private var rl_record_tip: RelativeLayout? = null
    private var rl_result: RelativeLayout? = null
    private var progress_time: TextView? = null
    private var tv_record_again: TextView? = null
    private var tv_upload: TextView? = null
    private var compose_record_btn: ComposeRecordBtn? = null
    private var record_status = RecordStatus.TYPE_DEFAULT
    private val mMinDuration = 5000
    private var record_file: String? = null
    private var time = 0
    private var cameraControl: CameraControl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initView(inflater, container)
    }

    fun initView(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.fragment_record_video2, container, false)
        mTextureView = view.findViewById(R.id.textureView)
        recorder_facing = view.findViewById(R.id.recorder_facing)
        playerView = view.findViewById(R.id.playerView)
        record_layout = view.findViewById(R.id.record_layout)
        rl_record_tip = view.findViewById(R.id.rl_record_tip)
        rl_result = view.findViewById(R.id.rl_result)
        progress_time = view.findViewById(R.id.process_time)
        tv_record_again = view.findViewById(R.id.tv_record_again)
        tv_upload = view.findViewById(R.id.tv_upload)
        compose_record_btn = view.findViewById(R.id.compose_record_btn)
        compose_record_btn!!.setOnClickListener(this)
        tv_record_again!!.setOnClickListener(this)
        tv_upload!!.setOnClickListener(this)
        recorder_facing!!.setOnClickListener(this)
        initDataFragment()
        return view
    }

    private fun initDataFragment() {
        cameraControl = CameraControl(activity, mTextureView)
        cameraControl!!.setRecordVideoInterface(this)
    }

    fun startRecord() {
        cameraControl!!.prepareMediaRecorder()
        cameraControl!!.startMediaRecorder() //开始录制
    }

    private fun stopRecord() {
        cameraControl!!.stopRecording(true)
    }

    override fun startRecordRes() {
        updateRecordStatus(RecordStatus.TYPE_START_RECORD)
    }

    override fun onRecording(milliSecond: Long) {
        if (activity != null && !activity!!.isFinishing) {
            activity!!.runOnUiThread {
                compose_record_btn!!.setProgress(milliSecond.toInt())
                time = (milliSecond / 1000f).toInt()
                val fnum = DecimalFormat("##0.0")
                val timeFormat = fnum.format(time.toLong())
                progress_time!!.text = timeFormat
                if (time < mMinDuration / 1000f) {
                    compose_record_btn!!.tag = "false"
                } else {
                    compose_record_btn!!.tag = "true"
                }
            }
        }
    }

    override fun onRecordFinish(videoPath: String?) {
        record_file = videoPath
        playerView!!.setDataBean(videoPath, time)
        updateRecordStatus(RecordStatus.TYPE_STOP_RECORD)
    }

    override fun onRecordError() {
        CustomToast.showToast(R.string.record_video_error)
        updateRecordStatus(RecordStatus.TYPE_STOP_RECORD)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.compose_record_btn -> switchRecord()
            R.id.tv_upload -> {
                //todo 上传视频
                cameraControl!!.finishControl()
                compose_record_btn!!.releaseRecord()
            }
            R.id.tv_record_again -> recordAgain()
            R.id.recorder_facing -> switchFacing()
        }
    }

    private fun switchFacing() {
        if (!CommonUtil.fastClick()) {
            cameraControl!!.switchCamera()
        }
    }

    private fun recordAgain() {
        FileUtils.deleteFile(record_file)
        updateRecordStatus(RecordStatus.TYPE_DEFAULT)
    }

    private fun switchRecord() {
        when (record_status) {
            RecordStatus.TYPE_DEFAULT, RecordStatus.TYPE_STOP_RECORD -> startRecord()
            RecordStatus.TYPE_START_RECORD -> if (compose_record_btn!!.tag == "false") {
                CustomToast.showToast(R.string.aleast_record_5s)
            } else {
                stopRecord()
            }
        }
    }

    private fun updateRecordStatus(type: Int) {
        record_status = type
        if (type == RecordStatus.TYPE_DEFAULT) {
            mTextureView!!.visibility = View.VISIBLE
            compose_record_btn!!.visibility = View.VISIBLE
            compose_record_btn!!.releaseRecord()
            playerView!!.visibility = View.GONE
            record_layout!!.setBackgroundResource(R.color.black)
            rl_record_tip!!.visibility = View.GONE
            rl_result!!.visibility = View.GONE
            recorder_facing!!.visibility = View.VISIBLE
        } else if (type == RecordStatus.TYPE_START_RECORD) {
            mTextureView!!.visibility = View.VISIBLE
            compose_record_btn!!.visibility = View.VISIBLE
            compose_record_btn!!.startRecord()
            playerView!!.visibility = View.GONE
            record_layout!!.setBackgroundResource(R.drawable.tran)
            rl_record_tip!!.visibility = View.VISIBLE
            rl_result!!.visibility = View.GONE
            recorder_facing!!.visibility = View.GONE
        } else if (type == RecordStatus.TYPE_STOP_RECORD) {
            mTextureView!!.visibility = View.GONE
            compose_record_btn!!.visibility = View.GONE
            playerView!!.visibility = View.VISIBLE
            record_layout!!.setBackgroundResource(R.color.black)
            rl_record_tip!!.visibility = View.GONE
            rl_result!!.visibility = View.VISIBLE
            recorder_facing!!.visibility = View.GONE
        }
    }
}