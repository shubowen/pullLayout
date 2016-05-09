package com.xiaosu.pulllayout;

import android.view.View;

import com.xiaosu.pulllayout.PullLayout.OnPullCallBackListener;

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
     * 重置状态
     */
    void reset();

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

    /**
     * @param mListener 拖动的回调.这个必须在子类实现,因为pullLayout并不知道什么时候执行操作
     */
    void setOnPullListener(OnPullCallBackListener mListener);
}
