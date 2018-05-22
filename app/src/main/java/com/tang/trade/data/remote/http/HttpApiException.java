package com.tang.trade.data.remote.http;

/**
 * Created by leo on 02/03/2018.
 */

public class HttpApiException extends RuntimeException {
    private String code;
    private String msg;
    private String httpCode;

    public HttpApiException(String httpCode, String code, String msg) {
        super(errorMsg(httpCode, code, msg));
        this.httpCode = httpCode;
        this.code = code;
        this.msg = msg;
    }

    private static String errorMsg(String httpCode, String code, String msg) {
        return "httpCode: " + httpCode + "code: " + code + "; msg: " + msg;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public String getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }
}
