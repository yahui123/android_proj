package com.tang.trade.module.recover.fileRecover;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.tang.net.model.AccountModel;

import java.util.List;

public interface FileRecoverContract {
    interface View extends IBaseView<Presenter> {

        void loadSuccess();

        void userInfoSuccess(List<UserToKey> list);
    }

    interface Presenter extends IBasePresenter {

        void loadWallet(String path, String pwd);

        void getUserInfoFromWallet(String path,String pwd);
    }
}