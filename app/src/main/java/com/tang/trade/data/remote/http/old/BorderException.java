package com.tang.trade.data.remote.http.old;

import com.tang.trade.data.remote.http.HttpResponseError;

import java.util.List;

/**
 * Created by langchen on 01/02/2018.
 */

public class BorderException extends RuntimeException {
    private String msg;
    private String errorCode;
    private List<HttpResponseError> list;


    public BorderException(String message) {
        super(errorMsg(message));
        this.msg = message;
    }

    public BorderException(String message,String errorCode) {
        this.msg = message;
        this.errorCode = errorCode;
    }

    public BorderException(List<HttpResponseError> list){
        this.list = list;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    private static String errorMsg(String msg) {
        return msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public List<HttpResponseError> getList() {
        return list;
    }

    public void setList(List<HttpResponseError> list) {
        this.list = list;
    }
}
