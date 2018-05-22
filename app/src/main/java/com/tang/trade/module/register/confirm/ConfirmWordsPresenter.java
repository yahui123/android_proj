package com.tang.trade.module.register.confirm;

import com.tang.trade.base.AbsBasePresenter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/4/10.
 */

public class ConfirmWordsPresenter extends AbsBasePresenter<ConfirmWordsContract.View, ConfirmWordsContract.Model> implements ConfirmWordsContract.Presenter {


    public ConfirmWordsPresenter(ConfirmWordsContract.View view, String word) {
        super(view, new ConfirmWordsModel(word));
    }

    @Override
    public ArrayList<String> getShowWord() {

        return mModel.getShowWord();
    }

    @Override
    public ArrayList<Word> getSelectWord() {
        return mModel.getSelectWord();
    }

    @Override
    public String backCommitWord(int wordType) {
        return mModel.getOldWord(wordType);
    }

    @Override
    public void sendResetWord(int position) {
        mModel.resetWord(position);
    }

    @Override
    public void sendResetSelect(int position) {
        mModel.resetSelect(position);
    }

    @Override
    public void sendAddShow(String word) {
        mModel.addShow(word);
    }

    @Override
    public boolean sendCheckSort() {
        return mModel.checkSort();
    }


}
