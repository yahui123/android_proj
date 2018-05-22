package com.tang.trade.module.recover.wordRecover;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.base.AbsMVPFragment;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.recover.setPassword.SetPwdActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.AllCapsTransformationMethod;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.BdsTextWatcher;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/4/10.
 * 速记词恢复
 */

public class WordRecoverFragment extends AbsMVPFragment<WordRecoverContract.Presenter> implements WordRecoverContract.View {

    @BindView(R.id.et_words)
    EditText mEtWords;
    @BindView(R.id.et_username)
    EditText mEtUsername;
    @BindView(R.id.tv_btn)
    TextView mTvBtn;

    public static WordRecoverFragment newInstance(/*String param1, String param2*/) {
        WordRecoverFragment fragment = new WordRecoverFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_word_recover_400;
    }

    @Override
    protected void setupWidget(View view, Bundle savedInstanceState) {
        AlphaUtil.setDisable(mTvBtn);

        BdsTextWatcher watcher = new BdsTextWatcher() {

            @Override
            protected void afterChanged(Editable s) {
                String word = mEtWords.getText().toString().trim();
                String name = mEtUsername.getText().toString().trim();

                if (TextUtils.isEmpty(word) || TextUtils.isEmpty(name)) {
                    AlphaUtil.setDisable(mTvBtn);

                } else {
                    AlphaUtil.setEnable(mTvBtn);

                }
            }
        };
        mEtWords.addTextChangedListener(watcher);
        mEtUsername.addTextChangedListener(watcher);
    }

    @Override
    public WordRecoverContract.Presenter getPresenter() {
        return new WordRecoverPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(getContext(), error.getErrorMessage());
    }

    @OnClick(R.id.tv_btn)
    public void onViewClicked() {
        String words = mEtWords.getText().toString().trim();
        String userName = mEtUsername.getText().toString().trim();
        if (TextUtils.isEmpty(words) || TextUtils.isEmpty(userName)) {
            ToastAlert.showToast(getContext(), getString(R.string.name_or_word_not_be_null));
            return;
        }
        /*0.获取私钥*/
        mPresenter.generateKey(words);
    }

    @Override
    public void generateKeySuccess(String privateKey) {
        /*获取私钥成功后*/
        String userName = mEtUsername.getText().toString().trim();
        String words = mEtWords.getText().toString().trim();
        SetPwdActivity.start(getContext(), userName, privateKey, words, SetPwdActivity.SHORTHAND_RECOVER);
    }

}
