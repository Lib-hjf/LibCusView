package org.hjf.view.refresh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.hjf.view.refresh.BaseAttachView;

public class DefaultAutoLoadMoreFootView extends BaseAttachView {


    public DefaultAutoLoadMoreFootView(Context context) {
        super(context);
    }

    public DefaultAutoLoadMoreFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultAutoLoadMoreFootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        onNormal();
    }

    @Override
    public boolean canLoad(int scrollY) {
        return false;
    }

    @Override
    public void onNormal() {
        setVisibility(View.GONE);
    }

    @Override
    public void onReady() {
        setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete() {
        setVisibility(View.GONE);
    }
}
