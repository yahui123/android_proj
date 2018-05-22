package com.tang.trade.data.remote.http;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by leo on 2018/4/18.
 */

public class HttpBaseResponse<T> {
    @SerializedName("errors")
    private List<HttpResponseError>  mErrors;

    @SerializedName("success")
    private HttpResponseSuccess<T> mSuccess;

    public List<HttpResponseError> getErrors() {
        return mErrors;
    }

    public void setErrors(List<HttpResponseError> errors) {
        mErrors = errors;
    }

    public HttpResponseSuccess<T> getSuccess() {
        return mSuccess;
    }

    public void setSuccess(HttpResponseSuccess<T> success) {
        mSuccess = success;
    }
}
