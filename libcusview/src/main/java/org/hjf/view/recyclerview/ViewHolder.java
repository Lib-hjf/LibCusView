package org.hjf.view.recyclerview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hjf.view.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViewCache;

    ViewHolder(View itemView) {
        super(itemView);
        mViewCache = new SparseArray<>();
    }

    public View getItemView() {
        return ViewHolder.this.itemView;
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

    public void setText(@IdRes int viewId, String text) {
        TextView textView = getView(viewId);
        textView.setText(text);
    }


    /**
     * 建造者
     */
    public static final class Build {

        @SwipeModel
        private int swipeModel = SwipeModel.NONE;

        @LayoutRes
        private int layoutRes;

        @LayoutRes
        private int swipeMenuLayoutRes = R.layout.item_swipe_menu_delete;

        public Build setSwipeModel(@SwipeModel int swipeModel) {
            this.swipeModel = swipeModel;
            return this;
        }

        // TODO
        public Build setSwipeMenuLayoutRes(@LayoutRes int layoutRes) {
            this.swipeMenuLayoutRes = layoutRes;
            return this;
        }

        public Build setLayoutRes(@LayoutRes int layoutRes) {
            this.layoutRes = layoutRes;
            return this;
        }

        public ViewHolder build(Context context, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            // 滑动菜单的 Item
            if (swipeModel != SwipeModel.NONE) {
                SwipeLayout swipeLayout = new SwipeLayout(context);
                swipeLayout.setSwipeModel(swipeModel);
                View contentView = inflater.inflate(layoutRes, swipeLayout, false);
                View menuView = inflater.inflate(swipeMenuLayoutRes, swipeLayout, false);
                swipeLayout.setView(contentView, menuView);
                return new SwipeHolder(swipeLayout);
            }
            // 普通的Item
            else {
                View view = inflater.inflate(layoutRes, parent, false);
                return new ViewHolder(view);
            }
        }
    }


    /**
     * 滑动模式
     */
    @IntDef({
            SwipeModel.NONE,
            SwipeModel.LEFT,
            SwipeModel.RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SwipeModel {
        int NONE = 0;
        int LEFT = 1;
        int RIGHT = 2;
    }

}
