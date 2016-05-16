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

}
