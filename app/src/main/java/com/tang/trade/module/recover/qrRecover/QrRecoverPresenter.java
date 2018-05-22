package com.tang.trade.module.recover.qrRecover;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.model.entity.PhoneResult;
import com.tang.trade.data.model.entity.RandomEncryptResult;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;

import io.reactivex.disposables.Disposable;

public class QrRecoverPresenter extends AbsBasePresenter<QrRecoverContract.View, IBaseModel> implements QrRecoverContract.Presenter {

    public QrRecoverPresenter(QrRecoverContract.View view) {
        super(view);
    }

    @Override
    public void checkUser(final String userName, final String words) {
        BorderlessDataManager.getInstance().checkUserExist(userName, new AsyncObserver<String>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证...");
            }

            @Override
            public void onNext(String s) {
                genetateKey(userName, words);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void genetateKey(final String userName, final String words) {
        BorderlessDataManager.getInstance().generatePrivateKeyFromWord(words.toUpperCase(), new AsyncObserver<Key>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证...");
            }

            @Override
            public void onNext(Key key) {
                mView.checkSuccess(userName, key.getPrivateKey(), words);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void checkUserOnly(final String username, final String key) {
        BorderlessDataManager.getInstance().checkUserExist(username, new AsyncObserver<String>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证...");
            }

            @Override
            public void onNext(String s) {
                mView.checkSuccess(username, key, "");
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void correspondingPhone(String md5QrAes) {
        HttpDataManager.getInstance().correspondingPhone(md5QrAes, new BorderObserver<PhoneResult>() {

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在获取手机号...");
            }

            @Override
            public void onNext(PhoneResult result) {
                mView.correspondingSuccess(result);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

            @Override
            public void onError(String errorMsg) {
                mView.correspondingFailed(errorMsg);
                mView.dissLoading();
            }
        });
    }

    @Override
    public void sendCode(String phone) {
        HttpDataManager.getInstance().sendSMS(phone, new BorderObserver<SMSResult>() {
            @Override
            public void onError(String errorMsg) {
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在发送验证码...");
            }

            @Override
            public void onNext(SMSResult smsResult) {
                mView.sendCodeSuccess(smsResult);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void getRandomEncrypt(String md5Key, String phone, String code, String identityCode) {
        HttpDataManager.getInstance().getRandomEncrypt(md5Key,phone,code,identityCode,new BorderObserver<RandomEncryptResult>(){

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading();
            }

            @Override
            public void onNext(RandomEncryptResult result) {
                mView.randomEncryptSuccess(result);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

            @Override
            public void onError(String errorMsg) {
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);
                mView.dissLoading();
            }
        });
    }


}