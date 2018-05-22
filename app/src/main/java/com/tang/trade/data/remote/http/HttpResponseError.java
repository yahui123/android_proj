package com.tang.trade.data.remote.http;

/**
 * Created by leo on 2018/4/18.
 */

public class HttpResponseError {
    private String code;
    private String msg;
    private String info;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
