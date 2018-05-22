package com.tang.trade.module.register.create;

import android.content.Context;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;

/**
 * Created by Administrator on 2018/4/10.
 */

public interface CreateUserContract {
    interface Model extends IBaseModel {
        void GeneratePublicPrivateKey(String word, AsyncObserver<Key> observer);

        void register(final String user, String publicKey, String referName, String privateKey, BorderObserver observer);

        void createWalletFile(String user, String secondPassword, AsyncObserver<Object> observer);

        void keyImport(String userName, AsyncObserver<Object> observer);
    }

    interface View extends IBaseView<CreateUserContract.Presenter> {
        void backGenerateKeyPair(String privateKey, String publicKey);

        void backRegister();

        void backCreateWalletFile();

        void backKeyImport();

        void verfiySuccess(String userName);

        void loginSuccess();

        void regIMSuccess(String userName);
    }

    interface Presenter extends IBasePresenter {
        void sendGenerateKeyPair(String word);

        void sendRegister(String user, String publicKey, String referName, String privateKey);

        void sendCreateWalletFile(String user, String secondPassword);

        void sendKeyImport(String user);

        void verfiyPwd(String userName, String pwd);

        void login(Context context, String userName);

        void regIM(String userName);
    }
}
