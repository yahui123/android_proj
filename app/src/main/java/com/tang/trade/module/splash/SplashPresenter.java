package com.tang.trade.module.splash;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.tang.utils.Device;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class SplashPresenter extends AbsBasePresenter<SplashContract.View, IBaseModel> implements SplashContract.Presenter {
    private SplashContract.View mView;

    public SplashPresenter(SplashContract.View view) {
        super(view);
    }

    @Override
    public void getNodes() {
        HttpDataManager.getInstance().getNodes(new BorderObserver<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在获取节点...");
            }

            @Override
            public void onNext(List<String> list) {
                mView.nodeSuccess(list);
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
    public void pingIPAddress() {
        BorderlessDataManager.getInstance().pingIP(new BorderlessOnSubscribe() {
            @Override
            public String onPre() {
                boolean b = Device.pingIpAddress();
                if (b) {
                    return "true";
                } else {
                    return "false";
                }

            }
        }, new AsyncObserver() {
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