package com.xiaosu.pulllayout;

import android.content.Context;
import android.util.AttributeSet;

import com.xiaosu.pulllayout.base.BasePullLayout;
import com.xiaosu.pulllayout.footer.SimpleLoadFooter;
import com.xiaosu.pulllayout.head.SimpleRefreshHead;

public class SimplePullLayout extends BasePullLayout {

    public SimplePullLayout(Context context) {
        this(context, null);
    }

    public SimplePullLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimplePullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        attachHeadView(new SimpleRefreshHead());
        attachFooterView(new SimpleLoadFooter());
    }
}
