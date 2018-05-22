package com.tang.trade.base;

/**
 * Created by leo on 02/03/2018.
 */

public class AbsBasePresenter<V extends IBaseView, M extends IBaseModel> {
    protected V mView;
    protected M mModel;

    public AbsBasePresenter(V view) {
        this.mView = view;
    }

    public AbsBasePresenter(V view, M model) {
        this(view);
        this.mModel = model;
    }
}
