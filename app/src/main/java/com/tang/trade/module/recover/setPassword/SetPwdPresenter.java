package com.tang.trade.module.recover.setPassword;

import android.text.TextUtils;

import com.flh.framework.util.FileUtil;
import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.BorderlessException;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.module.recover.fileRecover.UserToKey;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;

import org.bitcoinj.core.ECKey;

import java.util.List;

import de.bitsharesmunich.graphenej.BrainKey;
import io.reactivex.disposables.Disposable;

import static com.tang.trade.tang.net.TangConstant.PATH;

public class SetPwdPresenter extends AbsBasePresenter<SetPwdContract.View, IBaseModel> implements SetPwdContract.Presenter {
    private final String SUCCESS = "success";
    private final String FAILED = "failed";

    public SetPwdPresenter(SetPwdContract.View view) {
        super(view);
    }

    @Override
    public void createWalletFile(final String userName, final String privateKey, String pwd) {
        BorderlessDataManager.getInstance().createWalletFile(userName, privateKey, pwd, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在生成钱包文件...");
            }

            @Override
            public void onNext(Object o) {
                //在登录页面，选择的哪个用户名 就选择的哪个钱包文件
//                SPUtils.put(Const.CURRTENT_BIN, TangConstant.PATH + userName + ".bin");
//                MyApp.set("wallet_path", TangConstant.PATH + binStr);
                mView.creatWalletSuccess(userName, privateKey,pwd);
            }

            @Override
            public void onComplete() {

            }
        });
    }


    /**
     * 私钥导入
     *
     * @param userName
     * @param privateKey
     */
    @Override
    public void keyImport(String userName, String privateKey) {

        BorderlessDataManager.getInstance().keyImport(userName, privateKey, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在导入钱包文件...");
            }

            @Override
            public void onNext(Object o) {
                mView.keyImportSuccess();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void importKey(String userName, String privateKey, String type) {
        BorderlessDataManager.getInstance().importKey(userName, privateKey, type, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在导入钱包文件...");
            }

            @Override
            public void onNext(Object o) {
                mView.keyImportSuccess();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    /**
     * 密码导入
     *
     * @param userName
     * @param myBrainKey 格式后的密码
     */
    private boolean server_have_pubkey;

    @Override
    public void importKeyForPwd(final String userName, final String myBrainKey) {
        BorderlessDataManager.getInstance().executeAsync(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {

                String binStr = userName + ".bin";

                //首先查账户存在否？
                account_object account = null;
                try {
                    account = BitsharesWalletWraper.getInstance().get_account_object(userName);
                    if (account == null) {
                        throw new BorderlessException("该账户不存在");
                    }
                } catch (NetworkStatusException e) {
                    throw new BorderlessException("当前网络不可用");
                }

                server_have_pubkey = false;
                //获取私钥
                for (int i = 0; i < 10; ++i) {
                    BrainKey brainKey = new BrainKey(myBrainKey, i);
                    ECKey ecKey = brainKey.getPrivateKey();
                    private_key privateKey = new private_key(ecKey.getPrivKeyBytes());
                    types.private_key_type privateKeyType = new types.private_key_type(privateKey);
                    types.public_key_type publicKeyType = new types.public_key_type(privateKey.get_public_key());
                    //校验 account public key
                    if (account.active.is_public_key_type_exist(publicKeyType) == false &&
                            account.owner.is_public_key_type_exist(publicKeyType) == false &&
                            account.options.memo_key.compare(publicKeyType) == false) {

                        continue;
                    } else {
                        //import_brain_key 会自动save_wallet_file
                        BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);


                        //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                        int flag = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + binStr, ChooseWalletActivity.PASSWORD);

                        int nRet = -1;
                        if (flag == 0) {
                            //java Android 钱包解锁
                            nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                        } else {
                            FileUtil.deleteFile(TangConstant.PATH+userName+".bin");
                            throw new BorderlessException("钱包加载失败");
                        }

                        //解锁成功
                        if (nRet != 0) {
                            FileUtil.deleteFile(TangConstant.PATH+userName+".bin");
                            throw new BorderlessException("钱包解锁失败");
                        }

                        if (0 == BitsharesWalletWraper.getInstance().import_brain_key(userName, ChooseWalletActivity.PASSWORD, myBrainKey)) {
                            server_have_pubkey = true;
                            return SUCCESS;
                        } else {
                            FileUtil.deleteFile(TangConstant.PATH+userName+".bin");
                            throw new BorderlessException("账号恢复失败");
                        }
                    }
                }

                if (server_have_pubkey) {
                    return SUCCESS;
                } else {
                    //密码不是V1版本的密码 提示
                    FileUtil.deleteFile(TangConstant.PATH+userName+".bin");
                    throw new BorderlessException("旧密码输入不正确");
                }

            }
        }, new AsyncObserver<Object>() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在导入钱包文件...");
            }

            @Override
            public void onNext(Object o) {
                mView.keyImportSuccess();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    /**
     * 文件恢复
     * 先创建空文件，再循环导入所有用户
     *
     * @param list
     * @param pwd
     */
    @Override
    public void recoverMultiWallet(final List<UserToKey> list, final String pwd) {
        BorderlessDataManager.getInstance().executeAsync(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {

                int count = 0;
                for (int i = 0; i < list.size(); i++) {

                    count++;

                    //生成钱包文件
                    if (!TextUtils.isEmpty(MyApp.CURRENT_NODE)) {

                        String binStr = list.get(i).getUserName() + ".bin";

                        BitsharesWalletWraper.getInstance().encrypt_password(pwd, MyApp.CURRENT_NODE);
                        BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);
                        BitsharesWalletWraper.getInstance().save_wallet_file();

                        ChooseWalletActivity.CURRTEN_BIN = binStr;
                        ChooseWalletActivity.PASSWORD = pwd;
//
//                        return "true";

                        //导入用户
                        try {
                            account_object obj = BitsharesWalletWraper.getInstance().get_account_object(list.get(i).getUserName());
                            if (obj == null) {

                                FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                                throw new BorderlessException("账户不存在");
                            }

                            types.public_key_type publicKeyType = obj.owner.get_keys().get(0);
                            String strPublicKey = publicKeyType.toString();

                            try {
                                types.private_key_type privateKeyType = new types.private_key_type(list.get(i).getPrivateKey());
                                if (privateKeyType == null) {

                                    FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                                    throw new BorderlessException("私钥错误");
                                }

                                public_key publicKey = privateKeyType.getPrivateKey().get_public_key();
                                types.public_key_type publicKeyType2 = new types.public_key_type(publicKey);
                                String strWifPubKey = publicKeyType2.toString();

                                if (!strPublicKey.equals(strWifPubKey)) {

                                    FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                                    throw new BorderlessException("私钥错误");
                                }
                            } catch (Exception e) {

                                FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                                throw new BorderlessException("私钥错误");
                            }

                        } catch (NetworkStatusException e) {

                            FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                            throw new BorderlessException("当前网络不可用");
                        }


                        BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);



                        //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                        int flag = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + binStr, pwd);

                        int nRet4 = -1;
                        if (flag == 0) {
                            //java Android 钱包解锁
                            nRet4 = BitsharesWalletWraper.getInstance().unlock(pwd);
                        } else {
                            FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                            throw new BorderlessException("钱包加载失败");
                        }

                        //解锁成功
                        if (nRet4 != 0) {
                            FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                            throw new BorderlessException(list.get(i).getUserName() + "钱包解锁失败");
                        }

                        if (0 == BitsharesWalletWraper.getInstance().import_key(list.get(i).getUserName(), pwd, list.get(i).getPrivateKey())) {
                            /*导入成功*/
                            if (count == list.size()) {
                                return SUCCESS;
                            } else {
                                continue;
                            }
                        } else {

                            FileUtil.deleteFile(TangConstant.PATH + list.get(i).getUserName() + ".bin");
                            throw new BorderlessException(list.get(i).getUserName() + "账户恢复失败");
                        }

                    } else {
                        throw new BorderlessException("节点链接失败");
                    }
                }

                return FAILED;
            }
        }, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在恢复账户...");
            }

            @Override
            public void onNext(Object o) {
                if (o.toString().equals(SUCCESS)) {
                    mView.recoverMultiSuccess();
                } else {
                    DataError error = new DataError();
                    error.setErrorMessage("钱包异常");
                    mView.onError(error);
                }
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }


}