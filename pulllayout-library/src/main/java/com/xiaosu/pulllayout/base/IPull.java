package com.xiaosu.pulllayout.base;

/**
 * 作者：疏博文 创建于 2016-04-28 21:49
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface IPull {

    /**
     * @param callback 动画执行的回调
     */
    void animToStartPosition(AnimationCallback callback);


    /**
     * @param targetY  偏移量
     * @param duration 动画时间
     * @param auto     是否代码调用
     * @param callback 动画执行的回调
     */
    void animToRightPosition(final float targetY, long duration, boolean auto, AnimationCallback callback);

    /**
     * @param targetY  偏移量
     * @param duration 动画时间
     * @param auto     是否代码调用
     */
    void animToRightPosition(final float targetY, long duration, boolean auto);

    /**
     * @param targetY  偏移量
     * @param callback 动画执行的回调
     */
    void animToRightPosition(final float targetY, AnimationCallback callback);

    /**
     * 上拉完成回调
     */
    void pullUpCallback();

    /**
     * 下拉回调
     */
    void pullDownCallback();

    /**
     * 使中间可滚动组件ScrollBy一段距离
     *
     * @param distance
     */
    void targetScrollBy(int distance);
}
