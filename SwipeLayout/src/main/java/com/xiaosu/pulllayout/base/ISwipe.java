package com.xiaosu.pulllayout.base;

/**
 * 作者：疏博文 创建于 2016-04-28 21:49
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface ISwipe {

    /**
     * 上拉完成回调
     */
    void notifyUp();

    /**
     * 下拉回调
     */
    void notifyDown();

    /**
     * 使中间可滚动组件ScrollBy一段距离
     *
     * @param offset 距离
     */
    void smoothScrollBy(int offset);
}
