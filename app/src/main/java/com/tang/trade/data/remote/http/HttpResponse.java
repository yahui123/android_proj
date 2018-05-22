package com.tang.trade.data.remote.http;

/**
 * Created by leo on 01/03/2018.
 */

public class HttpResponse<T> {
    public static final String CODE_SUCCESS = "200";
    public static final String CODE_FAIL = "250";
    public static final String CODE_TOEKN_OUT = "300";
    public static final String CODE_SERVER_EXCEPTION = "500";
    public static final String CODE_TRADE_FAILED = "E028";
    private String code;
    private String msg;
    private T data;

    public String getCode() {
        return code == null ? "" : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
