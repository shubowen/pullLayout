package com.ihaoyisheng.shubowen.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaosu.pulllayout.SimplePullLayout;
import com.xiaosu.pulllayout.base.BasePullLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 疏博文 新建于 17/1/3.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class FragmentDemo extends Fragment implements BasePullLayout.OnPullCallBackListener {

    static final String TAG = "Mr.su";

    RecyclerView mRecyclerView;
    SimplePullLayout mPullLayout;

    List<String> mList = new ArrayList<>();

    private boolean hasGetData = false;
    private int position;
    private boolean mActivityCreated;

    public FragmentDemo() {
        for (int i = 0; i < 10; i++) {
            mList.add("this is the " + i + " item");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lay_recycler_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mPullLayout = (SimplePullLayout) view.findViewById(R.id.pull_layout);

        mPullLayout.setOnPullListener(this);

        position = getArguments().getInt("position");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i(TAG, "onActivityCreated: " + position);
        if (getUserVisibleHint()) {
            mPullLayout.postRefresh();
            Log.i(TAG, position + " : postRefresh in onActivityCreated()");
        } else {
            mActivityCreated = true;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Log.i(TAG, "setUserVisibleHint: " + position + ", " + isVisibleToUser);
        if (isVisibleToUser && mActivityCreated) {
            mPullLayout.postRefresh();
            hasGetData = true;
            Log.i(TAG, position + " : postRefresh in setUserVisibleHint()");
        }
    }

    @Override
    public void onRefresh() {
        postDelay(new Runnable() {
            @Override
            public void run() {
                RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                if (null == adapter) {
                    adapter = new InnerAdapter();
                    mRecyclerView.setAdapter(adapter);
                } else {
                    mList.clear();
                    for (int i = 0; i < 10; i++) {
                        mList.add("this is the new " + i + " item");
                    }
                    adapter.notifyDataSetChanged();
                }
                mPullLayout.succeed();
            }
        }, 1000);
    }

    @Override
    public void onLoad() {

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
            itemView.setBackgroundColor(Color.WHITE);
            ButterKnife.bind(this, itemView);
        }
    }

    public void postDelay(Runnable action, long delayMillis) {
        getActivity().getWindow().getDecorView().postDelayed(action, delayMillis);
    }

}
