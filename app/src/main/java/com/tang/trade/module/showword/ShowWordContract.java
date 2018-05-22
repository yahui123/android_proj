package com.tang.trade.module.showword;

import com.tang.trade.base.IBasePresenter;
import com.tang.trade.base.IBaseView;
import com.tang.trade.base.IBaseModel;

/**
 * Created by Administrator on 2018/4/12.
 */

public interface ShowWordContract {
    interface Model extends IBaseModel {

    }

    interface View extends IBaseView<ShowWordContract.Presenter> {


    }

    interface Presenter extends IBasePresenter {

    }
}
