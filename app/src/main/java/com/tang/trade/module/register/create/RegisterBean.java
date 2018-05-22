package com.tang.trade.module.register.create;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/4/25.
 */

public class RegisterBean {
    @SerializedName("account")
    private String accountname;
    @SerializedName("publicKey")
    private String publickey;
    @SerializedName("referrer")
    private String referreraccount;

    public RegisterBean(String accountname, String publickey, String referreraccount) {
        this.accountname = accountname;
        this.publickey = publickey;
        this.referreraccount = referreraccount;
    }
}
