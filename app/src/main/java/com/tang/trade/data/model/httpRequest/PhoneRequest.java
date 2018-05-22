package com.tang.trade.data.model.httpRequest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by daibin on 2018/4/26.
 */

public class PhoneRequest extends AbsHttpRequest {
    @SerializedName("qrCodeKey")
    private String md5Key;

    public PhoneRequest(String md5Key) {
        this.md5Key = md5Key;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }
}
