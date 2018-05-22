package com.tang.trade.module.register.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.utils.AESCipher;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.DatabaseUtil;
import com.tang.trade.utils.Generate16;
import com.tang.trade.utils.SPUtils;
import com.tang.trade.widget.LimitEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建用户
 */
public class CreateUserActivity extends AbsMVPActivity<CreateUserContract.Presenter> implements CreateUserContract.View, LimitEditText.EnableListener, LimitEditText.InterceptListener {
    public static final int CREATE_RESULT = 101;
    public static final String CREATE_USER_WORD = "word";
    public static final String BACK_USER = "user";
    public static final String PASSWORD = "password";
    @BindView(R.id.et_create_user)
    LimitEditText etCreateUser;
    @BindView(R.id.et_create_password)
    LimitEditText etCreatePassword;
    @BindView(R.id.eye_password)
    ImageView eyePassword;
    @BindView(R.id.et_create_second_password)
    LimitEditText etCreateSecondPassword;
    @BindView(R.id.eye_second_password)
    ImageView eyeSecondPassword;
    @BindView(R.id.et_referrer)
    LimitEditText etReferrer;
    @BindView(R.id.tv_commit)
    TextView btnCommit;
    private String mUserName;
    private String secondPassword;
    private String referName;
    private String mMnemonicWord;
    private YWIMKit mIMKit;

    public static void start(Activity activity, String word) {
        Intent intent = new Intent(activity, CreateUserActivity.class);
        intent.putExtra(CREATE_USER_WORD, word);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_user_400);

        setupView();
    }

    @Override
    public void getIntentValue() {
        mMnemonicWord = getIntent().getStringExtra(CreateUserActivity.CREATE_USER_WORD);
    }

    @Override
    public CreateUserContract.Presenter getPresenter() {
        return new CreateUserPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    protected void setupView() {
        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
        AlphaUtil.setDisable(btnCommit);

        etCreateUser.setEnableListener(this);
        etCreatePassword.setEnableListener(this);
        etCreateSecondPassword.setEnableListener(this);

        etCreateSecondPassword.setInterceptListener(this);
        etCreatePassword.setInterceptListener(this);
        etCreateUser.setInterceptListener(str -> {
            if (str.toString().matches("[A-Z]+")) {
                return true;
            }
            if (etCreateUser.getText().length() == 0) {
                Pattern pattern = Pattern.compile("[0-9]");
                final Matcher matcher = pattern.matcher(str + "");
                if (matcher.find(0)) {
                    ToastAlert.showToast(CreateUserActivity.this, getString(R.string.alert_create_first_not_numer_400));
                    return true;
                }
            }

            return false;
        });
    }

    @OnClick(R.id.img_back)
    public void onBackClicked() {
        finish();
    }

    @OnClick(R.id.eye_password)
    public void onEyePasswordClicked() {
        etCreatePassword.showPassword(eyePassword);

    }

    @OnClick(R.id.eye_second_password)
    public void onEyePasswordSecondClicked() {
        etCreateSecondPassword.showPassword(eyeSecondPassword);

    }

    @OnClick(R.id.tv_commit)
    public void onCommitClicked() {
        commit();

    }

    @OnClick(R.id.tv_create_protocol)
    public void onCreateProtocolClicked() {
        ProtocolActivity.start(this);

    }

    /**
     * 确定
     */
    private void commit() {
        mUserName = etCreateUser.getText().toString();
        int userNameLength = mUserName.length();
        if (TextUtils.isEmpty(mUserName) || userNameLength > 20) {
            moveLastCursor(etCreateUser);
            ToastAlert.showToast(this, getString(R.string.alert_create_first_not_numer_400));
            return;
        }

        //首字母不能是数字
        Pattern pattern = Pattern.compile("[0-9]");
        final Matcher matcher = pattern.matcher(mUserName.charAt(0) + "");
        if (matcher.find(0)) {
            ToastAlert.showToast(this, getString(R.string.alert_create_first_not_numer_400));
            return;
        }

        String password = etCreatePassword.getText().toString();
        if (!com.tang.trade.utils.TextUtils.isNumberLetter(password)) {
            moveLastCursor(etCreatePassword);
            ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0302", getString(R.string.pwd_rule3_400)));
            return;
        }

        secondPassword = etCreateSecondPassword.getText().toString();
        if (TextUtils.isEmpty(secondPassword)) {
            moveLastCursor(etCreateSecondPassword);
            ToastAlert.showToast(this, getString(R.string.alert_create_not_second_password_400));
            return;
        }

        if (!password.equals(secondPassword)) {
            moveLastCursor(etCreateSecondPassword);
            ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0303", getString(R.string.pwd_rule3_400)));
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etCreateUser.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etCreatePassword.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etCreateSecondPassword.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etReferrer.getWindowToken(), 0);
        }

        mPresenter.sendGenerateKeyPair(mMnemonicWord);

    }

    private void moveLastCursor(EditText et) {
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et, 0);

    }

    @Override
    public void backGenerateKeyPair(String privateKey, String publicKey) {
        /**
         * 邀请人
         */
        referName = etReferrer.getText().toString();
        mPresenter.sendRegister(mUserName, publicKey, referName, privateKey);
    }

    @Override
    public void backRegister() {
        mPresenter.sendCreateWalletFile(mUserName, secondPassword);

        //2.2登录IM
        loginIM(mUserName);
    }

    @Override
    public void regIMSuccess(String userName) {
        loginIM(userName);
    }

    @Override
    public void backCreateWalletFile() {
        mPresenter.sendKeyImport(mUserName);
    }

    @Override
    public void backKeyImport() {
        try {
            /**
             * 通过密码将 速记词加密  并保存
             */
            String passwordKey = AESCipher.aesEncryptString(getIntent().getStringExtra(CreateUserActivity.CREATE_USER_WORD), Generate16.Generate16Number(secondPassword));
            SPUtils.put(mUserName, passwordKey);

            //还得保存一下裸着的速记词
            SPUtils.put(mUserName + Const.DIRECT_WORDS, getIntent().getStringExtra(CreateUserActivity.CREATE_USER_WORD));

            ToastAlert.showToast(this, getString(R.string.show_create_register_success_400));

            /*注册成功，继续走登录*/
            String pwd = etCreatePassword.getText().toString().trim();
            mPresenter.verfiyPwd(mUserName, pwd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void verfiySuccess(String userName) {
        mPresenter.login(this, userName);
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

    @Override
    public void loginSuccess() {
        MainActivity.start(this);
        finish();
    }

    @Override
    public void noInput() {
        AlphaUtil.setDisable(btnCommit);
    }

    @Override
    public void yesInput(String msg) {
        if (etCreateUser.isInput() && etCreatePassword.isInput() && etCreateSecondPassword.isInput()) {
            AlphaUtil.setEnable(btnCommit);
        }
    }

    @Override
    public boolean check(CharSequence str) {
        return com.tang.trade.utils.TextUtils.isBlank(str);
    }
}
