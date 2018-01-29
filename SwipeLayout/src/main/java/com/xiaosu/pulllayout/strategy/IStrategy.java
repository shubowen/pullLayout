package com.xiaosu.pulllayout.strategy;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述： 手势滑动策略抽象
 */

public interface IStrategy {

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec);

    void onLayout(boolean changed);

    boolean shouldDrawHeader();

    boolean shouldDrawFooter();

    void finishSwipe(CharSequence message, boolean result);
}
