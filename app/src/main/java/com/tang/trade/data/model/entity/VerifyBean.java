package com.tang.trade.data.model.entity;

/**
 * Created by Administrator on 2018/4/25.
 */

public class VerifyBean {
    public String phone;
    public String code;
    public String identityCode;

    public VerifyBean(String phone, String code, String identityCode) {
        this.phone = phone;
        this.code = code;
        this.identityCode = identityCode;
    }
}
