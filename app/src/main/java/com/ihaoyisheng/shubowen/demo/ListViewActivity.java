package com.ihaoyisheng.shubowen.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaosu.pulllayout.PullLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作者：疏博文 创建于 2016-05-09 18:46
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class ListViewActivity extends BaseActivity {


    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.pull_layout)
    PullLayout pullLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_list_view);
        ButterKnife.bind(this);
        pullLayout.setOnPullListener(this);
        listView.setAdapter(new InnerAdapter());
    }

    @Override
    protected PullLayout pullLayout() {
        return pullLayout;
    }

    class InnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView text = (TextView) convertView;
            text.setText(mList.get(position));
            return convertView;
        }
    }

}
