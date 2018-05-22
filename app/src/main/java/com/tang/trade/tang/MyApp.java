package com.tang.trade.tang;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.Gravity;
import android.widget.Toast;


import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.aop.AdviceBinder;
import com.alibaba.mobileim.aop.PointCutEnum;
import com.alibaba.wxlib.util.SysUtil;
import com.tang.trade.data.local.BorderDatabase;
import com.tang.trade.tang.aliui.ChattingOperationCustomSample;
import com.tang.trade.tang.aliui.ChattingUICustomSample;
import com.tang.trade.tang.aliui.ConversationListOperationCustomSample;
import com.tang.trade.tang.aliui.ConversationListUICustomSample;
import com.tang.trade.tang.aliui.MyYWSDKGlobalConfig;
import com.tang.trade.tang.net.OkGoClient;
import com.tang.trade.tang.utils.ActivityManagerUrils;
import com.tencent.bugly.crashreport.CrashReport;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by dagou on 2017/9/11.
 */

public class MyApp extends MultiDexApplication {
    private static final String TAG = MyApp.class.getSimpleName();
    private static final String PREF_NAME = "tang.pref";
    private static Context _context;
    private boolean isDebug = true;
    public static String CURRENT_NODE = "";
    public static List<Activity> activityList;
    public static String BDS_CO_PUBLICKEY = "";
    private static BorderDatabase db;

    //云旺OpenIM的DEMO用到的Application上下文实例
    public static Context getContext() {
        return _context;
    }

    //阿里百川appkey
    public static String APP_KEY = "24738110";

    @Override
    public void onCreate() {
        super.onCreate();


//        PgyCrashManager.register(this);//蒲公英
//
//        MyCrashHandler myCrashHandler=MyCrashHandler.getInstance();
//        myCrashHandler.init(this);

        //腾讯bugly
        CrashReport.initCrashReport(getApplicationContext(), "fbae0e8eb1", false);

        if (mustRunFirstInsideApplicationOnCreate()) {
            //todo 如果在":TCMSSevice"进程中，无需进行openIM和app业务的初始化，以节省内存
            return;
        }

        YWAPI.init(this, APP_KEY);

        //第二步：在Applicatoin类里将这个自定义接口绑定到单聊界面，加入以下代码（对于单聊界面的所有自定义UI定制只需要加一次）
        AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_OPERATION_POINTCUT, ChattingOperationCustomSample.class);
        AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_UI_POINTCUT, ChattingUICustomSample.class);
        AdviceBinder.bindAdvice(PointCutEnum.YWSDK_GLOBAL_CONFIG_POINTCUT, MyYWSDKGlobalConfig.class);
        AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_UI_POINTCUT, ConversationListUICustomSample.class);
        AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_OPERATION_POINTCUT, ConversationListOperationCustomSample.class);

        OkGoClient.init();


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public static MyApp context() {
        return (MyApp) _context;
    }

    public static void set(String key, int value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void set(String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void set(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static boolean get(String key, boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    public static String get(String key, String defValue) {
        return getPreferences().getString(key, defValue);
    }

    public static int get(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    public static long get(String key, long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    public static float get(String key, float defValue) {
        return getPreferences().getFloat(key, defValue);
    }

    public static SharedPreferences getPreferences() {
        return context().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void showToast(int message) {
        showToast(message, Toast.LENGTH_LONG, 0);
    }

    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_LONG, 0, Gravity.BOTTOM);
    }

    public static void showToast(int message, int icon) {
        showToast(message, Toast.LENGTH_LONG, icon);
    }

    public static void showToast(String message, int icon) {
        showToast(message, Toast.LENGTH_LONG, icon, Gravity.BOTTOM);
    }

    public static void showToastShort(int message) {
        showToast(message, Toast.LENGTH_SHORT, 0);
    }

    public static void showToastShort(String message) {
        showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM);
    }

    public static void showToastShort(int message, Object... args) {
        showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM, args);
    }

    public static void showToast(int message, int duration, int icon) {
        showToast(message, duration, icon, Gravity.BOTTOM);
    }

    public static void showToast(int message, int duration, int icon,
                                 int gravity) {
        showToast(context().getString(message), duration, icon, gravity);
    }

    public static void showToast(int message, int duration, int icon, int gravity, Object... args) {
        showToast(context().getString(message, args), duration, icon, gravity);
    }

    public static void showToast(String message, int duration, int icon, int gravity) {
        if (_context != null) {
            Toast.makeText(_context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static String getStringDouble5(String str) {
        BigDecimal decimal = new BigDecimal(str);

        String setScale1 = decimal.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return setScale1;
    }

    public void finishAllActivity() {
        ActivityManagerUrils.getActivityManager().popAllActivity();
        //  Log.e("MyService", "finishAllActivity: ");
        System.exit(0);//正常退出App
        System.gc();
    }

    private boolean mustRunFirstInsideApplicationOnCreate() {
        //必须的初始化
        SysUtil.setApplication(this);
        _context = getApplicationContext();
        return SysUtil.isTCMSServiceProcess(_context);
    }

}
