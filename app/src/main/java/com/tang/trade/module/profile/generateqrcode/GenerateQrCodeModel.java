package com.tang.trade.module.profile.generateqrcode;

import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;

/**
 * Created by Administrator on 2018/4/17.
 */

public class GenerateQrCodeModel implements GenerateQrCodeContract.Model {

    @Override
    public void sendSMS(String phone, BorderObserver<SMSResult> observer) {
        HttpDataManager.getInstance().sendSMS(phone, observer);
    }

    @Override
    public void saveQrCodeMessage(String phone, String code, String identityCode, String md5Key, String encryptKey, BorderObserver<Object> observer) {
        HttpDataManager.getInstance().saveQrCode(phone, code, identityCode, md5Key, encryptKey, observer);
    }
}
