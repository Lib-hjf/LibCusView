package org.hjf.view.recyclerview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hjf.log.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 给 RecyclerView 的通用显示
 */
public abstract class AbsRecyclerAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private static final int DEFAULT_ITEM_VIEW_TYPE = 1;


    protected Context mContextInAdapter;
    private ArrayList<T> mData;

    private int[] clickIds;
    private OnViewClickListener onItemClickListener;
    private int[] longClickIds;
    private OnViewLongClickListener onItemLongClickListener;

    public AbsRecyclerAdapter(Context context) {
        this.mContextInAdapter = context;
        this.mData = new ArrayList<>();
    }

    /**
     * create view holder
     *
     * @param parent       RecyclerView
     * @param itemViewType return by method {@link AbsRecyclerAdapter#getItemViewType(int)}
     * @return {@link RecyclerView.ViewHolder}
     */
    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        View layoutView = LayoutInflater.from(mContextInAdapter).inflate(getLayoutRes(itemViewType), parent, false);
        final ViewHolder viewHolder = getViewHolder(layoutView, itemViewType);

        // click
        if (this.onItemClickListener != null && clickIds != null) {
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onViewClickListener(v, viewHolder.getItemPosition());
                    }
                }
            };
            for (int clickId : clickIds) {
                View view = viewHolder.getView(clickId);
                if (view != null) {
                    view.setOnClickListener(onClickListener);
                } else {
                    LogUtil.e("Not found View, no set click.");
                }
            }
        }
        // long click
        if (onItemLongClickListener != null && longClickIds != null) {
            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        return onItemLongClickListener.onViewLongClickListener(v, viewHolder.getItemPosition());
                    }
                    return false;
                }
            };
            for (int longClickId : longClickIds) {
                View view = viewHolder.getView(longClickId);
                if (view != null) {
                    view.setOnLongClickListener(onLongClickListener);
                } else {
                    LogUtil.e("Not found View, no set long click.");
                }
            }
        }
        return viewHolder;
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {

        // for onViewClickListener and onViewLongClickListener
        holder.setItemPosition(position);

        onBindViewHolder(holder, this.mData.get(position), position);
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
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

    @LayoutRes
    protected abstract int getLayoutRes(int itemViewType);

    protected ViewHolder getViewHolder(View layoutView, int itemViewType) {
        return new ViewHolder(layoutView);
    }

    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();
    }

    /**
     * {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
     * <p>
     * Clicking events should not be set in this method internally.
     * only do bind data into view.
     */
    protected abstract void onBindViewHolder(ViewHolder holder, T data, int position);


    public void clearData() {
        this.mData.clear();
    }

    public void setDataList(@NonNull List<T> list) {
        this.clearData();
        this.mData.addAll(list);
        this.notifyDataSetChanged();
    }

    public void addData(@NonNull T data) {
        this.addData(this.mData.size(), data);
    }

    public void addData(int position, @NonNull T data) {
        this.mData.add(position, data);
        this.notifyItemInserted(position);
        this.notifyItemRangeChanged(position, this.mData.size() - position);
    }

    public void addDataList(int position, @NonNull List<T> list) {
        this.mData.addAll(position, list);
        this.notifyItemRangeInserted(position, list.size());
        this.notifyItemRangeChanged(position + list.size(), this.mData.size() - position);
    }

    public void addDataList(@NonNull List<T> list) {
        this.mData.addAll(list);
        this.notifyItemRangeInserted(this.mData.size() - list.size(), list.size());
    }

    @Nullable
    public T getData(int position) {
        if (position >= mData.size()) {
            return null;
        }
        return this.mData.get(position);
    }

    public void removeData(int position) {
        if (position < 0 || position >= mData.size()) {
            return;
        }
        this.mData.remove(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, this.mData.size() - position);
    }

    public void removeData(T data) {
        int position = this.mData.indexOf(data);
        this.removeData(position);
    }

    /**
     * Set OnClick for {@link ViewHolder#itemView} contain child views.
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
     * Set OnLongClick for {@link ViewHolder#itemView} contain child views.
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
