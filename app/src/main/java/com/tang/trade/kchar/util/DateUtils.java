package com.tang.trade.kchar.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Adu on 2015/10/7.
 */
public class DateUtils {

    public static String dateFormatyMdHm(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date(time);
        return df.format(date);
    }

    public static String dateFormatyMd(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(time);
        return df.format(date);
    }

    public static String dateFormatHm(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        return df.format(date);
    }


}
