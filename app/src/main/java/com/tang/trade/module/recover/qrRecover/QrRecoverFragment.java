package com.tang.trade.module.recover.qrRecover;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flh.framework.toast.ToastAlert;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tang.trade.base.AbsMVPFragment;
import com.tang.trade.data.model.entity.PhoneResult;
import com.tang.trade.data.model.entity.RandomEncryptResult;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.recover.scan.ScanQrActivity;
import com.tang.trade.module.recover.setPassword.SetPwdActivity;
import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.utils.AESCipher;
import com.tang.trade.utils.AlphaUtil;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/10.
 * 二维码恢复
 */

public class QrRecoverFragment extends AbsMVPFragment<QrRecoverContract.Presenter> implements QrRecoverContract.View, TextWatcher {
    private final String SHOW_FOR_OLD = "show_old";
    private final String SHOW_FOR_NEW = "show_new";
    private final String SHOW_FOR_NONE = "show_none";

    @BindView(R.id.iv_scan_qr)
    ImageView mIvScanQr;
    @BindView(R.id.tv_phone)
    TextView mTvPhone;
    @BindView(R.id.tv_send_code)
    TextView mTvSendCode;
    @BindView(R.id.et_code)
    EditText mEtCode;
    @BindView(R.id.tv_btn)
    TextView mTvBtn;
    @BindView(R.id.ll_new_qr)
    LinearLayout mLlNewQr;
    @BindView(R.id.ll_old_qr)
    LinearLayout mLlOldQr;
    @BindView(R.id.et_user)
    EditText mEtUser;
    @BindView(R.id.et_key)
    EditText mEtKey;
    @BindView(R.id.tv_scan_tip)
    TextView mTvScanTip;

    private Disposable mDisposable;
    private String show = SHOW_FOR_NONE;
    private String qrAes;       //二维码内容
    //32位加密字符串
    private String random;
    private String phone = "";   //手机号码
    private String identityCode; //身份识别码
    private String md5Key;
    private OnSaveQrContentListener listener;
    private String content = "";

    public static QrRecoverFragment newInstance(/*String param1, String param2*/) {
        QrRecoverFragment fragment = new QrRecoverFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_qr_recover_400;
    }

    @Override
    protected void setupWidget(View view, Bundle savedInstanceState) {

        AlphaUtil.setDisable(mTvBtn);
        BitsharesWalletWraper.getInstance().reset_400();

        // mEtPhone.addTextChangedListener(this);
        mEtCode.addTextChangedListener(this);
        mEtUser.addTextChangedListener(this);
        mEtKey.addTextChangedListener(this);

    }

    @Override
    public QrRecoverContract.Presenter getPresenter() {
        return new QrRecoverPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(getContext(), error.getErrorMessage());
    }

    @OnClick({R.id.iv_scan_qr, R.id.tv_send_code, R.id.tv_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_scan_qr:
                ScanQrActivity.startForResult(this);
                break;
            case R.id.tv_send_code:

                if (TextUtils.isEmpty(phone)) {
                    ToastAlert.showToast(getContext(), getString(R.string.phone_not_null_400));
                    return;
                }

                mTvSendCode.setClickable(false);
                //从0开始发射90个数字 延时0s 每1s发射一次
                //倒计时完毕设置为可以点击
                mDisposable = Flowable.intervalRange(0, 61, 0, 1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(aLong -> {
                            mTvSendCode.setTextColor(ContextCompat.getColor(getContext(), R.color.login_separator));
                            mTvSendCode.setText((60 - aLong) + "s后重新获取");
                        })
                        .doOnComplete(() -> {
                            //倒计时完毕设置为可以点击
                            mTvSendCode.setClickable(true);
                            mTvSendCode.setText(getString(R.string.send_code_400));
                            mTvSendCode.setTextColor(ContextCompat.getColor(getContext(), R.color.mine_security_text));
                        })
                        .subscribe();

                mPresenter.sendCode(phone);

                break;
            case R.id.tv_btn:
                if (show.equals(SHOW_FOR_NEW)) {

                    /*点击按钮：1.获取16位加密串 2.成功，解密二维码获取用户名和速记词 3.查询用户存在否 4.存在，根据速记词生成私钥*/

                    String code = mEtCode.getText().toString().trim();

                    if (TextUtils.isEmpty(code)) {
                        ToastAlert.showToast(getContext(), "请填写验证码");
                        return;
                    }

                    if (!TextUtils.isEmpty(qrAes) && !TextUtils.isEmpty(md5Key) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(identityCode)) {

                        mPresenter.getRandomEncrypt(md5Key, phone, code, identityCode);
                    } else {

                        ToastAlert.showToast(getContext(), getString(R.string.qr_error));
                    }

                } else if (show.equals(SHOW_FOR_OLD)) {

                    String username = mEtUser.getText().toString().trim();
                    String key = mEtKey.getText().toString().trim();

                    mPresenter.checkUserOnly(username, key);
                }

                break;
        }
    }

    @Override
    public void randomEncryptSuccess(RandomEncryptResult result) {
        try {
            //AES解密
            String s = AESCipher.aesDecryptString(qrAes, result.getEncryptKey());
            //获取用户名和速记词
            //{"username":"qwasdad","privateKeyWord":"5Hyn7VdQrUoGYpbEqTNZZ2ruXU5xB2hKdnSH2JFqAyasdasfQQ"}
            JSONObject obj = JSON.parseObject(s);
            String userName = obj.getString("username");
            String words = obj.getString("privateKeyWord");
            String privateKey = obj.getString("privateKey");

            if (TextUtils.isEmpty(userName)) {
                ToastAlert.showToast(getContext(), getString(R.string.qr_error));
                return;
            }

            if (TextUtils.isEmpty(privateKey) && !TextUtils.isEmpty(words)) {
                mPresenter.checkUser(userName, words);
            } else if (!TextUtils.isEmpty(privateKey) && TextUtils.isEmpty(words)) {
                mPresenter.checkUserOnly(userName, privateKey);
            } else {
                ToastAlert.showToast(getContext(), getString(R.string.qr_error));
            }

        } catch (Exception e) {
            ToastAlert.showToast(getContext(), getString(R.string.qr_error));
        }
    }

    @Override
    public void sendCodeSuccess(SMSResult smsResult) {
        identityCode = smsResult.getIdentityCode();
        ToastAlert.showToast(getContext(), "发送成功");
    }

    @Override
    public void checkSuccess(String userName, String privateKey, String words) {
        SetPwdActivity.start(getContext(), userName, privateKey, words, SetPwdActivity.WORD_RECOVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ScanQrActivity.SCAN_REQUEST_CODE && resultCode == ScanQrActivity.SCAN_RESULT_CODE) {
            if (data != null) {
                String content = data.getStringExtra(ScanQrActivity.EXTRA_RESULT);

                //识别二维码内容
                recognize(content);
            } else {

                showDefaultLayout();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Bitmap createCode(String url) {
        int width = 552;
        int height = 552;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix;
        try {

            QRCodeWriter writer = new QRCodeWriter();
            bitMatrix = writer.encode(url,// id
                    BarcodeFormat.QR_CODE, width, height, hints);
            return bitMatrix2Bitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private void recognize(String content) {
        try {
            // "borderless{\"name\":\""+ username+"\",\"key\":\""+suggestPrivateKey+"\"}";
            if (content.startsWith("borderless{")) {

                JSONObject obj = JSON.parseObject(content.substring(10));
                String name = obj.getString("name");
                String key = obj.getString("key");

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(key)) {
                    showDefaultLayout();
                    ToastAlert.showToast(getContext(), getString(R.string.qr_error));
                    return;
                }

                show = SHOW_FOR_OLD;
                mTvScanTip.setVisibility(View.GONE);
                mLlNewQr.setVisibility(View.GONE);
                mLlOldQr.setVisibility(View.VISIBLE);
                mEtUser.setText(name);
                mEtKey.setText(key);
                mIvScanQr.setImageBitmap(createCode(content));

            } else if (content.startsWith("borderless400AccountInfo:")) {

                //得到二维码内容中的 加密后的内容
                qrAes = content.split(":")[1];

                //  2018/4/24 将加密二维码MD5后传给服务器  获取电话号码以及32位加密串
                //  2018/4/26 为了体验 返回的时候只获取电话号码 其余操作在点击确定按钮的时候进行
                md5Key = MD5.md5(content);

                if (listener != null) {
                    listener.saveQrContent(content);
                }

                this.content = content;
                //获取MD5主键对应的手机号
                mPresenter.correspondingPhone(md5Key);


            } else {
                showDefaultLayout();
                ToastAlert.showToast(getContext(), getString(R.string.qr_must_be_borderless));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showDefaultLayout();
            ToastAlert.showToast(getContext(), getString(R.string.qr_error));
        }

    }

    private void showDefaultLayout() {
        mLlNewQr.setVisibility(View.GONE);
        mLlOldQr.setVisibility(View.GONE);
        mTvScanTip.setVisibility(View.VISIBLE);
    }

    @Override
    public void correspondingSuccess(PhoneResult result) {
        phone = result.getPhone();

        if (listener != null) {
            listener.savePhone(phone);
        }

        //获取对应手机号成功后 再展示布局
        show = SHOW_FOR_NEW;
        mIvScanQr.setImageBitmap(createCode(content));
        mTvScanTip.setVisibility(View.GONE);
        mLlOldQr.setVisibility(View.GONE);
        mLlNewQr.setVisibility(View.VISIBLE);
        mTvPhone.setText(com.tang.trade.utils.TextUtils.enPhone(result.getPhone()));
    }

    @Override
    public void correspondingFailed(String errorMsg) {
        showDefaultLayout();
        ToastAlert.showToast(getContext(), errorMsg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //  String phone = mTvPhone.getText().toString().trim();
        String code = mEtCode.getText().toString().trim();
        String username = mEtUser.getText().toString().trim();
        String key = mEtKey.getText().toString().trim();

        if (SHOW_FOR_NEW.equalsIgnoreCase(show)) {
            if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(code)) {
                AlphaUtil.setDisable(mTvBtn);
            } else {
                AlphaUtil.setEnable(mTvBtn);
            }
        } else if (SHOW_FOR_OLD.equalsIgnoreCase(show)) {
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(key)) {
                AlphaUtil.setDisable(mTvBtn);
            } else {
                AlphaUtil.setEnable(mTvBtn);
            }
        } else {
            AlphaUtil.setDisable(mTvBtn);
        }

    }

    public interface OnSaveQrContentListener {
        void saveQrContent(String qrContent);

        void savePhone(String phone);
    }

    public void setOnSaveQrContentListener(OnSaveQrContentListener listener) {
        this.listener = listener;
    }
}
