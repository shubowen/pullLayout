package com.xiaosu.pulllayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.xiaosu.pulllayout.base.BasePullLayout;
import com.xiaosu.pulllayout.footer.FooterView;
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
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullLayout);
        int indicatorArrowColor = a.getColor(R.styleable.PullLayout_indicatorArrowColor, Color.GRAY);
        int loadStartColor = a.getColor(R.styleable.PullLayout_loadStartColor, 0xFF555555);
        int loadEndColor = a.getColor(R.styleable.PullLayout_loadEndColor, 0xFFDDDDDD);
        int textColor = a.getColor(R.styleable.PullLayout_android_textColor, Color.GRAY);
        a.recycle();

        attachHeadView(new SimpleRefreshHead());

        FooterView footer = new FooterView(context);
        footer.setTextColor(textColor);
        footer.setIndicatorArrowColorColor(indicatorArrowColor);
        footer.setLoadStartColor(loadStartColor);
        footer.setLoadEndColor(loadEndColor);
        footer.setLayoutParams(new LayoutParams(-1, -2));
        attachFooterView(footer);
    }
}
