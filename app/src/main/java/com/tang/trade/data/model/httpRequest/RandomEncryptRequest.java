package com.tang.trade.data.model.httpRequest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by daibin on 2018/4/26.
 */

public class RandomEncryptRequest extends AbsHttpRequest{

    @SerializedName("qrCodeKey")
    private String md5Key;

    private String phone;
    private String code;
    private String identityCode;

    public RandomEncryptRequest(String md5Key, String phone, String code, String identityCode) {
        this.md5Key = md5Key;
        this.phone = phone;
        this.code = code;
        this.identityCode = identityCode;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIdentityCode() {
        return identityCode;
    }

    public void setIdentityCode(String identityCode) {
        this.identityCode = identityCode;
    }
}
