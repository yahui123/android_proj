package com.tang.trade.tang.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dagou on 2017/9/12.
 */

public class Device {
    public static boolean isWifiOpen() {
        ConnectivityManager cm = (ConnectivityManager) MyApp
                .context().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        if (!info.isAvailable() || !info.isConnected()) return false;
        if (info.getType() != ConnectivityManager.TYPE_WIFI) return false;
        return true;
    }

    public static int getVersionCode() {
        return getVersionCode(MyApp.context().getPackageName());
    }

    public static int getVersionCode(String packageName) {
        try {
            return MyApp.context()
                    .getPackageManager()
                    .getPackageInfo(packageName, 0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            return 0;
        }
    }

    // check all network connect, WIFI or mobile
    public static boolean isNetworkAvailable(final Context context) {
        boolean hasWifoCon = false;
        boolean hasMobileCon = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfos = cm.getAllNetworkInfo();
        for (NetworkInfo net : netInfos) {
            String type = net.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {

                if (net.isConnected()) {
                    hasWifoCon = true;
                }
            }

            if (type.equalsIgnoreCase("MOBILE")) {

                if (net.isConnected()) {
                    hasMobileCon = true;
                }
            }
        }
        return hasWifoCon || hasMobileCon;

    }

    public static boolean sdcardExit() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        return sdCardExist;
    }

    public static void openFile(Context mContext, File f) {
        // mProgressDialog.dismiss();
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(mContext, "tang", f);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(f);
        }

        // 设定intent的file与MimeType，安装文件
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        BuildConfig.UPDATE = true;
    }

    /**
     * 版本号比较
     * 0代表相等，1代表version1大于version2，-1代表version1小于version2
     *
     * @param version1
     * @param version2
     * @return
     */
    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");
        Log.d("HomePageActivity", "version1Array==" + version1Array.length);
        Log.d("HomePageActivity", "version2Array==" + version2Array.length);
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;
        // 循环判断每位的大小
        Log.d("HomePageActivity", "verTag2=2222=" + version1Array[index]);
        while (index < minLen
                && (diff = Integer.parseInt(version1Array[index])
                - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public static String getRunningActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //完整类名
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        String contextActivity = runningActivity.substring(runningActivity.lastIndexOf(".") + 1);
        return contextActivity;
    }

    public static String getNetSleep(String url){

        String sleep = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("ping -c 1 " + url);
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str ;

            while((str=buf.readLine())!=null){
                if(str.contains("avg")){
                    int i=str.indexOf("/", 20);
                    int j=str.indexOf(".", i);
                    sleep =str.substring(i+1, j);
                    //sleep = sleep+"ms";
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  sleep;
    }

    public static boolean pingIpAddress() {
//        int status = -1;
//
//        if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()){
//            try {
//                String node = MyApp.CURRENT_NODE;
//                status =  CliCmdExecutor.TryConnect("4a93e8abe6ab5f2b935d692e13eea73cdbfb288959fb41640b829d25b7f4bd84",node);
//            } catch (JNIException e) {
//                e.printStackTrace();
//            }
//
//            if (status == -1) {
//                return false;
//            }
//
//        }else {
//
//        }

        global_property_object global_property_object = null;
        int len = MyApp.CURRENT_NODE.substring(MyApp.CURRENT_NODE.lastIndexOf(":")+1,MyApp.CURRENT_NODE.length()).length();

        if (len == 5) {
            try {
                global_property_object = BitsharesWalletWraper.getInstance().get_global_properties();
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }else {
            return false;
        }

        if (global_property_object == null){
            return false;
        }

        return true;
    }
}
