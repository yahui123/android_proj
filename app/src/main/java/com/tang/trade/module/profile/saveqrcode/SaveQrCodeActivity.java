package com.tang.trade.module.profile.saveqrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.widget.EditTextDialog;
import com.tang.trade.utils.CountDownTimerUtils;
import com.tang.trade.utils.SPUtils;
import com.tang.trade.utils.TextUtils;
import com.tang.trade.widget.LimitEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 生成并保存二维码
 */
public class SaveQrCodeActivity extends AbsMVPActivity<SaveQrCodeContract.Presenter> implements SaveQrCodeContract.View {
    public static final String PHONE = "phone";
    public static final int BACK = 100;

    @BindView(R.id.img_qrcode)
    ImageView imgQrcode;
    @BindView(R.id.tv_defaultPhone)
    TextView tvDefaultPhone;
    @BindView(R.id.tv_modifyPhone)
    TextView tvModifyPhone;
    @BindView(R.id.ln_defaultShow)
    LinearLayout lnDefaultShow;
    @BindView(R.id.tv_commit)
    TextView tvCommit;
    @BindView(R.id.vs_modify)
    ViewStub vsModify;

//    @BindView(R.id.iv_dismiss)
//    ImageView mIvDismiss;
//    @BindView(R.id.code_view)
//    CodeView mCodeView;
//    @BindView(R.id.keyboard_view)
//    KeyboardView mKeyboardView;
//    @BindView(R.id.ll_input_pwd)
//    LinearLayout mLlInputPwd;

    private CountDownTimerUtils countDownTimerUtils;
    private String identityCode;
    private String mPhone;
    private LimitEditText etUnlockPhone;
    private LimitEditText etVerificationCode;
    private String phone;
    private EditTextDialog mEditTextDialog;

    public static void start(Activity activity, String phone) {
        Intent intent = new Intent(activity, SaveQrCodeActivity.class);
        intent.putExtra(PHONE, phone);
        activity.startActivityForResult(intent, BACK);
    }

    @Override
    public SaveQrCodeContract.Presenter getPresenter() {
        return new SaveQrCodePresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save);

        setupView();
    }

    private void setupView() {

        requestPermissions();

        setupDefultToolbar(R.string.title_save_qrcode_400);

        tvDefaultPhone.setText(TextUtils.enPhone(mPhone));
        String userName = SPUtils.getString(Const.USERNAME, "");
        String qrContent = SPUtils.getString(userName + Const.QR_CONTENT, "");
        imgQrcode.setImageBitmap(mPresenter.createCode(qrContent));
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x0010);
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
    public void verifySuccess() {
        if (mEditTextDialog.isVisible()) {
            mEditTextDialog.dismiss();
        }

        //修改手机号
        tvCommit.setText("确认修改");
        lnDefaultShow.setVisibility(View.GONE);
        View view1 = vsModify.inflate();
        etUnlockPhone = view1.findViewById(R.id.et_unlockPhone);
        etVerificationCode = view1.findViewById(R.id.et_verificationCode);
        final TextView tvSendVerificationCode = view1.findViewById(R.id.tv_sendVerificationCode);
        //点击发送验证码
        tvSendVerificationCode.setOnClickListener(v -> {
            phone = etUnlockPhone.getText().toString();

            if (!TextUtils.isPhone(phone)) {
                ToastAlert.showToast(SaveQrCodeActivity.this, getString(R.string.alert_phone_formate_not_allow_400));
                return;
            }
            if (countDownTimerUtils == null) {
                countDownTimerUtils = new CountDownTimerUtils(tvSendVerificationCode);
            }

            mPresenter.sendSMS(phone);
        });

    }

    @OnClick({R.id.tv_modifyPhone, R.id.tv_commit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_modifyPhone:

                showPasswordDialog();
                break;
            case R.id.tv_commit:

                if (lnDefaultShow.getVisibility() == View.VISIBLE) {
                    //保存二维码
                    mPresenter.saveQr(this);
                } else {
                    //修改手机号
                    if (android.text.TextUtils.isEmpty(etVerificationCode.getText().toString())) {
                        ToastAlert.showToast(SaveQrCodeActivity.this, getString(R.string.alert_verify_code_not_null_400));
                        return;
                    }
                    String phone = etUnlockPhone.getText().toString().trim();
                    mPresenter.modifyPhone(phone, etVerificationCode.getText().toString(), identityCode);
                }

                break;
        }
    }

    /**
     * 密码输入对话框
     */
    private void showPasswordDialog() {
        mEditTextDialog = new EditTextDialog(this);

        mEditTextDialog.setOnDialogOnClick(pwd -> mPresenter.verifyPwd(pwd));
    }

    /**
     * 最终成功
     */
    @Override
    public void backSuccessFish() {

        if (lnDefaultShow.getVisibility() == View.VISIBLE) {
            ToastAlert.showToast(SaveQrCodeActivity.this, getString(R.string.alert_save_success_400));
        } else {
            ToastAlert.showToast(SaveQrCodeActivity.this, getString(R.string.alert_modify_password_commit_success_400));
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void backSMS(String identityCode) {
        this.identityCode = identityCode;
        countDownTimerUtils.start();
    }

    @Override
    public void getIntentValue() {
        mPhone = getIntent().getStringExtra(PHONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEditTextDialog != null && mEditTextDialog.isVisible()) {
            mEditTextDialog.dismiss();
        }
    }
}
