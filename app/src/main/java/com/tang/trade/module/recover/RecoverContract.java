package com.tang.trade.module.recover;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

public interface RecoverContract {
    interface View extends IBaseView<Presenter> {

        void pingIPResult(String s);
    }

    interface Presenter extends IBasePresenter {

        void pingIPAddress(String nodes);

    }
}