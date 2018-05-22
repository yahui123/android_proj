package com.tang.trade.data.model.httpRequest;

/**
 * Created by Administrator on 2018/4/8.
 */

public class RegisterRequest extends AbsHttpRequest{
    private String accountname;
    private String publickey;
    private  String referreraccount;

    public RegisterRequest(String accountname, String publickey, String referreraccount) {
        this.accountname = accountname;
        this.publickey = publickey;
        this.referreraccount = referreraccount;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getPublickey() {
        return publickey;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }

    public String getReferreraccount() {
        return referreraccount;
    }

    public void setReferreraccount(String referreraccount) {
        this.referreraccount = referreraccount;
    }
}
