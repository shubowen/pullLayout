package com.xiaosu.pulllayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.xiaosu.pulllayout.PullLayout.OnPullCallBackListener;

public class WaterDropView extends View implements IRefreshHead {

    static final String TAG = "Mr.su";

    private Circle topCircle;
    private Circle bottomCircle;

    private Paint mPaint;
    private Path mPath;

    private float mMaxCircleRadius;
    private float mMinCircleRadius;

    private final static float STROKE_WIDTH = 2;

    //当前的角度
    private float degrees;

    final float orgRatio = 0.7f;
    private ValueAnimator mAnimator;

    private OnPullCallBackListener mListener;

    private boolean isRunning;

    private RingRefresh mRing;
    private RectF mRingBound;

    final float insetRatio = 0.5f;
    private float mCanvasOffset;

    final int[] COLORS = new int[]{
            Color.WHITE
    };

    private int mWaterColor = Color.GRAY;

    /*定圆的最大直径*/
    private float mMaxDiameter;

    //水珠滴落的手势最大距离(dp)
    private float maxDeformationRate = 50;

    private IPull pullLayout;
    private LoadDrawable mLoadDrawable;

    public WaterDropView(Context context) {
        this(context, null);
    }

    public WaterDropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(context, attrs);
        init();
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterDropView, 0, 0);
            try {
                mWaterColor = a.getColor(R.styleable.WaterDropView_waterdrop_color, mWaterColor);
                mMaxCircleRadius = a.getDimensionPixelSize(R.styleable.WaterDropView_max_circle_radius, 0);
                mMinCircleRadius = a.getDimensionPixelSize(R.styleable.WaterDropView_min_circle_radius, 0);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                a.recycle();
            }
        }
    }

    private void init() {
        initPaint();
        initCircle();
        //转换成像素
        maxDeformationRate *= getResources().getDisplayMetrics().density;

        mMaxDiameter = 2 * mMaxCircleRadius;

        float orgRadius = orgRatio * mMaxCircleRadius;
        float centerX = topCircle.getX();
        float centerY = topCircle.getY();
        mCanvasOffset = mMaxCircleRadius - orgRadius;

        createRing(STROKE_WIDTH);

        createLoadDrawable(orgRadius, centerX, centerY);
    }

    private void createLoadDrawable(float orgRadius, float centerX, float centerY) {
        mLoadDrawable = new LoadDrawable.Builder()
                .setStrokeAngle((float) Math.PI * 0.05f)
                .setStrokeNum(12)
                .build();
        mLoadDrawable.setBounds(
                (int) (centerX - orgRadius),
                (int) (centerY - orgRadius),
                (int) (centerX + orgRadius),
                (int) (centerY + orgRadius)
        );
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mWaterColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    private void initCircle() {
        topCircle = new Circle();
        bottomCircle = new Circle();

        topCircle.setRadius(mMaxCircleRadius);
        bottomCircle.setRadius(mMaxCircleRadius);

        topCircle.setX(mMaxCircleRadius);
        topCircle.setY(mMaxCircleRadius);

        bottomCircle.setX(mMaxCircleRadius);
        bottomCircle.setY(mMaxCircleRadius);
    }

    void createRing(double strokeWidth) {
        mRing = new RingRefresh(mCallback);

        mRing.setColors(COLORS);
        mRing.setAlpha(255);
        mRing.setShowArrow(true);
        mRing.setStartTrim(0f);
        mRing.setEndTrim(0.8f);

        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final float screenDensity = metrics.density;

        float iStrokeWidth = (float) (strokeWidth * screenDensity);
        mRing.setStrokeWidth(iStrokeWidth);
        mRing.setColorIndex(0);
        mRing.setArrowDimensions(iStrokeWidth * 3f, iStrokeWidth * 2f);
        float ringRadius = insetRatio * topCircle.getRadius();
        mRingBound = new RectF(
                topCircle.getX() - ringRadius,
                topCircle.getY() - ringRadius,
                topCircle.getX() + ringRadius,
                topCircle.getY() + ringRadius);
        mRing.setCenterRadius(mRingBound.width() * .5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) mMaxDiameter;
        //math.ceil(x)返回大于参数x的最小整数
        int height = (int) Math.ceil(bottomCircle.getY() + bottomCircle.getRadius());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isRefreshing()) {
            canvas.rotate(degrees, topCircle.getX(), topCircle.getY());
            canvas.translate(mCanvasOffset, mCanvasOffset);
            mLoadDrawable.draw(canvas);
        } else {
            makeBezierPath();
            canvas.drawPath(mPath, mPaint);
            canvas.drawCircle(topCircle.getX(), topCircle.getY(), topCircle.getRadius(), mPaint);
            canvas.drawCircle(bottomCircle.getX(), bottomCircle.getY(), bottomCircle.getRadius(), mPaint);
            mRing.draw(canvas, mRingBound);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null && mAnimator.isRunning())
            mAnimator.cancel();
    }

    private void makeBezierPath() {
        if (null == mPath) {
            mPath = new Path();
        }
        mPath.reset();
        double angle = getAngle();
        float top_x1 = (float) (topCircle.getX() - topCircle.getRadius() * Math.cos(angle));
        float top_y1 = (float) (topCircle.getY() + topCircle.getRadius() * Math.sin(angle));

        float top_x2 = (float) (topCircle.getX() + topCircle.getRadius() * Math.cos(angle));
        float top_y2 = top_y1;

        float bottom_x1 = (float) (bottomCircle.getX() - bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y1 = (float) (bottomCircle.getY() + bottomCircle.getRadius() * Math.sin(angle));

        float bottom_x2 = (float) (bottomCircle.getX() + bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y2 = bottom_y1;

        mPath.moveTo(topCircle.getX(), topCircle.getY());

        mPath.lineTo(top_x1, top_y1);

        mPath.quadTo(
                (bottomCircle.getX() - bottomCircle.getRadius()),
                (bottomCircle.getY() + topCircle.getY()) / 2,
                bottom_x1,
                bottom_y1
        );
        mPath.lineTo(bottom_x2, bottom_y2);

        mPath.quadTo(
                (bottomCircle.getX() + bottomCircle.getRadius()),
                (bottomCircle.getY() + top_y2) / 2,
                top_x2,
                top_y2
        );

        mPath.close();
    }

    private double getAngle() {
        if (bottomCircle.getRadius() > topCircle.getRadius()) {
            throw new IllegalStateException("bottomCircle's radius must be less than the topCircle's");
        }
        return Math.asin((topCircle.getRadius() - bottomCircle.getRadius()) / (bottomCircle.getY() - topCircle.getY()));
    }

    public ValueAnimator createAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 359).setDuration(800);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                degrees = (float) valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //动画开始的时候,表示开始执行刷新的操作
                if (null != mListener)
                    mListener.onRefresh();
                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isRunning = false;
            }
        });
        return valueAnimator;
    }

    public void update(float percent) {
        if (percent < 0 || percent > 1) {
            throw new IllegalStateException("completion percent should between 0 and 1!");
        }

        if (percent == 1) {//开始动画
            if (null == mAnimator)
                mAnimator = createAnimator();
            if (!mAnimator.isStarted())
                mAnimator.start();
        } else {
            updateCircle(percent);
            updateRing();
        }
        postInvalidate();
    }

    private void updateCircle(float percent) {
        float topRadiusOffset = 0.3f * percent * mMaxCircleRadius;
        float topRadius = mMaxCircleRadius - topRadiusOffset;
        float bottomRadius = (mMinCircleRadius - mMaxCircleRadius) * percent + mMaxCircleRadius;

        //底圆的偏移量
        float bottomCircleOffset = 3 * percent * mMaxCircleRadius;

        topCircle.setRadius(topRadius);
        bottomCircle.setRadius(bottomRadius);
        bottomCircle.setY(topCircle.getY() + bottomCircleOffset);
    }

    private void updateRing() {
        float ringRadius = insetRatio * topCircle.getRadius();
        mRingBound.set(topCircle.getX() - ringRadius,
                topCircle.getY() - ringRadius,
                topCircle.getX() + ringRadius,
                topCircle.getY() + ringRadius);
        mRing.setCenterRadius(mRingBound.width() * .5f);
    }

    public boolean isRefreshing() {
        return null != mAnimator && mAnimator.isRunning() && isRunning;
    }

    @Override
    public void refreshImmediately() {
        update(1);
    }

    @Override
    public void autoRefresh() {
        pullLayout.animToRightPosition(mMaxDiameter, true, true);
    }

    public Circle getTopCircle() {
        return topCircle;
    }

    public Circle getBottomCircle() {
        return bottomCircle;
    }

    public void setIndicatorColor(int color) {
        mPaint.setColor(color);
    }

    public int getIndicatorColor() {
        return mPaint.getColor();
    }

    @Override
    public void setOnPullListener(OnPullCallBackListener mListener) {
        this.mListener = mListener;
    }

    public void reset() {
        if (null != mAnimator)
            mAnimator.cancel();
        degrees = 0;
        isRunning = false;
        setVisibility(VISIBLE);
    }

    @Override
    public void pullLayout(IPull iPull) {
        this.pullLayout = iPull;
    }

    @Override
    public void finishPull(boolean isBeingDragged) {
        if (isBeingDragged)
            stop();
        else
            pullLayout.animToStartPosition(true);
    }

    public void stop() {
        setVisibility(INVISIBLE);
        isRunning = false;
    }

    final Drawable.Callback mCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable d) {

        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {

        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {

        }
    };

    @Override
    public View getTargetView() {
        return this;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {
        if (scrollY > mMaxDiameter && enable) {
            float offset = scrollY - mMaxDiameter;
            float ratio = offset / maxDeformationRate;
            ratio = ratio > 1 ? 1 : ratio;
            update(ratio);
        }
    }

    @Override
    public void onFingerUp(float scrollY) {
        if (isRefreshing() && scrollY < mMaxDiameter && scrollY >= 0)
            pullLayout.animToStartPosition(false);
        else if (isRefreshing())
            pullLayout.animToRightPosition(mMaxDiameter, true, false);
        else
            pullLayout.animToStartPosition(false);
    }

    @Override
    public void detach() {

    }
}
