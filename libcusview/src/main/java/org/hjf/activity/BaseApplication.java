package org.hjf.activity;

import android.app.Application;
import android.content.Context;

import org.hjf.util.ThreadUtil;


/**
 * Application 基类
 */
public class BaseApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        // 默认进程
        if (getPackageName().equals(ThreadUtil.getCurrProgressName(getApplicationContext()))){
            setContext();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 默认进程
        if (getPackageName().equals(ThreadUtil.getCurrProgressName(getApplicationContext()))){
            destroyDefaultProgress();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    /**
     * 默认进程销毁动作
     */
    private void destroyDefaultProgress() {
        mContext = null;
    }

    /**
     * 设置全局 Context 对象
     */
    private void setContext(){
        if (mContext == null){
            synchronized (BaseApplication.class){
                if (mContext == null){
                    mContext = getApplicationContext();
                }
            }
        }
    }

    /**
     * 获取全局 Context 对象
     */
    public static Context getContext() {
        return mContext;
    }
}
