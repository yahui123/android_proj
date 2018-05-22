package com.tang.trade.module.recover.keyRecover;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

public interface KeyRecoverContract {
    interface View extends IBaseView<Presenter> {

//        void keySuccess(String privateKey);

        void userExist();

    }

    interface Presenter extends IBasePresenter {

//        void generateKeyAccordingPwd(String pwd);

        void checkUserExist(String userName);
    }
}