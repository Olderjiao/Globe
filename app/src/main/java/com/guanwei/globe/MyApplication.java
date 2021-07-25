package com.guanwei.globe;

import android.app.Activity;
import android.app.Application;

import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    /*** 项目全局上下文 */
    private static MyApplication mAppContext;

    /*** 存储activity */
    public static List<Activity> activities;

    @Override
    public void onCreate() {
        activities = new ArrayList<>();

        super.onCreate();

        mAppContext = this;
        Utils.init(mAppContext);
        // 设置崩溃信息(发包前设置)
//        ExceptionCrashHandler.getInstance().init(this);
    }

    /**
     * 获取项目全局上下文
     *
     * @return 项目全局上下文
     */
    public static MyApplication getAppContext() {
        return mAppContext;
    }

    /*** 退出程序 */
    public static void exit() {
        for (Activity activity : activities) {
            activity.finish();
        }
    }
}
