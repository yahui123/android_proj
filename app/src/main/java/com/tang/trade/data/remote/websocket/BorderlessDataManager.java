package com.tang.trade.data.remote.websocket;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.flh.framework.rx.RxJavaUtil;
import com.flh.framework.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tang.trade.app.Const;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.utils.CountDownTimerUtils;
import com.tang.trade.utils.SPUtils;

import org.bitcoinj.core.ECKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;
import io.reactivex.Observable;
import io.reactivex.Observer;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

/**
 * Created by Administrator on 2018/4/8.
 */

public class BorderlessDataManager {
    public static object_id<account_object> loginAccountId = null;
    public static double gbdsTransferFee;

    private static volatile BorderlessDataManager instance;

    private final String SUCCESS = "success";
    private CountDownTimer mCountDownTimer;

    public static BorderlessDataManager getInstance() {
        if (instance == null) {
            synchronized (HttpDataManager.class) {
                if (instance == null) {
                    instance = new BorderlessDataManager();
                }
            }
        }

        return instance;
    }

    public void selectUser(BorderlessOnSubscribe onSubscribe, Observer observer) {
        Observable.create(onSubscribe)
                .compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }

    public void pingIP(BorderlessOnSubscribe onSubscribe, Observer observer) {
        Observable.create(onSubscribe)
                .compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }


    /**
     * 生成私钥
     */
    public void generatePrivateKeyFromWord(final String words, Observer observer) {
        Observable.create(new BorderlessOnSubscribe<Key>() {
            @Override
            public Key onPre() {
                BrainKey mBrainKey = new BrainKey(words, 0);
                try {
                    Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
                    ECKey ecKey = mBrainKey.getPrivateKey();
                    private_key accountPrivateKey = new private_key(ecKey.getPrivKeyBytes());
                    String suggestPublicKey = address.toString();
                    String suggestPrivateKey = mBrainKey.getWalletImportFormat();

                    types.public_key_type pubKeyType = new types.public_key_type(accountPrivateKey.get_public_key());
                    types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(pubKeyType);

                    if (!TextUtils.isEmpty(suggestPrivateKey) && !TextUtils.isEmpty(suggestPublicKey)) {
                        Key myKey = new Key();
                        myKey.setPrivateKey(suggestPrivateKey);
                        myKey.setPublicKey(suggestPublicKey);
                        return myKey;
                    } else {
                        throw new BorderlessException("私钥生成失败，请重试！");
                    }

                } catch (Exception e) {
                    throw new BorderlessException("私钥生成失败，请重试！");
                }
            }
        }).compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }

    /**
     * 生成钱包文件
     *
     * @param userName
     * @param privatekey
     * @param pwd
     * @param observer
     */
    private long startCliInitTime = 0;
    private long endCliInitTime = 0;

    public void createWalletFile(final String userName, final String privatekey, final String pwd, Observer<Object> observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                //生成钱包文件
                if (!TextUtils.isEmpty(MyApp.CURRENT_NODE)) {
                    String binStr = userName + ".bin";

                    BitsharesWalletWraper.getInstance().reset_400();

                    int nRet1 = -1;

//                    for (int i = 0; i < 5; i++) {
//                        nRet1 = BitsharesWalletWraper.getInstance().cli_init();
//                        if (nRet1 != -1) {
//                            break;
//                        }
//                    }


//                    if (nRet1 == -1) {
//                        BitsharesWalletWraper.getInstance().setCliUsedSwitch(false);
//                    }

//                    if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                        BitsharesWalletWraper.getInstance().cli_set_password(pwd);
//                        BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);
//                        BitsharesWalletWraper.getInstance().save_wallet_file();
//                        int load = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + binStr, pwd);
//                        if (load == -1) {
//                            throw new BorderlessException("当前网络不可用");
//                        }
//                        BitsharesWalletWraper.getInstance().encrypt_password(pwd, MyApp.CURRENT_NODE);
//                        //此处unlock是否为了cli钱包导入私有key
//                        BitsharesWalletWraper.getInstance().cli_unlock(pwd);
//                    } else {
                    String s = BitsharesWalletWraper.getInstance().encrypt_password_400(pwd, MyApp.CURRENT_NODE);
                    if (s.equals("failed")) {
                        throw new BorderlessException("当前网络不可用");
                    }

                    BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);
                    BitsharesWalletWraper.getInstance().save_wallet_file();
//                    }

                    ChooseWalletActivity.CURRTEN_BIN = binStr;
                    ChooseWalletActivity.PASSWORD = pwd;
//
                    return "true";

                } else {
                    throw new BorderlessException("节点链接失败");
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }


    public void executeAsync(BorderlessOnSubscribe onSubscribe, Observer observer) {
        Observable.create(onSubscribe)
                .compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }

    /**
     * 私钥导入方式
     * 同时验证了用户名和私钥
     *
     * @param userName
     * @param privateKey
     * @param observer
     */
    int nRet1_cli = -1;

    public void keyImport(final String userName, final String privateKey, Observer<Object> observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                try {
                    account_object obj = BitsharesWalletWraper.getInstance().get_account_object(userName);
                    if (obj == null) {
                        FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                        throw new BorderlessException("账户不存在");
                    }

                    types.public_key_type publicKeyType = obj.owner.get_keys().get(0);
                    String strPublicKey = publicKeyType.toString();

                    try {
                        types.private_key_type privateKeyType = new types.private_key_type(privateKey);
                        if (privateKeyType == null) {

                            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                            throw new BorderlessException("私钥错误");
                        }

                        public_key publicKey = privateKeyType.getPrivateKey().get_public_key();
                        types.public_key_type publicKeyType2 = new types.public_key_type(publicKey);
                        String strWifPubKey = publicKeyType2.toString();

                        if (!strPublicKey.equals(strWifPubKey)) {

                            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                            throw new BorderlessException("私钥错误");
                        }
                    } catch (Exception e) {

                        FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                        throw new BorderlessException("私钥错误");
                    }

                } catch (NetworkStatusException e) {

                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("当前网络不可用");
                }


                BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + userName + ".bin");


                //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                int flag = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + userName + ".bin", ChooseWalletActivity.PASSWORD);

                int nRet = -1;
                if (flag == 0) {
                    //java Android 钱包解锁
                    nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                } else {
//                    MyApp.showToast(getString(R.string.bds_wallet_load_err));
                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("钱包加载失败");
                }

                //解锁成功
                if (nRet != 0) {
                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("钱包解锁失败");
                }

                if (0 == BitsharesWalletWraper.getInstance().import_key(userName, ChooseWalletActivity.PASSWORD, privateKey)) {
                    /*导入成功*/
                    return SUCCESS;
                } else {

                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException(userName + "账户导入失败");
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);

    }

    /**
     * 私钥导入方式
     *
     * @param userName
     * @param privateKey
     * @param observer
     */
    public void importKey(final String userName, final String privateKey, final String type, Observer<Object> observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                try {
                    account_object obj = BitsharesWalletWraper.getInstance().get_account_object(userName);
                    if (obj == null) {
                        FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                        throw new BorderlessException("账户不存在");
                    }

                    types.public_key_type publicKeyType = obj.owner.get_keys().get(0);
                    String strPublicKey = publicKeyType.toString();

                    try {
                        types.private_key_type privateKeyType = new types.private_key_type(privateKey);
                        if (privateKeyType == null) {

                            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");

                            if (type.equals("shorthand")) {
                                throw new BorderlessException("速记词输入错误");
                            } else {
                                throw new BorderlessException("私钥错误");
                            }
                        }

                        public_key publicKey = privateKeyType.getPrivateKey().get_public_key();
                        types.public_key_type publicKeyType2 = new types.public_key_type(publicKey);
                        String strWifPubKey = publicKeyType2.toString();

                        if (!strPublicKey.equals(strWifPubKey)) {

                            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                            if (type.equals("shorthand")) {
                                throw new BorderlessException("速记词输入错误");
                            } else {
                                throw new BorderlessException("私钥错误");
                            }
                        }
                    } catch (Exception e) {

                        FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                        if (type.equals("shorthand")) {
                            throw new BorderlessException("速记词输入错误");
                        } else {
                            throw new BorderlessException("私钥错误");
                        }
                    }

                } catch (NetworkStatusException e) {

                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("当前网络不可用");
                }

                BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH +  userName + ".bin");

                //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                int flag = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + userName + ".bin", ChooseWalletActivity.PASSWORD);

                int nRet = -1;
                if (flag == 0) {
                    //java Android 钱包解锁
                    nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                } else {
                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("钱包加载失败");
                }

                //解锁成功
                if (nRet != 0) {
                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException("钱包解锁失败");
                }

                if (0 == BitsharesWalletWraper.getInstance().import_key(userName, ChooseWalletActivity.PASSWORD, privateKey)) {
                    /*导入成功*/
                    return SUCCESS;
                } else {

                    FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
                    throw new BorderlessException(userName + "账户导入失败");
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }


    /**
     * 根据保存的sp 筛选节点
     */
    public void pingIPAddress(final String nodes, Observer observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {

                List<String> list = new Gson().fromJson(nodes, new TypeToken<List<String>>() {
                }.getType());

                for (int i = 0; i < list.size(); i++) {

                    MyApp.CURRENT_NODE = list.get(i);
                    boolean b = Device.pingIpAddress();

                    if (b) {
                        break;
                    } else {
                        if (i == list.size() - 1) {
                            throw new BorderlessException("节点链接失败,请退出重试");
                        }
                    }
                }

                return "true";

            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }


    /**
     * 查询账户存在
     *
     * @param userName
     */
    public void checkUserExist(final String userName, Observer observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {

                account_object obj = null;
                try {
                    obj = BitsharesWalletWraper.getInstance().get_account_object(userName);
                    if (obj == null) {
                        throw new BorderlessException("该账户不存在");
                    } else {
                        return SUCCESS;
                    }
                } catch (NetworkStatusException e) {
                    throw new BorderlessException("当前网络不可用");
                }

            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }

    /**
     * 登录
     *
     * @param context
     * @param observer
     */
    public void login(final Context context, final String userName, Observer observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {
            @Override
            public String onPre() {
                global_property_object object = null;
                try {
                    object = BitsharesWalletWraper.getInstance().get_global_properties();
                } catch (NetworkStatusException e) {
                    throw new BorderlessException(context.getString(R.string.network_useless_400));
                }

                if (object == null) {
                    throw new BorderlessException(context.getString(R.string.network_useless_400));
                }

                //查询账户id
                try {
                    account_object account_object = BitsharesWalletWraper.getInstance().get_account_object(userName);
                    if (account_object != null) {

                        BitsharesWalletWraper.getInstance().clear();

                        BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + userName + ".bin", PASSWORD);

                        BitsharesWalletWraper.getInstance().unlock(PASSWORD);

                        types.public_key_type publicKeyType = account_object.owner.get_keys().get(0);
                        types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);

                        if (privateKeyType != null) {
                            String strPrivateKey = privateKeyType.toString();

                            SPUtils.put(userName + Const.PRIVATEKEY, strPrivateKey);
                        }

                        if (privateKeyType == null || privateKeyType.getPrivateKey() == null) {
                            throw new BorderlessException(context.getString(R.string.wallet_error_400));
                        }

                        loginAccountId = account_object.id;
                        gbdsTransferFee = Double.parseDouble(BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_TRANSER_OPERATION));
                    } else {
                        throw new BorderlessException(context.getString(R.string.user_not_exist));
                    }

                } catch (NetworkStatusException e) {
                    throw new BorderlessException(context.getString(R.string.network_useless_400));

                }

                account_object account = null;
                try {
                    account = BitsharesWalletWraper.getInstance().get_account_object(loginAccountId.toString());
                } catch (NetworkStatusException e) {
                    throw new BorderlessException(context.getString(R.string.network_useless_400));
                }

                if (account != null) {
                    if (account.membership_expiration_date.contains("1969")) {
//                        isLifeMember = "1";

                        //是否是终身会员
                        SPUtils.put(Const.IS_LIFE_MEMBER, true);
                    } else {
                        SPUtils.put(Const.IS_LIFE_MEMBER, false);
//                        isLifeMember = "0";
                    }

                    // 用户存在 并且钱包密码验证成功 保存信息
                    try {
                        String str = FileUtils.readSDFile(TangConstant.PATH + userName + ".bin").trim();
                        String str1 = str.replace("\n", "");
                        if (!str1.equalsIgnoreCase("")) {
                            JSONObject data1 = new JSONObject(str1);
                            JSONArray jsonArray = data1.optJSONArray("my_accounts");

                            int userCount = 0;

                            for (int i = 0; i < jsonArray.length(); i++) {

                                userCount = i;
                                //jsonArray中应该只有一个用户
                                JSONObject data2 = jsonArray.optJSONObject(i);
                                if (TextUtils.isEmpty(data2.optString("name")) || "null".equals(data2.optString("name"))) {
                                    if (userCount == jsonArray.length() - 1) {
                                        throw new BorderlessException("用户信息读取失败");
                                    } else {
                                        continue;
                                    }

                                } else {
                                    MyApp.set(BuildConfig.ID, data2.optString("id"));
                                    SPUtils.put(Const.USERNAME, data2.optString("name"));
                                    MyApp.set(BuildConfig.MEMOKEY, data2.optJSONObject("options").optString("memo_key"));
                                    return SUCCESS;
                                }
                            }

                            throw new BorderlessException("用户信息读取失败");

                        } else {
                            throw new BorderlessException("用户信息读取失败");
                        }

                    } catch (JSONException e) {
                        throw new BorderlessException("用户信息读取失败");
                    }

                } else {
                    throw new BorderlessException(context.getString(R.string.user_not_exist), BorderlessCode.USER_NOT_EXIST);
                }

            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }

    /**
     * 登录之前验证密码，主要是赋值
     *
     * @param userName
     * @param pwd
     */
    public void verifyPwd(final String userName, final String pwd, Observer observer) {
        Observable.create(new BorderlessOnSubscribe<String>() {

            @Override
            public String onPre() {

                String binStr = userName + ".bin";

//                BitsharesWalletWraper.getInstance().reset_400();

                int load = BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + binStr, pwd);
                if (load == -1) {
                    throw new BorderlessException("钱包加载失败");
                }

                int unlock = BitsharesWalletWraper.getInstance().unlock(pwd);
                if (unlock == 0) {

                    /*密码正确，把文件名和该钱包密码全局赋值 (我也不想这样呀。。。。)*/
                    ChooseWalletActivity.CURRTEN_BIN = binStr;
                    ChooseWalletActivity.PASSWORD = pwd;
                    return SUCCESS;
                } else {

                    throw new BorderlessException("密码输入有误", BorderlessCode.PASSWORD_INCORRECT);
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);
    }

    /**
     * 创建账户
     * @param pubKey
     * @param strAccountName
     * @param strRegistar
     * @param strReferrer
     * @param refferPercent
     * @param observer
     */
    public void registerForWebSocket(String pubKey, String strAccountName, String strRegistar, String strReferrer, int refferPercent,Observer observer) {

        Observable.create(new BorderlessOnSubscribe<String>() {

            @Override
            public String onPre() {

                signed_transaction signed_transaction = null;

                try {
                    signed_transaction = BitsharesWalletWraper.getInstance().create_account_with_pub_key(pubKey,strAccountName,strRegistar,strReferrer,refferPercent);
                } catch (NetworkStatusException e) {
                    throw new BorderlessException("账户注册失败", BorderlessCode.PASSWORD_INCORRECT);
                }

                if (signed_transaction != null) {

                    return SUCCESS;
                } else {

                    throw new BorderlessException("账户注册失败", BorderlessCode.PASSWORD_INCORRECT);
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);

    }


    /**
     * 提取矿池收益
     * @param name_or_id
     * @param vesting_name
     * @param amount
     * @param asset_symbol
     * @param observer
     */
    public void withdrawVestingWebSocket(String name_or_id, String vesting_name,String amount,String asset_symbol,Observer observer) {

        Observable.create(new BorderlessOnSubscribe<String>() {

            @Override
            public String onPre() {

                signed_transaction signed_transaction = null;

                try {
                    signed_transaction = BitsharesWalletWraper.getInstance().withdraw_vesting(name_or_id,vesting_name,amount,asset_symbol);
                } catch (NetworkStatusException e) {
                    throw new BorderlessException("提取收益失败", BorderlessCode.ERROR_FOR_TOAST);
                }

                if (signed_transaction != null) {

                    return SUCCESS;
                } else {

                    throw new BorderlessException("提取收益失败", BorderlessCode.ERROR_FOR_TOAST);
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);

    }

    /**
     * 升级终身会员
     * @param name
     * @param observer
     */
    public void upgradeAccountWebSocket(String name,Observer observer) {

        Observable.create(new BorderlessOnSubscribe<String>() {

            @Override
            public String onPre() {

                signed_transaction signed_transaction = null;

                try {
                    signed_transaction = BitsharesWalletWraper.getInstance().upgrade_account(name,true);
                } catch (NetworkStatusException e) {
                    throw new BorderlessException("会员升级失败", BorderlessCode.ERROR_FOR_TOAST);
                }

                if (signed_transaction != null) {

                    return SUCCESS;
                } else {

                    throw new BorderlessException("会员升级失败", BorderlessCode.ERROR_FOR_TOAST);
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);

    }


    /**
     * 借入、平仓
     * @param type  1---借入，2---平仓
     * @param account
     * @param amount_to_borrow
     * @param asset_symbol
     * @param amount_to_collateral
     * @param observer
     */
    public void borrowWebSocket(int type,String account,String amount_to_borrow,String asset_symbol,String amount_to_collateral,Observer observer) {

        Observable.create(new BorderlessOnSubscribe<String>() {

            @Override
            public String onPre() {

                signed_transaction signed_transaction = null;

                try {
                    signed_transaction = BitsharesWalletWraper.getInstance().borrow_asset(account,amount_to_borrow,asset_symbol,amount_to_collateral);
                } catch (NetworkStatusException e) {
                    if (type == 1) {
                        throw new BorderlessException("借入失败", BorderlessCode.ERROR_FOR_TOAST);
                    }else {
                        throw new BorderlessException("平仓失败", BorderlessCode.ERROR_FOR_TOAST);
                    }
                }

                if (signed_transaction != null) {

                    return SUCCESS;
                } else {

                    if (type == 1) {
                        throw new BorderlessException("借入失败", BorderlessCode.ERROR_FOR_TOAST);
                    }else {
                        throw new BorderlessException("平仓失败", BorderlessCode.ERROR_FOR_TOAST);
                    }
                }
            }
        }).compose(RxJavaUtil.threadTransform()).subscribe(observer);

    }

//        Observable.create(new OnSubscribe<Drawable>() {
//            @Override
//            public void call(Subscriber<? super Drawable> subscriber) {
//                Drawable drawable = getTheme().getDrawable(drawableRes));
//                subscriber.onNext(drawable);
//                subscriber.onCompleted();
//            }
//        })
//                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
//                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
//                .subscribe(new Observer<Drawable>() {
//                    @Override
//                    public void onNext(Drawable drawable) {
//                        imageView.setImageDrawable(drawable);
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show();
//                    }
//                });
}

