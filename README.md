# pullLayout
一个Android下拉刷新和上拉加载的库

基于官方**SwipeRefreshLayout**修改,并添加上拉功能,可自定义头部和尾部

**目前只支持RecyclerView(LinearLayoutManager-VERTICAL模式)**

![image](https://github.com/shubowen/pullLayout/blob/master/app/image.gif)

**使用方法**:

项目根目录的build.gradle添加

    allprojects {
        repositories {
            jcenter()
            maven {
                url "http://117.78.40.97:9003/nexus/content/repositories/releases/"
            }
        }
    }

在项目build.gradle 文件中添加依赖:
    
    compile 'com.xiaosu:pullLayout:3.0.4'
    
布局文件中使用:
    
    <com.xiaosu.pulllayout.SimplePullLayout
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

代码拉回:

    mPullLayout.finishPull();

**很多的时候我们需要在列表界面第一次进入的时候显示刷新头的加载动画,这个时候,在设置了PullLayout回调监听的前提下,只要在OnCreate中调用一行代码:**

    mPullLayout.postRefresh();

**就可实现,该方法会回调onRefresh()方法,不需要做其他的任何操作.界面绘制完成的情况在如果要代码刷新界面可调用:**
    
    mPullLayout.autoRefresh();

**该方法会回调onRefresh()方法**

1.布局中使用
    
    <com.xiaosu.pulllayout.SimplePullLayout
             android:layout_width="match_parent"
             android:layout_height="match_parent">

2.禁止下拉(默认开启):app:pullDownEnable="false"或者代码setPullDownEnable(false)
    
    <com.xiaosu.pulllayout.PullLayout
            android:id="@+id/pull_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:pullDownEnable="false">

3.禁止上拉(默认开启):app:pullUpEnable="false"或者代码setPullUpEnable(false)

    <com.xiaosu.pulllayout.PullLayout
            android:id="@+id/pull_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:pullUpEnable="false">

**自定义头部和尾部**:自定义头部需继承IRefreshHead接口,尾部需继承ILoadFooter接口
下面对这两个接口的方法做一些简单的说明,具体请参考WaterDropView和FooterView类实现
    
    pullLayout(IPull iPull) //方法目的是将PullLayout暴露出来,因为头部和尾部必须要操纵PullLayout收放
    onPull(float scrollY, boolean enable) //方法将当前手指拖动的距离暴露出来,可根据需求实现相应的逻辑
    onFingerUp(float scrollY) //方法将手指松开时PullLayout拉动的距离暴露出来,可根据需求实现相应的逻辑
    finishPull(boolean isBeingDragged) //表示一个完整的下拉或者上拉已经结束,isBeingDragged表示手指是否还在拖拽
    ......
    更多方法请参考接口说明,已经标注的很清楚
    
最后接口的实现逻辑都完成,可以跟我一样继承BasePullLayout在构造函数中调用
    
    attachHeadView(IRefreshHead head)关联头部
    attachFooterView(ILoadFooter footer)关联尾部

**如果还有不明白的地方或者问题,欢迎在issues上提出
**如果哪位大牛有什么修改意见,非常欢迎**
