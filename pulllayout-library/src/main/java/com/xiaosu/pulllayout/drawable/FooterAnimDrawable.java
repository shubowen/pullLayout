package com.xiaosu.pulllayout.drawable;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import static android.animation.ValueAnimator.INFINITE;
import static android.animation.ValueAnimator.RESTART;

/**
 * 作者：疏博文 创建于 2016-04-28 16:26
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class FooterAnimDrawable extends Drawable {

    /*true -> 正在加载状态*/
    boolean loading;
    /*true -> 箭头向上的状态*/
    boolean isUp = true;

    float mCurrentDegrees;

    AnimatorUpdateListener DegreeUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentDegrees = (float) animation.getAnimatedValue();
            invalidateSelf();
        }
    };

    private ValueAnimator mRotateAnimator;

    private final Arrow mArrow;
    private final LoadDrawable mLoadDrawable;

    public FooterAnimDrawable() {
        mArrow = new Arrow.builder()
                .setAngle((float) Math.PI * 0.15f)
                .build();

        mLoadDrawable = new LoadDrawable.Builder()
                .setStrokeAngle((float) Math.PI * 0.05f)
                .setStrokeNum(12)
                .build();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.rotate(mCurrentDegrees, getBounds().centerX(), getBounds().centerY());
        if (loading) {
            mLoadDrawable.draw(canvas);
        } else {
            mArrow.draw(canvas);
        }
    }

    public void arrowDown() {
        if (loading || !isUp) {
            return;
        }
        createArrowAnimator(0, 180f).start();
        isUp = false;
    }

    public void arrowUp() {
        if (loading || isUp) {
            return;
        }
        createArrowAnimator(180f, 360f).start();
        isUp = true;
    }

    public void rotating() {
        if (loading) {
            return;
        }
        loading = true;
        mRotateAnimator = ValueAnimator.ofFloat(0, 359).setDuration(700);
        mRotateAnimator.setRepeatCount(INFINITE);
        mRotateAnimator.setRepeatMode(RESTART);
        mRotateAnimator.setInterpolator(new LinearInterpolator());
        mRotateAnimator.addUpdateListener(DegreeUpdateListener);
        mRotateAnimator.start();
    }

    public void showArrow() {
        if (mRotateAnimator != null && mRotateAnimator.isRunning()) {
            mRotateAnimator.cancel();
            mRotateAnimator = null;
            mCurrentDegrees = 0;
        }
        loading = false;
        arrowUp();
        invalidateSelf();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mArrow.setBounds(left, top, right, bottom);
        mLoadDrawable.setBounds(left, top, right, bottom);
    }

    private ValueAnimator createArrowAnimator(float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end).setDuration(180);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(DegreeUpdateListener);
        return animator;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void setIndicatorArrowColorColor(int themeColor) {
        mArrow.color(themeColor);
    }

    public void setLoadStartColor(int loadStartColor) {
        mLoadDrawable.setStartColor(loadStartColor);
    }

    public void setLoadEndColor(int loadEndColor) {
        mLoadDrawable.setEndColor(loadEndColor);
    }
}
