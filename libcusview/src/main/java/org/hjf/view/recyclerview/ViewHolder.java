package org.hjf.view.recyclerview;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

public class ViewHolder extends RecyclerView.ViewHolder {

    private int itemPosition;
    private SparseArray<View> mViewCache;

    ViewHolder(View itemView) {
        super(itemView);
        mViewCache = new SparseArray<>();
    }

    /**
     * In order to realize the method {@link AbsRecyclerAdapter#setOnViewClickListener(OnViewClickListener)} and
     * method {@link AbsRecyclerAdapter#setOnViewLongClickListener(OnViewLongClickListener)}
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
}
