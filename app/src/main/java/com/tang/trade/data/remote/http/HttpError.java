package com.tang.trade.data.remote.http;


/**
 * Created by leo on 09/03/2018.
 */

public class HttpError extends DataError {
    private String httpCode;

    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }
}
