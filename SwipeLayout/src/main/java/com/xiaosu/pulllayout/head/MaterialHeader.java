package com.xiaosu.pulllayout.head;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.xiaosu.pulllayout.R;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.drawable.MaterialProgressDrawable;
import com.xiaosu.pulllayout.view.CircleImageView;

/**
 * Material 主题下拉头
 * Created by SCWANG on 2017/6/2.
 */
@SuppressWarnings("unused")
public class MaterialHeader extends ViewGroup implements IRefreshHead {

    // Maps to ProgressBar.Large style
    public static final int SIZE_LARGE = 0;
    // Maps to ProgressBar default style
    public static final int SIZE_DEFAULT = 1;

    private int colorPrimary = Color.RED;

    private static final float MAX_PROGRESS_ANGLE = .8f;
    @VisibleForTesting
    private static final int CIRCLE_DIAMETER = 40;
    @VisibleForTesting
    private static final int CIRCLE_DIAMETER_LARGE = 56;

    private boolean mFinished;
    private int mCircleDiameter;
    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;

    /**
     * 贝塞尔背景
     */
    private int mWaveHeight;
    private int mHeadHeight;
    private Path mBezierPath;
    private Paint mBezierPaint;
    private boolean mShowBezierWave = false;
    private int mThreshold;

    //<editor-fold desc="MaterialHeader">
    public MaterialHeader(Context context) {
        super(context);
        initView(context, null);
    }

    public MaterialHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        mThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int[] layout_attrs = new int[]{
                    android.R.attr.colorPrimary
            };
            final TypedArray a = context.obtainStyledAttributes(attrs, layout_attrs);
            colorPrimary = a.getColor(0, colorPrimary);
            a.recycle();
        }

        mProgress = new MaterialProgressDrawable(context, this);
        mProgress.setColorSchemeColors(Color.WHITE);
        mProgress.showArrow(true);
        mProgress.setStartEndTrim(0f, MAX_PROGRESS_ANGLE);
        mProgress.setArrowScale(1);
        mProgress.setAlpha(255);

        mCircleView = new CircleImageView(context, colorPrimary);
        mCircleView.setImageDrawable(mProgress);
        addView(mCircleView);

        mCircleDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_DIAMETER, getResources().getDisplayMetrics());

        mBezierPath = new Path();
        mBezierPaint = new Paint();
        mBezierPaint.setAntiAlias(true);
        mBezierPaint.setStyle(Paint.Style.FILL);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MaterialHeader);
        mShowBezierWave = ta.getBoolean(R.styleable.MaterialHeader_mhShowBezierWave, mShowBezierWave);
        mBezierPaint.setColor(ta.getColor(R.styleable.MaterialHeader_mhPrimaryColor, 0xff11bbff));
        if (ta.hasValue(R.styleable.MaterialHeader_mhShadowRadius)) {
            int radius = ta.getDimensionPixelOffset(R.styleable.MaterialHeader_mhShadowRadius, 0);
            int color = ta.getColor(R.styleable.MaterialHeader_mhShadowColor, 0xff000000);
            mBezierPaint.setShadowLayer(radius, 0, 0, color);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        ta.recycle();

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY));
        setMeasuredDimension(mCircleDiameter, mCircleDiameter);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();

        mCircleView.layout(0, 0, circleWidth, circleHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mShowBezierWave) {
            //重置画笔
            mBezierPath.reset();
            mBezierPath.lineTo(0, mHeadHeight);
            //绘制贝塞尔曲线
            mBezierPath.quadTo(getMeasuredWidth() / 2, mHeadHeight + mWaveHeight * 1.9f, getMeasuredWidth(), mHeadHeight);
            mBezierPath.lineTo(getMeasuredWidth(), 0);
            canvas.drawPath(mBezierPath, mBezierPaint);
        }
        super.dispatchDraw(canvas);
    }

    //</editor-fold>

    //<editor-fold desc="API">


    public MaterialHeader setSize(int size) {
        if (size != SIZE_LARGE && size != SIZE_DEFAULT) {
            return this;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == SIZE_LARGE) {
            mCircleDiameter = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        mCircleView.setImageDrawable(null);
        mProgress.updateSizes(size);
        mCircleView.setImageDrawable(mProgress);
        return this;
    }

    public MaterialHeader setShowBezierWave(boolean show) {
        this.mShowBezierWave = show;
        return this;
    }

    @Override
    public View getView(ViewGroup parent) {
        return this;
    }

    @Override
    public void detach() {

    }

    @Override
    public int threshold() {
        return 0;
    }

    @Override
    public void onActive() {
        mProgress.start();
    }

    @Override
    public void onUnderThreshold() {

    }

    @Override
    public void onResult(CharSequence message, boolean result) {
        mProgress.stop();
    }

    @Override
    public int delay() {
        return 0;
    }

    @Override
    public void onBeyondThreshold() {

    }

    @Override
    public void onHidden() {
        mProgress.stop();
    }

    @Override
    public void onSwipe(boolean active, int dis) {

        if (active) return;

        if (mShowBezierWave) {
            mHeadHeight = Math.min(dis, mThreshold);
            mWaveHeight = Math.max(0, dis - mThreshold);
            postInvalidate();
        }

        float originalDragPercent = 1f * dis / mThreshold;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float extraOS = Math.abs(dis) - mThreshold;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, (float) mThreshold * 2)
                / (float) mThreshold);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        float strokeStart = adjustedPercent * .8f;
        mProgress.showArrow(true);
        mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
        mProgress.setArrowScale(Math.min(1f, adjustedPercent));

        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
        mProgress.setProgressRotation(rotation);
    }

    //</editor-fold>


    //<editor-fold desc="RefreshHeader">
    /*@Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int extendHeight) {
        if (!mShowBezierWave) {
            kernel.requestDefaultHeaderTranslationContent(false);
        }
        if (isInEditMode()) {
            mWaveHeight = mHeadHeight = height / 2;
        }
    }*/

    /*@Override
    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
        if (mShowBezierWave) {
            mHeadHeight = Math.min(offset, headerHeight);
            mWaveHeight = Math.max(0, offset - headerHeight);
            postInvalidate();
        }

        if (mState != RefreshState.Refreshing) {
            float originalDragPercent = 1f * offset / headerHeight;

            float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
            float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
            float extraOS = Math.abs(offset) - headerHeight;
            float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, (float) headerHeight * 2)
                    / (float) headerHeight);
            float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                    (tensionSlingshotPercent / 4), 2)) * 2f;
            float strokeStart = adjustedPercent * .8f;
            mProgress.showArrow(true);
            mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
            mProgress.setArrowScale(Math.min(1f, adjustedPercent));

            float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
            mProgress.setProgressRotation(rotation);
            mCircleView.setAlpha(Math.min(1f, originalDragPercent*2));
        }

        float targetY = offset / 2 + mCircleDiameter / 2;
        mCircleView.setTranslationY(Math.min(offset, targetY));//setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop, true *//* requires update *//*);
    }*/

    /*@Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {
        if (!mProgress.isRunning() && !mFinished) {
            onPullingDown(percent, offset, headerHeight, extendHeight);
        } else {
            if (mShowBezierWave) {
                mHeadHeight = Math.min(offset, headerHeight);
                mWaveHeight = Math.max(0, offset - headerHeight);
                postInvalidate();
            }
        }
    }*/

    /*@Override
    public void onRefreshReleased(RefreshLayout layout, int headerHeight, int extendHeight) {
        mProgress.start();
        if ((int) mCircleView.getTranslationY() != headerHeight / 2 + mCircleDiameter / 2) {
            mCircleView.animate().translationY(headerHeight / 2 + mCircleDiameter / 2);
        }
    }*/

    /*@Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        mState = newState;
        switch (newState) {
            case None:
                break;
            case PullDownToRefresh:
                mFinished = false;
                mCircleView.setVisibility(VISIBLE);
                mCircleView.setScaleX(1);
                mCircleView.setScaleY(1);
                break;
            case ReleaseToRefresh:
                break;
            case Refreshing:
                break;
        }
    }*/

    /*@Override
    public int onFinish(@NonNull RefreshLayout layout, boolean success) {
        mProgress.stop();
        mCircleView.animate().scaleX(0).scaleY(0);
        mFinished = true;
        return 0;
    }*/
    //</editor-fold>
}
