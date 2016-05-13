package com.xiaosu.pulllayout.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

/**
 * 作者：疏博文 创建于 2016-05-03 18:14
 * 邮箱：shubowen123@sina.cn
 * 描述：用等腰三角形构建箭头
 */
public class Arrow extends SizeDrawable {

    //斜边的长
    float mHypotenuseHeight;
    //斜边的宽
    float mHypotenuseWidth;
    //中垂线的高
    float mMidPerpendicularHeight;
    //中垂线的宽
    float mMidPerpendicularWidth;
    //斜边和中垂线的夹角
    float mAngle = (float) (Math.PI * .15f);
    //颜色
    int color = Color.BLACK;

    private final Path mPath;
    private final Paint mPaint;
    private float mHeadHeight;

    private Arrow() {
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate(0, (getBounds().height() - mHeadHeight - mMidPerpendicularHeight) * 0.5f);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mPath.reset();

        float centerX = bounds.centerX();

        float sin = (float) sin(mAngle);
        float cos = (float) cos(mAngle);

        if (-1 == mMidPerpendicularWidth) {
            mMidPerpendicularWidth = bounds.width() * 0.1f;
        }

        if (-1 == mHypotenuseWidth) {
            mHypotenuseWidth = mMidPerpendicularWidth * 0.8f;
        }

        if (-1 == mMidPerpendicularHeight) {
            mMidPerpendicularHeight = bounds.height() * 0.7f;
        }

        if (-1 == mHypotenuseHeight) {
            mHypotenuseHeight = mMidPerpendicularHeight * 0.6f;
        }

        /*中垂线和顶点之间的距离*/
        mHeadHeight = mHypotenuseWidth / sin + (mMidPerpendicularWidth * 0.5f) / (float) tan(mAngle);

        PointF point0 = new PointF(centerX, 0);
        PointF point1 = new PointF(centerX - mHypotenuseHeight * sin, mHypotenuseHeight * cos);
        PointF point2 = new PointF(point1.x + mHypotenuseWidth * cos, point1.y + mHypotenuseWidth * sin);
        PointF point3 = new PointF(centerX - mMidPerpendicularWidth * 0.5f, mHeadHeight);
        PointF point4 = new PointF(point3.x, point3.y + mMidPerpendicularHeight);

        PointF _point4 = symmetricPoint(point4, centerX);
        PointF _point3 = symmetricPoint(point3, centerX);
        PointF _point2 = symmetricPoint(point2, centerX);
        PointF _point1 = symmetricPoint(point1, centerX);

        mPath.moveTo(point0.x, point0.y);

        mPath.lineTo(point1.x, point1.y);
        mPath.lineTo(point2.x, point2.y);
        mPath.lineTo(point3.x, point3.y);
        mPath.lineTo(point4.x, point4.y);

        mPath.lineTo(_point4.x, _point4.y);
        mPath.lineTo(_point3.x, _point3.y);
        mPath.lineTo(_point2.x, _point2.y);
        mPath.lineTo(_point1.x, _point1.y);
        mPath.close();
    }

    PointF symmetricPoint(final PointF org, float center) {
        float dis = center - org.x;
        return new PointF(center + dis, org.y);
    }

    public float getHypotenuseHeight() {
        return mHypotenuseHeight;
    }

    public Arrow hypotenuseHeight(float hypotenuseHeight) {
        this.mHypotenuseHeight = hypotenuseHeight;
        return this;
    }

    public float getHypotenuseWidth() {
        return mHypotenuseWidth;
    }

    public Arrow hypotenuseWidth(float hypotenuseWidth) {
        this.mHypotenuseWidth = hypotenuseWidth;
        return this;
    }

    public float getMidPerpendicularHeight() {
        return mMidPerpendicularHeight;
    }

    public Arrow midPerpendicularHeight(float midPerpendicularHeight) {
        mMidPerpendicularHeight = midPerpendicularHeight;
        return this;
    }

    public float getMidPerpendicularWidth() {
        return mMidPerpendicularWidth;
    }

    public Arrow midPerpendicularWidth(float midPerpendicularWidth) {
        mMidPerpendicularWidth = midPerpendicularWidth;
        return this;
    }

    public float getAngle() {
        return mAngle;
    }

    public Arrow angle(float angle) {
        this.mAngle = angle;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Arrow color(int color) {
        this.color = color;
        mPaint.setColor(color);
        return this;
    }

    public static class builder {
        //斜边的长
        float mHypotenuseHeight = -1;
        //斜边的宽
        float mHypotenuseWidth = -1;
        //中垂线的高
        float mMidPerpendicularHeight = -1;
        //中垂线的宽
        float mMidPerpendicularWidth = -1;
        //斜边和中垂线的夹角
        float mAngle = -1;
        //颜色
        int mColor = Color.BLACK;

        private final Arrow mArrow;

        public builder() {
            mArrow = new Arrow();
        }

        public builder setHypotenuseHeight(float hypotenuseHeight) {
            mHypotenuseHeight = hypotenuseHeight;
            return this;
        }

        public builder setHypotenuseWidth(float hypotenuseWidth) {
            mHypotenuseWidth = hypotenuseWidth;
            return this;
        }

        public builder setMidPerpendicularHeight(float midPerpendicularHeight) {
            mMidPerpendicularHeight = midPerpendicularHeight;
            return this;
        }

        public builder setMidPerpendicularWidth(float midPerpendicularWidth) {
            mMidPerpendicularWidth = midPerpendicularWidth;
            return this;
        }

        public builder setAngle(float angle) {
            mAngle = angle;
            return this;
        }

        public builder setColor(int color) {
            this.mColor = color;
            return this;
        }

        public Arrow build() {
            if (mAngle == -1) {
                throw new RuntimeException("请设置Angle的值");
            }
            mArrow.hypotenuseHeight(mHypotenuseHeight)
                    .hypotenuseWidth(mHypotenuseWidth)
                    .angle(mAngle)
                    .color(mColor)
                    .midPerpendicularHeight(mMidPerpendicularHeight)
                    .midPerpendicularWidth(mMidPerpendicularWidth);
            return mArrow;
        }

    }

}
