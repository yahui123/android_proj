package com.tang.trade.utils;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Generate16 {
    /**
     * 将数据长度定为16位
     * @param secondPassword
     * @return
     */
    public static String Generate16Number(String secondPassword) {
        if (secondPassword.length() < 16) {
            StringBuilder builder = new StringBuilder(secondPassword);
            for (int i = 0; i < 16 - secondPassword.length(); i++) {
                builder.append(" ");
            }
            secondPassword = builder.toString();
        } else if (secondPassword.length() > 16) {
            secondPassword = secondPassword.substring(0, 16);
        }
        return secondPassword;
    }
}
