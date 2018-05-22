package com.tang.trade.tang.net.callback;

import com.tang.trade.tang.utils.TLog;

/**
 * Created by dagou on 2017/9/22.
 */

interface BaseViewCallback<T> {
    public void setData(T t);
}
