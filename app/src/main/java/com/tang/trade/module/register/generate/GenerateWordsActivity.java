package com.tang.trade.module.register.generate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.register.confirm.ConfirmWordsActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.DatabaseUtil;
import com.tang.trade.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifImageView;

/**
 * 生成速记词
 * Created by Administrator on 2018/4/9.
 */

public class GenerateWordsActivity extends AbsMVPActivity<GenerateWordsContract.Presenter> implements GenerateWordsContract.View {
    public final static int REQUEST_CODE = 100;

    static {
        System.loadLibrary("native-lib");
    }

    @BindView(R.id.et_words)
    TextView etWords;

    @BindView(R.id.btn_next)
    TextView btnNext;

    @BindView(R.id.tv_attention)
    TextView mTvAttention;

    @BindView(R.id.ll_mask)
    LinearLayout mLlMask;
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.ll_save)
    LinearLayout mLlSave;
    @BindView(R.id.iv_refresh)
    ImageView mIvRefresh;
    @BindView(R.id.iv_switch)
    ImageView mIvSwitch;
    @BindView(R.id.ll_words)
    LinearLayout mLlWords;
    @BindView(R.id.iv_login_bottom)
    GifImageView mIvLoginBottom;

    private int mWordType = 0;
    private String mWords;
    private String mCnWords = "";
    private String mElWords = "";

    public static void start(Context context) {
        Intent starter = new Intent(context, GenerateWordsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_generate_words_400);
        ButterKnife.bind(this);

        setupView();

        mWordType = GenerateWordsContract.Model.CHINESE;
        mPresenter.getWords(mWordType);
    }

    @Override
    public GenerateWordsContract.Presenter getPresenter() {
        return new GenerateWordsPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    private void setupView() {
        //第一次显示引导蒙版
        if (SPUtils.getBoolean(Const.FIRST_MASK, false)) {
            mLlMask.setVisibility(View.GONE);
        } else {
            SPUtils.put(Const.FIRST_MASK, true);
        }

        mTvAttention.setText(DatabaseUtil.getMsg("MSG_0102", getString(R.string.save_words_warning_400)));
        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
    }

    @OnClick(R.id.iv_refresh)
    protected void onRefreshClicked() {
        mPresenter.getWords(mWordType);
    }

    @OnClick(R.id.iv_switch)
    protected void onSwitchClicked() {
        switch (mWordType) {
            case GenerateWordsContract.Model.CHINESE:
                mWordType = GenerateWordsContract.Model.ENGLISH_LOWER_CASE;
                mPresenter.changeWord(mWordType, mElWords);
                break;
            case GenerateWordsContract.Model.ENGLISH_LOWER_CASE:
                mWordType = GenerateWordsContract.Model.CHINESE;
                mPresenter.changeWord(mWordType, mCnWords);
                break;
        }

        //    mPresenter.getWords(mWordType);
    }

    @OnClick(R.id.btn_next)
    protected void onNextClicked() {
        ConfirmWordsActivity.start(this, mWords, mWordType);
    }

    @OnClick(R.id.img_back)
    protected void onBackClicked() {
        finish();
    }

    @Override
    public void showWord(int type, String words) {
        switch (mWordType) {
            case GenerateWordsContract.Model.CHINESE:
                mCnWords = words;
                break;
            case GenerateWordsContract.Model.ENGLISH_LOWER_CASE:
                mElWords = words;
                break;
            default:
                break;
        }

        mWords = words;
        etWords.setText(mWords);
    }

    @Override
    public void showChangeWord(int wordType, String word) {
        switch (mWordType) {
            case GenerateWordsContract.Model.CHINESE:
                mCnWords = word;
                break;
            case GenerateWordsContract.Model.ENGLISH_LOWER_CASE:
                mElWords = word;
                break;
            default:
                break;
        }
        mWords = word;
        etWords.setText(mWords);
    }

    @Override
    public void getIntentValue() {

    }

    @OnClick(R.id.ll_mask)
    public void onViewClicked() {
        mLlMask.setVisibility(View.GONE);
    }
}

