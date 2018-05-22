package com.tang.trade.module.profile.security;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.old.BorderObserver;

/**
 * Created by Administrator on 2018/4/13.
 */

public interface ModifyPasswordContract {
    interface Model extends IBaseModel {
        void modifyWalletPassword(final String newPwd, BorderObserver observer);

        void jump(BorderObserver observer);
    }

    interface View extends IBaseView<ModifyPasswordContract.Presenter> {

        void backModifyPasswordSuccess();

        void backModifyPasswordError();

        void backJump();

    }

    interface Presenter extends IBasePresenter {
        void sendModifyWalletPassword(final String newPwd, String oldPass);

        void sendJump();

    }
}
