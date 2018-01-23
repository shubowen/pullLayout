package com.xiaosu.pulllayout;

import android.util.Log;

/**
 * 疏博文 新建于 2018/1/17.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class WLog {

    static final boolean enable = true;

    static final String TAG = "SwipeLayout";

    public static void d(String msg) {
        if (enable) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (enable) {
            Log.d(tag, msg);
        }
    }

}
