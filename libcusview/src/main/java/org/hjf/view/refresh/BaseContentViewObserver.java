package org.hjf.view.refresh;

import android.support.annotation.Nullable;
import android.view.View;

abstract class BaseContentViewObserver {

    private int itemCountAgo;
    int residualItemNum;
    private boolean isAutoLoading = false;
    boolean hasMoreData = true;

    @Nullable
    OnLoadListener loadListener;
    @Nullable
    private IAttachView autoLoadMoreView;
    @Nullable
    View emptyView;


    /**
     * first item complete visible
     * Refresh Head View Show
     */
    protected boolean isTop() {
        return false;
    }

    /**
     * last item complete visible
     * Load More Foot View Show
     */
    boolean isBottom() {
        return false;
    }

    /**
     * The empty view bind into the content view
     * <p>
     * Content view data observer
     *
     * @return true : isBinding, need not to add empty view into {@link RefreshLayout}
     */
    boolean bindEmptyView(View emptyView) {
        this.emptyView = emptyView;
        return false;
    }


    /**
     * Auto Load More Data
     * Content view scroll observer
     */
    void setAutoLoadMoreResidualNum(int loadMoreWhenResidualItemNum, @Nullable IAttachView autoLoadMoreView, @Nullable OnLoadListener loadListener) {
        this.residualItemNum = loadMoreWhenResidualItemNum;
        this.autoLoadMoreView = autoLoadMoreView;
        this.loadListener = loadListener;
    }

    boolean isAutoLoading() {
        return isAutoLoading;
    }

    protected abstract int getCurrentItemCount();

    void toAutoLoad() {
        itemCountAgo = getCurrentItemCount();
        isAutoLoading = true;
        if (autoLoadMoreView != null) {
            autoLoadMoreView.onLoading();
        }
        if (loadListener != null) {
            loadListener.onLoadMore();
        }
    }

    void onAutoLoadComplete() {
        int currCount = getCurrentItemCount();
        hasMoreData = currCount > itemCountAgo;
        itemCountAgo = currCount;
        if (autoLoadMoreView != null) {
            autoLoadMoreView.onComplete();
        }
        isAutoLoading = false;
    }

    /**
     * destroy
     */
    void destroy() {
        loadListener = null;
        autoLoadMoreView = null;
        emptyView = null;
    }
}
