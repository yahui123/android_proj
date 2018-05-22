package com.tang.trade.tang.ui.loginactivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.websocket_api;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.ActivityManagerUrils;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.utils.SPUtils;

import butterknife.BindView;


/**
 * Created by Administrator on 2018/2/5.
 */

public class ChangePasswordActivity extends BaseActivity {
    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.et_OldPassword)
    EditText et_OldPassword;

    @BindView(R.id.et_NewPassword)
    EditText et_NewPassword;

    @BindView(R.id.et_confirm)
    EditText et_confirm;

    @BindView(R.id.btn_summit)
    Button btn_summit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_change_password;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            case R.id.btn_summit:
                if (UtilOnclick.isFastClick()) {
                    String oldPwd = et_OldPassword.getText().toString().trim();
                    String newPwd = et_NewPassword.getText().toString().trim();
                    String comfrimPwd = et_confirm.getText().toString().trim();
                    if (TextUtils.isEmpty(oldPwd)) {
                        MyApp.showToast(getString(R.string.bds_input_old_pwd));
                    } else if (TextUtils.isEmpty(newPwd)) {
                        MyApp.showToast(getString(R.string.bds_input_new_pwd));
                    } else if (TextUtils.isEmpty(comfrimPwd)) {
                        MyApp.showToast(getString(R.string.bds_input_comfirm_new_pwd));
                    } else if (!ChooseWalletActivity.PASSWORD.equals(oldPwd)) {
                        MyApp.showToast(getString(R.string.bds_input_old_pwd_fail));
                    } else if (newPwd.length() < 6 || comfrimPwd.length() < 6) {
                        MyApp.showToast(getString(R.string.bds_PasswordLenghCheck));
                    } else if (!newPwd.equals(comfrimPwd)) {
                        MyApp.showToast(getString(R.string.bds_note_pwd_not_agree));
                    } else if (!newPwd.matches("^([0-9a-zA-Z]{6,20})$")) {
                        MyApp.showToast(getString(R.string.bds_pwd_format));
                    } else {

                        int nRet1;

                        BitsharesWalletWraper.getInstance().encrypt_password(newPwd, MyApp.CURRENT_NODE);
                        nRet1 = BitsharesWalletWraper.getInstance().unlock(newPwd);

                            if (nRet1 == 0) {
                                int nRet = BitsharesWalletWraper.getInstance().save_wallet_file();
                                if (nRet == 0) {
                                    ChooseWalletActivity.PASSWORD = newPwd;
                                    showPopupConfirm();
                                } else {
                                    MyApp.showToast(getString(R.string.bds_pwd_change_fail));
                                }
                            } else {
                                MyApp.showToast(getString(R.string.bds_pwd_change_fail));
                            }



                    }

                }

                break;
        }
    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        btn_summit.setOnClickListener(this);

    }

    @Override
    public void initData() {
    }

    private void showPopupConfirm() {

        final AlertDialog customizeDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).create();
        customizeDialog.setInverseBackgroundForced(false);
        customizeDialog.show();
        Window window = customizeDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.confirm_change_password);
        customizeDialog.setCanceledOnTouchOutside(false);

        customizeDialog.setCancelable(false);

        TextView tv_submit = (TextView) window.findViewById(R.id.tv_submit);

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setExit();

            }
        });


    }


    private void setExit() {
        if (Device.pingIpAddress()) {
            if (mIMKit != null) {
                IYWLoginService loginService = mIMKit.getIMCore().getLoginService();

                loginService.logout(new IWxCallback() {

                    @Override
                    public void onSuccess(Object... arg0) {
                        finishActivity();
                    }

                    @Override
                    public void onProgress(int arg0) {


                    }

                    @Override
                    public void onError(int errCode, String description) {
                        MyApp.showToast(getString(R.string.network));
                        finishActivity();
                    }
                });

            } else {
                finishActivity();
            }
        } else {
            MyApp.showToast(getString(R.string.network));
            finishActivity();
        }


    }

    /*
    * finish all activity to ChooseWalletActivity
    * **/
    private void finishActivity() {
        BitsharesWalletWraper.getInstance().close();

        websocket_api.WEBSOCKET_CONNECT_INVALID = -1;
        SPUtils.put(Const.USERNAME,"");
        MyApp.set(BuildConfig.ID, "");
        MyApp.set(BuildConfig.MEMOKEY, "");
        ActivityManagerUrils.getActivityManager().popAllActivityExceptOne(ChangePasswordActivity.class);
        Intent intent = new Intent(ChangePasswordActivity.this, ChooseWalletActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
