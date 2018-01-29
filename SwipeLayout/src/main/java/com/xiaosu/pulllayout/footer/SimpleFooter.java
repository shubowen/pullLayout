package com.xiaosu.pulllayout.footer;

import android.view.View;
import android.view.ViewGroup;

import com.xiaosu.pulllayout.base.ILoadFooter;

/**
 * 疏博文 新建于 2018/1/25.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class SimpleFooter implements ILoadFooter {

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }

    public void onLayout(int offsetY) {

        /*int childPaddingLeft = mParent.getPaddingLeft();
        int childPaddingRight = mParent.getPaddingRight();
        int childPaddingTop = mParent.getPaddingTop();
        int childPaddingBottom = mParent.getPaddingBottom();

        final int hLeft = (mParent.getMeasuredWidth() - childPaddingLeft - childPaddingRight - headerWidth) / 2;

        header.layout(hLeft,
                offsetY - headerHeight,
                hLeft + headerWidth,
                offsetY);*/
    }

    /**
     * @return 是否是激活状态，同时只能有一个激活状态
     */
    public boolean isActive() {
        return false;
    }

    @Override
    public View getView(ViewGroup parent) {
        return null;
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

    }

    @Override
    public void onUnderThreshold() {

    }

    @Override
    public void onResult(CharSequence message, boolean result) {

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

    }

    @Override
    public void onSwipe(boolean active, int dis) {

    }
}
