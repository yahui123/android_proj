package com.tang.trade.data.remote.http.old;


import com.tang.trade.data.remote.http.HttpExceptionHandler;
import com.tang.trade.data.remote.http.HttpResponseError;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;

/**
 * Created by leo on 08/01/2018.
 */

public abstract class BorderObserver<T> implements Observer<T> {

    @Override
    public void onError(Throwable e) {

        if (e instanceof BorderException) {

//            onError(((BorderException) e).getList());
            onError(e.getMessage());
        } else {

            HttpExceptionHandler.ResponseThrowable ex = HttpExceptionHandler.handleException(e);
//            List<HttpResponseError> list = new ArrayList<>();
//            HttpResponseError error = new HttpResponseError();
//            error.setCode("code");
//            error.setInfo("info");
//            error.setMsg(ex.message);
//            list.add(error);
            onError(ex.message);
        }
    }

//    public abstract void onError(List<HttpResponseError> errors);
    public abstract void onError(String errorMsg);
}
