package com.tang.trade.utils;

import android.content.Context;
import android.content.Intent;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.YWLoginParam;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.login.IYWConnectionListener;
import com.alibaba.mobileim.login.YWLoginCode;
import com.flh.framework.toast.ToastAlert;
import com.tang.trade.app.Const;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.tang.widget.MyProgressDialog;

import io.reactivex.disposables.Disposable;

/**
 * Created by daibin on 2018/5/9.
 */

public class IMListener {

    public static void startChatting(Context context, EServiceContact contact) {

        String userName = SPUtils.getString(Const.USERNAME, "");
        YWIMKit mIMKit = YWAPI.getIMKitInstance(userName, MyApp.APP_KEY);

        MyProgressDialog dialog = MyProgressDialog.getInstance(MyApp.getContext());

        if (SPUtils.getBoolean(Const.SP.IM_LOGIN_SUCCESS, false)) {
            Intent intent = mIMKit.getChattingActivityIntent(contact);
            context.startActivity(intent);
        } else {
            String password = MD5.md5(userName + "borderless");
            IYWLoginService loginService = mIMKit.getLoginService();
            YWLoginParam loginParam = YWLoginParam.createLoginParam(userName, password);
            loginService.login(loginParam, new IWxCallback() {
                @Override
                public void onSuccess(Object... arg0) {

                    dialog.dismiss();

                    Intent intent = mIMKit.getChattingActivityIntent(contact);
                    context.startActivity(intent);
                }

                @Override
                public void onProgress(int arg0) {
                    dialog.show();
                }

                @Override
                public void onError(int errCode, String description) {
                    if (errCode == YWLoginCode.LOGON_FAIL_INVALIDUSER) {

                        //注册IM
                        HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Object o) {
                                dialog.show();
                            }

                            @Override
                            public void onComplete() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(String errorMsg) {
                                ToastAlert.showToast(context, "客服功能维护中...");
                            }
                        });
                    }
                }
            });
        }

        IYWConnectionListener mConnectionListener = new IYWConnectionListener() {
            @Override
            public void onDisconnect(int code, String info) {

                String password = MD5.md5(userName + "borderless");
                IYWLoginService loginService = mIMKit.getLoginService();
                YWLoginParam loginParam = YWLoginParam.createLoginParam(userName, password);
                loginService.login(loginParam, new IWxCallback() {
                    @Override
                    public void onSuccess(Object... arg0) {

                        dialog.dismiss();

                        Intent intent = mIMKit.getChattingActivityIntent(contact);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onProgress(int arg0) {
                        dialog.show();
                    }

                    @Override
                    public void onError(int errCode, String description) {
                        if (errCode == YWLoginCode.LOGON_FAIL_INVALIDUSER) {

                            //注册IM
                            HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Object o) {
                                    dialog.show();
                                }

                                @Override
                                public void onComplete() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    ToastAlert.showToast(context, "客服功能维护中...");
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onReConnecting() {

            }

            @Override
            public void onReConnected() {

            }
        };
        mIMKit.getIMCore().addConnectionListener(mConnectionListener);

    }


    public static void startChatting(Context context, String targetUserId) {

        String userName = SPUtils.getString(Const.USERNAME, "");
        YWIMKit mIMKit = YWAPI.getIMKitInstance(userName, MyApp.APP_KEY);

        MyProgressDialog dialog = MyProgressDialog.getInstance(MyApp.getContext());

        if (SPUtils.getBoolean(Const.SP.IM_LOGIN_SUCCESS, false)) {
            Intent intent = mIMKit.getChattingActivityIntent(targetUserId, MyApp.APP_KEY);
            context.startActivity(intent);
        } else {
            String password = MD5.md5(userName + "borderless");
            IYWLoginService loginService = mIMKit.getLoginService();
            YWLoginParam loginParam = YWLoginParam.createLoginParam(userName, password);
            loginService.login(loginParam, new IWxCallback() {
                @Override
                public void onSuccess(Object... arg0) {

                    dialog.dismiss();

                    Intent intent = mIMKit.getChattingActivityIntent(targetUserId, MyApp.APP_KEY);
                    context.startActivity(intent);

                }

                @Override
                public void onProgress(int arg0) {
                    dialog.show();
                }

                @Override
                public void onError(int errCode, String description) {
                    if (errCode == YWLoginCode.LOGON_FAIL_INVALIDUSER) {

                        //注册IM
                        HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Object o) {
                                dialog.show();
                            }

                            @Override
                            public void onComplete() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(String errorMsg) {
                                ToastAlert.showToast(context, "聊天功能初始化失败");
                            }
                        });
                    }
                }
            });
        }

        IYWConnectionListener mConnectionListener = new IYWConnectionListener() {

            @Override
            public void onDisconnect(int code, String info) {

                String password = MD5.md5(userName + "borderless");
                IYWLoginService loginService = mIMKit.getLoginService();
                YWLoginParam loginParam = YWLoginParam.createLoginParam(userName, password);
                loginService.login(loginParam, new IWxCallback() {
                    @Override
                    public void onSuccess(Object... arg0) {

                        dialog.dismiss();

                        Intent intent = mIMKit.getChattingActivityIntent(targetUserId, MyApp.APP_KEY);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onProgress(int arg0) {
                        dialog.show();
                    }

                    @Override
                    public void onError(int errCode, String description) {
                        if (errCode == YWLoginCode.LOGON_FAIL_INVALIDUSER) {

                            //注册IM
                            HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Object o) {
                                    dialog.show();
                                }

                                @Override
                                public void onComplete() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    ToastAlert.showToast(context, "聊天功能初始化失败");
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onReConnecting() {

            }

            @Override
            public void onReConnected() {

            }
        };
        mIMKit.getIMCore().addConnectionListener(mConnectionListener);


    }

}
