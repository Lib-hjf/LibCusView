package org.hjf.view.recyclerview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Focus only on data changes and data manipulation
 *
 * @param <M>
 * @param <VH>
 */
public abstract class BaseDataAdapter<M, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private static final int DEFAULT_ITEM_VIEW_TYPE = 0;

    protected Context mContextInAdapter;
    private ArrayList<M> mData;

    public BaseDataAdapter(Context mContextInAdapter) {
        this.mContextInAdapter = mContextInAdapter;
        this.mData = new ArrayList<>();
    }

    /**
     * use multi view holder, need override the method for update item view holder
     *
     * @param position item position
     * @return item view type
     */
    @Override
    public int getItemViewType(int position) {
        return DEFAULT_ITEM_VIEW_TYPE;
    }

    /**
     * Get item layout resource according to difference view type.
     * <p>
     * The method be used in child class extends {@link BaseDataAdapter#onCreateViewHolder(ViewGroup, int)}
     *
     * @param itemViewType item type
     * @return item layout resource
     */
    @LayoutRes
    protected abstract int getItemLayoutRes(int itemViewType);

    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();
    }

    public final void clearData() {
        this.mData.clear();
    }

    public final void setDataList(@NonNull List<M> list) {
        this.clearData();
        this.mData.addAll(list);
        this.notifyDataSetChanged();
    }

    public final void addData(@NonNull M data) {
        this.addData(this.mData.size(), data);
    }

    public final void addData(int position, @NonNull M data) {
        this.mData.add(position, data);
        this.notifyItemInserted(position);
        this.notifyItemRangeChanged(position, this.mData.size() - position);
    }

    public final void addDataList(int position, @NonNull List<M> list) {
        this.mData.addAll(position, list);
        this.notifyItemRangeInserted(position, list.size());
        this.notifyItemRangeChanged(position + list.size(), this.mData.size() - position);
    }

    public final void addDataList(@NonNull List<M> list) {
        this.mData.addAll(list);
        this.notifyItemRangeInserted(this.mData.size() - list.size(), list.size());
    }

    @Nullable
    public final M getData(int position) {
        if (position >= mData.size()) {
            return null;
        }
        return this.mData.get(position);
    }

    public final void removeData(int position) {
        if (position < 0 || position >= mData.size()) {
            return;
        }
        this.mData.remove(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, this.mData.size() - position);
    }

    public final void removeData(M data) {
        int position = this.mData.indexOf(data);
        this.removeData(position);
    }
}
