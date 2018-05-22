package com.tang.trade.widget;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.tang.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wwg123 on 2018/5/9.
 */

public class ShowInfoDialog extends Dialog {


    @BindView(R.id.dl_tv_register)
    TextView mDlTvRegister;
    @BindView(R.id.dl_tv_restore)
    TextView mDlTvRestore;
    @BindView(R.id.dl_tv_cancel)
    TextView mDlTvCancel;
    private View.OnClickListener o;
    private Unbinder mUnbinder;

    public ShowInfoDialog(Context context, int themeResId, View.OnClickListener o) {
        super(context, themeResId);
        this.o = o;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_show_info);
        mUnbinder = ButterKnife.bind(this);
        //  initView();
        initEvent();
    }

    private void initView() {
        mDlTvRegister = findViewById(R.id.dl_tv_register);
        mDlTvRestore = findViewById(R.id.dl_tv_restore);
        mDlTvCancel = findViewById(R.id.dl_tv_cancel);
    }

    private void initEvent() {
        mDlTvRegister.setOnClickListener(o);
        mDlTvRestore.setOnClickListener(o);
        mDlTvCancel.setOnClickListener(o);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mUnbinder.unbind();
    }

}
