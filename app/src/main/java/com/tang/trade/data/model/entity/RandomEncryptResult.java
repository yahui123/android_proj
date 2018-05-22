package com.tang.trade.data.model.entity;

import com.tang.trade.data.model.httpRequest.AbsHttpRequest;

/**
 * Created by daibin on 2018/4/26.
 */

public class RandomEncryptResult extends AbsHttpRequest {
    private String encryptKey;

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
}
