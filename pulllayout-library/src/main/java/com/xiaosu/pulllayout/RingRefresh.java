package com.xiaosu.pulllayout;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * 作者：疏博文 创建于 2016-04-22 17:00
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class RingRefresh {

    static final float ARROW_OFFSET_ANGLE = 5;
    private static final String TAG = "Mr.su";

    private final RectF mTempBounds = new RectF();
    private final Paint mPaint = new Paint();
    private final Paint mArrowPaint = new Paint();

    private final Drawable.Callback mCallback;

    private float mStartTrim = 0.0f;
    private float mEndTrim = 0.0f;
    private float mRotation = 0.0f;

    /*弧的宽*/
    private float mStrokeWidth = 5.0f;
    /*弧的外半径到parent的边距*/
    private float mStrokeInset = 2.5f;

    private int[] mColors;
    // mColorIndex represents the offset into the available mColors that the
    // progress circle should currently display. As the progress circle is
    // animating, the mColorIndex moves by one to the next available color.
    private int mColorIndex;
    private float mStartingStartTrim;
    private float mStartingEndTrim;
    private float mStartingRotation;
    private boolean mShowArrow;
    private Path mArrow;
    private float mArrowScale = 1.0f;
    private double mRingCenterRadius;
    private int mArrowWidth;
    private int mArrowHeight;
    private int mAlpha;

    private final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private final Paint mCirclePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mBackgroundColor;
    private int mCurrentColor;

    public RingRefresh(Drawable.Callback callback) {
        mCallback = callback;

        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mArrowPaint.setStyle(Paint.Style.FILL);
        mArrowPaint.setAntiAlias(true);
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    /**
     * Set the dimensions of the arrowhead.
     *
     * @param width  Width of the hypotenuse of the arrow head
     * @param height Height of the arrow point
     */
    public void setArrowDimensions(float width, float height) {
        mArrowWidth = (int) width;
        mArrowHeight = (int) height;
    }

    /**
     * Draw the progress spinner
     */
    public void draw(Canvas c, RectF bounds) {
        final RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
//        arcBounds.inset(mStrokeInset, mStrokeInset);

        final float startAngle = (mStartTrim + mRotation) * 360;
        final float endAngle = (mEndTrim + mRotation) * 360;
        float sweepAngle = endAngle - startAngle;

        mPaint.setColor(mCurrentColor);

        //先画背景
        if (mAlpha < 255) {
            mCirclePaint.setColor(mBackgroundColor);
            mCirclePaint.setAlpha(255 - mAlpha);
            c.drawRect(bounds, mCirclePaint);
        }

        /*这个方法画的弧不是在RectF的内部,比RectF大*/
        c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);
        drawTriangle(c, startAngle, sweepAngle, bounds);
    }

    private void drawTriangle(Canvas c, float startAngle, float sweepAngle, RectF bounds) {
        if (mShowArrow) {
            if (mArrow == null) {
                mArrow = new android.graphics.Path();
                mArrow.setFillType(android.graphics.Path.FillType.EVEN_ODD);
            } else {
                mArrow.reset();
            }

            // Adjust the position of the triangle so that it is inset as
            // much as the arc, but also centered on the arc.
//            float inset = (int) mStrokeInset / 2 * mArrowScale;
            float inset = mArrowWidth / 2 * mArrowScale;
            float x = (float) (mRingCenterRadius + bounds.centerX());
            float y = bounds.centerY();

            // Update the path each time. This works around an issue in SKIA
            // where concatenating a rotation matrix to a scale matrix
            // ignored a starting negative rotation. This appears to have
            // been fixed as of API 21.
            //三角形左上角
            mArrow.moveTo(0, 0);
            //左上角和右上角连线
            mArrow.lineTo(mArrowWidth * mArrowScale, 0);
            //右上角和顶点连线
            mArrow.lineTo((mArrowWidth * mArrowScale / 2), (mArrowHeight * mArrowScale));
            mArrow.offset(x - inset, y);
            mArrow.close();
            // draw a triangle
            mArrowPaint.setColor(mCurrentColor);

            /*仅仅是转动画布,以前画在画布上面的内容是不随着画布的转动而转动的*/
            c.rotate(startAngle + sweepAngle - ARROW_OFFSET_ANGLE, bounds.centerX(), bounds.centerY());
            c.drawPath(mArrow, mArrowPaint);
        }
    }

    /**
     * Set the colors the progress spinner alternates between.
     *
     * @param colors Array of integers describing the colors. Must be non-<code>null</code>.
     */
    public void setColors(@NonNull int[] colors) {
        mColors = colors;
        // if colors are reset, make sure to reset the color index as well
        setColorIndex(0);
    }

    /**
     * Set the absolute color of the progress spinner. This is should only
     * be used when animating between current and next color when the
     * spinner is rotating.
     *
     * @param color int describing the color.
     */
    public void setColor(int color) {
        mCurrentColor = color;
    }

    /**
     * @param index Index into the color array of the color to display in
     *              the progress spinner.
     */
    public void setColorIndex(int index) {
        mColorIndex = index;
        mCurrentColor = mColors[mColorIndex];
    }

    /**
     * @return int describing the next color the progress spinner should use when drawing.
     */
    public int getNextColor() {
        return mColors[getNextColorIndex()];
    }

    private int getNextColorIndex() {
        return (mColorIndex + 1) % (mColors.length);
    }

    /**
     * Proceed to the next available ring color. This will automatically
     * wrap back to the beginning of colors.
     */
    public void goToNextColor() {
        setColorIndex(getNextColorIndex());
    }

    public void setColorFilter(ColorFilter filter) {
        mPaint.setColorFilter(filter);
        invalidateSelf();
    }

    /**
     * @param alpha Set the alpha of the progress spinner and associated arrowhead.
     */
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    /**
     * @return Current alpha of the progress spinner and arrowhead.
     */
    public int getAlpha() {
        return mAlpha;
    }

    /**
     * @param strokeWidth Set the stroke width of the progress spinner in pixels.
     */
    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        mPaint.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    @SuppressWarnings("unused")
    public void setStartTrim(float startTrim) {
        mStartTrim = startTrim;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getStartTrim() {
        return mStartTrim;
    }

    public float getStartingStartTrim() {
        return mStartingStartTrim;
    }

    public float getStartingEndTrim() {
        return mStartingEndTrim;
    }

    public int getStartingColor() {
        return mColors[mColorIndex];
    }

    @SuppressWarnings("unused")
    public void setEndTrim(float endTrim) {
        mEndTrim = endTrim;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getEndTrim() {
        return mEndTrim;
    }

    @SuppressWarnings("unused")
    public void setRotation(float rotation) {
        mRotation = rotation;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getRotation() {
        return mRotation;
    }

    public void setInsets(int width, int height) {
        final float minEdge = (float) Math.min(width, height);
        float insets;
        if (mRingCenterRadius <= 0 || minEdge < 0) {
            insets = (float) Math.ceil(mStrokeWidth / 2.0f);
        } else {
            insets = (float) (minEdge / 2.0f - mRingCenterRadius);
        }
        Log.i(TAG, "setInsets: " + insets);
        mStrokeInset = insets;
    }

    @SuppressWarnings("unused")
    public float getInsets() {
        return mStrokeInset;
    }

    /**
     * @param centerRadius Inner radius in px of the circle the progress
     *                     spinner arc traces.
     */
    public void setCenterRadius(double centerRadius) {
        mRingCenterRadius = centerRadius;
    }

    public double getCenterRadius() {
        return mRingCenterRadius;
    }

    /**
     * @param show Set to true to show the arrow head on the progress spinner.
     */
    public void setShowArrow(boolean show) {
        if (mShowArrow != show) {
            mShowArrow = show;
            invalidateSelf();
        }
    }

    /**
     * @param scale Set the scale of the arrowhead for the spinner.
     */
    public void setArrowScale(float scale) {
        if (scale != mArrowScale) {
            mArrowScale = scale;
            invalidateSelf();
        }
    }

    /**
     * @return The amount the progress spinner is currently rotated, between [0..1].
     */
    public float getStartingRotation() {
        return mStartingRotation;
    }

    /**
     * If the start / end trim are offset to begin with, store them so that
     * animation starts from that offset.
     */
    public void storeOriginals() {
        mStartingStartTrim = mStartTrim;
        mStartingEndTrim = mEndTrim;
        mStartingRotation = mRotation;
    }

    /**
     * Reset the progress spinner to default rotation, start and end angles.
     */
    public void resetOriginals() {
        mStartingStartTrim = 0;
        mStartingEndTrim = 0;
        mStartingRotation = 0;
        setStartTrim(0);
        setEndTrim(0);
        setRotation(0);
    }

    private void invalidateSelf() {
        mCallback.invalidateDrawable(null);
    }

}
