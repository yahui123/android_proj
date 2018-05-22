package com.tang.trade.base;

import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by Admin on 2016/1/14.
 *
 */
public class BaseViewHolder {
    public BaseViewHolder(View view) {
        ButterKnife.bind(this, view);
    }
}
