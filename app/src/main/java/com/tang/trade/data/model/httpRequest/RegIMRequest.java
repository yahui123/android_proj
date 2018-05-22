package com.tang.trade.data.model.httpRequest;

/**
 * Created by Administrator on 2018/4/8.
 */

public class RegIMRequest extends AbsHttpRequest{
    private String bdsaccount;

    public RegIMRequest(String bdsaccount) {
        this.bdsaccount = bdsaccount;
    }

    public String getBdsaccount() {
        return bdsaccount;
    }

    public void setBdsaccount(String bdsaccount) {
        this.bdsaccount = bdsaccount;
    }
}
