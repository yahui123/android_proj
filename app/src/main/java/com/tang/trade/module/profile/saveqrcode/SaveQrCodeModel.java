package com.tang.trade.module.profile.saveqrcode;

import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;

/**
 * Created by Administrator on 2018/4/17.
 */

public class SaveQrCodeModel implements SaveQrCodeContract.Model {


    @Override
    public void sendSMS(String phone, BorderObserver<SMSResult> observer) {
        HttpDataManager.getInstance().sendSMS(phone, observer);
    }


    @Override
    public void modifyPhone(String phone, String code, String identityCode, String md5Key, BorderObserver<Object> observer) {
        HttpDataManager.getInstance().modifyPhone(phone, code, identityCode, md5Key, observer);
    }


}
