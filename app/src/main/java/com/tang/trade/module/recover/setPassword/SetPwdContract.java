package com.tang.trade.module.recover.setPassword;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.module.recover.fileRecover.UserToKey;

import java.util.List;

public interface SetPwdContract {
    interface View extends IBaseView<Presenter> {

        void creatWalletSuccess(String userName, String privateKey,String pwd);

        void keyImportSuccess();

        void recoverMultiSuccess();

    }

    interface Presenter extends IBasePresenter {

        void createWalletFile(String userName, String privateKey,String pwd);

        void keyImport(String userName, String privateKey);

        void importKeyForPwd(String userName, String privateKey);

        void recoverMultiWallet( List<UserToKey> list, String pwd);

        void importKey(String userName, String privateKey, String type);
    }
}