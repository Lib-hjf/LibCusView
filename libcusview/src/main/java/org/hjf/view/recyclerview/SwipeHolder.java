package org.hjf.view.recyclerview;

import android.view.View;

/**
 * 侧滑Item
 * 使 {@link android.support.v7.widget.RecyclerView} 的Item具备侧滑功能
 */
public final class SwipeHolder extends ViewHolder {

    private SwipeLayout mSwipeLayout;

    /**
     * use {@link ViewHolder.Build} create object.
     */
    SwipeHolder(SwipeLayout itemView) {
        super(itemView);
        mSwipeLayout = itemView;
    }

    public View getMenuView() {
        return this.mSwipeLayout.getMenuView();
    }

    public View getContentView() {
        return this.mSwipeLayout.getContentView();
    }

}
