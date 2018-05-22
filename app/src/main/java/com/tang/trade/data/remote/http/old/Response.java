package com.tang.trade.data.remote.http.old;

/**
 * Created by leo on 29/12/2017.
 */

public class Response<T> {
    private static final String SUCCESS = "success";
    private String status;
    private String msg;
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isOK() {
        return status.equals(SUCCESS);
    }
}
