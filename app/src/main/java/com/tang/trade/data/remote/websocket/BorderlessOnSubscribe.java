package com.tang.trade.data.remote.websocket;


import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * Created by Administrator on 2018/4/8.
 */

public abstract class BorderlessOnSubscribe<T> implements ObservableOnSubscribe<Object> {
    @Override
    public void subscribe(ObservableEmitter<Object> e) throws Exception {
        T t = onPre();
        e.onNext(t);
        e.onComplete();
    }

    public abstract T onPre();
}
