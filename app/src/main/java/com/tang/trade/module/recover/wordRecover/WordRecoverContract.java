package com.tang.trade.module.recover.wordRecover;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;

public interface WordRecoverContract {

    interface View extends IBaseView<WordRecoverContract.Presenter> {

        void generateKeySuccess(String s);
    }

    interface Presenter extends IBasePresenter {

        void generateKey(String words);

    }
}