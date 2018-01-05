package org.hjf.activity;

import android.annotation.SuppressLint;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;

import org.hjf.view.R;

/**
 * 带底部导航栏的 Activity 抽象类
 */
public class BaseActivityNavigationBar extends BaseActivity {

    /**
     * 底部导航栏占位控件
     */
    private ViewStub vsNavigationBarParentLayout;
    private View mNavigationBar;

    @Override
    @SuppressLint("InflateParams")
    protected void iniFrameLayoutView() {
        this.mBaseLayoutView = LayoutInflater.from(this).inflate(R.layout.a_base_navigation_bar, null);
        this.mAppBarParentLayout = mBaseLayoutView.findViewById(R.id.abl_base_activity_title);
        this.mContentParentLayout = mBaseLayoutView.findViewById(R.id.fl_base_activity_content);
        this.vsNavigationBarParentLayout = mBaseLayoutView.findViewById(R.id.vs_base_activity_navigation_bar);
    }


    /**
     * 设置底部导航栏
     */
    public View setNavigationBarLayout(@LayoutRes int layoutResId) {

        if (this.mNavigationBar == null) {
            this.vsNavigationBarParentLayout.setLayoutResource(layoutResId);
            this.mNavigationBar = this.vsNavigationBarParentLayout.inflate();
            this.mNavigationBar.setTag(layoutResId);
            this.mNavigationBar.setVisibility(View.VISIBLE);
        }

        return this.mNavigationBar;
    }
}
