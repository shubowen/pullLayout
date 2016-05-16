package com.xiaosu.pulllayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.xiaosu.pulllayout.base.BasePullLayout;

public class PullLayout extends BasePullLayout {

    public PullLayout(Context context) {
        this(context, null);
    }

    public PullLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullLayout, defStyleAttr, 0);
        int waterDropColor = a.getColor(R.styleable.PullLayout_waterDropColor, Color.GRAY);
        int refreshArrowColor = a.getColor(R.styleable.PullLayout_refreshArrowColor, Color.WHITE);
        int indicatorArrowColor = a.getColor(R.styleable.PullLayout_indicatorArrowColor, Color.GRAY);
        int loadStartColor = a.getColor(R.styleable.PullLayout_loadStartColor, 0xFF555555);
        int loadEndColor = a.getColor(R.styleable.PullLayout_loadEndColor, 0xFFDDDDDD);
        int textColor = a.getColor(R.styleable.PullLayout_android_textColor, Color.GRAY);
        a.recycle();

        WaterDropView headView = (WaterDropView) LayoutInflater.from(context).inflate(R.layout.lay_water_drop_view, this, false);
        headView.setWaterDropColor(waterDropColor);
        headView.setRefreshArrowColorColor(refreshArrowColor);
        headView.setLoadStartColor(loadStartColor);
        headView.setLoadEndColor(loadEndColor);
        attachHeadView(headView);

        FooterView footer = new FooterView(context);
        footer.setTextColor(textColor);
        footer.setIndicatorArrowColorColor(indicatorArrowColor);
        footer.setLoadStartColor(loadStartColor);
        footer.setLoadEndColor(loadEndColor);
        attachFooterView(footer);
    }
}
