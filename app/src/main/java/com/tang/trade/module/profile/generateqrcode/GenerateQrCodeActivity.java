package com.tang.trade.module.profile.generateqrcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.profile.saveqrcode.SaveQrCodeActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.CountDownTimerUtils;
import com.tang.trade.widget.LimitEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 验证手机号
 */
public class GenerateQrCodeActivity extends AbsMVPActivity<GenerateQrCodeContract.Presenter> implements GenerateQrCodeContract.View, LimitEditText.EnableListener {

    @BindView(R.id.et_phone)
    LimitEditText etPhone;

    @BindView(R.id.et_vericationCode)
    LimitEditText etVericationCode;

    @BindView(R.id.tv_sendVerificationCode)
    TextView tvSendVerificationCode;

    @BindView(R.id.tv_commit)
    TextView tvCommit;

    private CountDownTimerUtils countDownTimerUtils;
    private String identityCode;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, GenerateQrCodeActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generate_qr_code);

        setupView();
    }

    @Override
    public void getIntentValue() {

    }

    @Override
    public GenerateQrCodeContract.Presenter getPresenter() {
        return new GenerateQrCodePresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    private void setupView() {
        setupDefultToolbar(R.string.title_generate_qrcode_400);

        countDownTimerUtils = new CountDownTimerUtils(tvSendVerificationCode, this);
        etPhone.setEnableListener(this);
        etVericationCode.setEnableListener(this);

    }

    @OnClick(R.id.tv_sendVerificationCode)
    public void onSendVerificationCodeClicked() {
        String phone = etPhone.getText().toString().trim();
        if (phone.length() != 11) {
            // ToastAlert.showToast(this, getString(R.string.alert_phone_formate_not_allow_400));
            return;
        }
        mPresenter.sendSMS(phone);
    }

    @OnClick(R.id.tv_commit)
    public void onCommitClicked() {
        String verify = etVericationCode.getText().toString();
        String phone = etPhone.getText().toString().trim();

        if (android.text.TextUtils.isEmpty(verify)) {
            ToastAlert.showToast(this, getString(R.string.alert_verify_code_not_input_400));
            return;
        }
        mPresenter.saveQrCodeMessage(phone, verify, identityCode);
    }

    @Override
    public void noInput() {
        if (etPhone.isInput()) {
            tvSendVerificationCode.setEnabled(true);


        }
        if (tvCommit.isEnabled()) {
            tvCommit.setEnabled(false);
        }
        tvSendVerificationCode.setTextColor(ContextCompat.getColor(GenerateQrCodeActivity.this, R.color.common_text_gray));
        tvCommit.setBackgroundResource(R.drawable.btn_gray);
    }

    @Override
    public void yesInput(String msg) {
        if (etPhone.isInput()) {
            tvSendVerificationCode.setEnabled(true);
            countDownTimerUtils.setPhone(msg);
            if (msg.length() == 11) {

                tvSendVerificationCode.setTextColor(ContextCompat.getColor(GenerateQrCodeActivity.this, R.color.common_text_blue));
            } else {

                tvSendVerificationCode.setTextColor(ContextCompat.getColor(GenerateQrCodeActivity.this, R.color.common_text_gray));
            }
        }

        if (etVericationCode.isInput() && etPhone.isInput()) {
            if (!tvCommit.isEnabled()) {
                tvCommit.setEnabled(true);
                tvCommit.setBackgroundResource(R.drawable.selector_btn_bg);
            }
            if (!tvSendVerificationCode.isEnabled()) {
                tvSendVerificationCode.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void backSMS(String identityCode) {
        this.identityCode = identityCode;
        countDownTimerUtils.start();
    }

    @Override
    public void backVerify() {
        String phone = etPhone.getText().toString().trim();
        SaveQrCodeActivity.start(this, phone);
        finish();
    }

}
