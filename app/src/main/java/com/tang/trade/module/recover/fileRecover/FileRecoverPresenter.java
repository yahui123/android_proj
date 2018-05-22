package com.tang.trade.module.recover.fileRecover;

import android.text.TextUtils;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.BorderlessException;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class FileRecoverPresenter extends AbsBasePresenter<FileRecoverContract.View, IBaseModel> implements FileRecoverContract.Presenter {
    private final String SUCCESS = "success";
    private List<UserToKey> data = new ArrayList<>();

    public FileRecoverPresenter(FileRecoverContract.View view) {
        super(view);
    }

    @Override
    public void loadWallet(final String path, final String pwd) {
        BorderlessDataManager.getInstance().executeAsync(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {

                BitsharesWalletWraper.getInstance().clear();

                int load = BitsharesWalletWraper.getInstance().load_wallet_file(path, pwd);
                if (load == -1) {
                    throw new BorderlessException("钱包加载失败");
                }

                int nRet = BitsharesWalletWraper.getInstance().unlock(pwd);
                if (nRet == 0) {
                    return SUCCESS;
                } else {
                    throw new BorderlessException("钱包密码不正确");
                }


            }
        }, new AsyncObserver<String>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在解析钱包文件...");
            }

            @Override
            public void onNext(String s) {
                mView.loadSuccess();
            }

            @Override
            public void onComplete() {
//                mView.dissLoading();
            }
        });
    }

    @Override
    public void getUserInfoFromWallet(final String path, final String pwd) {
        BorderlessDataManager.getInstance().executeAsync(new BorderlessOnSubscribe() {
            @Override
            public List<UserToKey> onPre() {

                data.clear();

                try {
                    String str = FileUtils.readSDFile(path);
                    String str1 = str.replace("\n", "");
                    if (!str1.equalsIgnoreCase("")) {
                        JSONObject data1 = new JSONObject(str1);
                        JSONArray jsonArray = data1.optJSONArray("my_accounts");

                        if (jsonArray.length() <= 0) {
                            throw new BorderlessException("钱包内没有用户");
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            /*存储用户名和私钥的对象*/
                            UserToKey userToKey = new UserToKey();
                            JSONObject data2 = jsonArray.optJSONObject(i);
                            String userName = data2.optString("name");

                            userToKey.setUserName(userName);

                            /*根据用户名获取私钥*/
                            account_object upgradeAccount = null;
                            try {

                                BitsharesWalletWraper.getInstance().clear();
                                BitsharesWalletWraper.getInstance().load_wallet_file(path, pwd);
                                BitsharesWalletWraper.getInstance().unlock(pwd);

                                upgradeAccount = BitsharesWalletWraper.getInstance().get_account_object(userName);
                                if (null == upgradeAccount) {
                                    throw new BorderlessException("当前网络不可用");
                                }

                                types.public_key_type publicKeyType = upgradeAccount.owner.get_keys().get(0);

                                types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);

                                if (null != privateKeyType) {
                                    String strPrivateKey = privateKeyType.toString();

                                    if (!TextUtils.isEmpty(strPrivateKey)) {
                                        userToKey.setPrivateKey(strPrivateKey);
                                    } else {
                                        throw new BorderlessException("钱包内私钥异常");
                                    }

                                } else {
                                    throw new BorderlessException("钱包内私钥异常");
                                }

                            } catch (NetworkStatusException e) {
                                throw new BorderlessException("当前网络不可用");
                            }

                            data.add(userToKey);
                        }

                        return data;
                    } else {
                        throw new BorderlessException("解析失败");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new BorderlessException("解析失败");
                }
            }
        }, new AsyncObserver<List<UserToKey>>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在读取用户信息...");
            }

            @Override
            public void onNext(List<UserToKey> list) {
                mView.userInfoSuccess(list);
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }
}