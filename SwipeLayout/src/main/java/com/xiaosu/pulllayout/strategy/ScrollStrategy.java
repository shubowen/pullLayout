package com.xiaosu.pulllayout.strategy;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.xiaosu.pulllayout.AnimOptions;
import com.xiaosu.pulllayout.WLog;
import com.xiaosu.pulllayout.base.AnimationCallback;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.base.IRefreshHead;
import com.xiaosu.pulllayout.base.SwipeLayout;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class ScrollStrategy extends SimpleStrategy {

    private static final float DRAG_RATE = .6f;

    public ScrollStrategy(SwipeLayout parent, IRefreshHead header, ILoadFooter footer, View target) {
        super(parent, header, footer, target);
    }

    @Override
    public boolean shouldDrawHeader() {
        return mParent.getScrollY() < 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int rScrollY = -mParent.getScrollY();
        boolean swipeDownEnable = mParent.isSwipeDownEnable();
        boolean swipeUpEnable = mParent.isSwipeUpEnable();
        if (dy > 0 && rScrollY > 0 && swipeDownEnable) {
            if (dy > rScrollY) {
                consumed[1] = Math.round(dy - rScrollY);
            } else {
                consumed[1] = dy;
            }
            swipeBy(Math.round(dy * DRAG_RATE));
        } else if (dy < 0 && rScrollY < 0 && swipeUpEnable) {
            if (dy < rScrollY) {
                consumed[1] = Math.round(dy - rScrollY);
            } else {
                consumed[1] = dy;
            }

            swipeBy(Math.round(dy * DRAG_RATE));
        }
    }

    @Override
    protected void makeRefreshInternal() {
        View hv = mHeader.getView(mParent);
        animToRefresh(hv.getHeight());
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    private void animToRefresh(int height) {
        AnimOptions options = new AnimOptions.Builder()
                .setNotify(true)
                .setTarget(height)
                .setCallback(new AnimationCallback() {
                    @Override
                    public void onAnimationEnd() {
                        mHeader.onActive();
                    }
                })
                .get();
        animTo(options);
    }

    @Override
    public boolean shouldDrawFooter() {
        return mParent.getScrollY() > 0;
    }

    private void animToStart(AnimOptions options) {
        animTo(options);
    }

    private void animTo(AnimOptions options) {

        final float targetY = options.getTarget();
        long duration = options.getDuration();
        final boolean notify = options.isNotify();
        final int delay = options.getDelay();
        final int smoothScrollBy = options.getSmoothScrollBy();
        final AnimationCallback callback = options.getCallback();


        final int scrollY = -mParent.getScrollY();
        if (targetY == scrollY) {
            if (null != callback) {
                callback.onAnimationStart();
                callback.onAnimationEnd();
            }
            return;
        }

        Animation animation = new SimpleAnimation(scrollY, targetY, callback, notify, smoothScrollBy);

        animation.setStartOffset(delay);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(duration);

        mParent.clearAnimation();
        mParent.startAnimation(animation);
    }

    @Override
    public void finishSwipe(CharSequence message, boolean result) {
        if (isRefreshing) {
            mHeader.onResult(message, result);
            AnimOptions options = new AnimOptions.Builder()
                    .setDelay(mHeader.delay())
                    .setCallback(new AnimationCallback() {
                        @Override
                        public void onAnimationEnd() {
                            isRefreshing = false;
                            mHeader.onHidden();
                        }
                    })
                    .get();
            animToStart(options);
        } else if (isLoading) {
            mFooter.onResult(message, result);
            View footerView = mFooter.getView(mParent);
            AnimOptions options = new AnimOptions.Builder()
                    .setSmoothScrollBy(footerView.getHeight())
                    .setNotify(true)
                    .setDelay(mFooter.delay())
                    .setCallback(new AnimationCallback() {
                        @Override
                        public void onAnimationEnd() {
                            isLoading = false;
                            mFooter.onHidden();
                        }
                    })
                    .get();

            animToStart(options);
        }
    }

    @Override
    public boolean swipeBy(int dy) {

        /*boolean swipeDownEnable = mParent.isSwipeDownEnable();
        boolean swipeUpEnable = mParent.isSwipeUpEnable();

        int scrollY = -mParent.getScrollY();
        int state = mParent.getState();

        int wScrollY = scrollY - dy;
        *//*限制下拉和下拉同时出现*//*
        *//*switch (state) {
            case SwipeLayout.STATE_PULL_DOWN:
                dy = wScrollY < 0 || !swipeDownEnable  ? scrollY : dy;
                break;
            case SwipeLayout.STATE_PULL_UP:
                dy = wScrollY > 0 || !swipeUpEnable  ? scrollY : dy;
                break;
        }*//*
        *//*在加载的过程中，限制拉动的距离*//*
        *//*if (mHeader.isRefreshing()) {
            int distance = mHeader.threshold();
            dy = wScrollY > distance ? scrollY - distance : dy;
        } else if (mFooter.isLoading()) {
            int distance = mFooter.threshold();
            dy = -wScrollY > distance ? scrollY + distance : dy;
        }*/

        mParent.scrollBy(0, dy);

        linkage();

        return true;
    }

    private boolean footerPreActiveFlag = false;
    private boolean headerPreActiveFlag = false;

    private void linkage() {

        if (isRefreshing || isLoading) {
            return;
        }

        int cScrollY = -mParent.getScrollY();
        if (cScrollY >= 0) {
            View headerView = mHeader.getView(mParent);
            int height = headerView.getHeight();
            if (cScrollY > height && !headerPreActiveFlag) {
                headerPreActiveFlag = true;
                mHeader.onBeyondThreshold();
            } else if (cScrollY <= height && headerPreActiveFlag) {
                headerPreActiveFlag = false;
                mHeader.onUnderThreshold();
            }
        } else {
            View footerView = mFooter.getView(mParent);
            int height = footerView.getHeight();
            if (-cScrollY > height && !footerPreActiveFlag) {
                footerPreActiveFlag = true;
                mFooter.onBeyondThreshold();
            } else if (-cScrollY <= height && footerPreActiveFlag) {
                footerPreActiveFlag = false;
                mFooter.onUnderThreshold();
            }
        }
    }

    @Override
    public void swipeTo(int y) {
        mParent.scrollTo(0, -y);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View header = mHeader.getView(mParent);
        View footer = mFooter.getView(mParent);
        measureChild(header, widthMeasureSpec, heightMeasureSpec, -1);
        measureChild(footer, widthMeasureSpec, heightMeasureSpec, -1);
    }

    @Override
    public void onLayout(boolean changed,
                         int childPaddingLeft, int childPaddingTop, int childPaddingRight, int childPaddingBottom,
                         int parentMeasureWidth, int parentMeasureHeight,
                         View target) {

        View header = mHeader.getView(mParent);
        View footer = mFooter.getView(mParent);

        final int childWidth = parentMeasureWidth - childPaddingLeft - childPaddingRight;
        final int childHeight = parentMeasureHeight - childPaddingTop - childPaddingBottom;

        int footerWidth = footer.getMeasuredWidth();
        int footerHeight = footer.getMeasuredHeight();

        int headerWidth = header.getMeasuredWidth();
        int headerHeight = header.getMeasuredHeight();

        target.layout(childPaddingLeft, childPaddingTop,
                childPaddingLeft + childWidth, childPaddingTop + childHeight);

        int footerTop = childHeight + childPaddingBottom;
        footer.layout(0, footerTop, footerWidth, footerTop + footerHeight);

        int headerTop = -headerHeight + childPaddingTop;

        header.layout(0, headerTop, headerWidth, childPaddingTop);
    }

    public void onFingerRelease() {

        int scrollY = -mParent.getScrollY();
        if (scrollY == 0) {
            return;
        }

        if ((scrollY > 0 && isLoading) || (scrollY < 0 && isRefreshing)) {
            animToStart(new AnimOptions.Builder().get());
            WLog.d("--刷新或者加载状态下，反向拉动视为无效拉动，自动拉回--");
        } else if (scrollY > 0) {
            View headerView = mHeader.getView(mParent);
            int height = headerView.getHeight();
            if (scrollY >= height) {
                if (isRefreshing) {
                    WLog.d("--下拉距离已到临界点，但是正在刷新，自动拉回--");
                    AnimOptions options = new AnimOptions.Builder()
                            .setTarget(height)
                            .get();
                    animTo(options);
                } else {
                    WLog.d("--下拉距离已到临界点，自动拉回到临界点，触发刷新回调--");
                    isRefreshing = true;
                    animToRefresh(height);
                }
            } else if (!isRefreshing) {
                WLog.d("下拉距离没到临界点，自动拉回");
                AnimOptions options = new AnimOptions.Builder()
                        .setCallback(new AnimationCallback() {
                            @Override
                            public void onAnimationEnd() {
                                mHeader.onHidden();
                            }
                        })
                        .get();
                animToStart(options);
            }
        } else {
            View footerView = mFooter.getView(mParent);
            int height = footerView.getHeight();
            if (-scrollY >= height) {
                if (isLoading) {
                    WLog.d("--4--");
                    AnimOptions options = new AnimOptions.Builder()
                            .setTarget(-height)
                            .get();
                    animTo(options);
                } else {
                    WLog.d("--5--");
                    isLoading = true;
                    AnimOptions options = new AnimOptions.Builder()
                            .setNotify(true)
                            .setTarget(-height)
                            .setCallback(new AnimationCallback() {
                                @Override
                                public void onAnimationEnd() {
                                    mFooter.onActive();
                                }
                            })
                            .get();
                    animTo(options);

                }
            } else if (!isLoading) {
                WLog.d("--6--");
                AnimOptions options = new AnimOptions.Builder()
                        .setCallback(new AnimationCallback() {
                            @Override
                            public void onAnimationEnd() {
                                mFooter.onHidden();
                            }
                        })
                        .get();
                animToStart(options);
            }
        }
    }

    private class SimpleAnimation extends Animation {

        private final int mScrollY;
        private final float mTargetY;
        private final AnimationCallback mCallback;
        private final boolean mNotify;
        private final int mSmoothScrollBy;

        boolean ended;
        boolean started;

        SimpleAnimation(int scrollY, float targetY, AnimationCallback callback, boolean notify, int smoothScrollBy) {
            mScrollY = scrollY;
            mTargetY = targetY;
            mCallback = callback;
            mNotify = notify;
            mSmoothScrollBy = smoothScrollBy;
            ended = false;
            started = false;
        }

        @Override
        protected void applyTransformation(float fraction, Transformation t) {

            int y = evaluate(fraction, mScrollY, mTargetY);
            swipeTo(y);

            if (0 == fraction && !started) {
//                WLog.d("animToStart-onAnimationStart");
                if (null != mCallback) mCallback.onAnimationStart();
                started = true;
            }

            if (null != mCallback) mCallback.onAnimation(fraction);

            if (1.0f == fraction && !ended) {
                if (null != mCallback) mCallback.onAnimationEnd();
//                WLog.d("animToStart-onAnimationEnd");

                if (mNotify) {
                    if (mTargetY < 0) {
                        mParent.notifyUp();
                    } else {
                        mParent.notifyDown();
                    }
                }
                if (mSmoothScrollBy != 0) {
                    mParent.smoothScrollBy(mSmoothScrollBy);
                }
                ended = true;
            }
        }
    }
}
