package com.tang.trade.data.remote.http;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Administrator on 2018/4/8.
 */

public abstract class BorderlessOnSubscribe implements ObservableOnSubscribe<Object> {
    @Override
    public void subscribe(ObservableEmitter<Object> e) throws Exception {
        onPre();
        e.onNext("");
        e.onComplete();
    }

    public abstract void onPre();
}
