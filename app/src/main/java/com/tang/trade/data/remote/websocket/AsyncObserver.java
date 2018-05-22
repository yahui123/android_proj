package com.tang.trade.data.remote.websocket;

import com.tang.trade.data.remote.http.DataError;

import io.reactivex.Observer;

/**
 * Created by Administrator on 2018/4/8.
 */

public abstract class AsyncObserver<T> implements Observer<T> {
    @Override
    public void onError(Throwable e) {
        DataError error = new DataError();

        if (e instanceof BorderlessException){

            error.setErrorMessage(((BorderlessException) e).getMsg());
            error.setErrorCode(((BorderlessException) e).getCode());
        }

        onError(error);
    }

    public abstract void onError(DataError error);
}
