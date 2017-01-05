package com.xiaosu.pulllayout.footer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.xiaosu.pulllayout.R;
import com.xiaosu.pulllayout.base.AnimationCallback;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.base.IPull;
import com.xiaosu.pulllayout.drawable.ArrowAnimDrawable;

/**
 * 作者：疏博文 创建于 2016-09-13 09:48
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class SimpleLoadFooter implements ILoadFooter {

    private static final String TAG = "SimpleRefreshHead";

    boolean isLoading = false;

    private View mFooterView;
    private TextView mTvTip;
    private SpinKitView mSpinKit;
    private ImageView mIvArrow;

    private int mFooterHeight = -1;
    private ArrowAnimDrawable mAnimDrawable;
    private boolean mHasSprite;

    private boolean mArrowDown = false;
    private IPull iPull;
    private boolean mReturningToLoading;
    /*临界距离*/
    private int mCriticalDis;

    @Override
    public View getTargetView(ViewGroup parent) {
        if (null == mFooterView) {
            mFooterView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lay_refresh_head, parent, false);
            mIvArrow = (ImageView) mFooterView.findViewById(R.id.iv_arrow);

            mAnimDrawable = new ArrowAnimDrawable();

            mIvArrow.setImageDrawable(mAnimDrawable);
            mSpinKit = (SpinKitView) mFooterView.findViewById(R.id.spin_kit);
            mTvTip = (TextView) mFooterView.findViewById(R.id.tv_tip);
        }
        return mFooterView;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {

        if (!enable || mReturningToLoading || isLoading) return;

        if (mFooterHeight == -1) {
            mFooterHeight = mFooterView.getHeight();
            mCriticalDis = (int) (mFooterHeight * 1.3f);
        }

        if (-scrollY > mCriticalDis && !mArrowDown) {
            if (!mHasSprite) {
                Sprite sprite = SpriteFactory.create(Style.FADING_RECT);
                sprite.setColor(Color.GRAY);
                mSpinKit.setIndeterminateDrawable(sprite);
                mHasSprite = true;
            }
            showArrow();
            mAnimDrawable.arrowDown();
            mTvTip.setText(R.string.release_to_loading);

            mArrowDown = true;
        } else if (-scrollY <= mCriticalDis && mArrowDown) {
            showArrow();
            mAnimDrawable.arrowUp();
            mTvTip.setText(R.string.up_to_loading);

            mArrowDown = false;
        }
    }

    private void showArrow() {
        if (View.VISIBLE != mIvArrow.getVisibility())
            mIvArrow.setVisibility(View.VISIBLE);
        if (View.VISIBLE == mSpinKit.getVisibility())
            mSpinKit.setVisibility(View.GONE);
    }

    private void showSpinKit() {
        if (View.VISIBLE == mIvArrow.getVisibility())
            mIvArrow.setVisibility(View.GONE);
        if (View.VISIBLE != mSpinKit.getVisibility())
            mSpinKit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFingerUp(float scrollY) {

        if (isLoading) {
            iPull.animToRightPosition(-mFooterHeight, null);
            return;
        }

        if (!mArrowDown) {//回到原位
            iPull.animToStartPosition(new AnimationCallback() {
                @Override
                public void onAnimationEnd() {
                    showArrow();
                }
            });
        } else {
            mReturningToLoading = true;
            isLoading = true;
            iPull.animToRightPosition(-mFooterHeight, new AnimationCallback() {
                @Override
                public void onAnimationStart() {
                    showSpinKit();
                    mTvTip.setText(R.string.loading);
                }

                @Override
                public void onAnimationEnd() {
                    mReturningToLoading = false;
                    iPull.pullUpCallback();
                }
            });
        }
    }

    @Override
    public void detach() {

    }

    @Override
    public void pullLayout(IPull iPull) {
        this.iPull = iPull;
    }

    @Override
    public void finishPull(boolean isBeingDragged, final CharSequence msg, final boolean result) {
        showResult(msg, result);
        mFooterView.postDelayed(new Runnable() {
            @Override
            public void run() {
                iPull.animToStartPosition(new AnimationCallback() {
                    @Override
                    public void onAnimationEnd() {
                        //恢复场景
                        reset();
                        if (result)
                            iPull.targetScrollBy(mFooterHeight);
                    }
                });
            }
        }, 300);
    }

    private void showResult(CharSequence msg, boolean result) {
        mTvTip.setText(msg);
        if (result) {
            mIvArrow.setImageResource(R.drawable.succeed_vector);
        } else {
            mIvArrow.setImageResource(R.drawable.failed_vector);
            mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.failed));
        }
        showArrow();
    }

    private void reset() {
        isLoading = false;
        mIvArrow.setImageDrawable(mAnimDrawable);
        mTvTip.setText(R.string.pull_refresh);
        mAnimDrawable.arrowUp();
        mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.text_color));
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }
}
