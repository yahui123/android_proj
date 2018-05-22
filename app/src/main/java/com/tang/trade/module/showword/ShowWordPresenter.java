package com.tang.trade.module.showword;

import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.base.IBaseModel;

/**
 * Created by Administrator on 2018/4/12.
 */

public class ShowWordPresenter extends AbsBasePresenter<ShowWordContract.View, IBaseModel> implements ShowWordContract.Presenter {

    public ShowWordPresenter(ShowWordContract.View views) {
        super(views);

    }
}
