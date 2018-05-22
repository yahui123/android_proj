package com.tang.trade.module.profile.generateqrcode;

import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.utils.EnQrCode;
import com.tang.trade.utils.SPUtils;

import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/17.
 */

public class GenerateQrCodePresenter extends AbsBasePresenter<GenerateQrCodeContract.View, GenerateQrCodeContract.Model> implements GenerateQrCodeContract.Presenter {

    public GenerateQrCodePresenter(GenerateQrCodeContract.View view) {
        super(view, new GenerateQrCodeModel());
    }

    @Override
    public void sendSMS(String phone) {
        mModel.sendSMS(phone, new BorderObserver<SMSResult>() {

            @Override
            public void onError(String errorMsg) {
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading();
            }

            @Override
            public void onNext(SMSResult o) {
                mView.backSMS(o.getIdentityCode());
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void saveQrCodeMessage(String phone, String code, String identityCode) {
        EnQrCode.enQrCode(SPUtils.getString(Const.USERNAME, ""));
        mModel.saveQrCodeMessage(phone, code, identityCode, EnQrCode.getMd5Key(), EnQrCode.getRandomStr(), new BorderObserver<Object>() {

            @Override
            public void onError(String errorMsg) {
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在保存...");
            }

            @Override
            public void onNext(Object o) {
                SPUtils.put(SPUtils.getString(Const.USERNAME, "") + Const.PHONE, phone);
                mView.backVerify();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });

    }


}
