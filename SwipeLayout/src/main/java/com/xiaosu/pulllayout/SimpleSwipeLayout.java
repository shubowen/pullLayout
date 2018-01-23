package com.xiaosu.pulllayout;

import android.content.Context;
import android.util.AttributeSet;

import com.xiaosu.pulllayout.base.SwipeLayout;

public class SimpleSwipeLayout extends SwipeLayout {

    public SimpleSwipeLayout(Context context) {
        this(context, null);
    }

    public SimpleSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        /*attachHeadView(new SimpleRefreshHead());
        attachFooterView(new SimpleLoadFooter());*/
    }
}
