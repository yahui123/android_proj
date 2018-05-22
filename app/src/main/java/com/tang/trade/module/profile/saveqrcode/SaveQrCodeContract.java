package com.tang.trade.module.profile.saveqrcode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.old.BorderObserver;

/**
 * Created by Administrator on 2018/4/17.
 */

public interface SaveQrCodeContract {
    interface Model extends IBaseModel {

        void sendSMS(String phone, BorderObserver<SMSResult> observer);

        void modifyPhone(String phone, String code, String md5Key, String encryptKey, BorderObserver<Object> observer);
    }

    interface View extends IBaseView<SaveQrCodeContract.Presenter> {

        void backSuccessFish();

        void backSMS(String identityCode);

        void verifySuccess();
    }

    interface Presenter extends IBasePresenter {

        Bitmap createCode(String url);

        void saveQr(Activity activity);

        void enCode(String userName);

        void sendSMS(String phone);

        void modifyPhone(String phone, String code, String identityCode);

        void verifyPwd(String password);
    }
}
