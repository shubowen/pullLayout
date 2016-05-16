package com.xiaosu.pulllayout.base;

import android.view.View;

/**
 * 作者：疏博文 创建于 2016-04-28 17:55
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface IView {

    /**
     * @return 当前的view
     */
    View getTargetView();

    /**
     * 拖拽的回调
     *
     * @param scrollY 大于0表示下拉的距离,小于0表示上拉的距离
     * @param enable  head和footer制约的标记
     */
    void onPull(float scrollY, boolean enable);

    /**
     * 手指放开
     *
     * @param scrollY 手指放开时的拖拽距离
     */
    void onFingerUp(float scrollY);

    /**
     * 收尾
     */
    void detach();


    /**
     * 关联IPull
     *
     * @param iPull
     */
    void pullLayout(IPull iPull);

    /**
     * 拉回
     *
     * @param isBeingDragged true表示手指还在拖动状态
     */
    void finishPull(boolean isBeingDragged);

}
