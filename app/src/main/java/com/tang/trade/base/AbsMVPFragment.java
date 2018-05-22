package com.tang.trade.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tang.trade.tang.widget.MyProgressDialog;


/**
 * Created by leo on 02/03/2018.
 */

public abstract class AbsMVPFragment<T> extends AbsBaseFragment implements IBaseView<T> {

    protected T mPresenter;
    private MyProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = getPresenter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    public void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
        mProgressDialog = MyProgressDialog.getInstance(getActivity());

        mProgressDialog.show();
    }

    public void showProgressDialog(CharSequence message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
        mProgressDialog = MyProgressDialog.getInstance(getActivity());
        mProgressDialog.setTitle(message);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
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
