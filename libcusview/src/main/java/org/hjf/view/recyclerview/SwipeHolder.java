package org.hjf.view.recyclerview;

import android.view.View;

/**
 * 侧滑Item
 * 使 {@link android.support.v7.widget.RecyclerView} 的Item具备侧滑功能
 */
public final class SwipeHolder extends ViewHolder {

    private SwipeLayout itemView;

    public SwipeHolder(SwipeLayout itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public View getMenuView() {
        return itemView.getMenuView();
    }

    public View getContentView() {
        return itemView.getContentView();
    }

    public void open() {
        this.itemView.open();
    }

    public void close() {
        this.itemView.close();
    }

    @Override
    protected void setOnViewClickListener(final OnViewClickListener onItemClickListener, int[] clickIds) {
        if (super.onClickListener == null) {
            super.onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (v.getParent() == SwipeHolder.this.getMenuView()) {
                        close();
                    }
                    if (onItemClickListener != null) {
                        onItemClickListener.onViewClickListener(v, SwipeHolder.this.getItemPosition());
                    }
                }
            };
        }
        super.setOnViewClickListener(onItemClickListener, clickIds);
    }
}
