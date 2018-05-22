package com.tang.trade.module.splash;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

import java.util.ArrayList;
import java.util.List;

public interface SplashContract {
    interface View extends IBaseView<Presenter> {
        void nodeSuccess(List<String> list);

        void pingIPResult(String s);
    }

    interface Presenter extends IBasePresenter {
        void getNodes();

        void pingIPAddress();
    }
}