package com.tang.trade.tang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.tang.trade.tang.utils.*;
import com.tang.trade.tang.utils.BuildConfig;

/**
 * Created by dagou on 2017/9/30.
 */

public class PackageInstallReceiver extends BroadcastReceiver {
    private static PackageInstallReceiver mReceiver = new PackageInstallReceiver();
    private static IntentFilter mIntentFilter;

    public static void registerReceiver(Context context) {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addDataScheme("com.tang.trade.tang");
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        context.registerReceiver(mReceiver, mIntentFilter);
    }

    public static void unregisterReceiver(Context context) {
        context.unregisterReceiver(mReceiver);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            TLog.log("有应用被添加");
//            Toast.makeText(context, "", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            TLog.log("有应用被删除");
        }
    /*
    * else if(Intent.ACTION_PACKAGE_CHANGED.equals(action)){
    * Toast.makeText(context, "有应用被改变", Toast.LENGTH_LONG).show(); }
    */
        else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
//            Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();
            TLog.log("有应用被替换");
            String packageName = intent.getData().getSchemeSpecificPart();
            TLog.log(packageName);
            TLog.log(Device.getRunningActivityName(MyApp.context()));
            if (packageName == Device.getRunningActivityName(MyApp.context())) {
                BuildConfig.IS_UPDATE = true;
            }
        }
    /*
    * else if(Intent.ACTION_PACKAGE_RESTARTED.equals(action)){
    * Toast.makeText(context, "有应用被重启", Toast.LENGTH_LONG).show(); }
    */
    /*
    * else if(Intent.ACTION_PACKAGE_INSTALL.equals(action)){
    * Toast.makeText(context, "有应用被安装", Toast.LENGTH_LONG).show(); }
    */
    }
}
