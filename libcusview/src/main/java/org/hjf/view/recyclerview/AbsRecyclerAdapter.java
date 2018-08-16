package org.hjf.view.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * 给 RecyclerView 的通用显示
 */
public abstract class AbsRecyclerAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    protected Context mContextInAdapter;
    private ArrayList<T> mData;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public AbsRecyclerAdapter(Context context) {
        this.mContextInAdapter = context;
        this.mData = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int position) {
        ViewHolder viewHolder = getViewHolderBuild(position).build(mContextInAdapter, parent);
        if (this.onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AbsRecyclerAdapter.this.onItemClickListener != null) {
                        onItemClickListener.onItemClickListener(position);
                    }
                }
            });
        }
        if (onItemLongClickListener != null) {
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (AbsRecyclerAdapter.this.onItemLongClickListener != null) {
                        return onItemLongClickListener.onItemLongClickListener(position);
                    }
                    return false;
                }
            });
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        bindData2View(holder, this.mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();
    }

    protected abstract ViewHolder.Build getViewHolderBuild(int position);

    protected abstract void bindData2View(ViewHolder holder, T data, int position);

    @Deprecated
    @Override
    public int getItemViewType(int position) {
        return position;
    }

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

    public void addData(int index, @NonNull T data) {
        this.mData.add(index, data);
        this.notifyItemInserted(index);
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
    }

    public void removeData(T data) {
        int position = this.mData.indexOf(data);
        this.removeData(position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}
