package org.hjf.view.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import org.hjf.log.LogUtil;
import org.hjf.view.R;

/**
 * TODO COVER HEAD 实现方式
 * <p>
 */
public class RefreshLayout extends ViewGroup {

    /**
     * attrs RefreshLayout enum
     */
    private static final int SHOW_MODE_EXPAND = 1;
    private static final int SHOW_MODE_COVER = 2;

    private static final int FINGER_SCROLL_ACTION_NONE = 0;
    private static final int FINGER_SCROLL_ACTION_REFRESH = 1;
    private static final int FINGER_SCROLL_ACTION_LOAD_MORE = 2;

    /**
     * not to auto load more data
     */
    static final int NOT_AUTO_LOAD_MORE_DATA = -1;
    /**
     * 横向滑动距离 / 竖向滑动距离  最小比例
     */
    private static final float SCROLL_JUDGE_VERTICAL_HORIZONTAL = 1.0f;
    private static final float DAMPING = 0.4f;


    /**
     * Cache the touch slop from the context that created the view.
     * 刷新UI最小移动距离，没到这个距离不刷新UI
     */
    private float mTouchSlop;
    private int refreshHeadViewShowModel = RefreshLayout.SHOW_MODE_EXPAND;
    private boolean enableRefresh;
    private boolean enableLoadMore;
    private OnLoadListener mLoadListener;
    private BaseContentViewObserver mContentViewObserver;
    private int mLoadMoreWhenResidualItemNum = RefreshLayout.NOT_AUTO_LOAD_MORE_DATA;
    // content view
    private Rect mRect4Content = new Rect();
    private View mContentView;
    // attach view
    @Nullable
    private Rect mRect4Head, mRect4Foot, mRect4AutoLoadFoot, mRect4EmptyView;
    @Nullable
    private BaseAttachView mFootView;
    @Nullable
    private BaseAttachView mHeadView;
    @Nullable
    private BaseAttachView mAutoLoadFootView;
    @Nullable
    private View mEmptyView;
    private boolean isAddedEmptyView = false;
    // 上一次有效计算的X座标，每一次有效计算都要刷新一次
    private float mLastY;
    private float mDownX, mDownY;
    private int mScrollY;
    private boolean isGoodScroll = false;
    private float fingerScrollAction = FINGER_SCROLL_ACTION_NONE;
    // reset animal
    private int reset_animal_duration_millis = 2500;
    private Scroller mScroller;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop() * 0.5f);
        mScroller = new Scroller(context);
        // ignore log cat
        LogUtil.addIgnoreClassPath(RefreshLayout.class.getName());
        initFromAttributes(context, attrs, defStyleAttr, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mContentView == null) {
            initContentView();
        }

        final LayoutParams lpContentView = mContentView.getLayoutParams();
        measureChild(mContentView, widthMeasureSpec, heightMeasureSpec);

        // head view
        if (enableRefresh && mHeadView != null) {
            final LayoutParams lpHeadView = mHeadView.getLayoutParams();
            if (lpHeadView.width == RelativeLayout.LayoutParams.MATCH_PARENT
                    || lpHeadView.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
                lpHeadView.width = lpContentView.width;
                mHeadView.setLayoutParams(lpHeadView);
            }
            measureChild(mHeadView, widthMeasureSpec, heightMeasureSpec);
        }

        // foot view
        if (enableLoadMore && mFootView != null) {
            final LayoutParams lpFootView = mFootView.getLayoutParams();
            if (lpFootView.width == RelativeLayout.LayoutParams.MATCH_PARENT
                    || lpFootView.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
                lpFootView.width = lpContentView.width;
                mFootView.setLayoutParams(lpFootView);
            }
            measureChild(mFootView, widthMeasureSpec, heightMeasureSpec);
        }

        // auto load foot view
        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA && mAutoLoadFootView != null) {
            final LayoutParams lpAutoLoadFootView = mAutoLoadFootView.getLayoutParams();
            if (lpAutoLoadFootView.width == RelativeLayout.LayoutParams.MATCH_PARENT
                    || lpAutoLoadFootView.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
                lpAutoLoadFootView.width = lpContentView.width;
                mAutoLoadFootView.setLayoutParams(lpAutoLoadFootView);
            }
            measureChild(mAutoLoadFootView, widthMeasureSpec, heightMeasureSpec);
        }

        // empty view in refresh layout
        if (mEmptyView != null && isAddedEmptyView) {
            final LayoutParams lpEmptyView = mEmptyView.getLayoutParams();
            if (lpEmptyView.width == RelativeLayout.LayoutParams.MATCH_PARENT
                    || lpEmptyView.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
                lpEmptyView.width = lpContentView.width;
                mEmptyView.setLayoutParams(lpEmptyView);
            }
            measureChild(mEmptyView, widthMeasureSpec, heightMeasureSpec);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // head view show rect range
        if (enableRefresh && mHeadView != null) {
            if (mRect4Head == null) {
                mRect4Head = new Rect();
            }
            mRect4Head.left = 0;
            mRect4Head.top = -mHeadView.getMeasuredHeight();
            mRect4Head.right = mRect4Head.left + mHeadView.getMeasuredWidth();
            mRect4Head.bottom = mRect4Head.top + mHeadView.getMeasuredHeight();
            LogUtil.d("Rect Head View  -> [left{0},top{1},right{2},bottom{3}]",
                    mRect4Head.left, mRect4Head.top, mRect4Head.right, mRect4Head.bottom);
        }

        // content view show rect range
        mRect4Content.left = 0;
        mRect4Content.top = 0;
        mRect4Content.right = mRect4Content.left + mContentView.getMeasuredWidth();
        mRect4Content.bottom = mRect4Content.top + mContentView.getMeasuredHeight();
        LogUtil.d("Rect Content View  -> [left{0},top{1},right{2},bottom{3}]",
                mRect4Content.left, mRect4Content.top, mRect4Content.right, mRect4Content.bottom);

        // foot view show rect range
        if (enableLoadMore && mFootView != null) {
            if (mRect4Foot == null) {
                mRect4Foot = new Rect();
            }
            mRect4Foot.left = 0;
            mRect4Foot.top = mContentView.getMeasuredHeight();
            mRect4Foot.right = mRect4Foot.left + mFootView.getMeasuredWidth();
            mRect4Foot.bottom = mRect4Foot.top + mFootView.getMeasuredHeight();
            LogUtil.d("Rect Foot View  -> [left{0},top{1},right{2},bottom{3}]",
                    mRect4Foot.left, mRect4Foot.top, mRect4Foot.right, mRect4Foot.bottom);
        }

        // auto load foot view show rect range
        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA && mAutoLoadFootView != null) {
            if (mRect4AutoLoadFoot == null) {
                mRect4AutoLoadFoot = new Rect();
            }
            mRect4AutoLoadFoot.left = 0;
            mRect4AutoLoadFoot.top = mContentView.getMeasuredHeight() - mAutoLoadFootView.getMeasuredHeight();
            mRect4AutoLoadFoot.right = mRect4AutoLoadFoot.left + mAutoLoadFootView.getMeasuredWidth();
            mRect4AutoLoadFoot.bottom = mRect4AutoLoadFoot.top + mAutoLoadFootView.getMeasuredHeight();
            LogUtil.d("Rect Auto Load Foot View  -> [left{0},top{1},right{2},bottom{3}]",
                    mRect4AutoLoadFoot.left, mRect4AutoLoadFoot.top, mRect4AutoLoadFoot.right, mRect4AutoLoadFoot.bottom);
        }

        // empty view in refresh layout show rect range
        if (mEmptyView != null && isAddedEmptyView) {
            if (mRect4EmptyView == null) {
                mRect4EmptyView = new Rect();
            }
            mRect4EmptyView.left = 0;
            mRect4EmptyView.top = 0;
            mRect4EmptyView.right = mRect4EmptyView.left + mEmptyView.getMeasuredWidth();
            mRect4EmptyView.bottom = mRect4EmptyView.top + mEmptyView.getMeasuredHeight();
            LogUtil.d("Rect Empty View  -> [left{0},top{1},right{2},bottom{3}]",
                    mRect4EmptyView.left, mRect4EmptyView.top, mRect4EmptyView.right, mRect4EmptyView.bottom);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (enableRefresh && mHeadView != null && mRect4Head != null) {
            mHeadView.layout(mRect4Head.left, mRect4Head.top, mRect4Head.right, mRect4Head.bottom);
        }

        mContentView.layout(mRect4Content.left, mRect4Content.top, mRect4Content.right, mRect4Content.bottom);

        if (enableLoadMore && mFootView != null && mRect4Foot != null) {
            mFootView.layout(mRect4Foot.left, mRect4Foot.top, mRect4Foot.right, mRect4Foot.bottom);
        }

        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA && mAutoLoadFootView != null && mRect4AutoLoadFoot != null) {
            mAutoLoadFootView.layout(mRect4AutoLoadFoot.left, mRect4AutoLoadFoot.top, mRect4AutoLoadFoot.right, mRect4AutoLoadFoot.bottom);
        }

        if (mEmptyView != null && isAddedEmptyView && mRect4EmptyView != null) {
            mEmptyView.layout(mRect4EmptyView.left, mRect4EmptyView.top, mRect4EmptyView.right, mRect4EmptyView.bottom);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastY = mDownY;
                isGoodScroll = false;
                break;
            case MotionEvent.ACTION_MOVE:
                isGoodScroll = isGoodScroll(ev.getX(), ev.getY());
                break;

        }
        return isGoodScroll || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            // 所有 ChildView 不响应 DOWN 事件时，本ViewGroup进入此代码块
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastY = mDownY;
                isGoodScroll = false;
                break;

            case MotionEvent.ACTION_MOVE:
                // stop animation
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (!isGoodScroll) {
                    isGoodScroll = isGoodScroll(ev.getX(), ev.getY());
                }
                if (isGoodScroll) {
                    // 如果滑动没有到指定的距离，积累滑动事件
                    final float fingerScrollY = ev.getY() - mLastY;
                    if (Math.abs(fingerScrollY) < mTouchSlop) {
                        LogUtil.d("fingerScrollY:{0} < mTouchSlop:{1}", fingerScrollY, mTouchSlop);
                        return true;
                    }

                    // get finger scroll action
                    if (fingerScrollAction == FINGER_SCROLL_ACTION_NONE) {
                        fingerScrollAction = fingerScrollY > 0 ? FINGER_SCROLL_ACTION_REFRESH : FINGER_SCROLL_ACTION_LOAD_MORE;
                        LogUtil.d("set fingerScrollAction={0}  fingerScrollY{1}", fingerScrollAction, fingerScrollY);
                    }
                    // 刷新上次计算点
                    mLastY = ev.getY();
                    // 如果处于自动加载状态，不能进行刷新、加载更多动作
                    if (/*fingerScrollAction == FINGER_SCROLL_ACTION_LOAD_MORE && */mContentViewObserver.isAutoLoading()) {
                        return true;
                    }
                    final int moveY4Ask = (int) (fingerScrollY * (1 - DAMPING)) * -1;
                    // 实际计算后可以位移的量
                    LogUtil.d("get scrollable distance request : scrolled={0}  moveY4Ask={1}", getScrollY(), moveY4Ask);
                    int moveY4Result = getScrollableDistance(moveY4Ask);
                    LogUtil.d("get scrollable distance result: moveY4Result={0}", moveY4Result);
                    // 移动操作
                    myScrollBy(moveY4Result);
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // refresh
                if (enableRefresh && fingerScrollAction == FINGER_SCROLL_ACTION_REFRESH && mHeadView != null && mHeadView.canLoad(mScrollY)) {
                    mHeadView.onLoading();
                    if (mLoadListener != null) {
                        mLoadListener.onRefresh();
                    }
                }
                // load more. if auto loading, no load more
                else if (enableLoadMore && fingerScrollAction == FINGER_SCROLL_ACTION_LOAD_MORE
                        && mFootView != null && mFootView.canLoad(mScrollY)
                        && !mContentViewObserver.isAutoLoading()) {
                    mFootView.onLoading();
                    if (mLoadListener != null) {
                        mLoadListener.onLoadMore();
                    }
                }
                // reset scroll status
                else {
                    resetScrollAnimal();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            LogUtil.d(" Scroller get currentY={0}", mScroller.getCurrY());
            myScrollTo(mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void myScrollBy(int offsetY) {
        mScrollY += offsetY;
        myScrollTo(mScrollY);
    }

    private void myScrollTo(int scrollY) {
        mScrollY = scrollY;
        // because reset progress  fingerScrollAction = FINGER_SCROLL_ACTION_NONE
        if (fingerScrollAction != FINGER_SCROLL_ACTION_LOAD_MORE && enableRefresh && mHeadView != null) {
            mHeadView.onMoveTo(mScrollY);
        }
        LogUtil.d("fingerScrollAction={0}  ", fingerScrollAction);
        if (fingerScrollAction != FINGER_SCROLL_ACTION_REFRESH && enableLoadMore && mFootView != null) {
            mFootView.onMoveTo(mScrollY);
        }
        scrollTo(0, mScrollY);
    }

    /**
     * use the method when load more data complete refresh complete.
     */
    @MainThread
    public void loadComplete() {
        if (fingerScrollAction == FINGER_SCROLL_ACTION_REFRESH && enableRefresh
                && mHeadView != null && mHeadView.canLoad(mScrollY)) {
            mHeadView.onComplete();
        }
        if (fingerScrollAction == FINGER_SCROLL_ACTION_LOAD_MORE && enableLoadMore
                && mFootView != null && mFootView.canLoad(mScrollY)) {
            mFootView.onComplete();
        }
        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA && mContentViewObserver.isAutoLoading()) {
            mContentViewObserver.onAutoLoadComplete();
        }
        fingerScrollAction = FINGER_SCROLL_ACTION_NONE;
        resetScrollAnimal();
    }

    // activity onResume call
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    // activity onDestroy call, only once
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContentViewObserver.destroy();
    }

    /**
     * reset scroll animal
     */
    private void resetScrollAnimal() {
        mScroller.startScroll(0, getScrollY(), 0, 0 - getScrollY(), reset_animal_duration_millis);
        postInvalidate();
    }

    public void setLoadListener(@NonNull OnLoadListener loadListener) {
        this.mLoadListener = loadListener;
        if (this.mContentViewObserver != null) {
            this.mContentViewObserver.setAutoLoadMoreResidualNum(mLoadMoreWhenResidualItemNum, mAutoLoadFootView, mLoadListener);
        }
    }

    public void setAutoLoadMoreWhenResidualItemNum(int loadMoreWhenResidualItemNum) {
        mLoadMoreWhenResidualItemNum = Math.max(NOT_AUTO_LOAD_MORE_DATA, loadMoreWhenResidualItemNum);
        if (this.mContentViewObserver != null) {
            this.mContentViewObserver.setAutoLoadMoreResidualNum(loadMoreWhenResidualItemNum, mAutoLoadFootView, mLoadListener);
        }
    }

    @Nullable
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Does the slide need interception
     *
     * @param currX current x point
     * @param currY current y point
     * @return true: need interception
     */
    private boolean isGoodScroll(float currX, float currY) {

        float offsetX = Math.abs(currX - mDownX);
        float offsetY = Math.abs(currY - mDownY);
        LogUtil.d("move - offsetX:{0}, offsetY:{1}", offsetX, offsetY);

        // 没有达到最小滑动距离，不算滑动，积累滑动事件
        if (offsetX < mTouchSlop && offsetY < mTouchSlop) {
            LogUtil.d("offset distance < TouchSlop{0}", mTouchSlop);
            return false;
        }

        // 竖向滑动距离/横向滑动距离  不满足最小Y/X比率，不算竖向滑动
        if (offsetY / offsetX < SCROLL_JUDGE_VERTICAL_HORIZONTAL) {
            LogUtil.d("X/Y < {0}", SCROLL_JUDGE_VERTICAL_HORIZONTAL);
            return false;
        }

        // 禁用刷新功能
        if (!enableRefresh && currY > mDownY) {
            LogUtil.d("refresh function is unable.");
            return false;
        }

        // AttachMover 判定不能刷新
        if (currY > mDownY && !mContentViewObserver.isTop()) {
            LogUtil.d("AttachMover judge not to move.");
            return false;
        }

        // 禁用加载更多功能
        if (!enableLoadMore && currY < mDownY) {
            LogUtil.d("load more function is unable.");
            return false;
        }

        // AttachMover 判定不能加载更多
        if (currY < mDownY && !mContentViewObserver.isBottom()) {
            LogUtil.d("AttachMover judge not to move.");
            return false;
        }

        return true;
    }


    private int getScrollableDistance(int moveY4Ask) {
        int move4Result;
        int move4End = getScrollY() + moveY4Ask;
        int topPointY = 0;
        int bottomPointY = 0;

        // It is refresh action. head view in ScrollRange[-headViewHeight, 0]
        if (fingerScrollAction == FINGER_SCROLL_ACTION_REFRESH && enableRefresh && mHeadView != null) {
            topPointY = -mHeadView.getMeasuredHeight();
        }
        // It is load more action. foot view in ScrollRange[0, footViewHeight]
        if (fingerScrollAction == FINGER_SCROLL_ACTION_LOAD_MORE && enableLoadMore && mFootView != null) {
            bottomPointY = mFootView.getMeasuredHeight();
        }
        // end  < top
        if (move4End < topPointY) {
            move4Result = topPointY - getScrollY();
        }
        // top <= end <= bottom
        else if (move4End <= bottomPointY) {
            move4Result = moveY4Ask;
        }
        // end > bottom
        else {
            move4Result = bottomPointY - getScrollY();
        }
        return move4Result;
    }

    private void initContentView() {
        // content view
        if (getChildCount() > 1) {
            throw new RuntimeException("Refresh layout child view must be one.");
        }
        mContentView = getChildAt(0);
        if (mContentView == null) {
            throw new RuntimeException("Refresh layout not found child view.");
        }
        mContentViewObserver = ContentViewObserverFactory.create(mContentView);
        if (enableRefresh && mHeadView != null) {
            addView(mHeadView);
        }
        if (enableLoadMore && mFootView != null) {
            addView(mFootView);
        }
        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA && mAutoLoadFootView != null) {
            addView(mAutoLoadFootView);
            this.mContentViewObserver.setAutoLoadMoreResidualNum(mLoadMoreWhenResidualItemNum, mAutoLoadFootView, mLoadListener);
        }
        if (mEmptyView != null && (isAddedEmptyView = !mContentViewObserver.bindEmptyView(mEmptyView))) {
            if (isAddedEmptyView) {
                addView(mEmptyView);
            }
        }
    }

    private void initAttachView(@LayoutRes int refreshHeadLayoutRes, @LayoutRes int loadMoreFootLayoutRes, @LayoutRes int autoLoadMoreFootLayoutRes, @LayoutRes int emptyViewLayoutRes) {
        // head view
        if (enableRefresh) {
            View tempHeadView = LayoutInflater.from(super.getContext()).inflate(refreshHeadLayoutRes, this, false);
            if (tempHeadView instanceof BaseAttachView) {
                mHeadView = (BaseAttachView) tempHeadView;
                mHeadView.initView();
            }
            // error
            else {
                throw new RuntimeException("RefreshLayout ask refresh_head_layout top view must extends " + BaseAttachView.class.getName());
            }
        }
        // foot view
        if (enableLoadMore) {
            View tempFootView = LayoutInflater.from(super.getContext()).inflate(loadMoreFootLayoutRes, this, false);
            if (tempFootView instanceof BaseAttachView) {
                mFootView = (BaseAttachView) tempFootView;
                mFootView.initView();
            }
            // error
            else {
                throw new RuntimeException("RefreshLayout ask load_more_foot_layout top view must extends " + BaseAttachView.class.getName());
            }
        }
        // auto load foot view
        if (mLoadMoreWhenResidualItemNum != NOT_AUTO_LOAD_MORE_DATA) {
            View tempAutoFootView = LayoutInflater.from(super.getContext()).inflate(autoLoadMoreFootLayoutRes, this, false);
            if (tempAutoFootView instanceof BaseAttachView) {
                mAutoLoadFootView = (BaseAttachView) tempAutoFootView;
                mAutoLoadFootView.initView();
            }
            // error
            else {
                throw new RuntimeException("RefreshLayout ask auto_load_more_foot_layout top view must extends " + BaseAttachView.class.getName());
            }
        }
        // empty view
        if (emptyViewLayoutRes != -1) {
            mEmptyView = LayoutInflater.from(super.getContext()).inflate(emptyViewLayoutRes, this, false);
        }
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, defStyleAttr, defStyleRes);
        final int N = a.getIndexCount();
        @LayoutRes int emptyViewLayoutRes = -1;
        @LayoutRes int refreshHeadLayoutRes = R.layout.v_drip_refresh_head;
        @LayoutRes int loadMoreFootLayoutRes = R.layout.v_default_load_more_foot;
        @LayoutRes int autoLoadMoreFootLayoutRes = R.layout.v_default_auto_load_more_foot;
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            // enableRefresh
            if (attr == R.styleable.RefreshLayout_enable_refresh) {
                enableRefresh = a.getBoolean(attr, false);
            }
            // enableRefresh
            else if (attr == R.styleable.RefreshLayout_enable_load_more) {
                enableLoadMore = a.getBoolean(attr, false);
            }
            // enableRefresh
            else if (attr == R.styleable.RefreshLayout_load_more_when_residual_item) {
                mLoadMoreWhenResidualItemNum = a.getInt(attr, RefreshLayout.NOT_AUTO_LOAD_MORE_DATA);
            }
            // refresh head layout res
            else if (attr == R.styleable.RefreshLayout_refresh_head_layout) {
                refreshHeadLayoutRes = a.getResourceId(attr, refreshHeadLayoutRes);
            }
            // load more layout res
            else if (attr == R.styleable.RefreshLayout_load_more_foot_layout) {
                loadMoreFootLayoutRes = a.getResourceId(attr, loadMoreFootLayoutRes);
            }
            // auto load load more layout res
            else if (attr == R.styleable.RefreshLayout_auto_load_more_foot_layout) {
                autoLoadMoreFootLayoutRes = a.getResourceId(attr, autoLoadMoreFootLayoutRes);
            }
            // empty view layout res
            else if (attr == R.styleable.RefreshLayout_empty_view_layout) {
                emptyViewLayoutRes = a.getResourceId(attr, -1);
            }
            // refresh head iew show model
            else if (attr == R.styleable.RefreshLayout_refresh_head_show_model) {
                refreshHeadViewShowModel = a.getInt(attr, RefreshLayout.SHOW_MODE_EXPAND);
            }
            // reset animal duration millis
            else if (attr == R.styleable.RefreshLayout_reset_animal_duration_millis) {
                reset_animal_duration_millis = a.getInt(attr, reset_animal_duration_millis);
            }
        }
        a.recycle();
        initAttachView(refreshHeadLayoutRes, loadMoreFootLayoutRes, autoLoadMoreFootLayoutRes, emptyViewLayoutRes);
    }
}
