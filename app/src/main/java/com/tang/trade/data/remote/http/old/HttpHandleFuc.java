package com.tang.trade.data.remote.http.old;

import android.util.Log;

import io.reactivex.functions.Function;

/**
 * Created by leo on 29/12/2017.
 */

public class HttpHandleFuc<T> implements Function<Response<T>, T> {
    private static final String TAG = HttpHandleFuc.class.getSimpleName();

    @Override
    public T apply(Response<T> response) throws Exception {
        Log.d(TAG, "apply: ");
        if (!response.isOK()) {
            if (response.getMsg().equals("nodata")) {
                throw new BorderException("nodata");
            } else {
                throw new BorderException(response.getMsg() != null ? response.getMsg() : "");
            }
        }

        return response.getData();
    }
}
