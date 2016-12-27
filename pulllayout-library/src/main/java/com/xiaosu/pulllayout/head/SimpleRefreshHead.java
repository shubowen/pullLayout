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
    /*临界距离*/
    private int mCriticalDis;

    @Override
    public boolean isRefreshing() {
        return isRefreshing;
    }

    @Override
    public void refreshImmediately() {
        autoRefresh();
    }

    @Override
    public void autoRefresh() {
        getDistance();
        iPull.animToRightPosition(mHeadViewHeight, 0, new AnimationCallback() {
            @Override
            public void onAnimationEnd() {
                isRefreshing = true;
                showSpinKit();
                mTvTip.setText(R.string.refreshing);
                iPull.pullDownCallback();
            }
        });
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

        if (!enable || mReturningToRefresh || isRefreshing) return;

        getDistance();

        if (scrollY >= mCriticalDis && mArrowDown) {
            showArrow();
            mAnimDrawable.arrowUp();
            mTvTip.setText(R.string.release_refresh);

            mArrowDown = false;
        } else if (scrollY < mCriticalDis && !mArrowDown) {
            showArrow();
            mAnimDrawable.arrowDown();
            mTvTip.setText(R.string.pull_refresh);

            mArrowDown = true;
        }
    }

    private void initSprite() {
        if (!mHasSprite) {
            Sprite sprite = SpriteFactory.create(Style.FADING_RECT);
            sprite.setColor(Color.GRAY);
            mSpinKit.setIndeterminateDrawable(sprite);
            mHasSprite = true;
        }
    }

    private void getDistance() {
        if (mHeadViewHeight == -1) {
            mHeadViewHeight = mHeadView.getHeight();
            mCriticalDis = (int) (mHeadViewHeight * 1.3f);
        }
    }

    private void showArrow() {
        if (View.VISIBLE != mIvArrow.getVisibility())
            mIvArrow.setVisibility(View.VISIBLE);
        if (View.VISIBLE == mSpinKit.getVisibility())
            mSpinKit.setVisibility(View.GONE);
    }

    private void showSpinKit() {
        initSprite();
        if (View.VISIBLE == mIvArrow.getVisibility())
            mIvArrow.setVisibility(View.GONE);
        if (View.VISIBLE != mSpinKit.getVisibility())
            mSpinKit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFingerUp(float scrollY) {

        if (isRefreshing) {
            //刷新的情况下直接拉回
            iPull.animToRightPosition(mHeadViewHeight, null);
            return;
        }

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
    public void finishPull(boolean isBeingDragged, final CharSequence msg, final boolean result) {
        iPull.animToRightPosition(mHeadViewHeight, new AnimationCallback() {
            @Override
            public void onAnimationStart() {
                mTvTip.setText(msg);
                if (result) {
                    mIvArrow.setImageResource(R.drawable.succeed_vector);
                } else {
                    mIvArrow.setImageResource(R.drawable.failed_vector);
                    mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.failed));
                }
                showArrow();
            }

            @Override
            public void onAnimationEnd() {
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
                }, 300);
            }
        });
    }

    private void reset() {
        isRefreshing = false;
        mIvArrow.setImageDrawable(mAnimDrawable);
        mTvTip.setText(R.string.pull_refresh);
        mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.text_color));
    }
}
