package com.worldtech.camera2video.utils

import android.Manifest
import com.worldtech.camera2video.utils.Camera2Util.getMinPreSize
import com.worldtech.camera2video.view.CustomToast.showToast
import androidx.annotation.RequiresApi
import android.os.Build
import android.app.Activity
import android.view.TextureView
import java.lang.Runnable
import android.media.MediaRecorder
import android.view.TextureView.SurfaceTextureListener
import android.util.SparseIntArray
import com.worldtech.camera2video.utils.CameraControl
import android.view.Surface
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.hardware.camera2.CameraDevice
import com.worldtech.camera2video.utils.CameraControl.MyCameraCallback
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraCaptureSession
import com.worldtech.camera2video.utils.RecordVideoInterface
import java.io.File
import android.hardware.camera2.CameraManager
import android.content.Context
import android.hardware.camera2.params.StreamConfigurationMap
import com.worldtech.camera2video.utils.Camera2Util
import android.graphics.SurfaceTexture
import java.util.Collections
import java.util.Arrays
import android.graphics.ImageFormat
import java.util.Comparator
import java.lang.Exception
import com.worldtech.camera2video.view.CustomToast
import java.util.ArrayList
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import java.lang.Thread
import android.media.CamcorderProfile
import android.util.Log
import com.worldtech.camera2video.App
import java.io.IOException
import android.text.TextUtils
import android.graphics.RectF
import android.media.ImageReader.OnImageAvailableListener
import android.media.Image
import java.nio.ByteBuffer
import android.hardware.camera2.CameraMetadata
import android.util.Size
import com.worldtech.camera2video.R

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CameraControl(private val activity: Activity?, private val mTextureView: TextureView?) :
    Runnable, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, SurfaceTextureListener {
    companion object {
        //拍照方向
        private val ORIENTATION = SparseIntArray()

        init {
            ORIENTATION.append(Surface.ROTATION_0, 90)
            ORIENTATION.append(Surface.ROTATION_90, 0)
            ORIENTATION.append(Surface.ROTATION_180, 270)
            ORIENTATION.append(Surface.ROTATION_270, 180)
        }
    }

    private var mCameraId //后置摄像头ID
            : String? = null
    private var mCameraIdFront //前置摄像头ID
            : String? = null
    private var characteristics: CameraCharacteristics? = null
    private var isCameraFront = false //当前是否是前置摄像头
    private var mPreviewSize //预览的Size
            : Size? = null
    private var mCaptureSize //拍照Size
            : Size? = null
    private var width //TextureView的宽
            = 0
    private var height //TextureView的高
            = 0
    private var mImageReader: ImageReader? = null
    private val mStateCallback: CameraDevice.StateCallback = MyCameraCallback()

    //Camera2
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mCaptureRequest: CaptureRequest? = null
    private var mMediaRecorder //录像
            : MediaRecorder?
    private var mPreviewSession: CameraCaptureSession? = null
    private var mRecordVideoInterface: RecordVideoInterface? = null
    private var videoFile: File? = null
    private val maxTime = 120000 //最大录制时间
    private val maxSize = (30 * 1024 * 1024 * 12 //最大录制大小 默认30m
            ).toLong()
    private var isRecording //是否录制中
            = false
    private var mCountTime //当前录制时间
            = 0

    fun setRecordVideoInterface(videoInterface: RecordVideoInterface?) {
        mRecordVideoInterface = videoInterface
    }

    private fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun initCamera(width: Int, height: Int) {
        setupCamera(width, height)
        openCamera(mCameraId) //打开相机
    }

    private fun setupCamera(width: Int, height: Int) {
        val manager =
            activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager //初始化 CameraManager，主要用于管理系统摄像头
        try {
            //0表示后置摄像头,1表示前置摄像头
//            mCameraId = manager.getCameraIdList()[0];
//            mCameraIdFront = manager.getCameraIdList()[1];
            val cameraList = manager.cameraIdList //获取Android设备的摄像头列表
            for (id in cameraList) {
                val characteristics = manager.getCameraCharacteristics(id)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraIdFront = id
                } else {
                    mCameraId = id
                }
            }
            //前置摄像头和后置摄像头的参数属性不同，所以这里要做下判断
            characteristics = if (isCameraFront) {
                //获取摄像头的详细参数和支持的功能
                manager.getCameraCharacteristics(mCameraIdFront!!)
            } else {
                manager.getCameraCharacteristics(mCameraId!!)
            }
            //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
            val map = characteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            //选择预览尺寸
            mPreviewSize = getMinPreSize(
                map!!.getOutputSizes(
                    SurfaceTexture::class.java
                ), width, height, 1000
            )
            //获取相机支持的最大拍照尺寸
            mCaptureSize =
                Collections.max(Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG))) { lhs, rhs ->
                    java.lang.Long.signum((lhs.width * lhs.height - rhs.height * rhs.width).toLong())
                }
            configureTransform(width, height)
            //此ImageReader用于拍照所需
            setupImageReader()
            //MediaRecorder用于录像所需
            mMediaRecorder = MediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //预览录像
    fun prepareMediaRecorder() {
        if (mCameraDevice == null || !mTextureView!!.isAvailable || null == mPreviewSize) {
            showToast(activity!!.getString(R.string.permission_camera_image_failed_hint))
            return
        }
        try {
            closePreviewSession()
            setUpMediaRecorder()
            val texture = mTextureView.surfaceTexture!!
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mPreviewBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD) //创建捕获请求，在需要预览、拍照、再次预览的时候都需要通过创建请求来完成
            val surfaces: MutableList<Surface> = ArrayList()

            // Set up Surface for the camera preview
            val previewSurface = Surface(texture)
            surfaces.add(previewSurface)
            mPreviewBuilder!!.addTarget(previewSurface)

            // Set up Surface for the MediaRecorder
            val recorderSurface = mMediaRecorder!!.surface
            surfaces.add(recorderSurface)
            mPreviewBuilder!!.addTarget(recorderSurface)
            mCameraDevice!!.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            //创建捕获请求
                            mCaptureRequest = mPreviewBuilder!!.build()
                            mPreviewSession = session
                            //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                            mPreviewSession!!.setRepeatingRequest(mCaptureRequest!!, null, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        showToast(activity!!.getString(R.string.record_video_error))
                    }
                },
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打开Camera
     */
    @SuppressLint("MissingPermission")
    fun openCamera(CameraId: String?) {
        //获取摄像头的管理者CameraManager
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(CameraId!!, mStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //开始录像
    fun startMediaRecorder() {
        // Start recording
        try {
            mMediaRecorder!!.start()
            isRecording = true
            mCountTime = 0
            //开始计时，判断是否已经超过录制时间了
            if (mRecordVideoInterface != null) {
                mRecordVideoInterface!!.startRecordRes()
            }
            Thread(this).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 录像
     */
    private fun setUpMediaRecorder() {
        try {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            // 这里有点投机取巧的方式，不过证明方法也是不错的
            // 录制出来10S的视频，大概1.2M，清晰度不错，而且避免了因为手动设置参数导致无法录制的情况
            // 手机一般都有这个格式CamcorderProfile.QUALITY_480P,因为单单录制480P的视频还是很大的，所以我们在手动根据预览尺寸配置一下videoBitRate,值越高越大
            // QUALITY_QVGA清晰度一般，不过视频很小，一般10S才几百K
            // 判断有没有这个手机有没有这个参数
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P)
                profile.videoBitRate = mPreviewSize!!.width * mPreviewSize!!.height
                mMediaRecorder!!.setProfile(profile)
                mMediaRecorder!!.setPreviewDisplay(Surface(mTextureView!!.surfaceTexture))
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)
                profile.videoBitRate = mPreviewSize!!.width * mPreviewSize!!.height
                mMediaRecorder!!.setProfile(profile)
                mMediaRecorder!!.setPreviewDisplay(Surface(mTextureView!!.surfaceTexture))
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
                mMediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA))
                mMediaRecorder!!.setPreviewDisplay(Surface(mTextureView!!.surfaceTexture))
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
                mMediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF))
                mMediaRecorder!!.setPreviewDisplay(Surface(mTextureView!!.surfaceTexture))
            } else {
                mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                mMediaRecorder!!.setVideoEncodingBitRate(3 * mPreviewSize!!.width * mPreviewSize!!.height)
                mMediaRecorder!!.setVideoFrameRate(15) //帧数  一分钟帧，15帧就够了
                Log.d(
                    "lei",
                    "width = " + mPreviewSize!!.width + "height = " + mPreviewSize!!.height
                )
                mMediaRecorder!!.setVideoSize(mPreviewSize!!.width, mPreviewSize!!.height)
            }
            mMediaRecorder!!.setOnInfoListener(this)
            mMediaRecorder!!.setOnErrorListener(this)
            // 设置最大录制时间
            mMediaRecorder!!.setMaxFileSize(maxSize)
            mMediaRecorder!!.setMaxDuration(maxTime)
            //判断有没有配置过视频地址了
            videoDir()
            mMediaRecorder!!.setOutputFile(videoFile!!.absolutePath)

            //判断是不是前置摄像头,是的话需要旋转对应的角度
            if (isCameraFront) {
                mMediaRecorder!!.setOrientationHint(270)
            } else {
                mMediaRecorder!!.setOrientationHint(90)
            }
            mMediaRecorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun videoDir() {
        val sampleDir = App.context!!.getExternalFilesDir(FileUtils.VIDEO_PATH_NAME)
        if (!sampleDir!!.exists()) {
            sampleDir.mkdirs()
        }
        // 创建文件
        try {
            videoFile = File.createTempFile("recording", ".mp4", sampleDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun switchCamera() {
        if (isRecording) {
            showToast("录制中无法切换")
            return
        }
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
        if (isCameraFront) {
            isCameraFront = false
            setupCamera(width, height)
            openCamera(mCameraId)
        } else {
            isCameraFront = true
            setupCamera(width, height)
            openCamera(mCameraIdFront)
        }
    }

    override fun run() {
        while (isRecording) {
            updateCallBack(mCountTime)
            try {
                mCountTime += 100
                Thread.sleep(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //停止录像
    fun stopRecording(isSucessed: Boolean) {
        if (videoFile == null || !isRecording) {
            return
        }
        try {
            if (mMediaRecorder != null) {
                isRecording = false
                mMediaRecorder!!.stop()
                mMediaRecorder!!.release()
                mCountTime = 0
                resetCamera()
                if (isSucessed) {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface!!.onRecordFinish(videoFile!!.path)
                    }
                } else {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface!!.onRecordError()
                    }
                    updateCallBack(0)
                }
            }
        } catch (e: Exception) {
            updateCallBack(0)
        }
    }

    //重新配置打开相机
    fun resetCamera() {
        if (TextUtils.isEmpty(mCameraId)) {
            return
        }
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
        }
        setupCamera(width, height)
        openCamera(mCameraId)
    }

    /**
     * 回调录制时间
     *
     * @param recordTime
     */
    private fun updateCallBack(recordTime: Int) {
        if (mRecordVideoInterface != null) {
            mRecordVideoInterface!!.onRecording(recordTime.toLong())
        }
    }

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        showToast("录制失败，请重试")
        stopRecording(false)
    }

    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecording(true)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        setSize(width, height)
        initCamera(width, height) //配置相机参数
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        setSize(width, height)
        configureTransform(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        finishControl()
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    //通过CameraDevice.StateCallback监听摄像头的状态（主要包括onOpened、onClosed、onDisconnected、onErro四种状态）
    internal inner class MyCameraCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            startPreview()
            if (null != mTextureView) {
                configureTransform(mTextureView.width, mTextureView.height)
            }
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            mCameraDevice = null
        }
    }

    /**
     * Camera2成功打开，开始预览(startPreview)
     */
    fun startPreview() {
        if (null == mCameraDevice || !mTextureView!!.isAvailable || null == mPreviewSize) {
            return
        }
        val mSurfaceTexture = mTextureView!!.surfaceTexture
            ?: return //获取TextureView的SurfaceTexture，作为预览输出载体
        try {
            closePreviewSession()
            mSurfaceTexture.setDefaultBufferSize(
                mPreviewSize!!.width,
                mPreviewSize!!.height
            ) //设置TextureView的缓冲区大小
            mPreviewBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) //创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            val mSurface = Surface(mSurfaceTexture) //获取Surface显示预览数据
            mPreviewBuilder!!.addTarget(mSurface) //设置Surface作为预览数据的显示界面

            //默认预览不开启闪光灯
            mPreviewBuilder!!.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)

            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice!!.createCaptureSession(
                Arrays.asList(mSurface, mImageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        //系统向摄像头发送 Capture 请求，而摄像头会返回 CameraMetadata，这一切建立在一个叫作 CameraCaptureSession 的会话中
                        try {
                            //创建捕获请求
                            mCaptureRequest = mPreviewBuilder!!.build()
                            mPreviewSession = session
                            //不停的发送获取图像请求，完成连续预览
                            mPreviewSession!!.setRepeatingRequest(mCaptureRequest!!, null, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("lei", "捕获的异常$e")
        }
    }

    //清除预览Session
    private fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession!!.close()
            mPreviewSession = null
        }
    }

    /**
     * 屏幕方向发生改变时调用转换数据方法
     *
     * @param viewWidth  mTextureView 的宽度
     * @param viewHeight mTextureView 的高度
     */
    fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == mTextureView || null == mPreviewSize) {
            return
        }
        val rotation = activity!!.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
            0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width
                .toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize!!.height,
                viewWidth.toFloat() / mPreviewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        }
        mTextureView.setTransform(matrix)
    }

    //配置ImageReader
    private fun setupImageReader() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(
            mCaptureSize!!.width, mCaptureSize!!.height,
            ImageFormat.JPEG, 2
        )
        mImageReader!!.setOnImageAvailableListener({ reader -> //这里拍照完成
            val mImage = reader.acquireNextImage()
            val buffer = mImage.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer[data]
            mImage.close()
        }, null)
    }

    private fun unLockFocus() {
        try {
            // 构建失能AF的请求
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            //            //闪光灯重置为未开启状态
//            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            //继续开启预览
            mPreviewSession!!.setRepeatingRequest(mCaptureRequest!!, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除操作
     */
    fun finishControl() {
        try {
            if (mPreviewSession != null) {
                mPreviewSession!!.close()
                mPreviewSession = null
            }
            if (mCameraDevice != null) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (mImageReader != null) {
                mImageReader!!.close()
                mImageReader = null
            }
            if (mMediaRecorder != null) {
                mMediaRecorder!!.release()
                mMediaRecorder = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {

        //MediaRecorder用于录像所需
        mMediaRecorder = MediaRecorder()
        mTextureView!!.surfaceTextureListener = this
    }
}