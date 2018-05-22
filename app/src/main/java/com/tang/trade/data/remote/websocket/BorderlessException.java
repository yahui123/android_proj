package com.tang.trade.data.remote.websocket;

/**
 * Created by admin on 01/02/2018.
 */

public class BorderlessException extends RuntimeException {
    private String msg;
    private String code;

    public BorderlessException(String message) {
        super(errorMsg(message));
        this.msg = message;
    }

    public BorderlessException(String message,String code) {
        this.msg = message;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
