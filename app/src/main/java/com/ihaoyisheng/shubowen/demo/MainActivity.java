package com.ihaoyisheng.shubowen.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * 作者：疏博文 创建于 2016-05-09 22:30
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toRecyclerView(View view) {
        startActivity(new Intent(this, RecyclerViewActivity.class));
    }

    public void toListView(View view) {
        startActivity(new Intent(this, ListViewActivity.class));
    }

}
