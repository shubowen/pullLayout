package com.xiaosu.pulllayout.strategy;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.ScrollerCompat;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.xiaosu.pulllayout.OverScroller;
import com.xiaosu.pulllayout.WLog;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.base.SwipeLayout;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述：基于 requestLayout 和 嵌套滑动等 api 实现header缩放，footer移动的效果
 */

public class FixFrontStrategy extends SimpleStrategy {

    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsBeingDragged = false;
    private float mInitialDownY;

    private float mLastY;

    private int mTouchSlop;

    private static final int MAX_SCROLL_DURATION = 300;

    private static final float DRAG_RATE = .4f;
    private int mSwipeY = 0;

    private final ViewFlinger mViewFlinger = new ViewFlinger();
    private final NestedFlinger mNestedFlinger = new NestedFlinger();

    private final HeaderScrollToTask mHeaderScrollToTask = new HeaderScrollToTask();
    private final FooterScrollToTask mFooterScrollToTask = new FooterScrollToTask();

    private final int mMaxFlingDistance;

    private int mSwipeDownDis = 0;

    private int mSwipeUpDis = 0;

    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    private boolean flingConsumed;

    private boolean isSwipeDown = false;
    private boolean isSwipeUp = false;

    private final int mMinFlingVelocity;
    private boolean isHeaderScrollTo;
    private boolean isFooterScrollTo;

    public FixFrontStrategy(SwipeLayout parent, IRefreshHead header, ILoadFooter footer, View target) {
        super(parent, header, footer, target);

        final ViewConfiguration vc = ViewConfiguration.get(parent.getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();

        mMaxFlingDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200,
                mParent.getResources().getDisplayMetrics());
    }

    @Override
    public boolean swipeBy(int dy) {

        boolean canSwipeDown = canSwipeDown();
        boolean canSwipeUp = canSwipeUp();

        if (canSwipeDown && canSwipeUp) {
            WLog.d("mTarget滚动");
            return false;
        }

//        WLog.d("swipeBy -- [canSwipeDown = " + canSwipeDown + "]-[dy = " + dy + "]-[isSwipeDown = " + isSwipeDown + "]");


        if (isSwipeDown && mSwipeDownDis == 0 && dy < 0) {
            WLog.d("下拉回退的时候，最大只能退到 0 点位置");
            return false;
        }

        if (isSwipeUp && mSwipeUpDis == 0 && dy > 0) {
            WLog.d("上拉拉回退的时候，最大只能退到 0 点位置");
            return false;
        }

        if (canSwipeDown && (mSwipeDownDis > 0 || dy > 0) && !isSwipeDown && !isRefreshing && !isLoading) {
            WLog.d("开始下拉");
            isSwipeDown = true;
        } else if (canSwipeUp && (mSwipeUpDis < 0 || dy < 0) && !isSwipeUp && !isRefreshing) {
            WLog.d("开始上拉");
            isSwipeUp = true;
        }

        if (isSwipeDown) {
            int temp = mSwipeDownDis;
            temp += Math.round(dy * DRAG_RATE);

            //下拉回退的时候，最大只能退到 0 点位置
            mSwipeDownDis = temp < 0 && dy < 0 ? 0 : temp;

            WLog.d("[mSwipeDownDis : " + mSwipeDownDis + "]");
        } else if (isSwipeUp) {
            int temp = mSwipeUpDis;
            temp += Math.round(dy * DRAG_RATE);

            mSwipeUpDis = temp > 0 && dy > 0 ? 0 : temp;

            WLog.d("[mSwipeUpDis : " + mSwipeUpDis + "]");
        }

        reLayout();

        return true;
    }

    private void reLayout() {

        int childPaddingLeft = mParent.getPaddingLeft();
        int childPaddingRight = mParent.getPaddingRight();

        if (isSwipeDown || isHeaderScrollTo) {
            int height = mHeaderView.getHeight();
            int width = mHeaderView.getWidth();

            final int hLeft = (mParent.getMeasuredWidth() - childPaddingLeft - childPaddingRight - width) / 2;
            mHeaderView.layout(hLeft,
                    -height + mSwipeDownDis,
                    hLeft + height,
                    mSwipeDownDis);
        } else if (isSwipeUp || isFooterScrollTo) {
            int childPaddingTop = mParent.getPaddingTop();
            layoutTargetAndFooter(childPaddingLeft, childPaddingTop);
        }

        linkHeaderAndFooterStatus();
    }

    @Override
    public void swipeTo(int y) {

    }

    /**
     * 滑动的时候更新Header或者Footer状态
     */
    private void linkHeaderAndFooterStatus() {
        if (mSwipeDownDis > 0) {
            mHeader.onSwipe(isRefreshing, mSwipeY);
        } else if (mSwipeUpDis < 0) {
            mFooter.onSwipe(isLoading, mSwipeY);
        }
    }

    private boolean canSwipeVertically(int dy) {

        boolean canSwipeDown = canSwipeDown();
        boolean canSwipeUp = canSwipeUp();

        if (mSwipeY == 0 && canSwipeDown && canSwipeUp) {
            WLog.d("canSwipeVertically----[嵌套滚动]----");
            return false;
        }

        if (mSwipeY == 0 && dy < 0 && canSwipeDown) {
            WLog.d("canSwipeVertically----[下拉到顶]----");
            return false;
        }

        if (mSwipeY == 0 && dy > 0 && canSwipeUp) {
            WLog.d("canSwipeVertically----[上拉到顶]----");
            return false;
        }

        return true;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec, -1);
        measureChild(mFooterView, widthMeasureSpec, heightMeasureSpec, -1);
    }

    @Override
    public void onLayout(boolean changed) {

        final View header = mHeaderView;

        int childPaddingLeft = mParent.getPaddingLeft();
        int childPaddingRight = mParent.getPaddingRight();
        int childPaddingTop = mParent.getPaddingTop();


        int headerWidth = header.getMeasuredWidth();
        int headerHeight = header.getMeasuredHeight();

        final int hLeft = (mParent.getMeasuredWidth() - childPaddingLeft - childPaddingRight - headerWidth) / 2;

        header.layout(hLeft,
                mSwipeDownDis - headerHeight,
                hLeft + headerWidth,
                mSwipeDownDis);

        layoutTargetAndFooter(childPaddingLeft, childPaddingTop);
    }

    private void layoutTargetAndFooter(int childPaddingLeft, int childPaddingTop) {
        final int childWidth = mTarget.getMeasuredWidth();
        final int childHeight = mTarget.getMeasuredHeight();

        int tTop = childPaddingTop + mSwipeUpDis;
        int tBottom = tTop + childHeight;

        mTarget.layout(childPaddingLeft, tTop,
                childPaddingLeft + childWidth, tBottom);

        int footerWidth = mFooterView.getMeasuredWidth();
        int footerHeight = mFooterView.getMeasuredHeight();

        mFooterView.layout(childPaddingLeft, tBottom, footerWidth, tBottom + footerHeight);
    }

    @Override
    public int getChildDrawingOrder(int childCount, int i) {
        if (indexOfHeader < 0) {
            return i;
        } else if (i == childCount - 1) {
            return indexOfHeader;
        } else if (i >= indexOfHeader) {
            return i + 1;
        } else {
            return i;
        }
    }

    private void dispatchScroll(Runnable task) {

        if (mSwipeDownDis == 0 && mSwipeUpDis == 0) {
            WLog.d("没有发生拖拽，不滚动");
            return;
        }

        if (isRefreshing && mSwipeDownDis == mHeaderView.getHeight() * 2) {
            WLog.d("正在刷新状态，并且距离刚好在刷新的临界点，不做处理");
            return;
        }

        if (isLoading && -mSwipeUpDis > mFooterView.getHeight()) {
            WLog.d("dispatchScroll--[正在加载中，上拉超过临界点，拉回到临界点]");
            mFooterScrollToTask.scrollTo(-mFooterView.getHeight());

        } else if (isRefreshing && -mSwipeUpDis > mFooterView.getHeight()) {
            WLog.d("dispatchScroll--[正在刷新中，出现上拉，拉回]");
            mFooterScrollToTask.scrollTo(0);

        } else if ((isLoading && mSwipeDownDis > 0)) {
            WLog.d("dispatchScroll--[正在加载中，出现下拉，拉回]");
            mHeaderScrollToTask.scrollTo(0);

        } else if (!isRefreshing && !isLoading) {
            WLog.d("dispatchScroll--[不在刷新或者加载状态下]");
            task.run();
        }
    }

    @Override
    public void finishSwipe(CharSequence message, boolean result) {
        WLog.d("[--finishSwipe--]");
        int delay;
        if (isRefreshing) {
            mHeader.onResult(message, result);
            delay = mHeader.delay();
        } else if (isLoading) {
            mFooter.onResult(message, result);
            delay = mFooter.delay();
        } else {
            WLog.d("静止状态，不做操作");
            return;
        }
        Runnable scrollBackTask = new Runnable() {
            @Override
            public void run() {
                mHeaderScrollToTask.scrollTo(0, new Runnable() {
                    @Override
                    public void run() {
                        if (isRefreshing) {
                            isRefreshing = false;
                            mHeader.onHidden();
                        } else if (isLoading) {
                            isLoading = false;
                            mFooter.onHidden();
                            mParent.smoothScrollBy(mFooterView.getHeight());
                        }
                    }
                });
            }
        };
        if (delay == 0) {
            scrollBackTask.run();
        } else {
            mParent.postDelayed(scrollBackTask, delay);
        }
    }

    @Override
    protected void makeRefreshInternal() {
        mHeaderScrollToTask.scrollTo(mHeaderView.getHeight() * 2,
                new Runnable() {
                    @Override
                    public void run() {
                        isRefreshing = true;
                        mHeader.onActive();
                        mParent.notifyDown();
                    }
                });
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        flingConsumed = (mSwipeDownDis != 0 && !isRefreshing) || mSwipeUpDis != 0;

        WLog.d("onNestedPreFling-[target = " + target.getClass().getSimpleName() + "]-[ " + velocityY + " ]-[flingConsumed : " + flingConsumed + "]");

        if (flingConsumed) {
            int vy = (int) -velocityY;
            mViewFlinger.fling(vy, true);
        }

        return flingConsumed;
    }

    @Override
    public boolean shouldDrawHeader() {
        return mSwipeDownDis > 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        boolean swipeUp = isSwipeDown && dy > 0;
        boolean swipeDown = mSwipeUpDis != 0;

        if (swipeUp || swipeDown) {
            consumed[1] = dy;
        }
    }

    @Override
    public boolean shouldDrawFooter() {
        return -mSwipeUpDis > 0;
    }

    class ViewFlinger implements Runnable {

        int mLastFlingY;
        OverScroller mScroller;
        boolean notify;//越界后通知


        ViewFlinger() {
            mScroller = new OverScroller(mParent.getContext(), new AccelerateDecelerateInterpolator());
        }

        @Override
        public void run() {
            final OverScroller scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                final int dy = y - mLastFlingY;
                mLastFlingY = y;

//                WLog.d("ViewFlinger-[" + dy + "]");

                boolean swiped = swipeBy(dy);

                if (!swiped ||
                        mSwipeDownDis >= mMaxFlingDistance ||
                        -mSwipeUpDis >= mFooterView.getHeight() * 2 ||
                        scroller.isFinished()) {

                    WLog.d("----停止 fling---[" + swiped + "]-" +
                            "[" + (mSwipeDownDis >= mMaxFlingDistance) + "]-" +
                            "[" + (-mSwipeUpDis >= mFooterView.getHeight() * 2) + "]-" +
                            "[" + scroller.isFinished() + "]");

                    flingConsumed = false;
                    scroller.abortAnimation();

                    //下拉后 快速fling回退，退到顶端时将 fling 继续传递到 mTarget
                    int vy = (int) -mScroller.getCurrVelocityY();
//                    WLog.d("停止 fling--[isSwipeDown = " + isSwipeDown + "]-[vy = " + vy + "]");
                    if (isSwipeDown && mSwipeDownDis == 0 && vy > mMinFlingVelocity) {
                        WLog.d("下拉后 快速fling回退，退到顶端时将 fling 继续传递到 mTarget---[" + vy + "]");
                        FixFrontStrategy.this.fling(vy);
                        mNestedFlinger.fling(vy);
                    } else if (isSwipeUp && mSwipeUpDis == 0 && -vy > mMinFlingVelocity) {
                        WLog.d("上拉后 快速fling回退，退到底端时将 fling 继续传递到 mTarget---[" + vy + "]");
                        FixFrontStrategy.this.fling(vy);
                        mNestedFlinger.fling(vy);
                    }

                    isSwipeDown = false;
                    isSwipeUp = false;

                    FixFrontStrategy.this.dispatchScroll(new Runnable() {
                        @Override
                        public void run() {
                            if (notify) {
                                FixFrontStrategy.this.scrollBack();
                            } else if (mSwipeDownDis > 0) {
                                mHeaderScrollToTask.scrollTo(0);
                            } else if (mSwipeUpDis < 0) {
                                mFooterScrollToTask.scrollTo(0);
                            }
                        }
                    });
                } else {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }

        void fling(int velocityY, boolean overNotify) {
            this.notify = overNotify;
            mLastFlingY = 0;
            mScroller.fling(0, 0, 0, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            mParent.removeCallbacks(this);
            ViewCompat.postOnAnimation(mParent, this);
        }
    }

    private void scrollBack() {
        int headerViewHeight = mHeaderView.getHeight();

        if (mSwipeDownDis == 0 && mSwipeUpDis == 0) {
            WLog.d("没有发生拖拽，不滚动");
            return;
        }

        if (mSwipeDownDis > headerViewHeight) {
            WLog.d("下拉大于临界值，滚动到临界值并通知刷新");
            //滚动到刷新位置
            mHeaderScrollToTask.scrollTo(headerViewHeight * 2,
                    new Runnable() {
                        @Override
                        public void run() {
                            isRefreshing = true;
                            WLog.d("通知刷新");
                            mHeader.onActive();
                            mParent.notifyDown();
                        }
                    });
        } else if (mSwipeDownDis > 0) {
            WLog.d("下拉小于临界值，拉回");
            //滚动到刷新位置
            mHeaderScrollToTask.scrollTo(0);
        } else if (-mSwipeUpDis > mFooterView.getHeight()) {
            WLog.d("上拉大于临界值，滚动到临界值并通知加载");
            //滚动到加载位置
            mFooterScrollToTask.scrollTo(-mFooterView.getHeight(),
                    new Runnable() {
                        @Override
                        public void run() {
                            isLoading = true;
                            mFooter.onActive();
                            mParent.notifyUp();
                        }
                    });
        } else if (-mSwipeUpDis > 0) {
            WLog.d("上拉小于临界值，拉回");
            //滚动到加载位置
            mFooterScrollToTask.scrollTo(0);
        }
    }

    class HeaderScrollToTask implements Runnable {

        ScrollerCompat mScroller;
        Runnable mEndTask;

        HeaderScrollToTask() {
            mScroller = ScrollerCompat.create(mParent.getContext(), new FastOutSlowInInterpolator());
        }

        @Override
        public void run() {
            final ScrollerCompat scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                WLog.d("----scrollTo--[" + y + "]");

                mSwipeDownDis = y;
                reLayout();

                if (scroller.isFinished()) {
                    WLog.d("----stop scrollTo---");
                    scroller.abortAnimation();
                    isHeaderScrollTo = false;
                    if (null != mEndTask) mEndTask.run();
                } else {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }

        void scrollTo(int endY, int vy, Runnable endTask) {
            isHeaderScrollTo = true;
            this.mEndTask = endTask;
            int duration = computeScrollDuration(endY, vy);
            WLog.d("scrollTo-[endY : " + endY + "]-[duration : " + duration + "]");

            mScroller.startScroll(0, mSwipeDownDis, 0, endY - mSwipeDownDis, duration);
            ViewCompat.postOnAnimation(mParent, this);
        }

        void scrollTo(int endY) {
            scrollTo(endY, 0, null);
        }

        void scrollTo(int endY, Runnable endTask) {
            scrollTo(endY, 0, endTask);
        }
    }

    class FooterScrollToTask implements Runnable {

        ScrollerCompat mScroller;
        Runnable mEndTask;

        FooterScrollToTask() {
            mScroller = ScrollerCompat.create(mParent.getContext(), new FastOutSlowInInterpolator());
        }

        @Override
        public void run() {
            final ScrollerCompat scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                WLog.d("----scrollTo--[" + y + "]");

                mSwipeUpDis = y;
                reLayout();

                if (scroller.isFinished()) {
                    WLog.d("----stop scrollTo---");
                    scroller.abortAnimation();
                    isFooterScrollTo = false;
                    if (null != mEndTask) mEndTask.run();
                } else {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }

        void scrollTo(int endY, int vy, Runnable endTask) {
            isFooterScrollTo = true;
            this.mEndTask = endTask;
            int duration = computeScrollDuration(endY, vy);
            WLog.d("scrollTo-[endY : " + endY + "]-[duration : " + duration + "]");

            mScroller.startScroll(0, mSwipeUpDis, 0, endY - mSwipeUpDis, duration);
            ViewCompat.postOnAnimation(mParent, this);
        }

        void scrollTo(int endY) {
            scrollTo(endY, 0, null);
        }

        void scrollTo(int endY, Runnable endTask) {
            scrollTo(endY, 0, endTask);
        }
    }

    class NestedFlinger implements Runnable {

        private OverScroller mScroller;

        NestedFlinger() {
            mScroller = new OverScroller(mParent.getContext(), sQuinticInterpolator);
        }

        @Override
        public void run() {
            final OverScroller scroller = mScroller;
            if (scroller.computeScrollOffset()) {

                boolean canSwipeDown = canSwipeDown();
                boolean canSwipeUp = canSwipeUp();

                boolean finished = scroller.isFinished();
                if (finished) {
                    scroller.abortAnimation();
                    WLog.d("---NestedFling正常结束--");
                } else if (canSwipeDown && canSwipeUp) {
//                    WLog.d("--NestedFling--");
                    ViewCompat.postOnAnimation(mParent, this);
                } else if (canSwipeDown || canSwipeUp) {
                    scroller.abortAnimation();
                    int vy = (int) -mScroller.getCurrVelocityY();
                    WLog.d("---NestedFling结束，但是速度没有消耗完--[vy = " + vy + "]");

                    if (Math.abs(vy) > mMinFlingVelocity) {
                        WLog.d("----传递 fling 到 [Parent]---[" + vy + "]-[" + canSwipeDown + "]-[" + canSwipeUp + "]");
                        mViewFlinger.fling(vy, false);
                    }
                }
            }
        }

        void fling(int velocityY) {
            mScroller.fling(0, 0, 0, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            mParent.removeCallbacks(this);
            ViewCompat.postOnAnimation(mParent, this);
        }

    }

    @Override
    public void onNestedFling(View target, float velocityX, float velocityY) {
        WLog.d("--onNestedFling--[" + velocityY + "]");
        mNestedFlinger.fling((int) velocityY);
    }

    private int computeScrollDuration(int dy, int vy) {
        final int absDy = Math.abs(dy);
        final int containerSize = mParent.getHeight();
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * dy / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        if (vy > 0) {
            duration = 4 * Math.round(180 * Math.abs(distance / vy));
        } else {
            float absDelta = (float) (absDy);
            duration = (int) (((absDelta / containerSize) + 1) * 180);
        }
        WLog.d("computeScrollDuration-[" + vy + "]-[" + duration + "]");
        return Math.min(duration, MAX_SCROLL_DURATION);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (action == MotionEvent.ACTION_UP) {
            isSwipeDown = false;
            isSwipeUp = false;
        }

        mParent.invokeSuperDispatchTouchEvent(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);

                break;
            case MotionEvent.ACTION_MOVE:

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float y = ev.getY(pointerIndex);

                final float yDiff = Math.abs(y - mInitialDownY);
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mLastY = y;
                    mIsBeingDragged = true;
                }

                if (mIsBeingDragged) {
                    final int offsetY = Math.round((y - mLastY));
                    mLastY = y;
                    swipeBy(offsetY);
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!flingConsumed) {
                    WLog.d("---手指释放---");
                    dispatchScroll(new Runnable() {
                        @Override
                        public void run() {
                            scrollBack();
                        }
                    });
                }
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return true;
    }
}
