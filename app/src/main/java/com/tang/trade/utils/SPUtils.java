package com.tang.trade.utils;

import com.flh.framework.preferences.PreferenceManager;

import java.io.Serializable;

/**
 * Created by leo on 02/03/2018.
 */

public class SPUtils {
    public static void put(String key, int value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static void put(String key, float value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static void put(String key, long value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static void put(String key, String value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static void put(String key, Serializable value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static void put(String key, boolean value) {
        PreferenceManager.getDefault().put(key, value);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return PreferenceManager.getDefault().getBoolean(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return PreferenceManager.getDefault().getInt(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return PreferenceManager.getDefault().getFloat(key, defValue);
    }

    public static String getString(String key, String defValue) {
        return PreferenceManager.getDefault().getString(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return PreferenceManager.getDefault().getLong(key, defValue);
    }

    public static <T extends Serializable> T getSerializable(String key) {
        return PreferenceManager.getDefault().getSerializable(key);
    }

    public static void remove(String key) {
        PreferenceManager.getDefault().remove(key);
    }
}
