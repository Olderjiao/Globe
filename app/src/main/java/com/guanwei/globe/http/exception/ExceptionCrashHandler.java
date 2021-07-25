package com.guanwei.globe.http.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import com.guanwei.globe.utils.SharedPrefsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {

    // 单例设计模式
    private static ExceptionCrashHandler mInstance;
    // 留下原来的，便于开发的时候调试
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // 上下文  获取版本信息和手机信息
    private Context mContext;

    public static ExceptionCrashHandler getInstance() {
        if (mInstance == null) {
            synchronized (ExceptionCrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new ExceptionCrashHandler();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        Thread.currentThread().setUncaughtExceptionHandler(this);
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.mContext = context;
    }


    private ExceptionCrashHandler() {
    }

    /**
     * Method invoked when the given thread terminates due to the
     * given uncaught exception.
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     *
     * @param t  the thread
     * @param ex the exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        Log.w("MY_TAG", "uncaughtException===>到拦截闪退信息");
        // 1. 获取信息
        // 1.1 崩溃信息
        // 1.2 手机信息
        // 1.3 版本信息
        // 2.写入文件
        String crashFileName = saveInfoToSD(ex);

        Log.w("MY_TAG", "uncaughtException_fileName===>" + crashFileName);

        // 3. 缓存崩溃日志文件
        cacheCrashFile(crashFileName);
        // 系统默认处理
        mDefaultHandler.uncaughtException(t, ex);
    }

    /**
     * 缓存崩溃日志文件
     *
     * @param fileName packageName_preferences
     */
    private void cacheCrashFile(String fileName) {
        SharedPrefsUtil.setStringPreference(mContext, "CRASH_FILE_NAME", fileName);
    }

    /**
     * 获取崩溃文件名称
     *
     * @return
     */
    public File getCrashFile() {
        String crashFileName = SharedPrefsUtil.getStringPreference(mContext, "CRASH_FILE_NAME");
        Log.w("MY_TAG", "getCrashFile===>" + crashFileName);
        //第一次安装apk没有产生文件，获取不到文件名
        if (TextUtils.isEmpty(crashFileName)) {
            return null;
        }
        return new File(crashFileName);
    }

    /**
     * 保存获取的 软件信息，设备信息和出错信息保存在SDcard中
     *
     * @param ex
     * @return
     */
    private String saveInfoToSD(Throwable ex) {
        String fileName = null;
        StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, String> entry : obtainSimpleInfo(mContext)
                .entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }

        sb.append(obtainExceptionInfo(ex));

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(mContext.getFilesDir() + File.separator + mContext.getPackageName() + "_preferences"
                    + File.separator);

            // 先删除之前的异常信息
            if (dir.exists()) {
                deleteDir(dir);
            }

            // 再从新创建文件夹
            if (!dir.exists()) {
                dir.mkdir();
            }
            try {
                fileName = dir.toString()
                        + File.separator
                        + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    /**
     * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
     *
     * @return
     */
    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        map.put("versionName", mPackageInfo.versionName);
        map.put("versionCode", "" + mPackageInfo.versionCode);
        map.put("MODEL", "" + Build.MODEL);
        map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
        map.put("PRODUCT", "" + Build.PRODUCT);
        map.put("MOBLE_INFO", getMobileInfo());
        return map;
    }

    /**
     * Cell phone information
     *
     * @return
     */
    public static String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 获取系统未捕捉的错误信息
     *
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful. If a
     * deletion fails, the method stops attempting to delete and returns
     * "false".
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return true;
    }

    /*** 返回当前日期根据格式 */
    private String getAssignTime(String dateFormatStr) {
        DateFormat dataFormat = new SimpleDateFormat(dateFormatStr);
        long currentTime = System.currentTimeMillis();
        return dataFormat.format(currentTime);
    }
}
