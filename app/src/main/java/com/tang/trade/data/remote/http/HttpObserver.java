package com.tang.trade.data.remote.http;

import io.reactivex.Observer;

/**
 * Created by leo on 05/03/2018.
 */

public abstract class HttpObserver<T> implements Observer<T> {
    private static final String TAG = "HttpObserver";
    private HttpError mHttpError;

    @Override
    public void onError(Throwable e) {
        mHttpError = new HttpError();

        if (e instanceof HttpApiException) {
            mHttpError.setErrorMessage(((HttpApiException) e).getMsg());
            mHttpError.setErrorCode(((HttpApiException) e).getCode());
            mHttpError.setHttpCode(((HttpApiException) e).getHttpCode());

        } else {

            HttpExceptionHandler.ResponseThrowable ex = HttpExceptionHandler.handleException(e);
            mHttpError.setErrorMessage(ex.message);
            mHttpError.setErrorCode(String.valueOf(ex.code));
            mHttpError.setHttpCode(String.valueOf(ex.httpCode));
        }

        onError(mHttpError);
    }

    public abstract void onError(HttpError httpError);
}
