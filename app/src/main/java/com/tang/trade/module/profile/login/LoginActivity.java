package com.tang.trade.module.profile.login;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.YWLoginParam;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.login.YWLoginCode;
import com.flh.framework.toast.ToastAlert;
import com.flh.framework.util.FileUtil;
import com.google.gson.Gson;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.BorderlessCode;
import com.tang.trade.module.recover.RecoverActivity;
import com.tang.trade.module.register.generate.GenerateWordsActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.block_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.SPUtils;
import com.tang.trade.widget.ShowInfoDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/4/8.
 * 登录页面
 */

public class LoginActivity extends AbsMVPActivity<LoginContract.Presenter> implements LoginContract.View, TextWatcher {

    private static int REQUEST_CODE = 1001;

    @BindView(R.id.tv_account)
    TextView mTvAccount;
    @BindView(R.id.iv_account_spinner)
    ImageView mIvAccountSpinner;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.eye_password)
    ImageView mEyePassword;
    @BindView(R.id.tv_forget_pwd)
    TextView mTvForgetPwd;
    @BindView(R.id.btn_login)
    TextView mBtnLogin;
    @BindView(R.id.tv_register_account)
    TextView mTvRegisterAccount;
    @BindView(R.id.tv_import_account)
    TextView mTvImportAccount;
    @BindView(R.id.tv_test)
    TextView mTvtest;

    boolean isShow = true;
    private YWIMKit mIMKit;
    private AlertDialog.Builder singleChoiceDialog;
    private List<String> accounts = new ArrayList<>();
    private int position;
    private int index;
    private ShowInfoDialog mShowInfoDialog;

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        starter.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_400);
        setupViews();

        if (com.tang.trade.tang.BuildConfig.DEBUG) {
            mTvtest.setVisibility(View.VISIBLE);
        } else {
            mTvtest.setVisibility(View.GONE);
        }

        requestPermissions();

        String node = MyApp.get("current_node", "");
        if (TextUtils.isEmpty(node)) {

            //没有存储要重新获取节点
            mPresenter.getNodes();
        } else {
            MyApp.CURRENT_NODE = node;

            //ping不通也要重新请求获取
            mPresenter.pingIPAddress();
        }


        /* Const.USERNAME 在app内不需要remove或put空值，因为每次登录都会保存相应的用户名。
         * 那么只有在app卸载完重装的时候会取到空值，当取到空的时候，删除所有钱包文件（即用户必须重新恢复）
         *
         * 原因：如果不恢复，卸载后新用户再登录时没有速记词，生成二维码时会存成私钥而非速记词；
         * */
        String userName = SPUtils.getString(Const.USERNAME, "");
        if (!TextUtils.isEmpty(userName) && FileUtil.isFileExist(TangConstant.PATH + userName + ".bin")) {
            mTvAccount.setText(userName);
        } else {

            if (TextUtils.isEmpty(SPUtils.getString(Const.RECOVER_SUCCESS, ""))) {
                FileUtil.deleteFile(TangConstant.PATH);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void getIntentValue() {

    }

    private void setupViews() {
        AlphaUtil.setDisable(mBtnLogin);

        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));

        mTvAccount.addTextChangedListener(this);
        mEtPassword.addTextChangedListener(this);
    }

    @OnClick({R.id.iv_account_spinner, R.id.tv_account, R.id.eye_password, R.id.tv_forget_pwd, R.id.btn_login, R.id.tv_register_account, R.id.tv_import_account})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_account_spinner:
            case R.id.tv_account:
                /*获取钱包文件保存目录下的所有bin文件名*/
                List<String> fileNameList = FileUtil.getFileNameList(TangConstant.PATH);
                //先循环一遍，把非法文件剔除
                for (int i = 0; i < fileNameList.size(); i++) {
                    String file = fileNameList.get(i);

                    if (file.startsWith("null") || !file.contains(".bin")) {
                        FileUtil.deleteFile(TangConstant.PATH + file);
                    }
                }

                List<String> files = FileUtil.getFileNameList(TangConstant.PATH);
                String[] items = new String[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    items[i] = files.get(i).split("\\.")[0];
                }

                if (items.length == 0) {
                    mTvAccount.setText("");
                    mShowInfoDialog = new ShowInfoDialog(this, R.style.dialog_style, v -> {
                        switch (v.getId()) {
                            case R.id.dl_tv_restore:
                                RecoverActivity.start(LoginActivity.this);
                                mShowInfoDialog.dismiss();
                                break;
                            case R.id.dl_tv_register:
                                GenerateWordsActivity.start(LoginActivity.this);
                                mShowInfoDialog.dismiss();
                                break;
                            case R.id.dl_tv_cancel:
                                mShowInfoDialog.dismiss();
                                break;
                        }
                    });
                    mShowInfoDialog.show();
                } else {
                    showSingleAccountDialog(items);
                }

                break;
            case R.id.eye_password:
                if (isShow) {
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mEyePassword.setImageResource(R.mipmap.common_eye_true);
                    isShow = false;
                } else {
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mEyePassword.setImageResource(R.mipmap.common_eye_false);
                    isShow = true;
                }
                mEtPassword.setSelection(mEtPassword.getText().toString().trim().length());
                break;
            case R.id.tv_forget_pwd:

                showCancleableDialog("忘记密码", "恢复账户即可重新设置“登录密码”", "恢复", new DialogCallBack() {
                    @Override
                    public void onSuccess() {
                        RecoverActivity.start(LoginActivity.this);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            case R.id.btn_login:
                String userName = mTvAccount.getText().toString().trim();
                String pwd = mEtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
                    ToastAlert.showToast(this, getString(R.string.name_or_pwd_not_null));
                    return;
                }

                /*1.赋值 全局文件名以及密码*/
                ChooseWalletActivity.CURRTEN_BIN = userName + ".bin";

                /*2.1验证钱包密码是否正确 正确的话，需要赋值CURRENT_BIN和PASSWORD*/
                mPresenter.verifyPwd(userName, pwd);

                //2.2登录IM
                loginIM(userName);

                break;
            case R.id.tv_register_account:
                GenerateWordsActivity.start(this);
                break;
            case R.id.tv_import_account:
                RecoverActivity.start(this);
                break;
        }
    }

    @Override
    public LoginContract.Presenter getPresenter() {
        return new LoginPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        if (null == error.getErrorCode()) {

            ToastAlert.showToast(this, error.getErrorMessage());

        } else if (error.getErrorCode().equals(BorderlessCode.USER_NOT_EXIST) || error.getErrorCode().equals(BorderlessCode.PASSWORD_INCORRECT)) {

            showConfirmDialog(error.getErrorMessage(), "提示");
        } else {

            ToastAlert.showToast(this, error.getErrorMessage());
        }
    }

    @Override
    public void passwordRight(String userName) {
        /*3. 密码正确，登录（查询账户存在否)*/
        mPresenter.login(this, userName);
    }

    @Override
    public void userExist(String userName) {
        // 7.登录IM成功
        MainActivity.start(LoginActivity.this);
        finish();
    }

    @Override
    public void regIMSuccess(String userName) {
        loginIM(userName);
    }

    @Override
    public void nodeSuccess(final List<String> list) {
        //获取到节点
        Log.i(TAG, "nodeSuccess: " + list.toString());

        //保存节点列表
        SPUtils.put(Const.NODES, new Gson().toJson(list));

        showLoading("正在筛选节点...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list.size() > 0) {
                    for (int j = 0; j < 2; j++) {
                        for (int i = 0; i < list.size(); i++) {
                            MyApp.CURRENT_NODE = list.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag) {
                                MyApp.set("current_node", list.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }
                        if (index == 1) {
                            break;
                        }
                    }

                } else {
                    Gson gson = new Gson();
                    ArrayList<String> data = gson.fromJson(TangConstant.NODE_JSON, ArrayList.class);
                    for (int j = 0; j < 2; j++) {
                        for (int i = 0; i < data.size(); i++) {
                            MyApp.CURRENT_NODE = data.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag) {
                                MyApp.set("current_node", list.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }
                        if (index == 1) {
                            break;
                        }
                    }
                }

                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        MyApp.set("nodes", list.size());
                        MyApp.set("node" + i, list.get(i));
                    }
                }
            }
        }).start();


    }

    @Override
    public void pingIPResult(String s) {
        if (s.equals("true")) {
            //ping通  则不需要操作
        } else if (s.equals("false")) {
            //本地存储的节点ping不通，需要重新获取
            mPresenter.getNodes();
        }
    }

    private void loginIM(final String userName) {
        //开始登录
        String userid = userName;
        mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);

        String password = MD5.md5(userName + "borderless");
        IYWLoginService loginService = mIMKit.getLoginService();
        YWLoginParam loginParam = YWLoginParam.createLoginParam(userid, password);
        loginService.login(loginParam, new IWxCallback() {
            @Override
            public void onSuccess(Object... arg0) {

                //IM登录成功的标志
                SPUtils.put(Const.SP.IM_LOGIN_SUCCESS, true);
            }

            @Override
            public void onProgress(int arg0) {
                showProgressDialog("正在登录...");
            }

            @Override
            public void onError(int errCode, String description) {
                if (errCode == YWLoginCode.LOGON_FAIL_INVALIDUSER) {

                    /*5.IM账户不存在 需要去注册*/
                    mPresenter.regIM(userName);
                }
            }
        });
    }

    private void showSingleAccountDialog(final String[] items) {
        if (singleChoiceDialog == null) {
            singleChoiceDialog = new AlertDialog.Builder(this, R.style.SingleChoiceDialogStyle);
//            singleChoiceDialog.setTitle(getString(R.string.bds_select_account));
            singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel), null);
        }

        singleChoiceDialog.setSingleChoiceItems(items, position, (dialog, which) -> {
            position = which;
            mTvAccount.setText(items[position]);
            dialog.dismiss();
        });
        singleChoiceDialog.show();

    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = mTvAccount.getText().toString().trim();
        String pwd = mEtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            AlphaUtil.setDisable(mBtnLogin);
        } else {
            AlphaUtil.setEnable(mBtnLogin);
        }
    }

}
