package com.tang.trade.tang;

import android.text.TextUtils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by dagou on 2017/9/12.
 */

public class AppConfig {
    public static final  String IS_FIRST_COMING = "isFirstComing";
    public static final  String TECENT_BUGLY_APPID = "a1018226bd";


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
