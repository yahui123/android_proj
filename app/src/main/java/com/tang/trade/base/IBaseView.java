package com.tang.trade.base;


import com.tang.trade.data.remote.http.DataError;

/**
 * Created by leo on 27/12/2017.
 */


/**
 *  MVP 中 View 的接口
 * @param <T> presenter 的范型
 */
public interface IBaseView<T> {
    T getPresenter();
    void onError(DataError error);
    void showLoading(String message);
    void showLoading();
    void dissLoading();
}
