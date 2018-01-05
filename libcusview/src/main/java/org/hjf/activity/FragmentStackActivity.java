package org.hjf.activity;

import android.support.annotation.IdRes;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import org.hjf.view.R;


/**
 * 此 Activity 实现深度 Fragment 多层显示
 * 建立将显示内容尽量放入Fragment，Activity只显示关键内容，根据需求而定
 * 为 Fragment 提供保存和恢复数据的 Bundle 对象，key 值就用 Fragment 实例的 hashCode
 */
public abstract class FragmentStackActivity extends BaseActivity {

    /**
     * 获取深层显示Fragment内容的FrameLayout的ID
     */
    @IdRes
    public abstract int getFragmentContentId();

    /**
     * 增加Fragment显示，只支持一个 FrameLayout 置换时有回退效果
     * 因为 Fragment 在回退栈时执行 onDestroyView 销毁View，需要保存状态
     */
    public void addFragmentInBackStack(BaseFragment fragment, boolean needAnimation) {
        if (fragment == null) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 添加动画效果(进入：左出右进；退出：左进右出)，得在前面调用替换 Fragment 之前调用。
        if (needAnimation) {
            transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
        }

        transaction.replace(getFragmentContentId(), fragment, fragment.getClass().getName());
        // 将这个 transaction 加入BackStack，有Activity管理，按下返回键回到上一个Fragment
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        // 用户离开Activity之前会commit用户回复，在离开之后commit会抛不能回复此时提交内容的异常
        // 如果丢失也么关系，使用 commitAllowingStateLoss
        transaction.commitAllowingStateLoss();
    }

    /**
     * 移除Fragment
     */
    public void removeFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    /**
     * 捕获返回按键时间
     * onKeyDown 调用时间快于 onBackPressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            removeFragment();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
