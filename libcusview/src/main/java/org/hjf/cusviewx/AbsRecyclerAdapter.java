package org.hjf.cusviewx;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 给 RecyclerView 的通用显示
 */
public abstract class AbsRecyclerAdapter<T> extends RecyclerView.Adapter<AbsRecyclerAdapter.ViewHolder> {

    protected Context mContextInAdapter;
    private ArrayList<T> mData;

    public AbsRecyclerAdapter(Context context) {
        this.mContextInAdapter = context;
        this.mData = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(mContextInAdapter).inflate(getLayoutResId(position, this.mData.get(position)), parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        bindData2View(holder, this.mData.get(position), position);
    }


    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();
    }

    protected abstract int getLayoutResId(int position, T data);

    protected abstract void bindData2View(ViewHolder holder, T data, int position);

    @Deprecated
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * ViewHolder
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder{

        private SparseArray<View> mViewCache;

        public ViewHolder(View itemView) {
            super(itemView);
            mViewCache = new SparseArray<>();
        }

        public View getItemView(){
            return ViewHolder.this.itemView;
        }

        public <T extends View>T getView(@IdRes int viewID){
            View view = mViewCache.get(viewID);
            if (view == null){
                view = this.itemView.findViewById(viewID);
                mViewCache.put(viewID, view);
            }
            //noinspection unchecked
            return (T) view;
        }

        public void setText(@IdRes int viewId, String text){
            TextView textView = getView(viewId);
            textView.setText(text);
        }
    }

    public void clearData(){
        this.mData.clear();
    }

    public void setDataList(@NonNull List<T> list){
        clearData();
        this.mData.addAll(list);
        this.notifyDataSetChanged();
    }

    public void addData(int index, @NonNull T data){
        this.mData.add(index, data);
        this.notifyItemInserted(index);
    }

    public void addData(@NonNull T data){
        this.addData(this.mData.size(), data);
    }

    public void addDataList(@NonNull List<T> list){
        this.mData.addAll(list);
        this.notifyItemRangeInserted(this.mData.size() - list.size(), list.size());
    }
}
