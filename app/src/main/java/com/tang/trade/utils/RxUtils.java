package com.tang.trade.utils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/4/11.
 */

public class RxUtils {
    private static Observable ObervableBase(ObservableOnSubscribe obser){
        return  Observable.create( obser).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 具有返回操作
     * @param listener
     * @param <T>
     */
    public static <T>  void RxThread(final RxListener<T> listener ){

        ObervableBase(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) throws Exception {
                e.onNext(listener.Option() );
            }
        }).subscribe(new Observer<T>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(T s) {
                        listener.onUI(s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * 子线程执行
     * @param listener
     */
    public static void RxNoUI(final RxListenerOption listener ){
        ObervableBase(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                listener.Option();
                e.onNext("" );
            }
        });
    }

    public interface RxListener<T>{
        T Option();
        void onUI(T data);
    }
    public interface RxListenerOption {
        void Option();

    }
}
