package org.hjf.view.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import org.hjf.util.log.LogUtil;


/**
 * 解释：measureChildren、measureChild 等方法
 * https://blog.csdn.net/chunqiuwei/article/details/50722061
 */
public class SwipeLayout extends ViewGroup {

    /**
     * 横向滑动距离 / 竖向滑动距离  最小比例
     */
    private static final float SCROLL_JUDGE_VERTICAL_HORIZONTAL = 1.0f;
    private static final float DAMPING = 0.4f;
    private static final float AUTO_SCROLL_OPEN_PERCENT = 0.45f;

    /**
     * Cache the touch slop from the context that created the view.
     * 刷新UI最小移动距离，没到这个距离不刷新UI
     */
    private int mTouchSlop;
    @ViewHolder.SwipeModel
    private int mSwipeModel = ViewHolder.SwipeModel.NONE;
    private Rect mRectContent = new Rect();
    private Rect mRectMenu = new Rect();
    private View mContentView, mMenuView;
    // 上一次有效计算的X座标，每一次有效计算都要刷新一次
    private float mLastX;
    private float mDownX, mDownY;
    // 是否是横向滑动
    private boolean isHorizontalScroll = false;
    /**
     * 位移取值范围，只能在该范围内移动
     * [leftXPoint, rightXPoint]
     */
    private int[] mScrollRange = new int[2];

    public SwipeLayout(Context context) {
        super(context);
        // 设置过高滑动不流畅
        mTouchSlop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop() * 0.5f);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new RuntimeException("SwipeLayout need code set ChildView. Should not apply to xml.");
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new RuntimeException("SwipeLayout need code set ChildView. Should not apply to xml.");
    }

    /**
     * 规定：
     * {@link SwipeLayout#mContentView} 的宽度强制 == SwipeLayout.width
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mContentView == null || mMenuView == null) {
            throw new RuntimeException("ContentView or MenuView is null.");
        }
        if (getChildCount() != 2) {
            throw new RuntimeException("SwipeLayout child count must be 2, but Now it's the " + getChildCount());
        }

        // 本ViewGroup需要的大小
        int groupViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int groupViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        // ParentView 的mode
        // 解析是 match_parent，还是wrap_content，还是具体值。采取不同动作
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 各组件尺寸设置策略
        // 设置 ContentView 的宽度
        final LayoutParams lpContent = mContentView.getLayoutParams();
        lpContent.width = groupViewWidth;
        // 设置 MenuWidth 的高度
        final LayoutParams lpMenu = mMenuView.getLayoutParams();
        if (lpMenu.height == LayoutParams.WRAP_CONTENT || lpMenu.height == LayoutParams.MATCH_PARENT) {
            lpMenu.height = lpContent.height;
            mMenuView.setLayoutParams(lpMenu);
        }
        // 设置 SwipeLayout 的高度
        groupViewHeight = Math.max(lpContent.height, groupViewHeight);
//        LogUtil.d("Swipe Layout  Width{0} Height{1}", groupViewWidth, groupViewHeight);

        // 测量两个子View
        measureChild(mContentView, widthMeasureSpec, heightMeasureSpec);
        measureChild(mMenuView, widthMeasureSpec, heightMeasureSpec);

        //这里将宽度和高度与Google为我们设定的建议最低宽高对比，确保我们要求的尺寸不低于建议的最低宽高。
        groupViewWidth = Math.max(groupViewWidth, getSuggestedMinimumWidth());
        groupViewHeight = Math.max(groupViewHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSizeAndState(groupViewWidth, widthMeasureSpec, 0),
                resolveSizeAndState(groupViewHeight, heightMeasureSpec, 0));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 1 确立 ContentView 的显示区域
        mRectContent.left = 0;
        mRectContent.right = mRectContent.left + mContentView.getMeasuredWidth();
        mRectContent.top = 0;
        mRectContent.bottom = mRectContent.top + mContentView.getMeasuredHeight();
        LogUtil.d("Rect ContentView  -> [left{0},top{1},right{2},bottom{3}]",
                mRectContent.left, mRectContent.top, mRectContent.right, mRectContent.bottom);


        // 2.1 确立 MenuView 的显示区域
        // 2.2 给位移取值范围赋值
        if (mMenuView.getMeasuredWidth() == 0){
            throw new RuntimeException("MenuView width is 0, Should set a specific value.");
        }
        if (mSwipeModel == ViewHolder.SwipeModel.RIGHT) {
            mRectMenu.left = mRectContent.right;
            mScrollRange[0] = 0;
            mScrollRange[1] = mMenuView.getMeasuredWidth();
        } else if (mSwipeModel == ViewHolder.SwipeModel.LEFT) {
            mRectMenu.left = mRectContent.left - mMenuView.getMeasuredWidth();
            mScrollRange[0] = mMenuView.getMeasuredWidth() * -1;
            mScrollRange[1] = 0;
        }
        mRectMenu.right = mRectMenu.left + mMenuView.getMeasuredWidth();
        mRectMenu.top = mRectContent.top;
        mRectMenu.bottom = mRectMenu.top + mMenuView.getMeasuredHeight();
        LogUtil.d("Rect MenuView  -> [left{0},top{1},right{2},bottom{3}]",
                mRectMenu.left, mRectMenu.top, mRectMenu.right, mRectMenu.bottom);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(mRectContent.left, mRectContent.top, mRectContent.right, mRectContent.bottom);
        mMenuView.layout(mRectMenu.left, mRectMenu.top, mRectMenu.right, mRectMenu.bottom);
    }

    // 设定滑动条件，满足位移条件才判定为横向滑动，拦截响应整个滑动事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastX = mDownX;
//                LogUtil.d("onInterceptTouchEvent - ACTION_DOWN ");
                break;

            // 有 ChildView 响应 DOWN 事件
            // 拦截符合条件的滑动事件
            case MotionEvent.ACTION_MOVE:
//                LogUtil.d("onInterceptTouchEvent - ACTION_MOVE ");
                isHorizontalScroll = isHorizontalScroll(ev.getX(), ev.getY());
                break;

        }
        return isHorizontalScroll || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            // 所有 ChildView 不响应 DOWN 事件
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastX = mDownX;
                return true;

            // 处理滑动事件
            case MotionEvent.ACTION_MOVE:

                // 判断是否满足移动条件
                if (!isHorizontalScroll) {
                    isHorizontalScroll = isHorizontalScroll(ev.getX(), ev.getY());
                }
                if (!isHorizontalScroll) {
                    return true;
                }

                // 设置父控件不拦截事件，如：移动条目时，不让RecyclerView滑动列表
                requestDisallowInterceptTouchEvent(true);

                // 如果滑动没有到指定的距离，积累滑动事件
                final float fingerScrollX = ev.getX() - mLastX;
                if (Math.abs(fingerScrollX) < mTouchSlop) {
                    LogUtil.d("onTouchEvent - fingerScrollX:{0} < mTouchSlop:{1}", fingerScrollX, mTouchSlop);
                    return true;
                }
                // 刷新上次计算点
                mLastX = ev.getX();
                // 用户动作：finger  <--左滑---   offsetY4Ask < 0
                // 目标效果：View    <--左滑---   offsetY4End > 0
                // View申请位移量：offsetY4Ask = -1 * 手指滑动距离 * 阻尼
                final int offsetX4Ask = (int) (fingerScrollX * (1 - DAMPING)) * -1;
                // 实际计算后可以位移的量
                int offsetX4End = getScrollableDistance(offsetX4Ask);
                // 移动操作
                scrollBy(offsetX4End, 0);
                return true;
            // 松手，取消滑动
            // 移动达一定距离自动展示侧栏菜单，否则自动复原

            // 取消事件，以后的手势操作不会传递到本 View
            case MotionEvent.ACTION_CANCEL:
                // 松手操作
            case MotionEvent.ACTION_UP:
                autoSelectActionAfterUp();
                isHorizontalScroll = false;
                // 恢复父控件的拦截功能
                requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 结合 Scroller 实现平滑移动
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
    }


    /**
     * 设置侧栏菜单显示位置
     */
    public void setSwipeModel(int swipeModel) {
        this.mSwipeModel = swipeModel;
    }

    /**
     * 设置视图
     */
    public void setView(View contentView, View menuView) {
        this.mContentView = contentView;
        this.mMenuView = menuView;
        this.removeAllViews();
        this.addView(mContentView);
        this.addView(mMenuView);
    }

    public View getContentView() {
        return this.mContentView;
    }

    public View getMenuView() {
        return this.mMenuView;
    }

    /**
     * 当手势抬起时，自动判断打开或关闭侧栏菜单
     */
    private void autoSelectActionAfterUp() {
        final int menuWidth = mMenuView.getMeasuredWidth();
        boolean isOpen = 1f * Math.abs(getScrollX()) / mMenuView.getMeasuredWidth() >= AUTO_SCROLL_OPEN_PERCENT;
        LogUtil.d("{0}/{1}={2}", getScrollX(), mMenuView.getMeasuredWidth(), (getScrollX()) / mMenuView.getMeasuredWidth());
        // 右菜单
        if (mSwipeModel == ViewHolder.SwipeModel.RIGHT) {
            scrollTo(isOpen ? menuWidth : 0, 0);
        }
        // 左菜单
        else if (mSwipeModel == ViewHolder.SwipeModel.LEFT) {
            scrollTo(isOpen ? menuWidth * -1 : 0, 0);
        }
    }

    /**
     * 是否是横向滑动
     */
    private boolean isHorizontalScroll(float currX, float currY) {
        float offsetX = Math.abs(currX - mDownX);
        float offsetY = Math.abs(currY - mDownY);
        LogUtil.d("move - offsetX:{0}, offsetY:{1}", offsetX, offsetY);
        // 没有达到最小滑动距离，不算滑动
        if (offsetX < mTouchSlop || offsetY < mTouchSlop) {
            LogUtil.d("offset distance < TouchSlop{0}", mTouchSlop);
            return false;
        }
        // 横向滑动距离/竖向滑动距离  不满足最小X/Y比率，不算横向滑动
        if (offsetX / offsetY < SCROLL_JUDGE_VERTICAL_HORIZONTAL) {
            LogUtil.d("X/Y < {0}", SCROLL_JUDGE_VERTICAL_HORIZONTAL);
            return false;
        }
        // 不能横向滑动
        if (mSwipeModel == ViewHolder.SwipeModel.NONE) {
            return false;
        }
        // 滑动方向错误，不满足: 右菜单 - 左滑 规则
        // 除非左滑显示右菜单
        if (mSwipeModel == ViewHolder.SwipeModel.RIGHT && currX > mDownX) {
            return getScrollX() > 0;
        }
        // 滑动方向错误，不满足: 左菜单 - 右滑
        // 除非右滑显示左菜单
        if (mSwipeModel == ViewHolder.SwipeModel.LEFT && currX < mDownX) {
            return getScrollX() < 0;
        }
        return true;
    }

    /**
     * 提交申请位移量，根据已位移情况，获取可移动距离
     */
    private int getScrollableDistance(int offsetX4Ask) {
        int offsetX4End = getScrollX() + offsetX4Ask;
        // 申请位移量没有超过限制
        if (offsetX4End >= mScrollRange[0] && offsetX4End <= mScrollRange[1]) {
            return offsetX4Ask;
        }
        // 申请位移量超过限制,需要计算可位移量
        int offsetX4Result;
        // View <--左移--
        if (offsetX4Ask >= 0) {
            offsetX4Result = mScrollRange[1] - getScrollX();
        }
        // View <--右移--
        else {
            offsetX4Result = mScrollRange[0] - getScrollX();
        }
        return offsetX4Result;
    }
}
