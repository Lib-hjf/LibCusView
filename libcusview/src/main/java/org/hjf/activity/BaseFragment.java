package org.hjf.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment 基类，制定基础的使用规范
 */
public abstract class BaseFragment extends Fragment {

    private View mContentView;

    protected BaseActivity mActivityInBaseFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivityInBaseFragment = (BaseActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mContentView = inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView();
    }

    @LayoutRes
    public abstract int getLayoutResId();

    public abstract void bindView();

    protected View findViewById(@IdRes int viewId) {
        return mContentView.findViewById(viewId);
    }

    /**
     * 在 UI 线程中运行
     */
    public void runOnUiThread(Runnable runnable){
        mActivityInBaseFragment.runOnUiThread(runnable);
    }

    /**
     * 显示Dialog，请在UI线程中运行
     */
    public void showProgress() {
        mActivityInBaseFragment.showProgress();
    }

    /**
     * 隐藏Dialog，请在UI线程中运行
     */
    public void hideProgress() {
        mActivityInBaseFragment.hideProgress();
    }

    /**
     * 显示加载 Progress 的时候拦截触摸事件
     */
    public void setActionableWhenMask(boolean actionable) {
        mActivityInBaseFragment.setActionableWhenMask(actionable);
    }
}
