package com.worldtech.camera2video.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.worldtech.camera2video.R

class RecordStartView(//开始时间
    private var mContext: Context, attrs: AttributeSet?, defStyleAttr: Int
) : View(
    mContext, attrs, defStyleAttr
) {
    val CENTER_WHAT = 100
    val RING_WHAT = 101
    private val mLongPressRunnable: Runnable? = null
    private val mRingProgressPaint: Paint
    private val mCenterPaint: Paint? = null
    private val mRingPaint: Paint

    //圆环颜色
    var mRingColor: Int

    // 圆环进度的颜色
    var mRingProgressColor: Int

    //圆环的宽度
    var mRingWidth: Int

    //控件宽高
    private var mWidth = 0
    private var mHeight = 0

    //中间X坐标
    private var centerX = 0

    //中间Y坐标
    private var centerY = 0

    //进度
    internal var progress = 0

    //中间方法比例
    private val centerScale = 0.8f

    //半径
    private var radius = 0

    //最大时间
    private var mRingMax = 120000

    //时间间隔
    private val timeSpan: Long = 1000

    constructor(context: Context) : this(context, null) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        mContext = context
    }

    //    public void pauseRecord() {
    //        record_status = STATUS_PAUSE;
    //        pauseTime = System.currentTimeMillis();
    //        lastRecordTime = pauseTime - startTime;
    //    }
    fun resumeRecord() {
//        record_status = STATUS_RESUME;
//        startTime= System.currentTimeMillis();
    }

    fun stopRecord() {
        progress = 0
        postInvalidate()
    }

    fun deleteLast() {
        stopRecord()
    }

    fun setProgress(milliSecond: Int) {
        if (progress < mRingMax) {
            progress = milliSecond
            postInvalidate()
        } else {
            stopRecord()
        }
    }

    fun getProgress(): Int {
        return progress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width = if (widthMode == MeasureSpec.AT_MOST) dp2px(100) else widthSize
        height = if (heightMode == MeasureSpec.AT_MOST) dp2px(100) else heightSize
        setMeasuredDimension(Math.min(width, height), Math.min(width, height))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = width
        mHeight = height
        //获取中心点的位置
        centerX = width / 2
        centerY = height / 2
        radius = centerX - mRingWidth / 2 - 10
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //        drawCenter(canvas);
        drawRing(canvas)
        drawRingProgress(canvas)
    }

    /**
     * 绘制圆环
     *
     * @param canvas
     */
    private fun drawRing(canvas: Canvas) {
        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            radius.toFloat(),
            mRingPaint
        )
    }

    /**
     * 绘制中间的圆
     *
     * @param canvas
     */
    private fun drawCenter(canvas: Canvas) {
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            centerScale * radius,
            mCenterPaint!!
        )
    }

    /**
     * 绘制圆环进度
     *
     * @param canvas
     */
    private fun drawRingProgress(canvas: Canvas) {
        val rectF = RectF(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat()
        )
        canvas.drawArc(rectF, -90f, 360 * (1.0f * progress / mRingMax), false, mRingProgressPaint)
    }

    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    fun dp2px(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    /**
     * 设置最大时间
     *
     * @param maxTime
     */
    fun setMaxTime(maxTime: Int) {
        mRingMax = maxTime
    }

    companion object {
        const val STATUS_DEF = 0 //初始状态
        const val STATUS_START = 1 //开始
        const val STATUS_PAUSE = 1 //暂停
        const val STATUS_RESUME = 2 //恢复
        const val STATUS_STOP = 3 //结束
    }

    init {
        val typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.RecordStartView)
        mRingColor = typedArray.getColor(
            R.styleable.RecordStartView_mRingColor, resources.getColor(
                R.color.record_progress_bg
            )
        )
        mRingProgressColor =
            typedArray.getColor(R.styleable.RecordStartView_mRingProgressColor, Color.WHITE)
        mRingWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordStartView_mRingWidth, 20)
        mRingMax = typedArray.getInt(R.styleable.RecordStartView_mRingMax, 120000)
        typedArray.recycle()
        mRingPaint = Paint()
        mRingPaint.color = mRingColor
        mRingPaint.style = Paint.Style.STROKE
        mRingPaint.isAntiAlias = true
        mRingPaint.strokeWidth = mRingWidth.toFloat()
        mRingProgressPaint = Paint()
        mRingProgressPaint.color = mRingProgressColor
        mRingProgressPaint.style = Paint.Style.STROKE
        mRingProgressPaint.isAntiAlias = true
        mRingProgressPaint.strokeWidth = mRingWidth.toFloat()
    }
}