package com.xiaosu.pulllayout;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaosu.pulllayout.PullLayout.OnPullCallBackListener;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.base.IPull;
import com.xiaosu.pulllayout.drawable.FooterAnimDrawable;


/**
 * 作者：疏博文 创建于 2016-03-06 23:07
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class FooterView extends LinearLayout implements ILoadFooter {

    private static final String TAG = "Mr.su";
    private final ImageView ivArrow;

    private final TextView tvTip;

    private final FooterAnimDrawable mAnimDrawable;

    /*true->预加载状态*/
    boolean mPreLoading = false;

    static float maxRate = 1.5f;

    /*上拉的临界点(dp)*/
    float criticalDistance = 50;

    private boolean isLoading;

    private IPull pullLayout;

    private OnPullCallBackListener mListener;

    public FooterView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.lay_refresh_footer, this);

        criticalDistance *= getResources().getDisplayMetrics().density;

        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        mAnimDrawable = new FooterAnimDrawable();
        ivArrow.setImageDrawable(mAnimDrawable);
        tvTip = (TextView) findViewById(R.id.tv_tip);

        reset();
    }

    public void update(float rate) {
        if (isLoading) {
            return;
        }
        if (rate >= maxRate && !mPreLoading) {
            mAnimDrawable.arrowDown();
            mPreLoading = true;
            tvTip.setText(R.string.release_to_loading);
        } else if (rate < maxRate && mPreLoading) {
            mAnimDrawable.arrowUp();
            mPreLoading = false;
            tvTip.setText(R.string.up_to_loading);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(criticalDistance), MeasureSpec.EXACTLY));
    }

    public void loading() {
        isLoading = true;
        mAnimDrawable.rotating();
        tvTip.setText(R.string.loading);
        if (null != mListener)
            mListener.onLoad();
    }

    public void reset() {
        clearAnimation();
        isLoading = false;
        mAnimDrawable.showArrow();
        tvTip.setText("上拉加载");
    }

    @Override
    public void pullLayout(IPull iPull) {
        this.pullLayout = iPull;
    }

    @Override
    public void finishPull(boolean isBeingDragged) {
        pullLayout.animToStartPosition(true);
    }

    @Override
    public void setOnPullListener(OnPullCallBackListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public View getTargetView() {
        return this;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {
        if (enable) {
            float ratio = -scrollY / criticalDistance;
            update(ratio);
        }
    }

    @Override
    public void onFingerUp(float scrollY) {
        if (mPreLoading) {
            loading();
            pullLayout.animToRightPosition(-criticalDistance, false, false);
        } else
            pullLayout.animToStartPosition(true);
    }

    @Override
    public void detach() {

    }
}
