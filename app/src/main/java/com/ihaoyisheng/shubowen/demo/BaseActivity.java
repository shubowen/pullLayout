package com.ihaoyisheng.shubowen.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xiaosu.pulllayout.PullLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：疏博文 创建于 2016-04-23 09:12
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public abstract class BaseActivity extends AppCompatActivity implements PullLayout.OnPullCallBackListener {

    protected List<String> mList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 20; i++) {
            mList.add("this is the " + i + " item");
        }
    }

    public void postDelay(Runnable action, long delayMillis) {
        getWindow().getDecorView().postDelayed(action, delayMillis);
    }

    @Override
    public void onRefresh() {
        postDelay(new Runnable() {
            @Override
            public void run() {
                pullLayout().finishPull();
            }
        }, 3000);
    }

    @Override
    public void onLoad() {
        postDelay(new Runnable() {
            @Override
            public void run() {
                int size = mList.size();
                for (int i = 20; i < size + 20; i++) {
                    mList.add("this is the " + i + " item");
                }
                onLoadInner();
                pullLayout().finishPull();
            }
        }, 3000);
    }

    protected void onLoadInner() {

    }

    protected abstract PullLayout pullLayout();

}
