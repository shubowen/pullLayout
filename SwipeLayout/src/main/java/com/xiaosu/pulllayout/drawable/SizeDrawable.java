package com.xiaosu.pulllayout.drawable;

import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

/**
 * 作者：疏博文 创建于 2016-05-10 10:54
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public abstract class SizeDrawable extends Drawable {

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

    @Override
    final public void setBounds(int left, int top, int right, int bottom) {
        if (getBounds().width() != (right - left) || getBounds().height() != (bottom - top)) {
            onSizeChanged(getBounds().width(), getBounds().height(), right - left, bottom - top);
        }
        super.setBounds(left, top, right, bottom);
    }

    protected void onSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight) {

    }

}
