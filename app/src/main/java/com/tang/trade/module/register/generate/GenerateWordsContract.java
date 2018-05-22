package com.tang.trade.module.register.generate;

import com.tang.trade.base.IBaseModel;
import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

/**
 * Created by Administrator on 2018/4/9.
 */

public interface GenerateWordsContract {
    interface Model extends IBaseModel {
        int CHINESE = 0;               //中文

        int ENGLISH_UPPER_CASE = 1;   //英文大写

        int ENGLISH_LOWER_CASE = 2;   //英文小写

        String generateWord(int wordType);
    }

    interface View extends IBaseView<GenerateWordsContract.Presenter> {
        void showWord(int wordType, String word);

        void showChangeWord(int wordType, String word);
    }

    interface Presenter extends IBasePresenter {
        void getWords(int wordType);

        void changeWord(int wordType, String word);
    }
}
