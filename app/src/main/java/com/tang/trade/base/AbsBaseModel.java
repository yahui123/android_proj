package com.tang.trade.base;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/4/10.
 */

public abstract class AbsBaseModel {
    protected  Observable ObervableBase(ObservableOnSubscribe obser){
        return  Observable.create( obser).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}