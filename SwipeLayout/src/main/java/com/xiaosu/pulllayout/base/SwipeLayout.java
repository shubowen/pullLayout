package com.xiaosu.pulllayout.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ScrollView;

import com.xiaosu.pulllayout.R;
import com.xiaosu.pulllayout.footer.SimpleLoadFooter;
import com.xiaosu.pulllayout.head.SimpleRefreshHead;
import com.xiaosu.pulllayout.strategy.ScaleStrategy;
import com.xiaosu.pulllayout.strategy.SimpleStrategy;

import java.lang.reflect.Method;

public class SwipeLayout
        extends ViewGroup implements
        NestedScrollingParent,
        NestedScrollingChild,
        ISwipe {


    private final int[] mParentScrollConsumed = new int[2];

    private View mTarget; // the target of the gesture

    private OnPullCallBackListener mListener;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;

    private final int[] mParentOffsetInWindow = new int[2];

    private boolean mNestedScrollInProgress;

    //正在执行返回动画
    private boolean mReturningToStart;

    /*头*/
    IRefreshHead mRefreshHead;
    /*尾*/
    ILoadFooter mLoadFooter;

    //是否可下拉
    private boolean mSwipeDownEnable;
    //是否可上拉
    private boolean mSwipeUpEnable;
    private boolean mReturningToRight;

    //静止
    public static final int STATE_IDLE = 0;

    //状态：0代表静止,1代表下拉,-1代表上拉
    private int mState = STATE_IDLE;

    //标记拉动的方向
    private boolean mDragStateFlag = false;

    private SimpleStrategy mStrategy;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshHead.detach();
        mLoadFooter.detach();
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        mSwipeDownEnable = array.getBoolean(R.styleable.SwipeLayout_swipeDownEnable, true);
        mSwipeUpEnable = array.getBoolean(R.styleable.SwipeLayout_swipeUpEnable, true);
        array.recycle();
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

        if (child == mRefreshHead && !mStrategy.shouldDrawHeader()) {
//            WLog.d("不绘制头布局");
            return false;
        }

        if (child == mLoadFooter && !mStrategy.shouldDrawFooter()) {
//            WLog.d("不绘制脚布局");
            return false;
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return null != p && p instanceof LayoutParams;
    }

    private void ensureTarget() {

        if (null != mTarget && null != mRefreshHead && null != mLoadFooter)
            return;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (null == mRefreshHead && lp.gravity == Gravity.TOP && child instanceof IRefreshHead) {
                mRefreshHead = (IRefreshHead) child;
            } else if (null == mLoadFooter && lp.gravity == Gravity.BOTTOM && child instanceof ILoadFooter) {
                mLoadFooter = (ILoadFooter) child;
            } else if (null == mTarget) {
                mTarget = child;
            } /*else {
                removeViewInLayout(child);
            }*/
        }

        if (null == mRefreshHead) {
            mRefreshHead = new SimpleRefreshHead();
            View v = mRefreshHead.getView(this);
            addViewInLayout(v, 0, v.getLayoutParams());
        }

        if (null == mLoadFooter) {
            mLoadFooter = new SimpleLoadFooter(getContext());
            View v = mLoadFooter.getView(this);
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.gravity = Gravity.BOTTOM;
            addViewInLayout(v, 2, lp);
        }

        if (null == mStrategy) {
            mStrategy = new ScaleStrategy(this, mRefreshHead, mLoadFooter, mTarget);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }

        ensureTarget();

        if (mTarget == null) {
            return;
        }

        // TODO: 2018/1/18 mRefreshHead == null || mLoadFooter == null

        View footerView = mLoadFooter.getView(this);
        View headView = mRefreshHead.getView(this);

        final int childPaddingLeft = getPaddingLeft();
        final int childPaddingTop = getPaddingTop();
        final int childPaddingBottom = getPaddingBottom();
        final int childPaddingRight = getPaddingRight();

        mStrategy.onLayout(changed,
                childPaddingLeft, childPaddingTop, childPaddingRight, childPaddingBottom,
                getMeasuredWidth(), getMeasuredHeight(),
                mTarget);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureTarget();

        if (null == mTarget) {
            return;
        }

        mStrategy.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mTarget.getMeasuredWidth(), mTarget.getMeasuredHeight());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public static final int UNSPECIFIED_GRAVITY = -1;
        public int gravity = UNSPECIFIED_GRAVITY;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray arr = c.obtainStyledAttributes(attrs, R.styleable.SwipeLayout_Layout);
            gravity = arr.getInt(R.styleable.SwipeLayout_Layout_android_layout_gravity, UNSPECIFIED_GRAVITY);
            arr.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTarget();
    }

    /**
     * @return child在竖直方向上是否能向上滚动
     */
    public boolean canChildScrollUp() {
        return mTarget.canScrollVertically(-1);
    }

    public boolean canChildScrollDown() {
        return mTarget.canScrollVertically(1);
    }

    /**
     * 更新拖动的状态
     *
     * @param state
     */
    private void updateDragStateInner(int state) {
        if (mState != state) mState = state;
    }

    private boolean canNestScroll() {
        return canChildScrollDown() && canChildScrollUp();
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mStrategy.dispatchTouchEvent(ev);
    }

    public boolean invokeSuperDispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
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

        mStrategy.onNestedPreScroll(target, dx, dy, consumed);

        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    private void updateNestedScrollState(int state) {
        if (!mDragStateFlag) {
            updateDragStateInner(state);
            mDragStateFlag = true;
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        stopNestedScroll();
        mDragStateFlag = false;
        mNestedScrollInProgress = false;
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
//        WLog.d("[onNestedScroll]-[" + mTarget.canScrollVertically(-1) + "]-[" + mTarget.canScrollVertically(1) + "]");
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];

        /*if (dy < 0 && !canChildScrollUp() && mSwipeDownEnable) {
            //下拉
            updateNestedScrollState(STATE_PULL_DOWN);

            mStrategy.swipeBy(Math.round(dy * DRAG_RATE));

        } else if (dy > 0 && !canChildScrollDown() && mSwipeUpEnable) {
            //上拉
            updateNestedScrollState(STATE_PULL_UP);

            mStrategy.swipeBy(Math.round(dy * DRAG_RATE));
        }*/
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
        return mStrategy.onNestedPreFling(target, velocityX, velocityY);
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        mStrategy.onNestedFling(target, velocityX, velocityY);
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

    @Override
    public void clearAnimation() {
        super.clearAnimation();
        //解决clearAnimation导致的Animation没有走onAnimationEnd回调问题
        if (mReturningToRight) mReturningToRight = false;
        if (mReturningToStart) mReturningToStart = false;
    }

    @Override
    public void notifyUp() {
        if (null != mListener) mListener.onLoad();
    }

    @Override
    public void notifyDown() {
        if (null != mListener) mListener.onRefresh();
    }

    public int getState() {
        return mState;
    }

    @Override
    public void smoothScrollBy(int offset) {
        final View view = mTarget;
        if (view instanceof RecyclerView)
            ((RecyclerView) view).smoothScrollBy(0, offset);
        else if (view instanceof ScrollView)
            ((ScrollView) view).smoothScrollBy(0, offset);
        else if (view instanceof AbsListView)
            ((AbsListView) view).smoothScrollBy(offset, 150);
        else {
            try {
                Method method = view.getClass().getDeclaredMethod("smoothScrollBy", Integer.class, Integer.class);
                method.invoke(view, 0, offset);
            } catch (Exception e) {
                view.scrollBy(0, offset);
            }
        }
    }

    /**
     * 自动刷新数据
     */
    public void refresh() {
        mStrategy.refresh();
    }

    /**
     * 成功
     */
    public void success() {
        finishPull(getContext().getString(
                mStrategy.isRefreshing() ? R.string.refresh_succeed : R.string.load_succeed
        ), true);

    }

    /**
     * 失败
     */
    public void failed() {
        finishPull(getContext().getString(
                mStrategy.isRefreshing() ? R.string.refresh_failed : R.string.load_failed
        ), false);
    }

    /**
     * 拉回
     */
    public void finishPull(CharSequence message, boolean result) {
        mStrategy.finishSwipe(message, result);
    }

    public void setOnPullListener(OnPullCallBackListener listener) {
        this.mListener = listener;
    }

    public int getRScrollY() {
        return -getScrollY();
    }

    public interface OnPullCallBackListener {
        void onRefresh();

        void onLoad();
    }

    public boolean isSwipeDownEnable() {
        return mSwipeDownEnable;
    }

    public void setSwipeDownEnable(boolean swipeDownEnable) {
        mSwipeDownEnable = swipeDownEnable;
    }

    public boolean isSwipeUpEnable() {
        return mSwipeUpEnable;
    }

    public void setSwipeUpEnable(boolean swipeUpEnable) {
        mSwipeUpEnable = swipeUpEnable;
    }
}
