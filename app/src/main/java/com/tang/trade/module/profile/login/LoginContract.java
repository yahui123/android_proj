package com.tang.trade.module.profile.login;

import android.content.Context;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

import java.util.List;

public interface LoginContract {
    interface View extends IBaseView<Presenter> {

        void userExist(String userName);

        void regIMSuccess(String userName);

        void nodeSuccess(List<String> list);

        void pingIPResult(String s);

        void passwordRight(String userName);
    }

    interface Presenter extends IBasePresenter {
        void login(Context context,String userName);

        void regIM(String userName);

        void getNodes();

        void pingIPAddress();

        void verifyPwd(String userName, String pwd);
    }
}