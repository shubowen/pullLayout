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
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.xiaosu.pulllayout.base.AnimationCallback;
import com.xiaosu.pulllayout.base.IPull;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.drawable.LoadDrawable;
import com.xiaosu.pulllayout.drawable.RingRefresh;

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

    /*LoadDrawable内嵌的距离与高度的比例*/
    final float mLoadingInsetRatio = 0.1f;

    private ValueAnimator mAnimator;

    private boolean isRunning;

    private RingRefresh mRing;

    private RectF mRingBound;

    final float insetRatio = 0.5f;

    private int mWaterColor = Color.GRAY;

    /*定圆的最大直径*/
    private float mMaxDiameter;

    //水珠滴落的手势最大距离(dp)
    private float mMaxDeformationRate = 50;

    private IPull pullLayout;

    private LoadDrawable mLoadDrawable;

    private boolean mVisible = true;

    private boolean mHasCenterX;

    private int mLoadingOffset;

    //true表示需要重置
    private boolean mNeedReset;

    AnimationCallback mAnimToStartPositionCallback = new AnimationCallback() {
        @Override
        public void onAnimationEnd() {
            resetCircleRadius();
            if (mNeedReset) {
                reset();
                mNeedReset = false;
            }
        }
    };

    AnimationCallback mAnimToRightPositionCallback = new AnimationCallback() {

        @Override
        public void onAnimation(float fraction) {
            if (fraction == 1) {
                refreshImmediately();
            }
        }
    };

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
        mMaxDeformationRate *= getResources().getDisplayMetrics().density;

        mMaxDiameter = 2 * mMaxCircleRadius;

        createRing(STROKE_WIDTH);

        createLoadDrawable();
    }

    private void createLoadDrawable() {
        mLoadDrawable = new LoadDrawable.Builder()
                .setStrokeAngle((float) Math.PI * 0.05f)
                .setStrokeNum(12)
                .build();
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

        resetCircleRadius();
    }

    void createRing(double strokeWidth) {
        mRing = new RingRefresh();

        mRing.setAlpha(255);
        mRing.setShowArrow(true);
        mRing.setStartTrim(0f);
        mRing.setEndTrim(0.8f);

        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        final float screenDensity = metrics.density;

        float iStrokeWidth = (float) (strokeWidth * screenDensity);
        mRing.setStrokeWidth(iStrokeWidth);
        mRing.setArrowDimensions(iStrokeWidth * 3f, iStrokeWidth * 2f);

        mRingBound = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //math.ceil(x)返回大于参数x的最小整数
        int height = (int) Math.ceil(bottomCircle.getY() + bottomCircle.getRadius());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasCenterX) {
            //设置水滴在中间位置
            float centerX = getWidth() * 0.5f;
            topCircle.setX(centerX);
            bottomCircle.setX(centerX);
            Rect rect = new Rect(left, top, right, bottom);
            mLoadingOffset = (int) (getHeight() * mLoadingInsetRatio);
            rect.inset(mLoadingOffset, mLoadingOffset);
            mLoadDrawable.setBounds(rect);

            updateRing();
            mHasCenterX = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mVisible) return;

        if (isRefreshing()) {
            canvas.rotate(degrees, topCircle.getX(), topCircle.getY());
            canvas.translate(mLoadingOffset, mLoadingOffset);
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
                if (!isRunning) return;

                degrees = (float) valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //动画开始的时候,表示开始执行刷新的操作
                pullLayout.pullDownCallback();
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
            if (!mAnimator.isStarted()) {
                isRunning = true;
                mAnimator.start();
            }
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
        float bottomCircleOffset = 3 * mMaxCircleRadius * percent;

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
        pullLayout.animToRightPosition(mMaxDiameter, mAnimToRightPositionCallback);
    }

    /**
     * 重置两个圆的半径
     */
    private void resetCircleRadius() {
        topCircle.setRadius(mMaxCircleRadius);
        topCircle.setY(mMaxCircleRadius);

        bottomCircle.setRadius(mMaxCircleRadius);
        bottomCircle.setY(mMaxCircleRadius);
    }

    public Circle getTopCircle() {
        return topCircle;
    }

    public Circle getBottomCircle() {
        return bottomCircle;
    }

    public void setWaterDropColor(int color) {
        mPaint.setColor(color);
    }

    public int getColor() {
        return mPaint.getColor();
    }

    public void reset() {
        if (null != mAnimator)
            mAnimator.cancel();
        degrees = 0;
        isRunning = false;
        mVisible = true;
        invalidate();
    }

    @Override
    public void pullLayout(IPull iPull) {
        this.pullLayout = iPull;
    }

    @Override
    public void finishPull(boolean isBeingDragged) {
        if (isBeingDragged)
            stop();
        else {
            pullLayout.animToStartPosition(mAnimToStartPositionCallback);
            mNeedReset = true;
        }
    }

    /**
     * 暂停
     */
    public void stop() {
        mVisible = false;
        isRunning = false;
    }

    public boolean isStop() {
        return !mVisible && !isRunning;
    }

    @Override
    public View getTargetView() {
        return this;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {
        if (scrollY > mMaxDiameter && enable) {
            float offset = scrollY - mMaxDiameter;
            float ratio = offset / mMaxDeformationRate;
            ratio = ratio > 1 ? 1 : ratio;
            update(ratio);
        }
    }

    @Override
    public void onFingerUp(float scrollY) {
        if (isRefreshing() && scrollY < mMaxDiameter && scrollY >= 0) {
            pullLayout.animToStartPosition(mAnimToStartPositionCallback);
        } else if (isRefreshing()) {
            pullLayout.animToRightPosition(mMaxDiameter, mAnimToRightPositionCallback);
        } else {
            pullLayout.animToStartPosition(mAnimToStartPositionCallback);
            mNeedReset = isStop();
        }
    }

    @Override
    public void detach() {

    }

    public void setRefreshArrowColorColor(int refreshArrowColorColor) {
        mRing.setColor(refreshArrowColorColor);
    }

    public void setLoadStartColor(int loadStartColor) {
        mLoadDrawable.setStartColor(loadStartColor);
    }

    public void setLoadEndColor(int loadEndColor) {
        mLoadDrawable.setEndColor(loadEndColor);
    }
}
