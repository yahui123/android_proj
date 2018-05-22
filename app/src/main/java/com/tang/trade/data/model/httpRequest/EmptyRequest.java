package com.tang.trade.data.model.httpRequest;

/**
 * Created by Administrator on 2018/4/2.
 */

public class EmptyRequest extends AbsHttpRequest {
    private String type;

    public EmptyRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
