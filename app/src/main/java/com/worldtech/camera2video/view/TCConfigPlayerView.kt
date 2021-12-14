package com.worldtech.camera2video.view

import android.app.Service
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.AttrRes
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodPlayConfig
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.worldtech.camera2video.R
import com.worldtech.camera2video.utils.FileUtils.getDiskCacheDir
import com.worldtech.camera2video.utils.PlayStatus
import com.worldtech.camera2video.utils.VideoUtils.getVideoThumb
import java.lang.ref.WeakReference
import java.util.*

class TCConfigPlayerView : RelativeLayout, View.OnClickListener, ITXVodPlayListener {
    private var mContext: Context? = null
    private var mTXCloudVideoView: TXCloudVideoView? = null
    private var mPlayIcon: ImageView? = null
    private var mTextProgress: TextView? = null
    private var all_time: TextView? = null
    protected var mBgImageView: ImageView? = null
    private var mSeekBar: SeekBar? = null
    private var mPlayStatus = PlayStatus.TYPE_DEFAULT
    private var videoUrl: String? = null
    private val videoImgPath: String? = null
    private var mTXVodPlayer: TXVodPlayer? = null
    private val mTXConfig = TXVodPlayConfig()
    private var mPhoneListener: PhoneStateListener? = null
    private var mStartSeek = false //是否滑动了seekbar
    private var mTrackingTouchTS: Long = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mContext = context
        val view = LayoutInflater.from(mContext).inflate(R.layout.tc_config_player_view, this)
        initView(view)
        initPlayer()
    }

    private fun initPlayer() {
        if (mTXVodPlayer == null) {
            mTXConfig.setCacheFolderPath(getDiskCacheDir("democache").absolutePath)
            mTXConfig.setMaxCacheItems(3)
            mTXVodPlayer = TXVodPlayer(context)
            mTXVodPlayer!!.setVodListener(this)
            mTXVodPlayer!!.setPlayerView(mTXCloudVideoView)
            mTXVodPlayer!!.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)
            mTXVodPlayer!!.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN)
            mTXVodPlayer!!.setConfig(mTXConfig)
            mTXVodPlayer!!.setAutoPlay(true)
            mPhoneListener = TXPhoneStateListener(mTXVodPlayer!!)
            val tm =
                context.applicationContext.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun initView(view: View) {
        mTXCloudVideoView = view.findViewById(R.id.video_view)
        mTextProgress = view.findViewById(R.id.progress_time)
        all_time = view.findViewById(R.id.all_time)
        mPlayIcon = view.findViewById(R.id.play_btn)
        mSeekBar = view.findViewById(R.id.seekbar)
        mBgImageView = view.findViewById(R.id.background)
        mSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, bFromUser: Boolean) {
                updateTextProgress(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mStartSeek = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mStartSeek = false
                mTXVodPlayer!!.seek(seekBar.progress)
                mTrackingTouchTS = System.currentTimeMillis()
            }
        })
        mPlayIcon!!.setOnClickListener(this)
        mTXCloudVideoView!!.setOnClickListener(this)
    }

    private fun updateTextProgress(progress: Int) {
        if (mTextProgress != null) {
            mTextProgress!!.text =
                String.format(Locale.CHINA, "%02d:%02d", progress % 3600 / 60, progress % 3600 % 60)
        }
    }

    fun setDataBean(url: String?, alltime: Int) {
        videoUrl = url
        val bitmap = getVideoThumb(url)
        mBgImageView!!.setImageBitmap(bitmap)
        all_time!!.text =
            String.format(Locale.CHINA, "%02d:%02d", alltime % 3600 / 60, alltime % 3600 % 60)
    }

    fun updatePlayStatus(status: Int) {
        if (mPlayIcon != null) {
            mPlayStatus = status
            if (mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING) {
                mPlayIcon!!.visibility = VISIBLE
                mPlayIcon!!.setBackgroundResource(R.drawable.play_start)
            } else if (mPlayStatus == PlayStatus.TYPE_START_PLAYING || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING || mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES) {
                mPlayIcon!!.visibility = INVISIBLE
                mBgImageView!!.visibility = GONE
            } else if (mPlayStatus == PlayStatus.TYPE_STOP_PLAYING) {
                stopVideoPlay()
            }
        }
    }

    fun updateSeekView(progress: Int, duration: Int) {
        if (mSeekBar != null) {
            mSeekBar!!.progress = progress
            if (mSeekBar!!.max != duration) {
                mSeekBar!!.max = duration
            }
        }
        updateTextProgress(progress)
    }

    private fun clickPlayBtn() {
        if (mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING) {
            resumeVod()
        } else if (mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING) {
            pauseVod()
        } else if (mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_STOP_PLAYING) {
            startPlay()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.play_btn -> clickPlayBtn()
            R.id.video_view -> clickVideoView()
        }
    }

    private fun clickVideoView() {
        if (isInPlaying) {
            mPlayIcon!!.visibility = VISIBLE
            mPlayIcon!!.setBackgroundResource(R.drawable.play_pause)
        }
    }

    val isDefaultStatus: Boolean
        get() = mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_STOP_PLAYING

    private fun startPlay() {
        updatePlayStatus(PlayStatus.TYPE_START_PLAYING)
        val result = mTXVodPlayer!!.startPlay(videoUrl)
        if (0 != result) {
            stopPlay(true)
        }
    }

    fun pauseVod() {
        if (isInPlaying) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer!!.pause()
                updatePlayStatus(PlayStatus.TYPE_PAUSE_PLAYING)
            }
        }
    }

    fun resumeVod() {
        if (isPausing) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer!!.resume()
                updatePlayStatus(PlayStatus.TYPE_RESUME_PLAYING)
            }
        } else {
            startPlay()
        }
    }

    private val isPausing: Boolean
        private get() = mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING
    private val isInPlaying: Boolean
        private get() = mPlayStatus == PlayStatus.TYPE_START_PLAYING || mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING

    fun stopVideoPlay() {
        mSeekBar!!.progress = 0
        mPlayIcon!!.visibility = VISIBLE
        mPlayIcon!!.setBackgroundResource(R.drawable.play_start)
        updateTextProgress(0)
        mBgImageView!!.visibility = VISIBLE
        stopPlay(true)
    }

    protected fun stopPlay(clearLastFrame: Boolean) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer!!.setVodListener(null)
            mTXVodPlayer!!.stopPlay(clearLastFrame)
        }
    }

    override fun onPlayEvent(txVodPlayer: TXVodPlayer, event: Int, param: Bundle) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            if (mStartSeek) {
                return
            }
            val progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS)
            val duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION)
            val curTS = System.currentTimeMillis()
            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            if (Math.abs(curTS - mTrackingTouchTS) < 500) {
                return
            }
            if (progress > duration) {
                return
            }
            mTrackingTouchTS = curTS
            updateSeekView(progress, duration)
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            updatePlayStatus(PlayStatus.TYPE_STOP_PLAYING)
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            updatePlayStatus(PlayStatus.TYPE_START_PLAYING_RES)
        }
    }

    override fun onNetStatus(txVodPlayer: TXVodPlayer, status: Bundle) {
        if (mTXVodPlayer == null) {
            return
        }
        if (status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) > status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)) {
            mTXVodPlayer!!.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE)
        } else {
            mTXVodPlayer!!.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)
        }
    }

    internal class TXPhoneStateListener(player: TXVodPlayer) : PhoneStateListener() {
        var mPlayer: WeakReference<TXVodPlayer>
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            super.onCallStateChanged(state, incomingNumber)
            val player = mPlayer.get()
            when (state) {
                TelephonyManager.CALL_STATE_RINGING, TelephonyManager.CALL_STATE_OFFHOOK -> player?.setMute(
                    true
                )
                TelephonyManager.CALL_STATE_IDLE -> player?.setMute(false)
            }
        }

        init {
            mPlayer = WeakReference(player)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destory()
    }

    fun destory() {
        if (!isDefaultStatus) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer!!.stopPlay(true)
                mTXVodPlayer!!.setVodListener(null)
            }
        }
        mPhoneListener = null
    }

    companion object {
        private val TAG = TCConfigPlayerView::class.java.simpleName
    }
}