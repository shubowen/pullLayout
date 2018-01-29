package com.ihaoyisheng.shubowen.demo.practice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ihaoyisheng.shubowen.demo.R;
import com.xiaosu.pulllayout.base.SwipeLayout;

/**
 * 微博列表
 */
public class FeedlistPracticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_feedlist);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.refreshLayout);
        swipeLayout.refresh();
        swipeLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onRefresh() {
                swipeLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.success();
                    }
                }, 3000);
            }

            @Override
            public void onLoad() {
                swipeLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.success();
                    }
                }, 3000);
            }
        });
    }

}
