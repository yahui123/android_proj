package com.tang.trade.module.recover.wordRecover;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.AsyncObserver;

import io.reactivex.disposables.Disposable;

public class WordRecoverPresenter extends AbsBasePresenter<WordRecoverContract.View, IBaseModel> implements WordRecoverContract.Presenter {

    public WordRecoverPresenter(WordRecoverContract.View view) {
        super(view);
    }

    @Override
    public void generateKey(final String words) {
        BorderlessDataManager.getInstance().generatePrivateKeyFromWord(words.toUpperCase(), new AsyncObserver<Key>() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在生成私钥...");
            }

            @Override
            public void onNext(Key key) {
                mView.generateKeySuccess(key.getPrivateKey());
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