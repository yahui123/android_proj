package com.tang.trade.data.model.entity;

import com.tang.trade.data.model.httpRequest.AbsHttpRequest;

/**
 * Created by Administrator on 2018/4/25.
 */

public class QrCodeBean extends AbsHttpRequest {
    private String phone;
    private String qrCodeKey;
    private String encryptKey;
    private String code;
    private String identityCode;

    public QrCodeBean(String phone, String code, String identityCode, String md5Key, String encryptKey) {
        this.phone = phone;
        this.qrCodeKey = md5Key;
        this.encryptKey = encryptKey;
        this.code = code;
        this.identityCode = identityCode;
    }
}
