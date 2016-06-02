package com.ihaoyisheng.shubowen.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaosu.pulllayout.PullLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作者：疏博文 创建于 2016-04-23 09:11
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class RecyclerViewActivity extends BaseActivity {

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.pull_layout)
    PullLayout mPullLayout;
    private InnerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_recycler_list);
        ButterKnife.bind(this);

        mAdapter = new InnerAdapter();
        recyclerView.setAdapter(mAdapter);
        mPullLayout.setOnPullListener(this);

        mPullLayout.autoRefreshOnCreate();
    }

    @Override
    protected PullLayout pullLayout() {
        return mPullLayout;
    }

    @Override
    protected void onLoadInner() {
        mAdapter.notifyDataSetChanged();
    }

    class InnerAdapter extends RecyclerView.Adapter<TextViewHolder> {

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TextViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(TextViewHolder holder, int position) {
            holder.text1.setTextColor(Color.BLACK);
            holder.text1.setText(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    class TextViewHolder extends RecyclerView.ViewHolder {

        @Bind(android.R.id.text1)
        TextView text1;

        public TextViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
            ButterKnife.bind(this, itemView);
        }
    }

}
