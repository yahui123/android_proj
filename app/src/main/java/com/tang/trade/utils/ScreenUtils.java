package com.tang.trade.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {
    public static double getScreenAspect(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        float density = dm.density;// 屏幕密度（像素比例：0.75/1.0/1.5/2.0）

        int densityDPI = dm.densityDpi;// 屏幕密度（每寸像素：120/160/240/320）

        int screenWidth = dm.widthPixels;// 屏幕宽（像素，如：3200px）

        int screenHeight = dm.heightPixels;// 屏幕高（像素，如：1280px）

        return (double) screenHeight / (double) screenWidth;
    }
}
