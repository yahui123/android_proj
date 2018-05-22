package com.tang.trade.module.register.confirm;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/4/10.
 */

public interface ConfirmWordsContract {
    interface Model extends IBaseModel {

        void initData(String word);

        ArrayList<String> getShowWord();

        ArrayList<Word> getSelectWord();

        void resetWord(int position);

        void resetSelect(int position);

        void addShow(String word);

        boolean checkSort();

        String getOldWord(int wordType);
    }

    interface View extends IBaseView<ConfirmWordsContract.Presenter> {

    }

    interface Presenter extends IBasePresenter {


        ArrayList<String> getShowWord();

        ArrayList<Word> getSelectWord();

        String backCommitWord(int wordType);

        void sendResetWord(int position);

        void sendResetSelect(int position);

        void sendAddShow(String word);

        boolean sendCheckSort();

    }
}
