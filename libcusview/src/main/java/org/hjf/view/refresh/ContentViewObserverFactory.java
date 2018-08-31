package org.hjf.view.refresh;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AbsListView;

import org.hjf.log.LogUtil;

public class ContentViewObserverFactory {

    public static BaseContentViewObserver create(View contentView) {
        // list view、grid view
        if (contentView instanceof AbsListView) {
            return new AbsListLoadActionJudge((AbsListView) contentView);
        }
        //recycler view
        else if (contentView instanceof RecyclerView) {
            return new RecyclerViewLoadActionJudge((RecyclerView) contentView);
        }
        // default
        else {
            return new DefaultLoadActionJudge();
        }
    }


    static final class DefaultLoadActionJudge extends BaseContentViewObserver {

        @Override
        protected int getCurrentItemCount() {
            return 0;
        }

    }

    static final class AbsListLoadActionJudge extends BaseContentViewObserver {

        @NonNull
        private AbsListView absListView;
        @Nullable
        private AbsListView.OnScrollListener onScrollListener;

        AbsListLoadActionJudge(@NonNull AbsListView view) {
            this.absListView = view;
        }

        @Override
        public boolean isTop() {
            // 第一个Item是否可见
            return absListView.getFirstVisiblePosition() == 0
                    // 没有数据也显示刷新头部
                    // ListView滑动到顶部没有,由于之前的判断，此时第一个ChildView展示的一定是第一个Item的内容
                    && (absListView.getChildAt(0) == null || absListView.getChildAt(0).getTop() >= 0);
        }

        @Override
        public boolean isBottom() {
            // AbsListView适配器不能为空
            return absListView.getAdapter() != null
                    // 最后一个Item是否可见
                    && absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1
                    // ListView滑动到底部没有,由于之前的判断，此时最后一个ChildView展示的一定是最后一个Item的内容。
                    // ChildView.bottom 参考的是 AbsListView，而不是本View
                    && absListView.getHeight() == absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
        }

        @Override
        public boolean bindEmptyView(View view) {
            super.bindEmptyView(view);
            if (emptyView != null) {
                absListView.setEmptyView(emptyView);
            }
            return true;
        }

        @Override
        public void setAutoLoadMoreResidualNum(int loadMoreWhenResidualItemNum, @Nullable IAttachView view, @Nullable OnLoadListener listener) {
            super.setAutoLoadMoreResidualNum(loadMoreWhenResidualItemNum, view, loadListener);
            if (onScrollListener == null) {
                onScrollListener = new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            return;
                        }
                        if (loadListener == null) {
                            return;
                        }
                        if (!hasMoreData || isAutoLoading() || residualItemNum == RefreshLayout.NOT_AUTO_LOAD_MORE_DATA) {
                            return;
                        }
                        if (view.getAdapter() == null) {
                            return;
                        }
                        if (view.getAdapter().getCount() <= residualItemNum) {
                            return;
                        }
                        if (view.getLastVisiblePosition() > view.getAdapter().getCount() - residualItemNum) {
                            toAutoLoad();
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                };
                absListView.setOnScrollListener(onScrollListener);
            }
        }

        @Override
        protected int getCurrentItemCount() {
            return absListView.getAdapter().getCount();
        }

        @Override
        public void destroy() {
            super.destroy();
            absListView.setOnScrollListener(null);
        }
    }

    static final class RecyclerViewLoadActionJudge extends BaseContentViewObserver {
        @NonNull
        private RecyclerView recyclerView;
        @Nullable
        private RecyclerView.OnScrollListener onScrollListener;
        @Nullable
        private RecyclerView.AdapterDataObserver adapterDataObserver;
        @Nullable
        private LinearLayoutManager linearLayoutManager;
        @Nullable
        private StaggeredGridLayoutManager staggeredGridLayoutManager;

        RecyclerViewLoadActionJudge(@NonNull RecyclerView view) {
            this.recyclerView = view;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            // grid and linear
            if (layoutManager instanceof LinearLayoutManager) {
                linearLayoutManager = (LinearLayoutManager) layoutManager;
            }
            // staggered grid
            else if (layoutManager instanceof StaggeredGridLayoutManager) {
                staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            }
        }

        @Override
        public boolean isTop() {
            if (linearLayoutManager != null) {
//                LogUtil.d("AttachMover RecyclerView LinearLayoutManager FirstCompletelyVisibleItemPosition={0}",
//                        linearLayoutManager.findFirstCompletelyVisibleItemPosition());
                return linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
            }
            if (staggeredGridLayoutManager != null && staggeredGridLayoutManager.getOrientation() == StaggeredGridLayoutManager.VERTICAL) {
                int[] positions = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null);
//                LogUtil.d("AttachMover RecyclerView StaggeredGridLayoutManager FirstCompletelyVisibleItemPositions");
//                LogUtil.d(positions);
                return positions[0] == 0
                        && recyclerView.getChildAt(0).getTop() >= 0;
            }
            return false;
        }

        @Override
        public boolean isBottom() {
            if (linearLayoutManager != null) {
                LogUtil.d("AttachMover RecyclerView LinearLayoutManager FirstCompletelyVisibleItemPosition={0}",
                        linearLayoutManager.findLastCompletelyVisibleItemPosition());
                return recyclerView.getAdapter() != null
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition()
                        == recyclerView.getAdapter().getItemCount() - 1;
            }
            if (staggeredGridLayoutManager != null && staggeredGridLayoutManager.getOrientation() == StaggeredGridLayoutManager.VERTICAL) {
                int[] positions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
                LogUtil.d("AttachMover RecyclerView StaggeredGridLayoutManager findLastCompletelyVisibleItemPositions");
                LogUtil.d(positions);
                return recyclerView.getAdapter() != null
                        && positions[positions.length - 1] == recyclerView.getAdapter().getItemCount() - 1
                        && recyclerView.getHeight() == recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom();
            }
            return false;
        }


        @Override
        public boolean bindEmptyView(View view) {
            super.bindEmptyView(view);
            if (adapterDataObserver == null) {
                adapterDataObserver = new RecyclerView.AdapterDataObserver() {

                    private boolean isEmptyAgo = true;

                    @Override
                    public void onChanged() {
                        showView();
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        showView();
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                        showView();
                    }

                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        showView();
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        showView();
                    }

                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        showView();
                    }

                    private void showView() {
                        boolean isEmpty = recyclerView.getAdapter().getItemCount() == 0;
                        if (isEmpty == isEmptyAgo) {
                            return;
                        }
                        if (emptyView != null) {
                            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                        }
                        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                        isEmptyAgo = isEmpty;
                    }
                };
                recyclerView.getAdapter().registerAdapterDataObserver(adapterDataObserver);
            }
            return false;
        }

        @Override
        public void setAutoLoadMoreResidualNum(int loadMoreWhenResidualItemNum, @Nullable IAttachView view, @Nullable OnLoadListener listener) {
            super.setAutoLoadMoreResidualNum(loadMoreWhenResidualItemNum, view, listener);
            if (onScrollListener == null) {
                onScrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                            return;
                        }
                        if (loadListener == null) {
                            return;
                        }
                        if (!hasMoreData || isAutoLoading() || residualItemNum == RefreshLayout.NOT_AUTO_LOAD_MORE_DATA) {
                            return;
                        }
                        if (recyclerView.getAdapter() == null) {
                            return;
                        }
                        if (recyclerView.getAdapter().getItemCount() <= residualItemNum) {
                            return;
                        }

                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                        // LinearLayoutManager
                        if (layoutManager instanceof LinearLayoutManager) {
                            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() >= recyclerView.getAdapter().getItemCount() - residualItemNum) {
                                toAutoLoad();
                            }
                        }
                        // StaggeredGridLayoutManager
                        else if (layoutManager instanceof StaggeredGridLayoutManager) {
                            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                            int[] positions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
                            if (positions[positions.length - 1] >= recyclerView.getAdapter().getItemCount() - residualItemNum) {
                                toAutoLoad();
                            }
                        }
                    }
                };
                recyclerView.addOnScrollListener(onScrollListener);
            }
        }

        @Override
        protected int getCurrentItemCount() {
            return recyclerView.getAdapter().getItemCount();
        }

        @Override
        public void destroy() {
            super.destroy();
            if (onScrollListener != null) {
                recyclerView.removeOnScrollListener(onScrollListener);
                onScrollListener = null;
            }
            if (adapterDataObserver != null) {
                recyclerView.getAdapter().unregisterAdapterDataObserver(adapterDataObserver);
                adapterDataObserver = null;
            }
        }
    }
}
