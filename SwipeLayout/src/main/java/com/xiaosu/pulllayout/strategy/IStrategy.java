package com.xiaosu.pulllayout.strategy;

import android.view.View;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述： 手势滑动策略抽象
 */

public interface IStrategy {

    boolean swipeBy(int dy);

    void swipeTo(int y);

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec);

    void onLayout(boolean changed,
                  int childPaddingLeft, int childPaddingTop, int childPaddingRight, int childPaddingBottom,
                  int parentMeasureWidth, int parentMeasureHeight,
                  View target);

    boolean shouldDrawHeader();

    boolean shouldDrawFooter();



    void finishSwipe(CharSequence message, boolean result);
}
