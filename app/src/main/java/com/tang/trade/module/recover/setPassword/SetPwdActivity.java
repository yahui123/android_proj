package com.tang.trade.module.recover.setPassword;

import android.Manifest;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.flh.framework.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.profile.login.LoginActivity;
import com.tang.trade.module.recover.fileRecover.UserToKey;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.utils.AESCipher;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.DatabaseUtil;
import com.tang.trade.utils.Generate16;
import com.tang.trade.utils.SPUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ad on 2018/4/11.
 * 恢复后需要重新设置密码
 */

public class SetPwdActivity extends AbsMVPActivity<SetPwdContract.Presenter> implements SetPwdContract.View, TextWatcher {

    private static final String USERNAME = "username";
    private static final String PRIVATE_KEY = "privateKey";
    private static final String USERLIST = "userList";
    public static final String TYPE = "type";
    private static final String WORDS = "words";

    public static final String FILE_RECOVER = "file";
    public static final String WORD_RECOVER = "word";
    public static final String SHORTHAND_RECOVER = "shorthand";
    public static final String PWD_RECOVER = "password";


    @BindView(R.id.et_set_pwd)
    EditText mEtSetPwd;
    @BindView(R.id.et_confirm_pwd)
    EditText mEtConfirmPwd;
    @BindView(R.id.tv_btn)
    TextView mTvBtn;
    @BindView(R.id.iv_cicle1)
    ImageView mIvCircle1;
    @BindView(R.id.tv_limit1)
    TextView mTvLimit1;
    @BindView(R.id.iv_cicle2)
    ImageView mIvCircle2;
    @BindView(R.id.tv_limit2)
    TextView mTvLimit2;
    @BindView(R.id.iv_eye_pwd)
    ImageView mIvEyePwd;
    @BindView(R.id.iv_eye_confirm_pwd)
    ImageView mIvEyeConfirmPwd;

    private String userName;
    private String privateKey;
    private List<UserToKey> list;
    private String type;
    private String words = "";//初始值 防止null奔溃
    private boolean isOneShow = false;
    private boolean isTwoShow = false;

    public static void start(Context context, String userName, String privateKey, String type) {
        Intent starter = new Intent(context, SetPwdActivity.class);
        starter.putExtra(USERNAME, userName);
        starter.putExtra(PRIVATE_KEY, privateKey);
        starter.putExtra(TYPE, type);
        context.startActivity(starter);
    }

    public static void start(Context context, String userListStr, String type) {
        Intent starter = new Intent(context, SetPwdActivity.class);
        starter.putExtra(USERLIST, userListStr);
        starter.putExtra(TYPE, type);
        context.startActivity(starter);
    }

    public static void start(Context context, String userName, String privateKey, String words, String type) {
        Intent starter = new Intent(context, SetPwdActivity.class);
        starter.putExtra(USERNAME, userName);
        starter.putExtra(PRIVATE_KEY, privateKey);
        starter.putExtra(WORDS, words);
        starter.putExtra(TYPE, type);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd_400);

        requestPermissions();

        getIntentValue();

        setupViews();
    }

    @Override
    public void getIntentValue() {
        userName = getIntent().getStringExtra(USERNAME);
        privateKey = getIntent().getStringExtra(PRIVATE_KEY);
        type = getIntent().getStringExtra(TYPE);
        words = getIntent().getStringExtra(WORDS); //速记词

        String userListStr = getIntent().getStringExtra(USERLIST);
        list = new Gson().fromJson(userListStr, new TypeToken<List<UserToKey>>() {
        }.getType());
    }

    private void setupViews() {
        AlphaUtil.setDisable(mTvBtn);

//        BdsTextWatcher watcher = new BdsTextWatcher() {
//            @Override
//            protected void afterChanged(Editable s) {
//                String pwd = mEtSetPwd.getText().toString().trim();
//                String confirm = mEtConfirmPwd.getText().toString().trim();
//
//                if (TextUtils.isEmpty(pwd) || TextUtils.isEmpty(confirm)){
//                    mTvBtn.setClickable(false);
//                    mTvBtn.setAlpha(0.4f);
//                }else {
//                    mTvBtn.setClickable(true);
//                    mTvBtn.setAlpha(1.0f);
//                }
//            }
//        };

        mEtSetPwd.addTextChangedListener(this);
        mEtConfirmPwd.addTextChangedListener(this);

        setupDefultToolbar(getString(R.string.set_pwd_400));
        setToolbarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));

    }

    @Override
    public SetPwdContract.Presenter getPresenter() {
        return new SetPwdPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    @OnClick(R.id.tv_btn)
    public void onViewClicked() {

        String pwd = mEtSetPwd.getText().toString().trim();
        String confirm = mEtConfirmPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            ToastAlert.showToast(this, getString(R.string.password_not_be_null_400));
            return;
        }

        if (!TextUtils.equals(pwd, confirm)) {
            ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0303", getString(R.string.password_different_400)));
            return;
        }

        if (!com.tang.trade.utils.TextUtils.isNumberLetter(pwd)) {
            ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0302", getString(R.string.pwd_rule3_400)));
            return;
        }

        if (type.equals(WORD_RECOVER)) { //通用恢复
            /*1.删除同名文件*/
            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
            /*2.创建以用户名为文件名的钱包文件*/
            mPresenter.createWalletFile(userName, privateKey, pwd);

        } else if (type.equals(FILE_RECOVER)) { //文件恢复

            //文件恢复
            mPresenter.recoverMultiWallet(list, pwd);

        } else if (type.equals(PWD_RECOVER)) {  //密码恢复

            //密码恢复 这里的privateKey就是老密码
            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
            mPresenter.createWalletFile(userName, privateKey, pwd);

        }else if (type.equals(SHORTHAND_RECOVER)){ //速记词恢复

            FileUtil.deleteFile(TangConstant.PATH + userName + ".bin");
            mPresenter.createWalletFile(userName, privateKey, pwd);
        }

    }

    @Override
    public void recoverMultiSuccess() {
        ToastAlert.showToast(this, "账户恢复成功");
        //恢复成功的标志
        SPUtils.put(Const.RECOVER_SUCCESS, "success");
        LoginActivity.start(this);
    }

    @Override
    public void creatWalletSuccess(String userName, String privateKey,String pwd) {
        if (type.equals(PWD_RECOVER)) {
            //密码恢复 导入方式与私钥不同
            mPresenter.importKeyForPwd(userName, privateKey);
        } else if (type.equals(SHORTHAND_RECOVER)){

            //type 可以用于不同恢复方式显示不同提示语
            mPresenter.importKey(userName, privateKey,type);
        }else {
            /*3.创建成功后，导入该用户到该文件(私钥导入方式)  此时已经验证了私钥，登录时不必再验证 只需验证钱包密码以及给CURRENT_BIN和PASSWORD赋值*/
            mPresenter.keyImport(userName, privateKey);
        }
    }

    @Override
    public void keyImportSuccess() {
        String confirm = mEtConfirmPwd.getText().toString().trim();
        if (!TextUtils.isEmpty(confirm) && !TextUtils.isEmpty(words)) {
            try {
                //所有恢复都可以直接保存 用的时候会进行非空判断
                String encryptWord = AESCipher.aesEncryptString(words, Generate16.Generate16Number(confirm));
                //成功后保存加密后的速记词(key是用户名)
                SPUtils.put(userName, encryptWord);

                SPUtils.put(userName + Const.DIRECT_WORDS, words);
            } catch (Exception e) {
                ToastAlert.showToast(this, "速记词保存失败");
                e.printStackTrace();
                return;
            }
        }
        ToastAlert.showToast(this, "账户恢复成功");

        //恢复成功的标志
        SPUtils.put(Const.RECOVER_SUCCESS, "success");

        LoginActivity.start(this);
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
        String pwd = mEtSetPwd.getText().toString().trim();
        String confirm = mEtConfirmPwd.getText().toString().trim();

        if (pwd.length() >= 8) {
            mIvCircle1.setBackgroundResource(R.drawable.shape_circle_select_bg_400);
            mTvLimit1.setTextColor(ContextCompat.getColor(this, R.color.mine_security_text));
        } else {
            mIvCircle1.setBackgroundResource(R.drawable.shape_circle_unselect_bg_400);
            mTvLimit1.setTextColor(ContextCompat.getColor(this, R.color.common_text_gray));
        }

        if (com.tang.trade.utils.TextUtils.isNumberLetter(pwd)) {
            mIvCircle2.setBackgroundResource(R.drawable.shape_circle_select_bg_400);
            mTvLimit2.setTextColor(ContextCompat.getColor(this, R.color.mine_security_text));
        } else {
            mIvCircle2.setBackgroundResource(R.drawable.shape_circle_unselect_bg_400);
            mTvLimit2.setTextColor(ContextCompat.getColor(this, R.color.common_text_gray));
        }

        if (TextUtils.isEmpty(pwd) || TextUtils.isEmpty(confirm)) {
            AlphaUtil.setDisable(mTvBtn);
        } else {
            AlphaUtil.setEnable(mTvBtn);
        }
    }


    @OnClick({R.id.iv_eye_pwd, R.id.iv_eye_confirm_pwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_eye_pwd:
                if (isOneShow) {
                    mEtSetPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIvEyePwd.setImageResource(R.mipmap.common_eye_true);
                    isOneShow = false;
                } else {
                    mEtSetPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIvEyePwd.setImageResource(R.mipmap.common_eye_false);
                    isOneShow = true;
                }
                mEtSetPwd.setSelection(mEtSetPwd.getText().toString().trim().length());
                break;
            case R.id.iv_eye_confirm_pwd:
                if (isTwoShow) {
                    mEtConfirmPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIvEyeConfirmPwd.setImageResource(R.mipmap.common_eye_true);
                    isTwoShow = false;
                } else {
                    mEtConfirmPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIvEyeConfirmPwd.setImageResource(R.mipmap.common_eye_false);
                    isTwoShow = true;
                }
                mEtConfirmPwd.setSelection(mEtConfirmPwd.getText().toString().trim().length());
                break;
        }
    }
}
