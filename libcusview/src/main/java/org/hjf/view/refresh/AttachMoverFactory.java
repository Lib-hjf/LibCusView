package org.hjf.view.refresh;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AbsListView;

public class AttachMoverFactory {

    public static IAttachMover create(View contentView) {
        // list view、grid view
        if (contentView instanceof AbsListView) {
            return new AbsListAttachMover((AbsListView) contentView);
        }
        //recycler view
        else if (contentView instanceof RecyclerView) {
            return new RecyclerViewAttachMover((RecyclerView) contentView);
        }
        // default
        else {
            return new DefaultAttachMover();
        }
    }


    static final class DefaultAttachMover implements IAttachMover {

        @Override
        public boolean canMoved4HeadView() {
            return false;
        }

        @Override
        public boolean canMoved4FootView() {
            return false;
        }
    }

    static final class AbsListAttachMover implements IAttachMover {

        private AbsListView view;

        AbsListAttachMover(AbsListView view) {
            this.view = view;
        }

        @Override
        public boolean canMoved4HeadView() {
            return view != null
                    // 第一个Item是否可见
                    && view.getFirstVisiblePosition() == 0
                    // 没有数据也显示刷新头部
                    // ListView滑动到顶部没有,由于之前的判断，此时第一个ChildView展示的一定是第一个Item的内容
                    && (view.getChildAt(0) == null || view.getChildAt(0).getTop() >= 0);
        }

        @Override
        public boolean canMoved4FootView() {
            return view != null
                    // AbsListView适配器不能为空
                    && view.getAdapter() != null
                    // 最后一个Item是否可见
                    && view.getLastVisiblePosition() == view.getAdapter().getCount() - 1
                    // ListView滑动到底部没有,由于之前的判断，此时最后一个ChildView展示的一定是最后一个Item的内容。
                    // ChildView.bottom 参考的是 AbsListView，而不是本View
                    && view.getHeight() == view.getChildAt(view.getChildCount() - 1).getBottom();
        }
    }

    static final class RecyclerViewAttachMover implements IAttachMover {

        private RecyclerView recyclerView;
        private LinearLayoutManager linearLayoutManager;
        private StaggeredGridLayoutManager staggeredGridLayoutManager;

        RecyclerViewAttachMover(RecyclerView view) {
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
        public boolean canMoved4HeadView() {
            if (linearLayoutManager != null) {
//                LogUtil.d("AttachMover RecyclerView LinearLayoutManager FirstCompletelyVisibleItemPosition={0}",
//                        linearLayoutManager.findFirstCompletelyVisibleItemPosition());
                return linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
            }
            if (staggeredGridLayoutManager != null && staggeredGridLayoutManager.getOrientation() == StaggeredGridLayoutManager.VERTICAL) {
                int[] positions = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null);
//                LogUtil.d("AttachMover RecyclerView StaggeredGridLayoutManager FirstCompletelyVisibleItemPositions");
//                LogUtil.d(positions);
                return recyclerView != null
                        && positions[0] == 0
                        && recyclerView.getChildAt(0).getTop() >= 0;
            }
            return false;
        }

        @Override
        public boolean canMoved4FootView() {
            if (linearLayoutManager != null) {
//                LogUtil.d("AttachMover RecyclerView LinearLayoutManager FirstCompletelyVisibleItemPosition={0}",
//                        linearLayoutManager.findLastCompletelyVisibleItemPosition());
                return linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                        recyclerView.getAdapter().getItemCount() - 1;
            }
            if (staggeredGridLayoutManager != null && staggeredGridLayoutManager.getOrientation() == StaggeredGridLayoutManager.VERTICAL) {
                int[] positions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
//                LogUtil.d("AttachMover RecyclerView StaggeredGridLayoutManager findLastCompletelyVisibleItemPositions");
//                LogUtil.d(positions);
                return recyclerView != null
                        && positions[positions.length - 1] == recyclerView.getAdapter().getItemCount() - 1
                        && recyclerView.getHeight() == recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom();
            }
            return false;
        }
    }
}
