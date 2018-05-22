package com.tang.trade.data.remote.http;

import com.tang.trade.data.remote.http.old.BorderException;

import java.util.List;

import io.reactivex.functions.Function;

/**
 * Created by daibin on 2018/4/18.
 */

public class HttpFunction<T> implements Function<HttpBaseResponse<T>,T> {
    @Override
    public T apply(HttpBaseResponse<T> baseResponse) throws Exception {

        if (baseResponse.getErrors() != null) {
            //失败
            List<HttpResponseError> errors = baseResponse.getErrors();
            throw new BorderException(errors.get(0).getMsg());

        } else if (baseResponse.getSuccess() != null){
            //成功
            if (baseResponse.getSuccess().getData() == null) {
                return (T) Irrelevant.INSTANCE;
            }
            HttpResponseSuccess<T> success = baseResponse.getSuccess();
            return success.getData();

        }else {
            throw new BorderException("服务器异常");
        }
    }
    enum Irrelevant {INSTANCE}

}