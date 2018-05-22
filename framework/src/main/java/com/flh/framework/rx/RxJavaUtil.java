package com.flh.framework.rx;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * RxJava 工具类
 * Created by leo on 2016-07-24.
 */
public class RxJavaUtil {
    
    /**
     * 倒计时，单位 s，返回回调在主线程
     * @param seconds 倒计时秒数
     * */
    public static Observable<Integer> countDown(final int seconds) {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(@NonNull Long increaseTime) throws Exception {
                        return seconds - increaseTime.intValue();
                    }
                });
    }

    /**
     * 统一线程处理
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> threadTransform() {    //compose简化线程
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    
}
