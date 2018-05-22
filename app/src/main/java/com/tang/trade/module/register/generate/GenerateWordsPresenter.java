package com.tang.trade.module.register.generate;

import android.text.TextUtils;

import com.tang.trade.base.AbsBasePresenter;


/**
 * Created by Administrator on 2018/4/9.
 */

public class GenerateWordsPresenter extends AbsBasePresenter<GenerateWordsContract.View, GenerateWordsContract.Model> implements GenerateWordsContract.Presenter {

    public GenerateWordsPresenter(GenerateWordsContract.View view) {
        super(view, new GenerateWordsModel());
    }

    @Override
    public void getWords(int wordType) {
        mView.showWord(wordType, mModel.generateWord(wordType));
    }

    @Override
    public void changeWord(int wordType, String words) {
        if (TextUtils.isEmpty(words)) {
            mView.showChangeWord(wordType, mModel.generateWord(wordType));
        } else {
            mView.showChangeWord(wordType, words);
        }
    }
}
