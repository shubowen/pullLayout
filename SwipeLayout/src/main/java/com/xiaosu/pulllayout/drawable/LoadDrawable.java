package com.xiaosu.pulllayout.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 作者：疏博文 创建于 2016-05-08 16:04
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class LoadDrawable extends SizeDrawable {

    /*Stroke弧度*/
    private float mStrokeAngle;
    /*Stroke高度*/
    private float mStrokeHeight;
    /*Stroke个数*/
    private int mStrokeNum;
    /*Stroke过渡弧形的高*/
    private float mStrokeHatHeight;

    private final Paint mPaint;

    private int mRadius;

    private int mStartColor;

    private int mEndColor;

//    List<Path> mPaths = new ArrayList<>();

    Map<Float, Path> mPathMap = new HashMap<>();

    private LoadDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas, getBounds());
    }

    private void draw(Canvas canvas, Rect bounds) {
        canvas.translate(bounds.width() * 0.5f - mRadius, bounds.height() * 0.5f - mRadius);
        Set<Map.Entry<Float, Path>> entrySet = mPathMap.entrySet();

        for (Map.Entry<Float, Path> entry : entrySet) {
            mPaint.setColor(evaluate(entry.getKey(), mStartColor, mEndColor));
            canvas.drawPath(entry.getValue(), mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        //最小宽
        int minWidth = Math.min(newWidth, newHeight);

        //默认使用minDiameter * 0.5f * .35f作为mStrokeHeight的值
        if (-1 == mStrokeHeight) {
            mStrokeHeight = minWidth * 0.5f * .35f;
        }

        //默认使用mStrokeHeight * .25f作为mStrokeHatHeight的值
        if (-1 == mStrokeHatHeight) {
            mStrokeHatHeight = mStrokeHeight * .25f;
        }

        //半径
        mRadius = (int) (minWidth * 0.5f - mStrokeHatHeight);

        float sin = (float) Math.sin(mStrokeAngle * 0.5f);
        float cos = (float) Math.cos(mStrokeAngle * 0.5f);

        /*Stroke宽度一半*/
        float strokeHalfWidth = mRadius * sin;
        /*内圆的半径*/
        int innerRadius = (int) Math.sqrt(Math.pow(mRadius * cos - mStrokeHeight, 2) + Math.pow(strokeHalfWidth, 2));

        /*内外圆Stroke弧度差的一半*/
        float strokeAngleHalfOffset = (float) Math.atan(strokeHalfWidth / (mRadius * cos - mStrokeHeight)) - mStrokeAngle * 0.5f;

        /*Stroke间隙的角度*/
        float gapAngle = (2 * (float) Math.PI - mStrokeAngle * mStrokeNum) / mStrokeNum;

        //计算路径
        for (int i = 1; i < mStrokeNum + 1; i++) {
            /*当前角标Stroke的左边角度*/
            float currentIndexAngle = i * gapAngle + (i - 1) * mStrokeAngle + mStrokeAngle * 0.5f;
            PointF coordinate0 = calculateCoordinate(mRadius, currentIndexAngle);
            Path path = new Path();
            path.moveTo(coordinate0.x, coordinate0.y);

            PointF coordinate1 = calculateCoordinate(mRadius, currentIndexAngle + mStrokeAngle);

            PointF mMidPerpendicularCoordinate1 = midPerpendicularCoordinate(coordinate0, coordinate1, mStrokeHatHeight);
            path.quadTo(mMidPerpendicularCoordinate1.x, mMidPerpendicularCoordinate1.y, coordinate1.x, coordinate1.y);

            PointF coordinate2 = calculateCoordinate(innerRadius, currentIndexAngle + mStrokeAngle + strokeAngleHalfOffset);
            path.lineTo(coordinate2.x, coordinate2.y);

            PointF coordinate3 = calculateCoordinate(innerRadius, currentIndexAngle - strokeAngleHalfOffset);
            PointF midPerpendicularCoordinate2 = midPerpendicularCoordinate(coordinate2, coordinate3, mStrokeHatHeight);
            path.quadTo(midPerpendicularCoordinate2.x, midPerpendicularCoordinate2.y, coordinate3.x, coordinate3.y);

            path.close();
            mPathMap.put(currentIndexAngle / (2 * (float) Math.PI), path);
        }
    }

    PointF calculateCoordinate(int radius, float angle) {
        return new PointF(mRadius + radius * (float) Math.sin(angle), mRadius - radius * (float) Math.cos(angle));
    }

    /**
     * @param rectF1
     * @param rectF2
     * @param offset
     * @return rectF1和rectF2中垂线上面凸offset距离的坐标
     */
    PointF midPerpendicularCoordinate(PointF rectF1, PointF rectF2, float offset) {
        float width = Math.abs(rectF1.x - rectF2.x);
        float height = Math.abs(rectF1.y - rectF2.y);
        float angle = (float) Math.atan(width / height);
        PointF centerPointF = new PointF((rectF2.x + rectF1.x) * 0.5f, (rectF2.y + rectF1.y) * 0.5f);
        if (rectF2.x >= rectF1.x && rectF2.y > rectF1.y) {
            //rectF2在rectF1右下
            return new PointF(centerPointF.x + offset * (float) Math.cos(angle), centerPointF.y - offset * (float) Math.sin(angle));
        } else if (rectF2.x < rectF1.x && rectF2.y >= rectF1.y) {
            //rectF2在rectF1左下
            return new PointF(centerPointF.x + offset * (float) Math.cos(angle), centerPointF.y + offset * (float) Math.sin(angle));
        } else if (rectF2.x < rectF1.x && rectF2.y < rectF1.y) {
            //rectF2在rectF1左上
            return new PointF(centerPointF.x - offset * (float) Math.cos(angle), centerPointF.y + offset * (float) Math.sin(angle));
        } else {
            //rectF2在rectF1右上
            return new PointF(centerPointF.x - offset * (float) Math.cos(angle), centerPointF.y - offset * (float) Math.sin(angle));
        }
    }

    int evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    public LoadDrawable setStrokeAngle(float strokeAngle) {
        mStrokeAngle = strokeAngle;
        return this;
    }

    public LoadDrawable setStrokeHeight(float strokeHeight) {
        mStrokeHeight = strokeHeight;
        return this;
    }

    public LoadDrawable setStrokeNum(int strokeNum) {
        mStrokeNum = strokeNum;
        return this;
    }

    public LoadDrawable setStrokeHatHeight(int strokeHatHeight) {
        mStrokeHatHeight = strokeHatHeight;
        return this;
    }

    public LoadDrawable setStartColor(int startColor) {
        mStartColor = startColor;
        return this;
    }

    public LoadDrawable setEndColor(int endColor) {
        mEndColor = endColor;
        return this;
    }

    public static class Builder {
        /*Stroke弧度*/
        private float mStrokeAngle = -1;
        /*Stroke高度*/
        private float mStrokeHeight = -1;
        /*Stroke个数*/
        private int mStrokeNum = -1;
        /*Stroke过渡弧形的高*/
        private int mStrokeHatHeight = -1;
        /*开始的颜色*/
        private int mStartColor = 0xFF555555;
        /*结束的颜色*/
        private int mEndColor = 0xFFDDDDDD;

        private final LoadDrawable mLoadDrawable;

        public Builder() {
            mLoadDrawable = new LoadDrawable();
        }

        public Builder setStrokeAngle(float strokeAngle) {
            mStrokeAngle = strokeAngle;
            return this;
        }

        public Builder setStrokeHeight(float strokeHeight) {
            mStrokeHeight = strokeHeight;
            return this;
        }

        public Builder setStrokeNum(int strokeNum) {
            mStrokeNum = strokeNum;
            return this;
        }

        public Builder setStrokeHatHeight(int strokeHatHeight) {
            mStrokeHatHeight = strokeHatHeight;
            return this;
        }

        public Builder setStartColor(int startColor) {
            mStartColor = startColor;
            return this;
        }

        public Builder setEndColor(int endColor) {
            mEndColor = endColor;
            return this;
        }

        public LoadDrawable build() {

            if (mStrokeAngle == -1) {
                throw new RuntimeException("请设置StrokeAngle的值");
            }

            if (mStrokeNum == -1) {
                throw new RuntimeException("请设置StrokeNum的值");
            }

            mLoadDrawable.setStrokeAngle(mStrokeAngle)
                    .setStrokeNum(mStrokeNum)
                    .setStrokeHatHeight(mStrokeHatHeight)
                    .setStrokeHeight(mStrokeHeight)
                    .setStartColor(mStartColor)
                    .setEndColor(mEndColor);

            return mLoadDrawable;
        }

    }

}
