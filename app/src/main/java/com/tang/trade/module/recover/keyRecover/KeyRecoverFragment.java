package com.tang.trade.module.recover.keyRecover;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.base.AbsMVPFragment;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.recover.setPassword.SetPwdActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.BdsTextWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018/4/10.
 */


public class KeyRecoverFragment extends AbsMVPFragment<KeyRecoverContract.Presenter> implements KeyRecoverContract.View {


    @BindView(R.id.et_username)
    EditText mEtUsername;
    @BindView(R.id.et_key)
    EditText mEtKeyOrPwd;
    @BindView(R.id.tv_btn)
    TextView mTvBtn;
    @BindView(R.id.eye_password)
    ImageView mEyePassword;
    Unbinder unbinder;
    private boolean isShow = true;

    public static KeyRecoverFragment newInstance(/*String param1, String param2*/) {
        KeyRecoverFragment fragment = new KeyRecoverFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_key_recover_400;
    }

    @Override
    protected void setupWidget(View view, Bundle savedInstanceState) {
        AlphaUtil.setDisable(mTvBtn);

        BdsTextWatcher watcher = new BdsTextWatcher() {

            @Override
            protected void afterChanged(Editable s) {
                String name = mEtUsername.getText().toString().trim();
                String key = mEtKeyOrPwd.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(key)) {
                    AlphaUtil.setDisable(mTvBtn);
                } else {
                    AlphaUtil.setEnable(mTvBtn);
                }

            }
        };
        mEtUsername.addTextChangedListener(watcher);
        mEtKeyOrPwd.addTextChangedListener(watcher);
    }

    @Override
    public KeyRecoverContract.Presenter getPresenter() {
        return new KeyRecoverPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(getContext(), error.getErrorMessage());
    }

    @OnClick(R.id.tv_btn)
    public void onViewClicked() {
        String keyOrPwd = mEtKeyOrPwd.getText().toString().trim();
        String userName = mEtUsername.getText().toString().trim();

        /*检查账户存不存在*/
        mPresenter.checkUserExist(userName);

    }

    @Override
    public void userExist() {
        String keyOrPwd = mEtKeyOrPwd.getText().toString().trim();
        String userName = mEtUsername.getText().toString().trim();
        if (keyOrPwd.startsWith("5") && keyOrPwd.length() == 51) { //输入的是私钥
            SetPwdActivity.start(getContext(), userName, keyOrPwd, SetPwdActivity.WORD_RECOVER);
        } else { // 输入的是密码
            String s = normailzeBrainKey(keyOrPwd);
//            mPresenter.generateKeyAccordingPwd(s);
            SetPwdActivity.start(getContext(), userName, s, SetPwdActivity.PWD_RECOVER);
        }
    }

//    @Override
//    public void keySuccess(String privateKey) {
//        String userName = mEtUsername.getText().toString().trim();
//        SetPwdActivity.start(getContext(), userName, privateKey, SetPwdActivity.WORD_RECOVER);
//    }

    public String normailzeBrainKey(String strBrainKey) {
        int i = 0;
        int j = 0;

        strBrainKey.replace('\t', ' ');
        strBrainKey.replace('\r', ' ');
        strBrainKey.replace('\n', ' ');
        strBrainKey.replace('\f', ' ');
        //strBrainKey.replace('\v',' ');
        int n = strBrainKey.length();
        byte[] sourceData = strBrainKey.getBytes();
        byte[] destData = new byte[n];
        int c;

        while (i < n) {
            c = sourceData[i++];
            switch (c) {
                case ' ':
                    destData[j++] = ' ';
                    break;
                case 'a':
                    c = 'A';
                    break;
                case 'b':
                    c = 'B';
                    break;
                case 'c':
                    c = 'C';
                    break;
                case 'd':
                    c = 'D';
                    break;
                case 'e':
                    c = 'E';
                    break;
                case 'f':
                    c = 'F';
                    break;
                case 'g':
                    c = 'G';
                    break;
                case 'h':
                    c = 'H';
                    break;
                case 'i':
                    c = 'I';
                    break;
                case 'j':
                    c = 'J';
                    break;
                case 'k':
                    c = 'K';
                    break;
                case 'l':
                    c = 'L';
                    break;
                case 'm':
                    c = 'M';
                    break;
                case 'n':
                    c = 'N';
                    break;
                case 'o':
                    c = 'O';
                    break;
                case 'p':
                    c = 'P';
                    break;
                case 'q':
                    c = 'Q';
                    break;
                case 'r':
                    c = 'R';
                    break;
                case 's':
                    c = 'S';
                    break;
                case 't':
                    c = 'T';
                    break;
                case 'u':
                    c = 'U';
                    break;
                case 'v':
                    c = 'V';
                    break;
                case 'w':
                    c = 'W';
                    break;
                case 'x':
                    c = 'X';
                    break;
                case 'y':
                    c = 'Y';
                    break;
                case 'z':
                    c = 'Z';
                    break;
                default:
                    break;
            }
            destData[j++] = (byte) c;
        }

        String result = new String(destData);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.eye_password)
    public void onViewEyeClicked() {
        if (isShow) {
            mEtKeyOrPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mEyePassword.setImageResource(R.mipmap.common_eye_true);
            isShow = false;
        } else {
            mEtKeyOrPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mEyePassword.setImageResource(R.mipmap.common_eye_false);
            isShow = true;
        }
        mEtKeyOrPwd.setSelection(mEtKeyOrPwd.getText().toString().trim().length());
    }
}
