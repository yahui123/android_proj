package com.tang.trade.utils;

import com.tang.trade.data.local.DatabaseManager;

/**
 * Created by daibin on 2018/5/8.
 *
 */
public class DatabaseUtil {

    /**
     * @param msgKey
     * @param defaultString 默认显示必须传，防止网络异常时没获取到信息
     * @return
     */
    public static String getMsg(String msgKey, String defaultString) {
        String msg_key = DatabaseManager.getInstance().borderDao().queryOne(msgKey);

        if (android.text.TextUtils.isEmpty(msg_key)) {
            msg_key = defaultString;
        }
        String replace = msg_key.replace("\\n", "\n");
        return replace;
    }

}
