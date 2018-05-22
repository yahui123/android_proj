package com.tang.trade.module.recover;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;

import io.reactivex.disposables.Disposable;

public class RecoverPresenter extends AbsBasePresenter<RecoverContract.View, IBaseModel> implements RecoverContract.Presenter {
    private RecoverContract.View mView;

    public RecoverPresenter(RecoverContract.View view) {
        super(view);
    }

    @Override
    public void pingIPAddress(final String nodes) {
        BorderlessDataManager.getInstance().pingIPAddress(nodes, new AsyncObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在链接节点...");
            }

            @Override
            public void onNext(Object o) {
                mView.pingIPResult(o.toString());
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

            @Override
            public void onError(DataError error) {

            }
        });
    }
}