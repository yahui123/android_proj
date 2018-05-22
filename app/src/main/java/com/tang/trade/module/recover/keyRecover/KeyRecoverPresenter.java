package com.tang.trade.module.recover.keyRecover;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;

import io.reactivex.disposables.Disposable;

public class KeyRecoverPresenter extends AbsBasePresenter<KeyRecoverContract.View, IBaseModel> implements KeyRecoverContract.Presenter {

    public KeyRecoverPresenter(KeyRecoverContract.View view) {
        super(view);
    }

//    @Override
//    public void generateKeyAccordingPwd(String pwd) {
//        BorderlessDataManager.getInstance().generateKey(pwd, new AsyncObserver<Key>() {
//            @Override
//            public void onError(DataError error) {
//                mView.onError(error);
//                mView.dissLoading();
//            }
//
//            @Override
//            public void onSubscribe(Disposable d) {
//                mView.showLoading("正在生成私钥...");
//            }
//
//            @Override
//            public void onNext(Key key) {
//                mView.keySuccess(key.getPrivateKey());
//            }
//
//            @Override
//            public void onComplete() {
//                mView.dissLoading();
//            }
//        });
//    }

    @Override
    public void checkUserExist(String userName) {
        BorderlessDataManager.getInstance().checkUserExist(userName, new AsyncObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在查询账户...");
            }

            @Override
            public void onNext(Object o) {
                if (o.toString().equals("success")) {
                    mView.userExist();
                }
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }
        });
    }
}