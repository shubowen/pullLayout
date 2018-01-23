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
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.drawable.ArrowAnimDrawable;

/**
 * 作者：疏博文 创建于 2016-09-13 09:48
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class SimpleRefreshHead implements IRefreshHead {

    private static final String TAG = "SimpleRefreshHead";

    private boolean isRefreshing = false;

    private View mHeadView;
    private TextView mTvTip;
    private SpinKitView mSpinKit;
    private ImageView mIvArrow;

    private int mHeadViewHeight = -1;
    private ArrowAnimDrawable mAnimDrawable;
    private boolean mHasSprite;

    private boolean mArrowDown = true;
    private boolean mReturningToRefresh;
    /*临界距离*/
    private int mCriticalDis;
    private boolean mReturnToReset;
    private boolean mReturningToStart;


    @Override
    public View getView(ViewGroup parent) {
        if (null == mHeadView) {
            mHeadView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lay_refresh_head, parent, false);
            mIvArrow = (ImageView) mHeadView.findViewById(R.id.iv_arrow);

            mAnimDrawable = new ArrowAnimDrawable();
            mAnimDrawable.arrowDown();

            mIvArrow.setImageDrawable(mAnimDrawable);
            mSpinKit = (SpinKitView) mHeadView.findViewById(R.id.spin_kit);
            mTvTip = (TextView) mHeadView.findViewById(R.id.tv_tip);
        }
        return mHeadView;
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
            mSpinKit.setVisibility(View.INVISIBLE);

        mSpinKit.stop();
    }

    private void showSpinKit() {
        initSprite();
        if (View.VISIBLE == mIvArrow.getVisibility())
            mIvArrow.setVisibility(View.INVISIBLE);
        if (View.VISIBLE != mSpinKit.getVisibility())
            mSpinKit.setVisibility(View.VISIBLE);

        mSpinKit.start();
    }

    @Override
    public void detach() {

    }

    @Override
    public int threshold() {
        return mCriticalDis;
    }


    @Override
    public void onActive() {

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

    public void onUnderThreshold() {
        isRefreshing = false;
        mReturnToReset = false;
        mIvArrow.setImageDrawable(mAnimDrawable);
        mTvTip.setText(R.string.pull_refresh);
        mAnimDrawable.arrowDown();
        mTvTip.setTextColor(mTvTip.getContext().getResources().getColor(R.color.text_color));
    }

    @Override
    public void onResult(CharSequence message, boolean result) {

    }

    @Override
    public int delay() {
        return 0;
    }

    @Override
    public void onBeyondThreshold() {

    }

    @Override
    public void onHidden() {

    }
}
