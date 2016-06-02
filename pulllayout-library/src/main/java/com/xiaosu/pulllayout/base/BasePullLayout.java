package com.xiaosu.pulllayout.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import com.xiaosu.pulllayout.R;

public class BasePullLayout
        extends ViewGroup implements
        NestedScrollingParent,
        NestedScrollingChild,
        IPull {

    private static final String LOG_TAG = BasePullLayout.class.getSimpleName();

    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    private static final String TAG = "Mr.su";

    private View mTarget; // the target of the gesture

    private OnPullCallBackListener mListener;
    private int mTouchSlop;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;

    private final int[] mParentOffsetInWindow = new int[2];

    private boolean mNestedScrollInProgress;

    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;

    private int mActivePointerId = INVALID_POINTER;

    //正在执行返回动画
    private boolean mReturningToStart;

    private int mHeadViewIndex = -1;

    float mScrollY = 0;

    private int mWidth;
    private int mHeight;

    private boolean mCorrected;

    /*头*/
    IRefreshHead mRefreshHead;
    /*尾*/
    ILoadFooter mLoadFooter;

    //是否可下拉
    private boolean mPullDownEnable;
    //是否可上拉
    private boolean mPullUpEnable;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshHead.detach();
        mLoadFooter.detach();
    }

    public BasePullLayout(Context context) {
        this(context, null);
    }

    public BasePullLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);

        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PullLayout_enable);
        mPullDownEnable = array.getBoolean(R.styleable.PullLayout_enable_pullDownEnable, true);
        mPullUpEnable = array.getBoolean(R.styleable.PullLayout_enable_pullUpEnable, true);
        array.recycle();
    }

    public void attachHeadView(IRefreshHead head) {
        if (null == head || null == head.getTargetView()) {
            throw new RuntimeException("head不能为空");
        }
        if (null != mRefreshHead) {
            removeView(mRefreshHead.getTargetView());
        }
        mRefreshHead = head;
        mRefreshHead.pullLayout(this);

        addView(mRefreshHead.getTargetView());
    }

    public void attachFooterView(ILoadFooter footer) {
        if (null == footer || null == footer.getTargetView()) {
            throw new RuntimeException("footer不能为空");
        }
        if (null != mLoadFooter) {
            removeView(mLoadFooter.getTargetView());
        }
        mLoadFooter = footer;
        mLoadFooter.pullLayout(this);

        addView(mLoadFooter.getTargetView());
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mHeadViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            // Draw the selected child last
            return mHeadViewIndex;
        } else if (i >= mHeadViewIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return i;
        }
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshHead.getTargetView()) && !child.equals(mLoadFooter.getTargetView())) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childPaddingLeft = getPaddingLeft();
        final int childPaddingTop = getPaddingTop();
        final int childPaddingBottom = getPaddingBottom();
        final int childPaddingRight = getPaddingRight();
        final int childWidth = mWidth - childPaddingLeft - childPaddingRight;
        final int childHeight = mHeight - childPaddingTop - childPaddingBottom;

        int mFooterWidth = mLoadFooter.getTargetView().getMeasuredWidth();
        int mFooterHeight = mLoadFooter.getTargetView().getMeasuredHeight();

        int mHeadWidth = mRefreshHead.getTargetView().getMeasuredWidth();
        int mHeadHeight = mRefreshHead.getTargetView().getMeasuredHeight();

        int offset = Math.round(mScrollY);
        child.layout(childPaddingLeft,
                childPaddingTop + offset,
                childPaddingLeft + childWidth,
                childPaddingTop + childHeight + offset);

        int footerTop = childHeight + childPaddingBottom + offset;
        mLoadFooter.getTargetView().layout(0, footerTop, mFooterWidth, footerTop + mFooterHeight);

        int next = -mHeadHeight + childPaddingTop + offset;
        next = next > 0 ? 0 : next;
        mRefreshHead.getTargetView().layout((mWidth - mHeadWidth) / 2, next, (mWidth + mHeadWidth) / 2, offset + childPaddingTop);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }

        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        mLoadFooter.getTargetView().measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshHead.getTargetView().measure(widthMeasureSpec, heightMeasureSpec);

        // Get the index of the mRefreshHead
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mRefreshHead.getTargetView()) {
                mHeadViewIndex = index;
                break;
            }
        }
    }

    /**
     * @return child在竖直方向上是否能向上滚动
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                int count = absListView.getAdapter().getCount();
                int fristPos = absListView.getFirstVisiblePosition();
                if (fristPos == 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop()) {
                    return false;
                }
                int lastPos = absListView.getLastVisiblePosition();
                return lastPos > 0 && count > 0 && lastPos == count - 1;
            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }


    @Override
    public boolean isEnabled() {
        return mPullDownEnable || mPullUpEnable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canNestScroll() || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialDownY;

                boolean canPullDown = canChildScrollDown() || (!canChildScrollDown() && !canChildScrollUp());

                //yDiff > mTouchSlop向下移动,这个值只赋值一次,记录child初始位置
                if (yDiff > mTouchSlop && canPullDown && !mIsBeingDragged && !mLoadFooter.isLoading() && mPullDownEnable) {
                    mInitialMotionY = mInitialDownY + mTouchSlop;
                    mIsBeingDragged = true;
                } else if (-yDiff > mTouchSlop && canChildScrollUp() && !mIsBeingDragged && !mRefreshHead.isRefreshing() && mPullUpEnable) {
                    //-yDiff > mTouchSlop向上移动,这个值只赋值一次,记录child初始位置
                    mInitialMotionY = mInitialDownY - mTouchSlop;
                    mIsBeingDragged = true;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged || isShowRefreshing() || isShowLoading();
    }

    private boolean isShowRefreshing() {
        return mRefreshHead.isRefreshing() && mScrollY > 0;
    }

    private boolean isShowLoading() {
        return mLoadFooter.isLoading() && mScrollY < 0;
    }

    private boolean canNestScroll() {
        return canChildScrollDown() && canChildScrollUp();
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() &&
                canNestScroll() &&
                !mReturningToStart &&
                !mRefreshHead.isRefreshing() &&
                !mLoadFooter.isLoading() &&
                (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private void moveSpinner(float overScroll) {
        //防止在上拉或者下拉的过程中,往回拉过头的现象
        boolean canPullDown = canChildScrollDown() || (!canChildScrollDown() && !canChildScrollUp());
        overScroll = canPullDown ? overScroll < 0 ? 0 : overScroll : overScroll > 0 ? 0 : overScroll;
        updateLayout(overScroll);
    }

    /**
     * 更新界面
     *
     * @param overScroll
     */
    private void updateLayout(float overScroll) {
        if (mScrollY != overScroll) {
            mScrollY = overScroll;
            if (mScrollY >= 0) {
                mRefreshHead.onPull(mScrollY, !mLoadFooter.isLoading());
            } else if (mScrollY < 0) {
                mLoadFooter.onPull(mScrollY, !mRefreshHead.isRefreshing());
            }
            mRefreshHead.getTargetView().requestLayout();
        }
    }

    private void finishSpinner(float overScroll) {
        clearAnimation();
        if (overScroll >= 0) {
            mRefreshHead.onFingerUp(overScroll);
        } else {
            mLoadFooter.onFingerUp(overScroll);
        }
    }

    /**
     * 拉回控件
     */
    @Override
    public void animToStartPosition(final AnimationCallback callback) {
        if (mScrollY == 0 && !mRefreshHead.isRefreshing() && !mLoadFooter.isLoading())
            return;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float fraction, Transformation t) {
                updateLayout(evaluate(fraction, mScrollY, 0));
                if (null != callback) callback.onAnimation(fraction);
            }
        };
        animation.setDuration(1000);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mReturningToStart = true;
                if (null != callback) callback.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mReturningToStart = false;
                if (null != callback) callback.onAnimationEnd();
            }
        });
        startAnimation(animation);
    }

    @Override
    public void animToRightPosition(final float targetY, final AnimationCallback callback) {
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float fraction, Transformation t) {
                updateLayout(evaluate(fraction, mScrollY, targetY));
                if (null != callback) callback.onAnimation(fraction);
            }
        };
        animation.setDuration(300);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mReturningToStart = true;
                if (null != callback) callback.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (null != callback) callback.onAnimationEnd();
            }
        });
        startAnimation(animation);
    }

    @Override
    public void pullUpCallback() {
        if (null != mListener) mListener.onLoad();
    }

    @Override
    public void pullDownCallback() {
        if (null != mListener) mListener.onRefresh();
    }

    /**
     * 自动刷新数据
     */
    public void autoRefresh() {
        mRefreshHead.autoRefresh();
    }

    /**
     * 在OnCreate方法中调用autoRefresh()方法
     */
    public void autoRefreshOnCreate() {
        post(new Runnable() {
            @Override
            public void run() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        autoRefresh();
                    }
                });
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canNestScroll() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                //刷新或者加载中,防止再次点击屏幕回弹
                correctInitialMotionY(y);
                final float overScroll = (y - mInitialMotionY) * DRAG_RATE;
                if (mIsBeingDragged || isShowRefreshing() || isShowLoading()) {
                    moveSpinner(overScroll);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                mIsBeingDragged = false;
                mCorrected = false;
                //松开手指
                finishSpinner(mScrollY);
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    /**
     * 纠正开始点击的Y坐标,防止刷新或者加载过程中再次的手势
     *
     * @param y
     */
    private void correctInitialMotionY(float y) {
        if (!mCorrected) {
            if (mRefreshHead.isRefreshing() || mLoadFooter.isLoading()) {
                mInitialMotionY = y - mScrollY / DRAG_RATE;
            }
            mCorrected = true;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    /**
     * 拉回
     */
    public void finishPull() {
        if (mScrollY >= 0) {
            mRefreshHead.finishPull(mIsBeingDragged);
        } else {
            mLoadFooter.finishPull(mIsBeingDragged);
        }
    }

    public void setOnPullListener(OnPullCallBackListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    /**
     * 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    public interface OnPullCallBackListener {
        void onRefresh();

        void onLoad();
    }

    public boolean isPullDownEnable() {
        return mPullDownEnable;
    }

    public void setPullDownEnable(boolean pullDownEnable) {
        mPullDownEnable = pullDownEnable;
    }

    public boolean isPullUpEnable() {
        return mPullUpEnable;
    }

    public void setPullUpEnable(boolean pullUpEnable) {
        mPullUpEnable = pullUpEnable;
    }
}
