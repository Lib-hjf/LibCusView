package org.hjf.view.recyclerview;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import org.hjf.log.LogUtil;

public class ViewHolder extends RecyclerView.ViewHolder {

    private int itemPosition;
    private SparseArray<View> mViewCache;
    protected View.OnClickListener onClickListener;
    protected View.OnLongClickListener onLongClickListener;

    ViewHolder(View itemView) {
        super(itemView);
        mViewCache = new SparseArray<>();
    }

    /**
     * In order to realize the method {@link AbsRecyclerAdapter#setOnViewClickListener(OnViewClickListener, int...)} and
     * method {@link AbsRecyclerAdapter#setOnViewLongClickListener(OnViewLongClickListener, int...)}
     * <p>
     * suggest ues the method on {@link AbsRecyclerAdapter#onBindViewHolder(ViewHolder, int)}
     *
     * @param itemPosition item position
     */
    void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setText(@IdRes int viewId, String text) {
        TextView textView = getView(viewId);
        textView.setText(text);
    }

    public <T extends View> T getView(@IdRes int viewID) {
        View view = mViewCache.get(viewID);
        if (view == null) {
            view = this.itemView.findViewById(viewID);
            mViewCache.put(viewID, view);
        }
        //noinspection unchecked
        return (T) view;
    }

    protected void setOnViewClickListener(final OnViewClickListener onItemClickListener, int[] clickIds) {
        if (this.onClickListener == null) {
            this.onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onViewClickListener(v, ViewHolder.this.getItemPosition());
                    }
                }
            };
        }
        for (int clickId : clickIds) {
            View view = this.getView(clickId);
            if (view != null) {
                view.setOnClickListener(onClickListener);
            } else {
                LogUtil.e("Not found View, no set click.");
            }
        }
    }

    protected void setOnViewLongClickListener(final OnViewLongClickListener onItemLongClickListener, int[] longClickIds) {
        if (this.onLongClickListener == null) {
            this.onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        return onItemLongClickListener.onViewLongClickListener(v, ViewHolder.this.getItemPosition());
                    }
                    return false;
                }
            };
        }
        for (int longClickId : longClickIds) {
            View view = this.getView(longClickId);
            if (view != null) {
                view.setOnLongClickListener(onLongClickListener);
            } else {
                LogUtil.e("Not found View, no set long click.");
            }
        }
    }
}
