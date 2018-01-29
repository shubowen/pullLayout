package com.xiaosu.pulllayout.footer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaosu.pulllayout.R;
import com.xiaosu.pulllayout.base.ILoadFooter;
import com.xiaosu.pulllayout.internal.PathsDrawable;
import com.xiaosu.pulllayout.internal.ProgressDrawable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class SimpleLoadFooter extends RelativeLayout implements ILoadFooter {

    public static String REFRESH_FOOTER_PULLUP = "上拉加载更多";
    public static String REFRESH_FOOTER_RELEASE = "释放立即加载";
    public static String REFRESH_FOOTER_LOADING = "正在加载...";
    public static String REFRESH_FOOTER_FINISH = "加载完成";
    public static String REFRESH_FOOTER_FAILED = "加载失败";

    protected TextView mTitleText;
    protected ImageView mArrowView;
    protected ImageView mProgressView;
    protected PathsDrawable mArrowDrawable;
    protected ProgressDrawable mProgressDrawable;

    protected int mFinishDuration = 500;
    protected int mPaddingTop = 20;
    protected int mPaddingBottom = 20;

    private boolean mFooterThresholdFlag = false;

    //<editor-fold desc="LinearLayout">
    public SimpleLoadFooter(Context context) {
        super(context);
        this.initView(context, null, 0);
    }

    public SimpleLoadFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initView(context, attrs, 0);
    }

    public SimpleLoadFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {

        mTitleText = new TextView(context);
        mTitleText.setId(android.R.id.widget_frame);
        mTitleText.setTextColor(0xff666666);
        mTitleText.setText(REFRESH_FOOTER_PULLUP);

        LayoutParams lpBottomText = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lpBottomText.addRule(CENTER_IN_PARENT);
        addView(mTitleText, lpBottomText);

        LayoutParams lpArrow = new LayoutParams(dip2px(20), dip2px(20));
        lpArrow.addRule(CENTER_VERTICAL);
        lpArrow.addRule(LEFT_OF, android.R.id.widget_frame);
        mArrowView = new ImageView(context);
        addView(mArrowView, lpArrow);

        LayoutParams lpProgress = new LayoutParams((ViewGroup.LayoutParams) lpArrow);
        lpProgress.addRule(CENTER_VERTICAL);
        lpProgress.addRule(LEFT_OF, android.R.id.widget_frame);
        mProgressView = new ImageView(context);
        mProgressView.animate().setInterpolator(new LinearInterpolator());
        addView(mProgressView, lpProgress);

        mProgressView.setVisibility(INVISIBLE);
        mArrowView.setVisibility(VISIBLE);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleLoadFooter);

        lpProgress.rightMargin = ta.getDimensionPixelSize(R.styleable.SimpleLoadFooter_srlDrawableMarginRight, dip2px(20));
        lpArrow.rightMargin = lpProgress.rightMargin;

        lpArrow.width = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableArrowSize, lpArrow.width);
        lpArrow.height = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableArrowSize, lpArrow.height);
        lpProgress.width = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableProgressSize, lpProgress.width);
        lpProgress.height = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableProgressSize, lpProgress.height);

        lpArrow.width = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableSize, lpArrow.width);
        lpArrow.height = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableSize, lpArrow.height);
        lpProgress.width = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableSize, lpProgress.width);
        lpProgress.height = ta.getLayoutDimension(R.styleable.SimpleRefreshHead_srlDrawableSize, lpProgress.height);

        mFinishDuration = ta.getInt(R.styleable.SimpleLoadFooter_srlFinishDuration, mFinishDuration);

        if (ta.hasValue(R.styleable.SimpleLoadFooter_srlDrawableArrow)) {
            mArrowView.setImageDrawable(ta.getDrawable(R.styleable.SimpleLoadFooter_srlDrawableArrow));
        } else {
            mArrowDrawable = new PathsDrawable();
            mArrowDrawable.parserColors(0xff666666);
            mArrowDrawable.parserPaths("M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z");
            mArrowView.setImageDrawable(mArrowDrawable);
            mArrowView.setRotation(180);
        }

        mProgressView.setImageResource(ta.getResourceId(
                R.styleable.SimpleLoadFooter_srlDrawableProgress,
                R.drawable.ic_index_dashboard));

        mTitleText.setTextSize(16);

        ta.recycle();

        if (getPaddingTop() == 0) {
            if (getPaddingBottom() == 0) {
                setPadding(getPaddingLeft(), mPaddingTop = dip2px(20), getPaddingRight(), mPaddingBottom = dip2px(20));
            } else {
                setPadding(getPaddingLeft(), mPaddingTop = dip2px(20), getPaddingRight(), mPaddingBottom = getPaddingBottom());
            }
        } else {
            if (getPaddingBottom() == 0) {
                setPadding(getPaddingLeft(), mPaddingTop = getPaddingTop(), getPaddingRight(), mPaddingBottom = dip2px(20));
            } else {
                mPaddingTop = getPaddingTop();
                mPaddingBottom = getPaddingBottom();
            }
        }

    }

    private int dip2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    @Override
    public View getView(ViewGroup parent) {
        return this;
    }

    boolean isLoading = false;

    @Override
    public void detach() {

    }

    @Override
    public int threshold() {
        return getHeight() * 3;
    }

    @Override
    public void onActive() {
        mArrowView.setVisibility(INVISIBLE);
        mProgressView.setVisibility(VISIBLE);
        mTitleText.setText(REFRESH_FOOTER_LOADING);
        if (mProgressDrawable != null) {
            mProgressDrawable.start();
        } else {
            mProgressView.animate().rotation(36000).setDuration(100000);
        }
    }

    @Override
    public void onUnderThreshold() {
        mArrowView.animate().rotation(180).setDuration(300).start();
        mTitleText.setText(REFRESH_FOOTER_PULLUP);
        mArrowView.setVisibility(VISIBLE);
    }

    @Override
    public void onHidden() {
        mTitleText.setText(REFRESH_FOOTER_PULLUP);
        mArrowView.setRotation(180);
        mArrowView.setVisibility(VISIBLE);
    }

    @Override
    public void onSwipe(boolean active, int dis) {
        int height = getHeight();
        if (0 > dis && -dis >= height && !mFooterThresholdFlag) {
            mFooterThresholdFlag = true;
            onBeyondThreshold();
        } else if (0 > dis && -dis < height && mFooterThresholdFlag) {
            mFooterThresholdFlag = false;
            onUnderThreshold();
        }
    }

    @Override
    public void onResult(CharSequence message, boolean result) {
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
        } else {
            mProgressView.animate().rotation(0).setDuration(300);
        }
        mProgressView.setVisibility(INVISIBLE);
        mTitleText.setText(result ? REFRESH_FOOTER_FINISH : REFRESH_FOOTER_FAILED);
    }

    @Override
    public int delay() {
        return 300;
    }

    @Override
    public void onBeyondThreshold() {
        mArrowView.animate().rotation(0).setDuration(300).start();
        mTitleText.setText(REFRESH_FOOTER_RELEASE);
    }

}
