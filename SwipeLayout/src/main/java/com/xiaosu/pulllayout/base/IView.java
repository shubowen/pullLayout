package com.xiaosu.pulllayout.base;

import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：疏博文 创建于 2016-04-28 17:55
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public interface IView {

    /**
     * @return 当前的view
     */
    View getView(ViewGroup parent);


    /**
     * 收尾
     */
    void detach();


    /**
     * @return 拉动的最大位置
     */
    int threshold();


    /**
     * 释放加载动画
     */
    void onActive();

    /**
     * 还原场景
     */
    void onUnderThreshold();

    /**
     * 刷新或者加载的结果回调
     *
     * @param message 提示信息
     * @param result  结果
     */
    void onResult(CharSequence message, boolean result);

    /**
     * @return onResult回调后展示结果的时间
     */
    int delay();

    void onBeyondThreshold();

    /**
     * 界面不可见时调用
     */
    void onHidden();

    /**
     * 滑动过程的回调
     *
     * @param active 是否是激活状态
     * @param dis    滑动的距离
     */
    void onSwipe(boolean active, int dis);
}
