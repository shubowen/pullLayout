package com.ihaoyisheng.shubowen.demo.practice

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.ihaoyisheng.shubowen.demo.R
import com.xiaosu.pulllayout.base.IRefreshHead
import pl.droidsonroids.gif.GifImageView

/**
 * 疏博文 新建于 2018/1/17.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */
class GIFHeader(ctx: Context, attr: AttributeSet) :
        IRefreshHead, GifImageView(ctx, attr) {

    override fun onHidden() {

    }

    override fun onBeyondThreshold() {

    }

    override fun delay(): Int = 0

    override fun onResult(message: CharSequence?, result: Boolean) {

    }

    override fun onUnderThreshold() {

    }

    override fun onActive() {

    }

    init {
        setImageResource(R.mipmap.gif_header_repast)
        scaleType = ScaleType.CENTER_CROP
    }

    override fun getView(parent: ViewGroup): View = this

    override fun detach() {

    }

    override fun threshold(): Int = height

}