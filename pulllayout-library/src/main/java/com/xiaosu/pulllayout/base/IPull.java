package com.xiaosu.pulllayout.base;

/**
 * 作者：疏博文 创建于 2016-04-28 21:49
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface IPull {

    /**
     * @param reset  true表示重置
     */
    void animToStartPosition(final boolean reset);

    /**
     * @param targetY       偏移量
     * @param isRefreshing true表示正在刷新
     * @param notify       true表示使刷新
     */
    void animToRightPosition(final float targetY, boolean isRefreshing, final boolean notify);

}
