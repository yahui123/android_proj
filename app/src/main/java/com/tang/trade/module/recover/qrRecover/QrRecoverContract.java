package com.tang.trade.module.recover.qrRecover;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.data.model.entity.PhoneResult;
import com.tang.trade.data.model.entity.RandomEncryptResult;
import com.tang.trade.data.model.httpRequest.SMSResult;


public interface QrRecoverContract {
    interface View extends IBaseView<Presenter> {

        void checkSuccess(String userName,String privateKey,String words);

        void correspondingSuccess(PhoneResult result);

        void sendCodeSuccess(SMSResult smsResult);

        void randomEncryptSuccess(RandomEncryptResult result);

        void correspondingFailed(String errorMsg);
    }

    interface Presenter extends IBasePresenter {

        void checkUser(String userName,String words);

        void genetateKey(String userName,String words);

        void checkUserOnly(String username, String key);

        void correspondingPhone(String md5QrAes);

        void sendCode(String phone);

        void getRandomEncrypt(String md5Key, String phone, String code, String identityCode);
    }
}