package com.tang.trade.module.register.create;

import android.content.Context;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;

import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/10.
 */

public class CreateUserPresenter extends AbsBasePresenter<CreateUserContract.View, CreateUserContract.Model> implements CreateUserContract.Presenter {

    public CreateUserPresenter(CreateUserContract.View view) {
        super(view, new CreateUserModel());
    }


    @Override
    public void sendGenerateKeyPair(String word) {
        mModel.GeneratePublicPrivateKey(word, new AsyncObserver<Key>() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在生成密钥对...");
            }

            @Override
            public void onNext(Key key) {
                mView.backGenerateKeyPair(key.getPrivateKey(), key.getPublicKey());

            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }

            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }
        });
    }

    @Override
    public void sendRegister(String user, String publiclKey, String referName, String privateKey) {

        mModel.register(user, publiclKey, referName, privateKey, new BorderObserver<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在注册...");
            }

            @Override
            public void onNext(Object o) {
                /**
                 * 保存密码
                 */
                mView.backRegister();
            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }

            @Override
            public void onError(String errorMsg) {
                DataError dataError = new DataError();
                dataError.setErrorMessage(errorMsg);
                mView.onError(dataError);
                mView.dissLoading();
            }
        });
    }

    @Override
    public void sendCreateWalletFile(String user, String secondPassword) {
        mModel.createWalletFile(user, secondPassword, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在生成钱包文件...");
            }

            @Override
            public void onNext(Object o) {
                mView.backCreateWalletFile();

            }

            @Override
            public void onComplete() {
//                presenter.dissLoading();
            }
        });
    }

    @Override
    public void sendKeyImport(String user) {
        mModel.keyImport(user, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在导入钱包文件...");
            }

            @Override
            public void onNext(Object o) {

                mView.backKeyImport();
            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }
        });
    }

    @Override
    public void verfiyPwd(final String userName, String pwd) {
        BorderlessDataManager.getInstance().verifyPwd(userName, pwd, new AsyncObserver<String>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证密码...");
            }

            @Override
            public void onNext(String s) {
                mView.verfiySuccess(userName);
            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }
        });
    }

    @Override
    public void login(Context context, String userName) {
        BorderlessDataManager.getInstance().login(context, userName, new AsyncObserver<String>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在登录...");
            }

            @Override
            public void onNext(String s) {
                mView.loginSuccess();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void regIM(final String userName) {
        HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {
                mView.regIMSuccess(userName);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(String errorMsg) {
//                DataError dataError = new DataError();
//                dataError.setErrorMessage("当前网络不可用");
//                mView.onError(dataError);

            }
        });
    }

}
