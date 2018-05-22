package com.tang.trade.data.remote.http;

import android.text.TextUtils;

import io.reactivex.functions.Function;

/**
 * Created by leo on 01/03/2018.
 */

public class HttpFuntion<T> implements Function<HttpResponse<T>, T> {
    @Override
    public T apply(HttpResponse<T> httpResponse) throws Exception {

        if (TextUtils.isEmpty(httpResponse.getCode())) { //防止code为空
            throw new HttpApiException("200", "null", "未知错误");
        }
//
//        if (Integer.parseInt(httpResponse.getCode()) > 30000 || Integer.parseInt(httpResponse.getCode()) <= 20000) {
//            throw new HttpApiException("200", httpResponse.getCode(), httpResponse.getMsg());
//        }

        if (null == httpResponse.getData()) {
            throw new HttpApiException("200", httpResponse.getCode(), httpResponse.getMsg());
        }

        if (httpResponse.getData() == null) {
            return (T) Irrelevant.INSTANCE;
        }

//        if (Integer.parseInt(httpResponse.getCode()) >20000 && Integer.parseInt(httpResponse.getCode()) < 30000){
//            ((BaseEmpty) httpResponse.getData()).setSuccessMessage(httpResponse.getMsg());
//        }
        return httpResponse.getData();
    }

    enum Irrelevant {INSTANCE}

}
