package com.ihaoyisheng.shubowen.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.xiaosu.lib.base.widget.PagerSlidingTabStrip;

/**
 * 疏博文 新建于 17/1/3.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class ViewPagerActivity extends AppCompatActivity {

    PagerSlidingTabStrip mTab;
    ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followup_residents);
        findView();
    }

    private void findView() {
        mTab = (PagerSlidingTabStrip) findViewById(R.id.tab);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mViewPager.setAdapter(new FragmentAdapterDemo(getSupportFragmentManager()));
        mTab.setViewPager(mViewPager);
    }
}
