package org.hjf.view.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.hjf.log.LogUtil;

public abstract class BaseAttachView extends RelativeLayout implements IAttachView {

    protected static final int STATUS_NORMAL = 1;
    protected static final int STATUS_READY = 2;
    protected static final int STATUS_LOADING = 3;
    protected static final int STATUS_COMPLETE = 4;

    protected int status = STATUS_NORMAL;

    public BaseAttachView(Context context) {
        this(context, null);
    }

    public BaseAttachView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseAttachView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected abstract void initView();

    public void onMoveTo(int scrollY) {
        LogUtil.d("ScrollY = {0}", scrollY);
        //   ready
        if (status == STATUS_NORMAL && canLoad(scrollY)) {
            onReady();
        }
        // reset
        else if (scrollY == 0) {
            onNormal();
        }
    }

    @Override
    public void onNormal() {
        status = STATUS_NORMAL;
    }

    @Override
    public void onReady() {
        status = STATUS_READY;
    }

    @Override
    public void onLoading() {
        status = STATUS_LOADING;
    }

    @Override
    public void onComplete() {
        status = STATUS_COMPLETE;
    }

}
