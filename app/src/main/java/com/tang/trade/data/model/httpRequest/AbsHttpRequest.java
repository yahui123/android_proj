package com.tang.trade.data.model.httpRequest;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2018/4/2.
 */

public class AbsHttpRequest {
    private static final String TAG = "BaseHttpReqest";

    @Override
    public String toString() {
        String gson = new Gson().toJson(this);
        Log.i(TAG, "toString: params=" + gson);
        return gson;
    }
}
