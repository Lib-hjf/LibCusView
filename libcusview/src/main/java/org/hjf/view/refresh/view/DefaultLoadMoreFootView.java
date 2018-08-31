package org.hjf.view.refresh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.hjf.view.R;
import org.hjf.view.refresh.BaseAttachView;

public class DefaultLoadMoreFootView extends BaseAttachView {

    private static final float canLoadPointPercent = 0.75f;

    private TextView tvHint;

    public DefaultLoadMoreFootView(Context context) {
        super(context);
    }

    public DefaultLoadMoreFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultLoadMoreFootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        tvHint = findViewById(R.id.tv_hint);
    }

    @Override
    public boolean canLoad(int scrollY) {
        return Math.abs(scrollY) >= getMeasuredHeight() * canLoadPointPercent;
    }

    @Override
    public void onNormal() {
        super.onNormal();
        tvHint.setText("上拉刷新数据");
    }

    @Override
    public void onReady() {
        super.onReady();
        tvHint.setText("松手刷新数据");
    }

    @Override
    public void onLoading() {
        super.onLoading();
        tvHint.setText("正在刷新...");
    }

    @Override
    public void onComplete() {
        super.onComplete();
        tvHint.setText("刷新数据完成");
    }
}
