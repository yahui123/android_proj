package com.tang.trade.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tang.trade.tang.widget.MyProgressDialog;


/**
 * Created by leo on 01/03/2018.
 */

public abstract class AbsMVPActivity<T extends IBasePresenter> extends AbsBaseActivity implements IBaseView<T> {
    protected T mPresenter;
    private MyProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentValue();

        mPresenter = getPresenter();
    }

    public abstract void getIntentValue();

    public void showProgressDialog() {
        if (!isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
        mProgressDialog = MyProgressDialog.getInstance(this);

        mProgressDialog.show();
    }

    public void showProgressDialog(CharSequence message) {
        if (!isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
        mProgressDialog = MyProgressDialog.getInstance(this);
        mProgressDialog.setTitle(message);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog == null) {
            return;
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void showLoading(String message) {
        showProgressDialog(message);
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void dissLoading() {
        dismissProgressDialog();
    }
}
