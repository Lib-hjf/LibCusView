package org.hjf.activity;

import android.support.annotation.LayoutRes;
import android.support.design.widget.AppBarLayout;

/**
 * {@link BaseActivity#setTitle(BaseTitle)}  时需要用到的抽象
 */
public abstract class BaseTitle {

    /**
     * 获取 Title 的 Layout 布局ID
     */
    @LayoutRes
    protected abstract int getTitleResId();

    /**
     * Title Layout布局资源 实例化成 View 并被添加到 {@link BaseActivity#mAppBarParentLayout} 容器后的回调
     */
    protected abstract void onTitleCreated(AppBarLayout appBarLayout);

}
