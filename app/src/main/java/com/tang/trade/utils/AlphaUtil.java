package com.tang.trade.utils;

import android.view.View;

import com.tang.trade.module.splash.SplashContract;

/**
 * Created by daibin on 2018/5/4.
 */

public class AlphaUtil {

    public static void setEnable(View view) {
        view.setClickable(true);
        view.setAlpha(1.0f);
    }

    public static void setDisable(View view) {
        view.setClickable(false);
        view.setAlpha(0.4f);
    }
}
