package org.hjf.view.recyclerview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * For multi layout. Read method note:
 * {@link BaseDataAdapter#getItemLayoutRes(int)} and {@link RecyclerView.Adapter#getItemViewType(int)}
 * <p>
 * For item view holder warp up. Read method note:
 * {@link AbsRecyclerAdapter#getViewHolder(View, int)}
 */
public abstract class AbsRecyclerAdapter<M> extends BaseDataAdapter<M, ViewCacheHolder> {

    private int[] clickIds;
    private OnViewClickListener onItemClickListener;
    private int[] longClickIds;
    private OnViewLongClickListener onItemLongClickListener;

    public AbsRecyclerAdapter(Context context) {
        super(context);
    }

    /**
     * create view holder
     *
     * @param parent       RecyclerView
     * @param itemViewType return by method {@link AbsRecyclerAdapter#getItemViewType(int)}
     * @return {@link RecyclerView.ViewHolder}
     */
    @Override
    public final ViewCacheHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        View layoutView = LayoutInflater.from(mContextInAdapter).inflate(getItemLayoutRes(itemViewType), parent, false);
        final ViewCacheHolder viewCacheHolder = getViewHolder(layoutView, itemViewType);
        // click
        if (this.onItemClickListener != null && clickIds != null) {
            viewCacheHolder.setOnViewClickListener(onItemClickListener, clickIds);
        }
        // long click
        if (onItemLongClickListener != null && longClickIds != null) {
            viewCacheHolder.setOnViewLongClickListener(onItemLongClickListener, longClickIds);
        }
        return viewCacheHolder;
    }

    /**
     * warp up view holder
     *
     * @param layoutView   item layout resource from protected method {@link BaseDataAdapter#getItemLayoutRes(int)}
     * @param itemViewType item type form protected method {@link BaseDataAdapter#getItemViewType(int)}
     * @return YourViewHolder extends {@link BaseViewHolder}
     */
    protected ViewCacheHolder getViewHolder(View layoutView, int itemViewType) {
        return new ViewCacheHolder(layoutView);
    }

    @Override
    public final void onBindViewHolder(ViewCacheHolder holder, int position, List<Object> payloads) {
        this.onBindViewHolder(holder, position);
    }

    @Override
    public final void onBindViewHolder(ViewCacheHolder holder, int position) {

        // for onViewClickListener and onViewLongClickListener
        holder.setItemPosition(position);

        // bind data on view
        onBindViewHolder(holder, getData(position), position);
    }

    /**
     * {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
     * <p>
     * Clicking events should not be set in this method internally.
     * only do bind data into view.
     */
    protected abstract void onBindViewHolder(ViewCacheHolder holder, M data, int position);


    /**
     * Set OnClick for {@link ViewCacheHolder#itemView} contain child views.
     * Before The method {@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)} be called.
     * Before The method {@link AbsRecyclerAdapter#setDataList(List)} be used.
     *
     * @param onClickListener on click listener
     * @param ids             need register click listener all child view id
     */
    public final void setOnViewClickListener(@NonNull OnViewClickListener onClickListener, @IdRes int... ids) {
        this.clickIds = ids;
        this.onItemClickListener = onClickListener;
    }

    /**
     * Set OnLongClick for {@link ViewCacheHolder#itemView} contain child views.
     * Before The method {@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)} be called.
     * Before The method {@link AbsRecyclerAdapter#setDataList(List)} be used.
     *
     * @param onLongClickListener on long click listener
     * @param ids                 need register long click listener all child view id
     */
    public final void setOnViewLongClickListener(@NonNull OnViewLongClickListener onLongClickListener, @IdRes int... ids) {
        this.longClickIds = ids;
        this.onItemLongClickListener = onLongClickListener;
    }
}
