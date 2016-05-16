package com.xiaosu.pulllayout;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaosu.pulllayout.base.AnimationCallback;
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

    private final TextView mTextViewTip;

    private final FooterAnimDrawable mAnimDrawable;

    /*true->预加载状态*/
    boolean mPreLoading = false;

    static float maxRate = 1.5f;

    /*上拉的临界点(dp)*/
    float criticalDistance = 50;

    private boolean isLoading;

    private IPull pullLayout;

    AnimationCallback mAnimationCallback = new AnimationCallback() {
        @Override
        public void onAnimationEnd() {
            reset();
        }
    };

    public FooterView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.lay_refresh_footer, this);

        criticalDistance *= getResources().getDisplayMetrics().density;

        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        mAnimDrawable = new FooterAnimDrawable();
        ivArrow.setImageDrawable(mAnimDrawable);
        mTextViewTip = (TextView) findViewById(R.id.tv_tip);

        reset();
    }

    public void update(float rate) {
        if (isLoading) {
            return;
        }
        if (rate >= maxRate && !mPreLoading) {
            mAnimDrawable.arrowDown();
            mPreLoading = true;
            mTextViewTip.setText(R.string.release_to_loading);
        } else if (rate < maxRate && mPreLoading) {
            mAnimDrawable.arrowUp();
            mPreLoading = false;
            mTextViewTip.setText(R.string.up_to_loading);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(criticalDistance), MeasureSpec.EXACTLY));
    }

    public void loading() {
        isLoading = true;
        mAnimDrawable.rotating();
        mTextViewTip.setText(R.string.loading);

        pullLayout.pullUpCallback();
    }

    @Override
    public void pullLayout(IPull iPull) {
        this.pullLayout = iPull;
    }

    @Override
    public void finishPull(boolean isBeingDragged) {
        pullLayout.animToStartPosition(mAnimationCallback);
    }

    /**
     * 重置状态
     */
    private void reset() {
        clearAnimation();
        isLoading = false;
        mAnimDrawable.showArrow();
        mTextViewTip.setText("上拉加载");
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
            pullLayout.animToRightPosition(-criticalDistance, null);
        } else
            pullLayout.animToStartPosition(mAnimationCallback);
    }

    @Override
    public void detach() {

    }

    public void setTextColor(int textColor) {
        mTextViewTip.setTextColor(textColor);
    }

    public void setIndicatorArrowColorColor(int themeColor) {
        mAnimDrawable.setIndicatorArrowColorColor(themeColor);
    }

    public void setLoadStartColor(int loadStartColor) {
        mAnimDrawable.setLoadStartColor(loadStartColor);
    }

    public void setLoadEndColor(int loadEndColor) {
        mAnimDrawable.setLoadEndColor(loadEndColor);
    }
}
