package org.hjf.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.hjf.log.LogUtil;


/**
 * 可能涉及到 Fragment - ViewPager - Fragment 使用这个方法：new TypesAdapter(getChildFragmentManager())
 * 博客（ViewPager适配器讲解）：http://blog.csdn.net/getchance/article/details/40263505
 */
/**
 * ：
 * 实现Fragment状态保存的新姿势 ： TODO
 * 不在 Fragment 中对View进行数据的保存和恢复，让 View 自己实现状态的保存和恢复。这也更符合 Android 的设计
 * 使用 Fragment、ViewPager、CusView 的结构
 * 1.【StatedFragment】、【NestedActivityResultFragment】
 * 		地址：https://github.com/nuuneoi/StatedFragment
 * 		地址：https://zhuanlan.zhihu.com/p/22141193
 */

/**
 * 情景一（屏幕旋转）：较为简单。
 * -- 存储数据：onSaveInstanceState()
 * -- 恢复数据：onActivityCreated() 或 onViewStateRestored()
 * <p>
 * <p>
 * 情景二（Fragment 中 回退栈返回 ）
 * 储存数据：不会调用 onSaveInstanceState()，会调用 onDestroyView()
 * 			这种情况下我们无需保存 Fragment 数据，因为只销毁了 Fragment 中的 View 的状态，使 View 回到了XML 布局的原始状态
 * 			需要注意的是：View 的状态不要再 Fragment 中的任务方法保存，请在使用 View 内部实现数据的存储和恢复，并为此 View 设置ID
 * <p>
 * <p>
 * 情景三（回退栈中有一个以上 Fragment 的时候旋转两次，会崩溃）没有最变态，只有更变态
 * 第一次旋转屏幕：onSaveInstanceState() 被调用，Fragment 数据如期保存
 * 				 onDestroyView() 被调用，View 数据被销毁。
 * 				  千万不要再此操作 View 状态保存，请在 View 内部实现。
 * 第二次旋转屏幕：onSaveInstanceState() 被调用，由于第一次旋转时View被销毁。再次进行保存操作时肯定 NULLPoint 空指针异常
 * 				  所以别再 Fragment 中处理 View 的状态保存和恢复，这不安全也不符合 android 的设计
 */
@Deprecated
public abstract class StatedFragment extends BaseFragment {

    private static final String SAVE_DATA_TAG = "internalSavedViewState8954201239547";
    private static final String TIP1 = "StatedFragment 在保存状态是需要用到 Arguments 参数，请设置";

    private Bundle savedState;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //先执行 初始创建动作(加载数据) 或 数据恢复
        if (!restoreStateFromArguments()) {
            onFirstTimeLaunched();
        }
        //在执行数据更改，如：bindView() 方法
        super.onActivityCreated(savedInstanceState);
    }

    protected void onFirstTimeLaunched() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveData2Arguments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveData2Arguments();
    }

    /**
     * 保存数据
     */
    private void saveData2Arguments() {
        if (getView() != null) {
            savedState = new Bundle();
            onSaveState(savedState);
        }
        Bundle b = getArguments();
        if (b == null) {
            throw new RuntimeException(TIP1);
        }
        if (savedState != null) {
            b.putBundle(SAVE_DATA_TAG, savedState);
        }
    }

    /**
     * 开始恢复数据，如没有保存信息代表是第一次启动
     */
    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();//可能没有设置 Arguments 参数
        if (b == null) {
            throw new RuntimeException(TIP1);
        }
        savedState =  b.getBundle(SAVE_DATA_TAG);
        if (savedState != null) {
            onRestoreState(savedState);
            return true;
        }
        // 没有保存的信息，是第一次启动 Fragment
        return false;
    }

    protected void onRestoreState(Bundle savedState) {
        LogUtil.d("恢复状态");
    }

    protected void onSaveState(Bundle state) {
        LogUtil.d("保存状态");
    }
}
