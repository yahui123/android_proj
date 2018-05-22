package com.tang.trade.module.profile.generateqrcode;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.old.BorderObserver;

/**
 * Created by Administrator on 2018/4/17.
 */

public interface GenerateQrCodeContract {
    interface Model extends IBaseModel {

        void sendSMS(String phone, BorderObserver<SMSResult> observer);

        void saveQrCodeMessage(String phone, String code, String identityCode, String md5Key, String encryptKey, BorderObserver<Object> observer);

    }

    interface View extends IBaseView<GenerateQrCodeContract.Presenter> {

        void backSMS(String identityCode);

        void backVerify();

    }

    interface Presenter extends IBasePresenter {

        void sendSMS(String phone);

        void saveQrCodeMessage(String phone, String code, String identityCode);
    }
}
