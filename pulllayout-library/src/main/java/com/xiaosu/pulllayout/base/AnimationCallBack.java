package com.xiaosu.pulllayout.base;

/**
 * 作者：疏博文 创建于 2016-05-14 10:40
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public abstract class AnimationCallback {
    /**
     * 动画开始
     */
    public void onAnimationStart() {
    }

    /**
     * 动画结束
     */
    public void onAnimationEnd() {
    }

    /**
     * 动画进行中
     *
     * @param fraction 动画执行的程度[0,1]
     */
    public void onAnimation(float fraction) {
    }
}
