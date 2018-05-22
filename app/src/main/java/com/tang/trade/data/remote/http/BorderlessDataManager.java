package com.tang.trade.data.remote.http;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.flh.framework.rx.RxJavaUtil;

import org.reactivestreams.Subscriber;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/4/8.
 */

public class BorderlessDataManager {
    private static volatile BorderlessDataManager instance;


    public static BorderlessDataManager getInstance() {
        if (instance == null) {
            synchronized (HttpDataManager.class) {
                if (instance == null) {
                    instance = new BorderlessDataManager();
                }
            }
        }


        return instance;
    }

    public void selectUser(BorderlessOnSubscribe onSubscribe, Observer observer) {
        Observable.create(onSubscribe)
                .compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }


//        Observable.create(new OnSubscribe<Drawable>() {
//            @Override
//            public void call(Subscriber<? super Drawable> subscriber) {
//                Drawable drawable = getTheme().getDrawable(drawableRes));
//                subscriber.onNext(drawable);
//                subscriber.onCompleted();
//            }
//        })
//                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
//                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
//                .subscribe(new Observer<Drawable>() {
//                    @Override
//                    public void onNext(Drawable drawable) {
//                        imageView.setImageDrawable(drawable);
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show();
//                    }
//                });
}

