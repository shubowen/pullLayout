package com.xiaosu.pulllayout.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.xiaosu.pulllayout.R;

/**
 * 作者：疏博文 创建于 2016-09-13 09:48
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class SimpleRefreshHead implements IRefreshHead {

    private View mHeadView;
    private TextView mTvTip;
    private SpinKitView mSpinKit;
    private ImageView mIvArrow;

    private int mHeadViewHeight = -1;

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void refreshImmediately() {

    }

    @Override
    public void autoRefresh() {

    }

    @Override
    public View getTargetView(ViewGroup parent) {
        if (null == mHeadView) {
            mHeadView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lay_refresh_head, parent, false);
            mIvArrow = (ImageView) mHeadView.findViewById(R.id.iv_arrow);
            mSpinKit = (SpinKitView) mHeadView.findViewById(R.id.spin_kit);
            mTvTip = (TextView) mHeadView.findViewById(R.id.tv_tip);
        }
        return mHeadView;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {
        if (mHeadViewHeight == -1)
            mHeadViewHeight = mHeadView.getHeight();
    }

    @Override
    public void onFingerUp(float scrollY) {

    }

    @Override
    public void detach() {

    }

    @Override
    public void pullLayout(IPull iPull) {

    }

    @Override
    public void finishPull(boolean isBeingDragged) {

    }
}
