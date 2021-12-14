package com.worldtech.camera2video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.worldtech.camera2video.R;

public class RecordStartView extends View {

    public final static int STATUS_DEF = 0;//初始状态
    public final static int STATUS_START = 1;//开始
    public final static int STATUS_PAUSE = 1;//暂停
    public final static int STATUS_RESUME = 2;//恢复
    public final static int STATUS_STOP = 3;//结束

    public final int CENTER_WHAT = 100;
    public final int RING_WHAT = 101;
    private Runnable mLongPressRunnable;
    private Paint mRingProgressPaint;
    private Paint mCenterPaint;
    private Paint mRingPaint;
    //圆环颜色
    public int mRingColor;
    // 圆环进度的颜色
    public int mRingProgressColor;
    //圆环的宽度
    public int mRingWidth;

    //控件宽高
    private int mWidth;
    private int mHeight;
    //中间X坐标
    private int centerX;
    //中间Y坐标
    private int centerY;
    //进度
    private int progress;
    //中间方法比例
    private float centerScale = 0.8f;
    //半径
    private int radius;
    //最大时间
    private int mRingMax = 120000;
    //时间间隔
    private long timeSpan = 1000;
    //开始时间
    private Context mContext;

    public RecordStartView(Context context) {
        this(context, null);
        mContext = context;
    }

    public RecordStartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public RecordStartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordStartView);
        mRingColor = typedArray.getColor(R.styleable.RecordStartView_mRingColor, getResources().getColor(R.color.record_progress_bg));
        mRingProgressColor = typedArray.getColor(R.styleable.RecordStartView_mRingProgressColor, Color.WHITE);
        mRingWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordStartView_mRingWidth, 20);
        mRingMax = typedArray.getInt(R.styleable.RecordStartView_mRingMax, 120000);
        typedArray.recycle();

        mRingPaint = new Paint();
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStrokeWidth(mRingWidth);

        mRingProgressPaint = new Paint();
        mRingProgressPaint.setColor(mRingProgressColor);
        mRingProgressPaint.setStyle(Paint.Style.STROKE);
        mRingProgressPaint.setAntiAlias(true);
        mRingProgressPaint.setStrokeWidth(mRingWidth);
    }

//    public void pauseRecord() {
//        record_status = STATUS_PAUSE;
//        pauseTime = System.currentTimeMillis();
//        lastRecordTime = pauseTime - startTime;
//    }


    public void resumeRecord() {
//        record_status = STATUS_RESUME;
//        startTime= System.currentTimeMillis();
    }

    public void stopRecord() {
        progress = 0;
        postInvalidate();
    }

    public void deleteLast() {
        stopRecord();
    }

    public void setProgress(int milliSecond) {
        if (progress < mRingMax) {
            progress = milliSecond;
            postInvalidate();
        } else {
            stopRecord();
        }
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (widthMode == MeasureSpec.AT_MOST) width = dp2px(100);
        else width = widthSize;
        if (heightMode == MeasureSpec.AT_MOST) height = dp2px(100);
        else height = heightSize;
        setMeasuredDimension(Math.min(width, height), Math.min(width, height));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
        //获取中心点的位置
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        radius = (centerX - mRingWidth / 2) - 10;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        drawCenter(canvas);
        drawRing(canvas);
        drawRingProgress(canvas);

    }

    /**
     * 绘制圆环
     *
     * @param canvas
     */
    private void drawRing(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, mRingPaint);
    }

    /**
     * 绘制中间的圆
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, centerScale * radius, mCenterPaint);
    }


    /**
     * 绘制圆环进度
     *
     * @param canvas
     */
    private void drawRingProgress(Canvas canvas) {
        RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(rectF, -90, 360 * (1.0f * progress / mRingMax), false, mRingProgressPaint);
    }


    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    public int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }


    /**
     * 设置最大时间
     *
     * @param maxTime
     */
    public void setMaxTime(int maxTime) {
        this.mRingMax = maxTime;
    }


}
