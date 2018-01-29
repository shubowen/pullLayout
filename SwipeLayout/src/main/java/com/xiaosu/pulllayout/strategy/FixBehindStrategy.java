package com.xiaosu.pulllayout.strategy;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.ScrollerCompat;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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

public class FixBehindStrategy extends SimpleStrategy {

    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsBeingDragged = false;
    private float mInitialDownY;

    private float mCurrentY;

    private int mTouchSlop;

    private static final int MAX_SCROLL_DURATION = 300;

    private static final float DRAG_RATE = .4f;
    private int mSwipeY = 0;

    private final ViewFlinger mViewFlinger = new ViewFlinger();
    private final NestedFlinger mNestedFlinger = new NestedFlinger();
    private final ScrollToTask mScrollToTask = new ScrollToTask();

    private final int mMaxFlingDistance;

    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    private boolean flingConsumed;

    public FixBehindStrategy(SwipeLayout parent, IRefreshHead header, ILoadFooter footer, View target) {
        super(parent, header, footer, target);

        final ViewConfiguration vc = ViewConfiguration.get(parent.getContext());
        mTouchSlop = vc.getScaledTouchSlop();

        mMaxFlingDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200,
                mParent.getResources().getDisplayMetrics());
    }


    @Override
    public boolean swipeBy(int dy) {

        boolean canSwipe = canSwipeVertically(dy);
        if (!canSwipe) return false;

        int temp = mSwipeY;
        temp += Math.round(dy * DRAG_RATE);

        //限定最大值和最小值
        mSwipeY = (mSwipeY > 0 && dy < 0 && temp < 0) || (mSwipeY < 0 && dy > 0 && temp > 0) ? 0 : temp;

//        WLog.d("----4----[" + mSwipeY + "]-[" + dy + "]-[" + temp + "]");

        reLayoutTarget();
        linkHeaderAndFooterStatus();
        return true;
    }

    /**
     * 滑动的时候更新Header或者Footer状态
     */
    private void linkHeaderAndFooterStatus() {
        if (mSwipeY > 0) {
            mHeader.onSwipe(isRefreshing, mSwipeY);
        } else if (mSwipeY > 0) {
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
    public void swipeTo(int y) {
        mSwipeY = y;
        reLayoutTarget();
        linkHeaderAndFooterStatus();
    }

    private void reLayoutTarget() {
        int paddingLeft = mParent.getPaddingLeft();
        int paddingTop = mParent.getPaddingTop();
        int height = mTarget.getHeight();
        int width = mTarget.getWidth();
        mTarget.layout(paddingLeft, paddingTop + mSwipeY, paddingLeft + width, paddingTop + mSwipeY + height);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec, -1);
        measureChild(mFooterView, widthMeasureSpec, heightMeasureSpec, -1);
    }

    @Override
    public void onLayout(boolean changed) {

        final View header = mHeaderView;
        final View footer = mFooterView;

        int childPaddingLeft = mParent.getPaddingLeft();
        int childPaddingTop = mParent.getPaddingTop();
        int childPaddingRight = mParent.getPaddingRight();
        int childPaddingBottom = mParent.getPaddingBottom();

        int parentMeasureWidth = mParent.getMeasuredWidth();
        int parentMeasureHeight = mParent.getMeasuredHeight();

        final int childWidth = parentMeasureWidth - childPaddingLeft - childPaddingRight;
        final int childHeight = parentMeasureHeight - childPaddingTop - childPaddingBottom;

        int headerWidth = header.getMeasuredWidth();
        int headerHeight = header.getMeasuredHeight();
        header.layout(childPaddingLeft, childPaddingTop, childPaddingLeft + headerWidth, childPaddingTop + headerHeight);

        mTarget.layout(childPaddingLeft, mSwipeY + childPaddingTop,
                childPaddingLeft + childWidth, mSwipeY + childPaddingTop + childHeight);

        int footerWidth = footer.getMeasuredWidth();
        int footerHeight = footer.getMeasuredHeight();
        int footerBottom = childHeight + childPaddingBottom;
        footer.layout(childPaddingLeft, footerBottom - footerHeight, footerWidth, footerBottom);
    }

    private void makeScroll(Runnable task) {

        if (mSwipeY == 0) return;

        int headerViewHeight = mHeaderView.getHeight();

        if (isRefreshing && mSwipeY > headerViewHeight) {
            mScrollToTask.scrollTo(headerViewHeight);
            WLog.d("makeScroll--[1]");
        } else if (isLoading && -mSwipeY > mFooterView.getHeight()) {
            mScrollToTask.scrollTo(-mFooterView.getHeight());
            WLog.d("makeScroll--[2]");
        } else if ((isRefreshing && mSwipeY < 0) || (isLoading && mSwipeY > 0)) {
            mScrollToTask.scrollTo(0);
            WLog.d("makeScroll--[3]");
        } else if (!isRefreshing && !isLoading) {
            WLog.d("makeScroll--[4]");
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
        mParent.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollToTask.scrollTo(0, new Runnable() {
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
        }, delay);
    }

    @Override
    protected void makeRefreshInternal() {
        mScrollToTask.scrollTo(mHeaderView.getHeight(),
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

        flingConsumed = mSwipeY != 0;

        WLog.d("onNestedPreFling-[target = " + target.getClass().getSimpleName() + "]-[ " + mSwipeY + " ]-[ " + velocityY + " ]-[flingConsumed : " + flingConsumed + "]");

        if (flingConsumed) {
            int vy = (int) -velocityY;
            mViewFlinger.fling(vy, true);
        }

        return flingConsumed;
    }

    @Override
    public boolean shouldDrawHeader() {
        return mSwipeY > 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        if (mSwipeY == 0) return;

        boolean swipeUp = mSwipeY > 0 && dy > 0;
        boolean swipeDown = mSwipeY < 0 && dy < 0;

        if (swipeUp || swipeDown) {
            consumed[1] = dy;
        }
    }

    @Override
    public boolean shouldDrawFooter() {
        return mSwipeY < 0;
    }

    class ViewFlinger implements Runnable {

        int mLastFlingY;
        OverScroller mScroller;
        boolean notify;//越界后通知


        ViewFlinger() {
            mScroller = new OverScroller(mParent.getContext(), sQuinticInterpolator);
        }

        @Override
        public void run() {
            final OverScroller scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                final int dy = y - mLastFlingY;
                mLastFlingY = y;

                boolean swiped = swipeBy(dy);
                if (!swiped ||
                        mSwipeY >= mMaxFlingDistance ||
                        (mSwipeY < 0 && -mSwipeY > mFooterView.getHeight() * 2f) ||
                        scroller.isFinished()) {
                    WLog.d("----stop fling---[" + swiped + "]-[" + (mSwipeY >= mMaxFlingDistance) + "]-[" + ((mSwipeY < 0 && -mSwipeY > mFooterView.getHeight() * 2f)) + "]-[" + scroller.isFinished() + "]");
                    flingConsumed = false;
                    scroller.abortAnimation();

                    int vy = (int) -mScroller.getCurrVelocityY();
                    if (mSwipeY == 0 && Math.abs(vy) > 0) {
                        WLog.d("----将 fling 传递到 [mTarget]---[" + vy + "]");
                        FixBehindStrategy.this.fling(vy);
                        mNestedFlinger.fling(vy);
                    }

                    FixBehindStrategy.this.makeScroll(new Runnable() {
                        @Override
                        public void run() {
                            if (notify) {
                                FixBehindStrategy.this.makeScroll();
                            } else {
                                mScrollToTask.scrollTo(0);
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

    private void makeScroll() {
        int headerViewHeight = mHeaderView.getHeight();
        if (mSwipeY > headerViewHeight) {
            //滚动到刷新位置
            mScrollToTask.scrollTo(headerViewHeight,
                    new Runnable() {
                        @Override
                        public void run() {
                            isRefreshing = true;
                            mHeader.onActive();
                            mParent.notifyDown();
                        }
                    });
        } else if (-mSwipeY > mFooterView.getHeight()) {
            //滚动到加载位置
            mScrollToTask.scrollTo(-mFooterView.getHeight(),
                    new Runnable() {
                        @Override
                        public void run() {
                            isLoading = true;
                            mFooter.onActive();
                            mParent.notifyUp();
                        }
                    });
        } else {
            mScrollToTask.scrollTo(0);
        }
    }

    class ScrollToTask implements Runnable {

        ScrollerCompat mScroller;
        Runnable mEndTask;

        ScrollToTask() {
            mScroller = ScrollerCompat.create(mParent.getContext(), new FastOutSlowInInterpolator());
        }

        @Override
        public void run() {
            final ScrollerCompat scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                swipeTo(y);
                if (scroller.isFinished()) {
                    WLog.d("----stop scrollTo---");
                    scroller.abortAnimation();

                    if (null != mEndTask) mEndTask.run();
                } else {
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        }

        void scrollTo(int endY, int vy, Runnable endTask) {
            this.mEndTask = endTask;
            int duration = computeScrollDuration(endY, vy);
            WLog.d("scrollTo-[endY : " + endY + "]-[duration : " + duration + "]");
            mScroller.startScroll(0, mSwipeY, 0, endY - mSwipeY, duration);
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
                    WLog.d("---NestedFling finish--");
                } else if (canSwipeDown && canSwipeUp) {
                    WLog.d("--NestedFling--");
                    ViewCompat.postOnAnimation(mParent, this);
                } else if (canSwipeDown || canSwipeUp) {
                    scroller.abortAnimation();
                    int vy = (int) -mScroller.getCurrVelocityY();
                    WLog.d("----传递 fling 到 [Parent]---[" + vy + "]-[" + canSwipeDown + "]-[" + canSwipeUp + "]");
                    //需要给 fling 添加一个距离限制
                    mViewFlinger.fling(vy, false);
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
        WLog.d("--onNestedFling--");
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
                    mCurrentY = y;
                    mIsBeingDragged = true;
                }

                if (mIsBeingDragged) {
                    final int offsetY = Math.round((y - mCurrentY));
                    mCurrentY = y;
                    swipeBy(offsetY);
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mSwipeY != 0 && !flingConsumed) {
                    WLog.d("---手指释放---");
                    makeScroll(new Runnable() {
                        @Override
                        public void run() {
                            makeScroll();
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
