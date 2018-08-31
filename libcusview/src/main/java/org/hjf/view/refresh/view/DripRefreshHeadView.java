package org.hjf.view.refresh.view;

import android.content.Context;
import android.util.AttributeSet;

import org.hjf.view.R;
import org.hjf.view.refresh.BaseAttachView;

final class DripRefreshHeadView extends BaseAttachView {

    private static final float canLoadPointPercent = 0.75f;

    private DripView mDripView;

    public DripRefreshHeadView(Context context) {
        super(context);
    }

    public DripRefreshHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DripRefreshHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        mDripView = findViewById(R.id.drip_view);
    }

    @Override
    public boolean canLoad(int scrollY) {
        return Math.abs(scrollY) >= getMeasuredHeight() * canLoadPointPercent;
    }

    @Override
    public void onMoveTo(int scrollY) {
        super.onMoveTo(scrollY);
        // percent
        if (super.status == BaseAttachView.STATUS_NORMAL) {
            mDripView.updateViewByScrollPercent(1f * Math.abs(scrollY) / getMeasuredHeight());
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        mDripView.updateView4ReadyLoad();
    }

    @Override
    public void onLoading() {
        super.onLoading();
        mDripView.openArrowRotate();
    }

    @Override
    public void onComplete() {
        super.onComplete();
        mDripView.closeArrowRotate();
    }
}
