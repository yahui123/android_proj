package com.tang.trade.module.profile.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.profile.login.LoginActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.websocket_api;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.DatabaseUtil;
import com.tang.trade.widget.LimitEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 修改密码
 */
public class ModifyPasswordActivity extends AbsMVPActivity<ModifyPasswordContract.Presenter> implements ModifyPasswordContract.View, LimitEditText.EnableListener {

    @BindView(R.id.et_old_password)
    LimitEditText etOldPassword;
    @BindView(R.id.eye_old)
    ImageView eyeOld;
    @BindView(R.id.et_new_password)
    LimitEditText etNewPassword;
    @BindView(R.id.eye_new)
    ImageView eyeNew;
    @BindView(R.id.et_second_new)
    LimitEditText etSecondNew;
    @BindView(R.id.eye_second_new)
    ImageView eyeSecondNew;

    @BindView(R.id.tv_commit)
    TextView tvCommit;

    @BindView(R.id.tv_alert_first)
    TextView tvAlertFirst;

    @BindView(R.id.tv_alert_second)
    TextView tvAlertSecond;


    private boolean isFirstLight, isSecondLight;
    private AlertDialog customizeDialog;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, ModifyPasswordActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_motify_password_400);

        setupView();
    }

    @Override
    public ModifyPasswordContract.Presenter getPresenter() {
        return new ModifyPasswordPresenter(this);
    }

    @Override
    public void onError(DataError error) {

    }

    private void setupView() {
        setupDefultToolbar(R.string.modify_password_title_400);

        etOldPassword.setEnableListener(this);
        etNewPassword.setEnableListener(this);
        etSecondNew.setEnableListener(this);
        etNewPassword.setInterceptListener(str -> {
            if (str.toString().matches("[A-Z]+")) {
                return true;
            }
            return false;
        });

    }

    @OnClick({R.id.eye_old, R.id.eye_new, R.id.eye_second_new, R.id.tv_commit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.eye_old:
                etOldPassword.showPassword(eyeOld);
                break;
            case R.id.eye_new:
                etNewPassword.showPassword(eyeNew);
                break;
            case R.id.eye_second_new:
                etSecondNew.showPassword(eyeSecondNew);
                break;
            case R.id.tv_commit:
                String oldPass = etOldPassword.getText().toString();
                String password = etNewPassword.getText().toString();
                String newPassword = etSecondNew.getText().toString();
                if (!password.equals(newPassword)) {
                    ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0303", getString(R.string.password_different_400)));
                    return;
                }
                if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(password) || TextUtils.isEmpty(newPassword)) {
                    return;
                }
                /**
                 * 数字和字母组合
                 */
                if (!com.tang.trade.utils.TextUtils.isNumberLetter(password)) {
                    ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0302", getString(R.string.pwd_rule3_400)));
                }

                mPresenter.sendModifyWalletPassword(newPassword, oldPass);
                break;
            default:
                break;
        }
    }

    @Override
    public void noInput() {
        tvCommit.setBackgroundResource(R.drawable.btn_gray);
        tvCommit.setEnabled(false);
    }

    @Override
    public void yesInput(String msg) {
        //符合字母和数字组合
        if (etNewPassword.isInput()) {
            String pwdTxt = etNewPassword.getText().toString();

            if (com.tang.trade.utils.TextUtils.checkNumberLetter(pwdTxt)) {
                isSecondLight = true;
            } else {
                isSecondLight = false;
            }
            //处理长度
            if (pwdTxt.length() >= 8) {
                isFirstLight = true;
            } else {
                isFirstLight = false;
            }
            if (isFirstLight && isSecondLight) {
                setShowColor(R.color.common_text_blue, R.color.common_text_blue);
                if (etOldPassword.isInput() && etNewPassword.isInput() && etSecondNew.isInput()) {
                    tvCommit.setEnabled(true);
                    tvCommit.setBackgroundResource(R.drawable.selector_btn_bg);
                }
            } else if (isFirstLight) {
                setShowColor(R.color.common_text_blue, R.color.common_text_gray);
                noInput();
            } else if (isSecondLight) {
                setShowColor(R.color.common_text_gray, R.color.common_text_blue);
                noInput();
            }

            if (!isSecondLight && !isFirstLight) {
                setShowColor(R.color.common_text_gray, R.color.common_text_gray);
                noInput();
            }
        }

    }


    private void setShowColor(int firstColor, int secondColor) {
        tvAlertFirst.setTextColor(ContextCompat.getColor(this, firstColor));
        tvAlertSecond.setTextColor(ContextCompat.getColor(this, secondColor));
    }


    /**
     * 弹出 密码修改成功 请重新登录的提示
     */
    private void showPopupConfirm() {
        customizeDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).create();
        customizeDialog.setInverseBackgroundForced(false);
        customizeDialog.show();
        Window window = customizeDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.confirm_change_password);
        customizeDialog.setCanceledOnTouchOutside(false);

        customizeDialog.setCancelable(false);

        TextView tv_submit = window.findViewById(R.id.tv_submit);

        tv_submit.setOnClickListener(v -> {
            customizeDialog.dismiss();
//                mPresenter.sendJump();
            //todo : 跳转到我的页面，type=安全中心
            //    MeActivity.start(ModifyPasswordActivity.this, MeActivity.TYPE_ANQUAN);
            finish();
        });

    }

    @Override
    public void backModifyPasswordSuccess() {
        showPopupConfirm();
//        ToastAlert.showToast(this, getString(R.string.alert_modify_password_commit_success_400));
    }

    @Override
    public void backModifyPasswordError() {
        ToastAlert.showToast(this, getString(R.string.alert_modify_password_not_allow_400));
        if (etOldPassword.getText().toString().length() != 0) {
            etOldPassword.setSelection(etOldPassword.getText().toString().length());
        }
    }

    @Override
    public void backJump() {
        BitsharesWalletWraper.getInstance().close();

        websocket_api.WEBSOCKET_CONNECT_INVALID = -1;
        MyApp.set(BuildConfig.ID, "");
        MyApp.set(BuildConfig.MEMOKEY, "");
        LoginActivity.start(this);
    }

    @Override
    public void getIntentValue() {

    }
}
