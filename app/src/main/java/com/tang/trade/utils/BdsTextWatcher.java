package com.tang.trade.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by daibin on 2018/4/20.
 */

public abstract class BdsTextWatcher implements TextWatcher{

    public BdsTextWatcher() {
    }

    public BdsTextWatcher(EditText view) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        afterChanged(s);
    }

    protected abstract void afterChanged(Editable s);
}
