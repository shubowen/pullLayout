package com.xiaosu.pulllayout.head;

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
import com.xiaosu.pulllayout.base.IPull;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.drawable.FooterAnimDrawable;

/**
 * 作者：疏博文 创建于 2016-09-13 09:48
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class SimpleRefreshHead implements IRefreshHead {

    private static final String TAG = "SimpleRefreshHead";

    boolean isRefreshing = false;

    private View mHeadView;
    private TextView mTvTip;
    private SpinKitView mSpinKit;
    private ImageView mIvArrow;

    private int mHeadViewHeight = -1;
    private FooterAnimDrawable mAnimDrawable;
    private boolean mHasSprite;

    private boolean mArrowDown = true;
    private IPull iPull;
    private boolean mReturningToRefresh;

    @Override
    public boolean isRefreshing() {
        return isRefreshing;
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

            mAnimDrawable = new FooterAnimDrawable();
            mAnimDrawable.arrowDown();

            mIvArrow.setImageDrawable(mAnimDrawable);
            mSpinKit = (SpinKitView) mHeadView.findViewById(R.id.spin_kit);
            mTvTip = (TextView) mHeadView.findViewById(R.id.tv_tip);
        }
        return mHeadView;
    }

    @Override
    public void onPull(float scrollY, boolean enable) {

        if (!enable || mReturningToRefresh) return;

        if (mHeadViewHeight == -1) {
            mHeadViewHeight = mHeadView.getHeight();
        }

        if (scrollY > mHeadViewHeight && mArrowDown) {
            if (!mHasSprite) {
                Sprite sprite = SpriteFactory.create(Style.FADING_RECT);
                sprite.setColor(Color.GRAY);
                mSpinKit.setIndeterminateDrawable(sprite);
                mHasSprite = true;
            }
            showArrow();
            mAnimDrawable.arrowUp();
            mTvTip.setText(R.string.release_refresh);

            mArrowDown = false;
        } else if (scrollY <= mHeadViewHeight && !mArrowDown) {
            showArrow();
            mAnimDrawable.arrowDown();
            mTvTip.setText(R.string.pull_refresh);

            mArrowDown = true;
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
        if (mArrowDown) {//回到原位
            iPull.animToStartPosition(new AnimationCallback() {
                @Override
                public void onAnimationEnd() {
                    showArrow();
                }
            });
        } else {
            mReturningToRefresh = true;
            isRefreshing = true;
            iPull.animToRightPosition(mHeadViewHeight, new AnimationCallback() {
                @Override
                public void onAnimationStart() {
                    showSpinKit();
                    mTvTip.setText(R.string.refreshing);
                }

                @Override
                public void onAnimationEnd() {
                    mReturningToRefresh = false;
                    iPull.pullDownCallback();
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
    public void finishPull(boolean isBeingDragged) {
        //显示一个刷新结果
        iPull.animToStartPosition(new AnimationCallback() {
            @Override
            public void onAnimationEnd() {
                showArrow();
            }
        });
    }

    @Override
    public void finishPull(boolean isBeingDragged, CharSequence msg, boolean result) {
        mTvTip.setText(msg);
        if (result) {
            mIvArrow.setImageResource(R.drawable.succeed_vector);
        } else {
            mIvArrow.setImageResource(R.drawable.failed_vector);
            mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.failed));
        }
        showArrow();
        mHeadView.postDelayed(new Runnable() {
            @Override
            public void run() {
                iPull.animToStartPosition(new AnimationCallback() {
                    @Override
                    public void onAnimationStart() {
                        //恢复场景
                        reset();
                    }
                });
            }
        }, 1000);
    }

    private void reset() {
        isRefreshing = false;
        mIvArrow.setImageDrawable(mAnimDrawable);
        mTvTip.setText(R.string.pull_refresh);
        mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.text_color));
    }
}
