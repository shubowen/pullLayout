package com.xiaosu.pulllayout.base;

/**
 * 作者：疏博文 创建于 2016-04-28 17:28
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface IRefreshHead extends IView {

    /**
     * @return true表示正在刷新
     */
    boolean isRefreshing();

    /**
     * 立即切换到刷新状态
     */
    void refreshImmediately();

    /**
     * 代码刷新
     */
    void autoRefresh();
}
