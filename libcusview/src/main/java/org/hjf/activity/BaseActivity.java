package org.hjf.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import org.hjf.view.R;


/**
 * Created by hjf on 2017/1/22.
 * Activity 基类
 *
 * 解决办法
 *  - 横竖屏切换：{@link #onConfigurationChanged(Configuration)}
 */
public class BaseActivity extends AppCompatActivity {

    protected View mBaseLayoutView;
    /**
     * 用于展示 Title 内容的父视图容器
     */
    protected AppBarLayout mAppBarParentLayout;
    /**
     * 用于展示 Content 内容的父视图容器
     */
    protected FrameLayout mContentParentLayout;

    /**
     * 加载框显示时是否能点击界面动作
     */
    private View mProgressLayoutView;

    /**
     * 显示正在加载遮罩的时候，遮罩下控件能否响应用户操作。
     * 默认：不响应用户操作
     */
    private boolean isActionableWhenMaskIsShowing = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniFrameLayoutView();
    }


    /**
     * 初始化显示内容的框架容器 View
     */
    @SuppressLint("InflateParams")
    protected void iniFrameLayoutView() {
        this.mBaseLayoutView = LayoutInflater.from(this).inflate(R.layout.a_base, null);
        this.mAppBarParentLayout = (AppBarLayout) mBaseLayoutView.findViewById(R.id.abl_base_activity_title);
        this.mContentParentLayout = (FrameLayout) mBaseLayoutView.findViewById(R.id.fl_base_activity_content);
    }


    /**
     * 内容视图发生变化时的回调
     * {@link AppCompatActivity#setContentView(int)} 添加内容进入ID标记为 R.id.content 的 View 后，立即回调此方法
     * 1. findViewById(..) 等操作应在此处进行
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }


    /**
     * Activity 横竖屏切换
     *
     * XML 配置 android:configChanges 属性
     *  - 不配置：竖->横【生命周期】X1、横->竖【生命周期】X2
     *  - orientation：竖->横、横->竖【生命周期】X1
     *  - "orientation|keyboardHidden|screenSize"：竖->横、横->竖【onConfigurationChanged() X1】
     *
     * 建议配置第三个，不会重走 Activity 生命周期，而会调起此方法。
     * 可以：{@link #setContentView(int)} --> {@link #onContentChanged()} 重新设置整个布局文件
     * 可以：hide、show 事先准备好的视图
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * 标题视图发生变化时的回调
     */
    public void onTitleChanged(@NonNull BaseTitle baseTitle) {
        this.mAppBarParentLayout.removeAllViews();
        LayoutInflater.from(this).inflate(baseTitle.getTitleResId(), this.mAppBarParentLayout, true);
        baseTitle.onTitleCreated(this.mAppBarParentLayout);
    }

    /**
     * 设置 BaseActivity 的 Title
     * 需要注意的是：有些控件初始化会检测 Content 的Theme是否合法，所以此处最好传 Activity。如 CollapsingToolbarLayout
     * {@link android.support.design.widget.CollapsingToolbarLayout} 调用了 {@link android.support.design.widget.ThemeUtils#checkAppCompatTheme(Context)}
     */
    public void setTitle(@NonNull BaseTitle baseTitle) {
        this.onTitleChanged(baseTitle);
    }

    /**
     * 设置 BaseActivity 的 Content.
     * 拦截不进行 Activity 的默认动作
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View contentView = LayoutInflater.from(this).inflate(layoutResID, this.mContentParentLayout, false);
        this.setContentView(contentView, contentView.getLayoutParams());
    }

    /**
     * 设置 BaseActivity 的 Content.
     * 拦截不进行 Activity 的默认动作
     */
    @Override
    public void setContentView(View view) {
        this.setContentView(view, null);
    }

    /**
     * 设置 BaseActivity 的 Content.
     * 拦截不进行 Activity 的默认动作
     */
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.mContentParentLayout.removeAllViews();
        this.mContentParentLayout.addView(view, params);
        super.setContentView(this.mBaseLayoutView);
    }

    /**
     * 显示Dialog，请在UI线程中运行
     */
    protected void showProgress() {
        if (this.mProgressLayoutView == null) {
            initProgressView();
        }
        this.mProgressLayoutView.setVisibility(View.VISIBLE);
    }


    /**
     * 隐藏Dialog，请在UI线程中运行
     */
    protected void hideProgress() {
        if (this.mProgressLayoutView != null) {
            this.mProgressLayoutView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化 ViewStub 并获取 ProgressView 控件
     */
    private void initProgressView() {
        ViewStub mProgressViewStub = (ViewStub) findViewById(R.id.vs_progress);
        mProgressViewStub.inflate();
        this.mProgressLayoutView = findViewById(R.id.fl_progress);

        // 加载框显示时是否能点击界面动作
        this.mProgressLayoutView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return BaseActivity.this.isActionableWhenMaskIsShowing;
            }
        });
    }

    public void setActionableWhenMask(boolean actionable) {
        this.isActionableWhenMaskIsShowing = actionable;
    }
}
