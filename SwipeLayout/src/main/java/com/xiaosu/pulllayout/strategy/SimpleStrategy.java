package com.xiaosu.pulllayout.strategy;

import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

import com.xiaosu.pulllayout.api.ContentWrapper;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.base.SwipeLayout;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public abstract class SimpleStrategy implements IStrategy {

    protected final SwipeLayout mParent;
    protected final IRefreshHead mHeader;
    protected final ILoadFooter mFooter;
    protected final View mTarget;

    protected final View mHeaderView;
    protected final View mFooterView;

    protected boolean isRefreshing;
    protected boolean isLoading;
    private View mNestedScrollView;

    protected final int indexOfTarget;
    protected final int indexOfFooter;
    protected final int indexOfHeader;


    public SimpleStrategy(SwipeLayout parent, IRefreshHead header, ILoadFooter footer, View target) {
        mParent = parent;
        mHeader = header;
        mFooter = footer;
        mTarget = target;

        mHeaderView = mHeader.getView(parent);
        mFooterView = mFooter.getView(parent);

        indexOfHeader = parent.indexOfChild(mHeaderView);
        indexOfFooter = parent.indexOfChild(mFooterView);
        indexOfTarget = parent.indexOfChild(mTarget);
    }

    @Override
    public boolean shouldDrawFooter() {
        return true;
    }

    @Override
    public boolean shouldDrawHeader() {
        return true;
    }

    protected void measureChild(View child, int parentWidthMeasureSpec,
                                int parentHeightMeasureSpec, int wantHeight) {
        int paddingLeft = 0;
        int paddingRight = 0;
        ViewParent parent = child.getParent();
        if (null != parent && parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            paddingLeft = vg.getPaddingLeft();
            paddingRight = vg.getPaddingRight();
        }
        final ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec,
                paddingLeft + paddingRight, lp.width);
        final int childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec,
                0, wantHeight == -1 ? lp.height : wantHeight);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    public abstract void onNestedPreScroll(View target, int dx, int dy, int[] consumed);

    /**
     * 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    protected int evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return Math.round(startFloat + fraction * (endValue.floatValue() - startFloat));
    }

    final public boolean isRefreshing() {
        return isRefreshing;
    }

    final public boolean isLoading() {
        return isLoading;
    }

    final public void refresh() {
        isRefreshing = true;
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                mParent.isLaidOut()) ||
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
                        mParent.getWidth() > 0 && mParent.getHeight() > 0)) {
            makeRefreshInternal();
        } else {
            mParent.post(new Runnable() {
                @Override
                public void run() {
                    makeRefreshInternal();
                }
            });
        }
    }

    protected abstract void makeRefreshInternal();

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    public void onNestedFling(View target, float velocityX, float velocityY) {
    }

    protected void fling(int velocity) {
        if (mTarget instanceof ScrollView) {
            ((ScrollView) mTarget).fling(velocity);
        } else if (mTarget instanceof AbsListView &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AbsListView) mTarget).fling(velocity);
        } else if (mTarget instanceof WebView) {
            ((WebView) mTarget).flingScroll(0, velocity);
        } else if (mTarget instanceof RecyclerView) {
            ((RecyclerView) mTarget).fling(0, velocity);
        } else if (mTarget instanceof NestedScrollView) {
            ((NestedScrollView) mTarget).fling(velocity);
        }
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollView = target;

        ContentWrapper wrapper = ContentWrapper.wrapper(child);
    }

    protected boolean canSwipeDown() {
        return null != mNestedScrollView ?
                mNestedScrollView.canScrollVertically(1) : mTarget.canScrollVertically(1);
    }

    protected boolean canSwipeUp() {
        return null != mNestedScrollView ?
                mNestedScrollView.canScrollVertically(-1) : mTarget.canScrollVertically(-1);
    }

    public int getChildDrawingOrder(int childCount, int i) {
        return i;
    }


    protected boolean swipeBy(int dy) {
        return true;
    }

    protected void swipeTo(int y) {

    }

}
