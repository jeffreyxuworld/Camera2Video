package com.worldtech.camera2video.view;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.worldtech.camera2video.R;
import com.worldtech.camera2video.utils.FileUtils;
import com.worldtech.camera2video.utils.PlayStatus;
import com.worldtech.camera2video.utils.VideoUtils;

import java.lang.ref.WeakReference;
import java.util.Locale;


public class TCConfigPlayerView extends RelativeLayout implements View.OnClickListener, ITXVodPlayListener {

    private static final String TAG = TCConfigPlayerView.class.getSimpleName();
    private Context mContext;
    private TXCloudVideoView mTXCloudVideoView;
    private ImageView mPlayIcon;
    private TextView mTextProgress, all_time;
    protected ImageView mBgImageView;
    private SeekBar mSeekBar;
    private int mPlayStatus = PlayStatus.TYPE_DEFAULT;
    private String videoUrl;
    private String videoImgPath;
    private TXVodPlayer mTXVodPlayer;
    private TXVodPlayConfig mTXConfig = new TXVodPlayConfig();
    private PhoneStateListener mPhoneListener = null;
    private boolean mStartSeek = false;//是否滑动了seekbar
    private long mTrackingTouchTS = 0;


    public TCConfigPlayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public TCConfigPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCConfigPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.tc_config_player_view, this);
        initView(view);
        initPlayer();
    }

    private void initPlayer() {
        if (mTXVodPlayer == null) {
            mTXConfig.setCacheFolderPath(FileUtils.getDiskCacheDir("democache").getAbsolutePath());
            mTXConfig.setMaxCacheItems(3);
            mTXVodPlayer = new TXVodPlayer(getContext());
            mTXVodPlayer.setVodListener(this);
            mTXVodPlayer.setPlayerView(mTXCloudVideoView);
            mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            mTXVodPlayer.setConfig(mTXConfig);
            mTXVodPlayer.setAutoPlay(true);
            mPhoneListener = new TXPhoneStateListener(mTXVodPlayer);
            TelephonyManager tm = (TelephonyManager) getContext().getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void initView(View view) {
        mTXCloudVideoView = view.findViewById(R.id.video_view);
        mTextProgress = view.findViewById(R.id.progress_time);
        all_time = view.findViewById(R.id.all_time);
        mPlayIcon = view.findViewById(R.id.play_btn);
        mSeekBar = view.findViewById(R.id.seekbar);
        mBgImageView = view.findViewById(R.id.background);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
                updateTextProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mStartSeek = false;
                mTXVodPlayer.seek(seekBar.getProgress());
                mTrackingTouchTS = System.currentTimeMillis();
            }
        });
        mPlayIcon.setOnClickListener(this);
        mTXCloudVideoView.setOnClickListener(this);
    }

    private void updateTextProgress(int progress) {
        if (mTextProgress != null) {
            mTextProgress.setText(String.format(Locale.CHINA, "%02d:%02d", (progress % 3600) / 60, (progress % 3600) % 60));
        }
    }

    public void setDataBean(String url, int alltime) {
        videoUrl = url;
        Bitmap bitmap = VideoUtils.getVideoThumb(url);
        mBgImageView.setImageBitmap(bitmap);
        all_time.setText(String.format(Locale.CHINA, "%02d:%02d", (alltime % 3600) / 60, (alltime % 3600) % 60));
    }

    public void updatePlayStatus(int status) {
        if (mPlayIcon != null) {
            mPlayStatus = status;
            if (mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING) {
                mPlayIcon.setVisibility(VISIBLE);
                mPlayIcon.setBackgroundResource(R.drawable.play_start);
            } else if (mPlayStatus == PlayStatus.TYPE_START_PLAYING || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING || mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES) {
                mPlayIcon.setVisibility(INVISIBLE);
                mBgImageView.setVisibility(GONE);
            } else if (mPlayStatus == PlayStatus.TYPE_STOP_PLAYING) {
                stopVideoPlay();
            }
        }
    }

    public void updateSeekView(int progress, int duration) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
            if (mSeekBar.getMax() != duration) {
                mSeekBar.setMax(duration);
            }
        }
        updateTextProgress(progress);
    }

    private void clickPlayBtn() {
        if (mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING) {
            resumeVod();
        } else if (mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING) {
            pauseVod();
        } else if (mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_STOP_PLAYING) {
            startPlay();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_btn:
                clickPlayBtn();
                break;
            case R.id.video_view:
                clickVideoView();
                break;
        }
    }

    private void clickVideoView() {
        if (isInPlaying()) {
            mPlayIcon.setVisibility(VISIBLE);
            mPlayIcon.setBackgroundResource(R.drawable.play_pause);
        }
    }


    public boolean isDefaultStatus() {
        if (mPlayStatus == PlayStatus.TYPE_DEFAULT || mPlayStatus == PlayStatus.TYPE_STOP_PLAYING) {
            return true;
        } else {
            return false;
        }
    }

    private void startPlay() {
        updatePlayStatus(PlayStatus.TYPE_START_PLAYING);
        int result = mTXVodPlayer.startPlay(videoUrl);
        if (0 != result) {
            stopPlay(true);
        }
    }

    public void pauseVod() {
        if (isInPlaying()) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer.pause();
                updatePlayStatus(PlayStatus.TYPE_PAUSE_PLAYING);
            }
        }
    }

    public void resumeVod() {
        if (isPausing()) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer.resume();
                updatePlayStatus(PlayStatus.TYPE_RESUME_PLAYING);
            }
        } else {
            startPlay();
        }
    }

    private boolean isPausing() {
        if (mPlayStatus == PlayStatus.TYPE_PAUSE_PLAYING) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isInPlaying() {
        if (mPlayStatus == PlayStatus.TYPE_START_PLAYING || mPlayStatus == PlayStatus.TYPE_START_PLAYING_RES || mPlayStatus == PlayStatus.TYPE_RESUME_PLAYING) {
            return true;
        } else {
            return false;
        }
    }

    public void stopVideoPlay() {
        mSeekBar.setProgress(0);
        mPlayIcon.setVisibility(VISIBLE);
        mPlayIcon.setBackgroundResource(R.drawable.play_start);
        updateTextProgress(0);
        mBgImageView.setVisibility(VISIBLE);
        stopPlay(true);
    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setVodListener(null);
            mTXVodPlayer.stopPlay(clearLastFrame);
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            if (mStartSeek) {
                return;
            }
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);
            long curTS = System.currentTimeMillis();
            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            if (Math.abs(curTS - mTrackingTouchTS) < 500) {
                return;
            }
            if (progress > duration) {
                return;
            }
            mTrackingTouchTS = curTS;
            updateSeekView(progress, duration);
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            updatePlayStatus(PlayStatus.TYPE_STOP_PLAYING);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            updatePlayStatus(PlayStatus.TYPE_START_PLAYING_RES);
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle status) {
        if (mTXVodPlayer == null) {
            return;
        }
        if (status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) > status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)) {
            mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
        } else {
            mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        }
    }

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TXVodPlayer> mPlayer;

        public TXPhoneStateListener(TXVodPlayer player) {
            mPlayer = new WeakReference<TXVodPlayer>(player);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch (state) {
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (player != null) player.setMute(true);
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (player != null) player.setMute(false);
                    break;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destory();
    }

    public void destory() {
        if (!isDefaultStatus()) {
            if (mTXVodPlayer != null) {
                mTXVodPlayer.stopPlay(true);
                mTXVodPlayer.setVodListener(null);
            }
        }
        mPhoneListener = null;
    }

}
