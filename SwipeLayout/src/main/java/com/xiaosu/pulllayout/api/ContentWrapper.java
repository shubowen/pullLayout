package com.xiaosu.pulllayout.api;

import android.view.View;

/**
 * 疏博文 新建于 2018/1/25.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class ContentWrapper {

    private View mTarget;

    private ContentWrapper(View target) {
        this.mTarget = target;
    }

    public boolean canSwipeDown() {
        return mTarget.canScrollVertically(1);
    }

    public boolean canSwipeUp() {
        return mTarget.canScrollVertically(-1);
    }

    public static ContentWrapper wrapper(View target) {
        return new ContentWrapper(target);
    }

}
