package com.tang.trade.module.register.confirm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.register.create.CreateUserActivity;
import com.tang.trade.tang.BuildConfig;
import com.tang.trade.tang.R;
import com.tang.trade.utils.DatabaseUtil;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 确定速记词
 * Created by Administrator on 2018/4/9.
 */

public class ConfirmWordsActivity extends AbsMVPActivity<ConfirmWordsContract.Presenter> implements ConfirmWordsContract.View {
    public static final int CONFIRM_RESULT = 100;
    public static final String WORD = "word";
    public static final String WORD_TYPE = "word_type";

    @BindView(R.id.btn_next)
    TextView btnNext;
    @BindView(R.id.tf_word)
    TagFlowLayout tfWord;
    @BindView(R.id.gv_word)
    GridView gvWord;
    @BindView(R.id.tv_dsc)
    TextView tvDsc;

    private ShowWordAdapter showWordAdapter;
    private ConfirmGVAdapter selectWordAdapter;
    private String mWords;
    private int mWordType;
    private List<String> mSelectedWords = new ArrayList<>();
    private List<Word> mWordsForList = new ArrayList<>();

    public static void start(Activity activity, String word, int wordType) {
        Intent intent = new Intent(activity, ConfirmWordsActivity.class);
        intent.putExtra(WORD, word);
        intent.putExtra(WORD_TYPE, wordType);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_confrim_words_400);
        ButterKnife.bind(this);

        setupData();

        setupView();
    }

    @Override
    public ConfirmWordsContract.Presenter getPresenter() {
        return new ConfirmWordsPresenter(this, mWords);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(this, error.getErrorMessage());
    }

    private void setupData() {

        for (String word : Arrays.asList(mWords.split(" "))) {
            mWordsForList.add(new Word(word, false));
        }

        Collections.shuffle(mWordsForList);
    }

    private void setupView() {
        btnNext.setEnabled(false);
        btnNext.setAlpha(0.4f);
        tvDsc.setVisibility(View.VISIBLE);

        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));

        //  初始化显示
        showWordAdapter = new ShowWordAdapter(mSelectedWords, this);
        tfWord.setAdapter(showWordAdapter);
        tfWord.setOnTagClickListener((view, position, parent) -> {
            String removedWord = mSelectedWords.remove(position);

            mWordsForList.get(mWordsForList.indexOf(new Word(removedWord, false))).setCheck(false);

            selectWordAdapter.notifyDataSetChanged();
            showWordAdapter.notifyDataChanged();

            if (!mSelectedWords.isEmpty()) {
                btnNext.setEnabled(true);
                btnNext.setAlpha(1.0f);
                tvDsc.setVisibility(View.GONE);
            } else {
                btnNext.setEnabled(false);
                btnNext.setAlpha(0.4f);
                tvDsc.setVisibility(View.VISIBLE);
            }

            return false;
        });

        //初始化单词
        selectWordAdapter = new ConfirmGVAdapter(this);
        selectWordAdapter.setItems(mWordsForList);
        gvWord.setAdapter(selectWordAdapter);
        gvWord.setOnItemClickListener((parent, view, position, id) -> {
            Word word = mWordsForList.get(position);
            word.setCheck(true);

            if (!mSelectedWords.contains(word.getWord())) {
                mSelectedWords.add(word.getWord());
            }

            selectWordAdapter.notifyDataSetChanged();
            showWordAdapter.notifyDataChanged();
            if (!mSelectedWords.isEmpty()) {
                btnNext.setEnabled(true);
                btnNext.setAlpha(1.0f);
                tvDsc.setVisibility(View.GONE);
            } else {
                btnNext.setEnabled(false);
                btnNext.setAlpha(0.4f);
                tvDsc.setVisibility(View.VISIBLE);
            }
        });


    }

    @OnClick({R.id.img_back, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_next:
                if (mWordsForList.size() != mSelectedWords.size()) {
                    ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0201", getString(R.string.short_words_incorrect_400)));
                    return;
                }
                if (!BuildConfig.DEBUG) {

                    if (!checkWordsSort()) {
                        ToastAlert.showToast(this, DatabaseUtil.getMsg("MSG_0201", getString(R.string.short_words_incorrect_400)));
                        return;
                    }
                }

                CreateUserActivity.start(this, mPresenter.backCommitWord(mWordType));
                break;
            default:
                break;
        }
    }

    private boolean checkWordsSort() {

        List<String> words = Arrays.asList(mWords.split(" "));
        int wordsSize = words.size();

        for (int i = 0; i < wordsSize; i++) {

            if (mSelectedWords.get(i).equals(words.get(i))) {
                continue;
            }

            return false;
        }
        return true;
    }

    @Override
    public void getIntentValue() {
        mWords = getIntent().getStringExtra(WORD);
        mWordType = getIntent().getIntExtra(WORD_TYPE, -1);
    }
}
