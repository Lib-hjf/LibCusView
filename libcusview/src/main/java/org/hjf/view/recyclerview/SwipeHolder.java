package org.hjf.view.recyclerview;

import android.view.View;

/**
 * 侧滑Item
 * 使 {@link android.support.v7.widget.RecyclerView} 的Item具备侧滑功能
 */
public final class SwipeHolder extends ViewHolder {

    private View menuView, contentView;

    public SwipeHolder(SwipeLayout itemView) {
        super(itemView);
        menuView = itemView.getMenuView();
        contentView = itemView.getContentView();
    }

    public View getMenuView() {
        return menuView;
    }

    public View getContentView() {
        return contentView;
    }

}
