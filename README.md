# pullLayout
一个Android下拉刷新和上拉加载的库,**仿IOS版QQ消息页面水滴下拉刷新效果**

**目前只支持RecyclerView**

![image](https://github.com/shuowen/pullLayout/app/image)

使用方法:需要配合support-v4包使用
    
    <com.xiaosu.pulllayout.PullLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">
    
            <android.support.v7.widget.RecyclerView
                android:id="@+id/recyclerView"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                app:layoutManager="android.support.v7.widget.LinearLayoutManager"/>
    
    </com.xiaosu.pulllayout.PullLayout>
    
代码设置回调监听:mPullLayout.setOnPullListener(this);
下拉和上拉的回调方法:

    public void onRefresh() {
        //处理下拉逻辑
    }

    public void onLoad() {
        //处理上拉逻辑
    }

**待更新...**
