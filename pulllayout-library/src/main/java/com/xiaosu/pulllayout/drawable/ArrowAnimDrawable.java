package com.xiaosu.pulllayout.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateInterpolator;

/**
 * 作者：疏博文 创建于 2016-04-28 16:26
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class ArrowAnimDrawable extends Drawable {

    /*true -> 箭头向上的状态*/
    private boolean isUp = true;

    private float mCurrentDegrees;

    private AnimatorUpdateListener DegreeUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentDegrees = (float) animation.getAnimatedValue();
            invalidateSelf();
        }
    };

    private final Arrow mArrow;
    private ValueAnimator mAnimator;

    public ArrowAnimDrawable() {
        mArrow = new Arrow.builder()
                .setAngle((float) Math.PI * 0.15f)
                .build();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.rotate(mCurrentDegrees, getBounds().centerX(), getBounds().centerY());
        mArrow.draw(canvas);
    }

    public void arrowDown() {
        if (!isUp && isAnimationRun()) {
            return;
        }
        createArrowAnimator(0, 180f).start();
    }

    private boolean isAnimationRun() {
        return null != mAnimator && mAnimator.isRunning();
    }

    public void arrowUp() {
        if (isUp && isAnimationRun()) {
            return;
        }
        createArrowAnimator(180f, 360f).start();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mArrow.setBounds(left, top, right, bottom);
    }

    private ValueAnimator createArrowAnimator(float start, float end) {
        mAnimator = ValueAnimator.ofFloat(start, end).setDuration(180);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addUpdateListener(DegreeUpdateListener);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isUp = !isUp;
            }
        });
        return mAnimator;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setIndicatorArrowColorColor(int themeColor) {
        mArrow.color(themeColor);
    }

}
