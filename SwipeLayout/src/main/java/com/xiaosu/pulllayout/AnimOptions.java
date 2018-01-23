package com.xiaosu.pulllayout;

import com.xiaosu.pulllayout.base.AnimationCallback;

/**
 * 疏博文 新建于 2018/1/18.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class AnimOptions {

    private float target = 0;//偏移量
    private long duration = 180;//动画时间
    private boolean notify = false;//是否触发回调
    private int delay = 0;//动画延时
    private int smoothScrollBy = 0;//滑动的距离
    private AnimationCallback callback;//动画执行的回调

    private AnimOptions() {
    }

    public float getTarget() {
        return target;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isNotify() {
        return notify;
    }

    public int getDelay() {
        return delay;
    }

    public int getSmoothScrollBy() {
        return smoothScrollBy;
    }

    public AnimationCallback getCallback() {
        return callback;
    }

    public static class Builder {

        AnimOptions options = new AnimOptions();

        public Builder setTarget(float target) {
            options.target = target;
            return this;
        }

        public Builder setDuration(long duration) {
            options.duration = duration;
            return this;
        }

        public Builder setNotify(boolean notify) {
            options.notify = notify;
            return this;
        }

        public Builder setDelay(int delay) {
            options.delay = delay;
            return this;
        }

        public Builder setSmoothScrollBy(int smoothScrollBy) {
            options.smoothScrollBy = smoothScrollBy;
            return this;
        }

        public Builder setCallback(AnimationCallback callback) {
            options.callback = callback;
            return this;
        }

        public AnimOptions get() {
            return options;
        }

    }

}
