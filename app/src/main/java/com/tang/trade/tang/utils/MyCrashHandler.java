package com.tang.trade.tang.utils;

import android.content.Context;
import android.os.Looper;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/10.
 */

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static MyCrashHandler instance = new MyCrashHandler();
    //程序的Context对象
    private Context mContext;
    private static final String TAG = MyCrashHandler.class.getSimpleName();
    /**
     * 崩溃应用包名KEY
     */
    private final String KEY_PKG = "key_pkg";
    /**
     * APP版本号KEY
     */
    private final String KEY_VER = "key_ver";
    /**
     * APP渠道号KEY
     */
    private final String KEY_CXHZCHANNEL = "key_cxhzchannel";
    /**
     * 厂商
     */
    private final String KEY_BRAND = "key_brand";
    /**
     * 机型
     */
    private final String KEY_MODEL = "key_model";
    /**
     * 系统版本KEY
     */
    private final String KEY_OS_VER = "key_os_ver";
    /**
     * 崩溃时间KEY
     */
    private final String KEY_TIME = "key_time";
    //用来存储设备信息和异常信息 系统崩溃日志
    private Map<String, String> infos;
    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter;

    /**
     * 保证只有一个CrashHandler实例
     */
    private MyCrashHandler() {
        infos = new HashMap<String, String>();
        formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static MyCrashHandler getInstance() {
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(final Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 初始化数据
     *
     * @param pkg
     */
    public void init(String pkg) {

    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        boolean handled = false;
        if (ex != null) {
            // 上传奔溃日志
            /// postCrash(getCrashInfo(ex));
           // PgyCrashManager.reportCaughtException(mContext,getCrashInfo(ex));
//            PgyCrashManager.reportCaughtException(mContext,(Exception) ex);
            // 保存奔溃日志

            System.exit(0);

//            writeCrashInfo(ex);
            //使用Toast来显示异常信息
//            if (infos.get(KEY_PKG).equals(mContext.getPackageName())) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Looper.prepare();
////                        Toast.makeText(mContext, "程序发生异常，正在退出……", Toast.LENGTH_LONG).show();
//                        try {
//                            sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                        Looper.loop();
//                    }
//                }.start();
////            }
            handled = true;
        }
        return handled;

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        handleException(e);


//        android.os.Process.killProcess(android.os.Process.myPid());


    }
}
