package com.xiaosu.pulllayout.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 作者：疏博文 创建于 2016-09-14 11:48
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class SucceedDrawable extends SizeDrawable {

    private static final String TAG = "SucceedDrawable";

    private Rect bounds;
    private final Paint mPaint;

    final float mStrokeWidth = 6;

    final double angle = Math.PI / 6;

    //圆角尺寸
    final float CORNER_SIZE = 6;

    public SucceedDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GRAY);

        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);

        Rect rect = new Rect(bounds);
        //1.画360度圆弧
        int inset = (int) (mStrokeWidth / 2);
        //向内缩进
        rect.inset(inset, inset);
        canvas.drawArc(new RectF(rect), 0, 360, false, mPaint);

        int inset2 = (int) (mStrokeWidth);
        rect.inset(inset2, inset2);
        drawRightSymbol(canvas, rect);
    }

    private void drawRightSymbol(Canvas canvas, Rect rect) {

//        canvas.drawRect(new RectF(rect),mPaint);
//        canvas.drawArc(new RectF(rect), 0, 360, false, mPaint);

        int centerX = bounds.centerX();
        int radius = rect.width() / 2;

        int end1y = (int) (radius * Math.cos(angle) * 2);
        int end2y = (int) (radius * Math.cos(angle) * 2 * .6f);

        float degrees = (float) (angle / (2 * Math.PI) * 360);

        mPaint.setStyle(Paint.Style.FILL);

        float offset = mStrokeWidth * 0.5f;

        int count = canvas.save();
        canvas.rotate(degrees, bounds.centerX(), rect.bottom);

        canvas.drawRoundRect(new RectF(centerX - offset, rect.bottom - end1y, centerX + offset, rect.bottom),
                CORNER_SIZE, CORNER_SIZE, mPaint);
        canvas.restoreToCount(count);

        int count2 = canvas.save();
        canvas.rotate(-degrees, bounds.centerX(), rect.bottom);
        canvas.drawRoundRect(new RectF(centerX - offset, rect.bottom - end2y, centerX + offset, rect.bottom),
                CORNER_SIZE, CORNER_SIZE, mPaint);
        canvas.restoreToCount(count2);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.bounds = clipSquare(bounds);
    }

    public Rect clipSquare(Rect rect) {
        int w = rect.width();
        int h = rect.height();
        int min = Math.min(w, h);
        int cx = rect.centerX();
        int cy = rect.centerY();
        int r = min / 2;
        return new Rect(
                cx - r,
                cy - r,
                cx + r,
                cy + r
        );
    }
}
