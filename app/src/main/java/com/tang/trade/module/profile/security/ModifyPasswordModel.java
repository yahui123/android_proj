package com.tang.trade.module.profile.security;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.flh.framework.rx.RxJavaUtil;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBaseModel;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.BorderlessException;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.utils.SPUtils;

import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/4/13.
 */

public class ModifyPasswordModel extends AbsBaseModel implements ModifyPasswordContract.Model {
    private final String SUCCESS = "success";
    private YWIMKit mIMKit;
    private String userid;
    private IYWLoginService loginService;

    public ModifyPasswordModel() {
        try {
            userid = SPUtils.getString(Const.USERNAME, "");
            mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);
//            addConnectionListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IYWLoginService getLoginService() {
        return loginService;
    }

    public void modifyWalletPassword(final String newPwd, BorderObserver observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                int nRet1;

                String s = BitsharesWalletWraper.getInstance().encrypt_password_400(newPwd, MyApp.CURRENT_NODE);
                if (s.equals("failed")) {
                    throw new BorderlessException("钱包密码修改失败");
                }

                nRet1 = BitsharesWalletWraper.getInstance().unlock(newPwd);

                if (nRet1 == 0) {
                    String userName = SPUtils.getString(Const.USERNAME, "");
                    BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + userName + ".bin");
                    int nRet = BitsharesWalletWraper.getInstance().save_wallet_file();
                    if (nRet == 0) {
                        ChooseWalletActivity.PASSWORD = newPwd;
                        return SUCCESS;
                    } else {
                        throw new BorderlessException("钱包密码修改失败");
                    }
                } else {
                    throw new BorderlessException("钱包密码修改失败");
                }

            }
        }).compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }


    public void jump(BorderObserver observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                if (Device.pingIpAddress()) {
                    if (mIMKit != null) {
                        loginService = mIMKit.getIMCore().getLoginService();

                        return "";
                    } else {
//                        finishActivity();
                        return "";
                    }
                } else {
//                    MyApp.showToast(getString(R.string.network));
//                    finishActivity();
                    return "";
                }


            }
        }).compose(RxJavaUtil.threadTransform())
                .subscribe(observer);

    }

}
