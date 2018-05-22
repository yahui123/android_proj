package com.tang.trade.module.profile.security;

import android.text.TextUtils;

import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.AESCipher;
import com.tang.trade.utils.Generate16;
import com.tang.trade.utils.SPUtils;

import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/13.
 */

public class ModifyPasswordPresenter extends AbsBasePresenter<ModifyPasswordContract.View, ModifyPasswordContract.Model> implements ModifyPasswordContract.Presenter {

    public ModifyPasswordPresenter(ModifyPasswordContract.View views) {
        super(views, new ModifyPasswordModel());
    }

    public void sendModifyWalletPassword(final String newPwd, final String oldPass) {
        mModel.modifyWalletPassword(newPwd, new BorderObserver() {
            @Override
            public void onError(String errorMsg) {
                DataError errorMsg1 = new DataError();
                errorMsg1.setErrorMessage(errorMsg);
                mView.onError(errorMsg1);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在修改钱包密码...");
            }

            @Override
            public void onNext(Object o) {
                try {
                    String userName = SPUtils.getString(Const.USERNAME, "");
                    /*保存了加密速记词的才需要修改密码  没有加密速记词的不需要*/
                    String encryptWords = SPUtils.getString(userName, "");
                    if (!TextUtils.isEmpty(encryptWords)) {
                        String words = AESCipher.aesDecryptString(encryptWords, Generate16.Generate16Number(oldPass));
                        String passwordKey = AESCipher.aesEncryptString(words, Generate16.Generate16Number(newPwd));
                        SPUtils.put(userName, passwordKey);
                    }
                    mView.backModifyPasswordSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    mView.backModifyPasswordError();
                }

            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });

    }


    public void sendJump() {
        mModel.jump(new BorderObserver() {
            @Override
            public void onError(String errorMsg) {
                DataError errorMsg1 = new DataError();
                errorMsg1.setErrorMessage(errorMsg);
                mView.onError(errorMsg1);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在退出...");
            }

            @Override
            public void onNext(Object o) {
//                if (model.getLoginService() != null) {
//
//
//                    model.getLoginService().logout(new IWxCallback() {
//
//                        @Override
//                        public void onSuccess(Object... arg0) {
////                        finishActivity();
//                            mView.backJump();
//
//                        }
//
//                        @Override
//                        public void onProgress(int arg0) {
//
//
//                        }
//
//                        @Override
//                        public void onError(int errCode, String description) {
////                        finishActivity();
//                            mView.backJump();
//                        }
//                    });
//                } else {
//                    mView.backJump();
//                }

                mView.backJump();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });

    }


}