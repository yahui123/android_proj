package com.tang.trade.data.model.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/4/26.
 */

public class ChangePhoneBean {
    private String phone;
    private String code;
    private String identityCode;
    @SerializedName("qrCodeKey")
    private String md5Key;

    public ChangePhoneBean(String phone, String code, String identityCode, String md5Key) {
        this.phone = phone;
        this.code = code;
        this.identityCode = identityCode;
        this.md5Key = md5Key;
    }
}
