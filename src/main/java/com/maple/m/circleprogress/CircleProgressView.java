package com.maple.m.circleprogress;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by Maple on 2017/3/20.
 */

public class CircleProgressView extends View {

    private Paint paint;
    /**
     * 圆环的颜色
     */
    private int roundColor;
    /**
     * 圆环的宽度
     */
    private float roundWidth;
    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;
    /**
     * 默认宽高
     */
    private int defaultSize;
    /**
     * 记录上次的进度
     */
    private float lastProgress;
    /**
     * 当前进度
     */
    private float progress;
    /**
     * 圆心
     */
    float center;
    /**
     * 圆环半径
     */
    float radius;
    /**
     * 进度文字
     */
    private String progressText;
    private int progressTextColor;
    private int progressTextSize;
    private RectF oval;
    private DecimalFormat df;
    private AnimatorSet animation;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressView);

        roundColor = array.getColor(R.styleable.CircleProgressView_cpbackground, Color.GRAY);
        roundProgressColor = array.getColor(R.styleable.CircleProgressView_cpcirclecolor, 0xffff8800);
        roundWidth = array.getDimensionPixelSize(R.styleable.CircleProgressView_cpringsize, 30);
        progressTextColor = array.getColor(R.styleable.CircleProgressView_cptextcolor, roundColor);
        progressTextSize = array.getDimensionPixelSize(R.styleable.CircleProgressView_cptextsize, 50);

        array.recycle();

        defaultSize = 750;  // 默认宽高
        progressText = "0.0";

        paint = new Paint();
        oval = new RectF();
        df = new DecimalFormat("###.#");
        animation = new AnimatorSet();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        center = Math.min(getWidth() / 2, getHeight() / 2);
        radius = (center - roundWidth / 2); // 圆环的半径
        paint.setColor(roundColor); // 进度背景颜色
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
        paint.setAntiAlias(true); // 消除锯齿
        paint.setStrokeCap(Paint.Cap.ROUND);// 圆角
        canvas.drawCircle(center, center, radius, paint); // 画出圆环

        paint.setColor(roundProgressColor);
        oval.set(center - radius, center - radius, center + radius, center
                + radius);
        canvas.drawArc(oval, -90, progress, false, paint);

        paint.setStrokeWidth(0);
        paint.setColor(progressTextColor);
        paint.setTextSize(progressTextSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float textWidth = paint.measureText(progressText + "%");
        canvas.drawText(progressText + "%", center - textWidth / 2,
                center + progressTextSize / 2, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthModel == MeasureSpec.AT_MOST && heightModel == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, defaultSize);
        } else if (widthMeasureSpec == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, heightSize);
        } else if (heightMeasureSpec == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, defaultSize);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animation.cancel();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        lastProgress = progress;
        progressText = df.format(progress);
        this.progress = progress * 360 / 100;
        invalidate();
    }

    public void stop() {
        if (animation.isPaused()) {
            animation.resume();
        } else {
            if (animation.isRunning()){
                animation.pause();
            }
        }
    }

    /**
     * @param progress 0-100之间,否则无效
     */
    public void setProgressWithAnimation(float progress) {

        if (progress < 0 || progress > 100) {
            return;
        }

        if (animation.isRunning()){
            animation.cancel();
        }

        ObjectAnimator progressAnimation = ObjectAnimator.ofFloat(this, "progress", lastProgress, progress);
        progressAnimation.setDuration(2000);// 动画执行时间

        /*
         * AccelerateInterpolator　　　　　                  加速，开始时慢中间加速
         * DecelerateInterpolator　　　 　　                 减速，开始时快然后减速
         * AccelerateDecelerateInterolator　                     先加速后减速，开始结束时慢，中间加速
         * AnticipateInterpolator　　　　　　                 反向 ，先向相反方向改变一段再加速播放
         * AnticipateOvershootInterpolator　                 反向加超越，先向相反方向改变，再加速播放，会超出目的值然后缓慢移动至目的值
         * BounceInterpolator　　　　　　　                        跳跃，快到目的值时值会跳跃，如目的值100，后面的值可能依次为85，77，70，80，90，100
         * CycleIinterpolator　　　　　　　　                   循环，动画循环一定次数，值的改变为一正弦函数：Math.sin(2 *
         * mCycles * Math.PI * input) LinearInterpolator　　　 线性，线性均匀改变
         * OvershottInterpolator　　　　　　                  超越，最后超出目的值然后缓慢改变到目的值
         * TimeInterpolator　　　　　　　　　                        一个接口，允许你自定义interpolator，以上几个都是实现了这个接口
         */
//        progressAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        animation.playTogether(progressAnimation);//动画同时执行,可以做多个动画
        animation.start();
    }
}
