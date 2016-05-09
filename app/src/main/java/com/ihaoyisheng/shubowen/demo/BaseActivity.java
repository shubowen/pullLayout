package com.ihaoyisheng.shubowen.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：疏博文 创建于 2016-04-23 09:12
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class BaseActivity extends AppCompatActivity {

    protected List<String> mList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 20; i++) {
            mList.add("this is the " + i + " item");
        }
    }

    public void postDelay(Runnable action, long delayMillis){
        getWindow().getDecorView().postDelayed(action,delayMillis);
    }

}
