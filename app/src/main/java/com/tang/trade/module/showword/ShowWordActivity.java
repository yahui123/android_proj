package com.tang.trade.module.showword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.AESCipher;
import com.tang.trade.utils.Generate16;
import com.tang.trade.utils.SPUtils;
import com.tang.trade.utils.TextUtils;
import com.tang.trade.widget.LimitEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 查看速记词
 */
public class ShowWordActivity extends AbsMVPActivity<ShowWordContract.Presenter> implements ShowWordContract.View, LimitEditText.EnableListener, LimitEditText.InterceptListener {


    @BindView(R.id.tv_look)
    TextView tvLook;
    @BindView(R.id.et_password)
    LimitEditText etCreatePassword;
    @BindView(R.id.btn_commit)
    TextView btnCommit;
    @BindView(R.id.ln_look_show)
    LinearLayout lnLookShow;
    @BindView(R.id.vs_look)
    ViewStub vsLook;
    @BindView(R.id.tv_show_word)
    TextView tvShowWord;
    @BindView(R.id.ln_password_visible)
    LinearLayout lnPasswordVisible;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, ShowWordActivity.class));
    }

    @Override
    public ShowWordContract.Presenter getPresenter() {
        return new ShowWordPresenter(this);
    }

    @Override
    public void onError(DataError error) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_word_400);

        setupView();
    }

    protected void setupView() {
        setupDefultToolbar(R.string.look_title_400);
        etCreatePassword.setEnableListener(this);
        etCreatePassword.setInterceptListener(this);
        String userName = SPUtils.getString(Const.USERNAME, "");
        String encryptWords = SPUtils.getString(userName, "");
        if (android.text.TextUtils.isEmpty(encryptWords)) {
            lnLookShow.setVisibility(View.GONE);
            vsLook.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_commit)
    public void onViewClicked() {
        dismissKeyBoard();
        String password = etCreatePassword.getText().toString();
        try {
            String showKey = AESCipher.aesDecryptString(SPUtils.getString(SPUtils.getString(Const.USERNAME, ""), ""), Generate16.Generate16Number(password));
            tvLook.setText(showKey);
            tvLook.setVisibility(View.VISIBLE);
            tvShowWord.setVisibility(View.VISIBLE);
            btnCommit.setVisibility(View.GONE);
            lnPasswordVisible.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
            ToastAlert.showToast(this, getString(R.string.alert_pwd_not_allow_400));
//            lnLookShow.setVisibility(View.GONE);
//           vsLook.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void noInput() {
        if (btnCommit.isEnabled()) {
            btnCommit.setEnabled(false);
        }

        btnCommit.setBackgroundResource(R.drawable.btn_gray);
    }

    @Override
    public void yesInput(String msg) {
        if (!btnCommit.isEnabled()) {
            btnCommit.setEnabled(true);
        }
        btnCommit.setBackgroundResource(R.drawable.selector_btn_bg);

    }


    @Override
    public boolean check(CharSequence str) {
        return com.tang.trade.utils.TextUtils.isBlank(str);
    }

    @Override
    public void getIntentValue() {

    }

    private void dismissKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etCreatePassword.getWindowToken(), 0);


    }
}
