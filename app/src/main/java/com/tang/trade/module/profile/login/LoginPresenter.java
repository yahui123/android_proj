package com.tang.trade.module.profile.login;

import android.content.Context;

import com.flh.framework.toast.ToastAlert;
import com.google.gson.reflect.TypeToken;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessCode;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.BorderlessException;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import io.reactivex.disposables.Disposable;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

public class LoginPresenter extends AbsBasePresenter<LoginContract.View, IBaseModel> implements LoginContract.Presenter {
    private final String SUCCESS = "success";

    public LoginPresenter(LoginContract.View view) {
        super(view);
    }

    @Override
    public void login(final Context context, final String userName) {
        BorderlessDataManager.getInstance().login(context, userName, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在登录...");
            }

            @Override
            public void onNext(Object o) {
                mView.userExist(userName);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

        });
    }

    @Override
    public void regIM(final String userName) {
        HttpDataManager.getInstance().regIM(userName, new BorderObserver<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {
                mView.regIMSuccess(userName);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(String errorMsg) {
                DataError dataError = new DataError();
                dataError.setErrorMessage("当前网络不可用");
                mView.onError(dataError);

            }
        });
    }

    @Override
    public void getNodes() {
        HttpDataManager.getInstance().getNodes(new BorderObserver<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在获取节点...");
            }

            @Override
            public void onNext(List<String> list) {
                mView.nodeSuccess(list);
            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }

            @Override
            public void onError(String errorMsg) {
                DataError dataError = new DataError();
                dataError.setErrorMessage(errorMsg);
                mView.onError(dataError);
                mView.dissLoading();
            }
        });
    }

    @Override
    public void pingIPAddress() {
        BorderlessDataManager.getInstance().pingIP(new BorderlessOnSubscribe() {
            @Override
            public String onPre() {
                boolean b = Device.pingIpAddress();
                if (b) {
                    return "true";
                } else {
                    return "false";
                }

            }
        }, new AsyncObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在链接节点...");
            }

            @Override
            public void onNext(Object o) {
                mView.pingIPResult(o.toString());
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }

            @Override
            public void onError(DataError error) {

            }
        });
    }

    @Override
    public void verifyPwd(final String userName, final String pwd) {
        BorderlessDataManager.getInstance().verifyPwd(userName, pwd, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证密码...");
            }

            @Override
            public void onNext(Object o) {
                mView.passwordRight(userName);
            }

            @Override
            public void onComplete() {

            }
        });
    }


}