package com.ihaoyisheng.shubowen.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * 疏博文 新建于 17/1/3.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class FragmentAdapterDemo extends FragmentPagerAdapter {

    public FragmentAdapterDemo(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        FragmentDemo fragment = new FragmentDemo();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position + "-tab";
    }
}
