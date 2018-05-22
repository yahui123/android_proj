package com.tang.trade.tang.net;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.tang.trade.tang.MyApp;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

/**
 * Created by dagou on 2017/9/20.
 */

public class OkGoClient {
    private static final String TAG = OkGoClient.class.getSimpleName();
    private static final int RETRY_COUNT = 3;
    private static final int READ_TIMEOUT = 10*1000;
    private static final int WRITE_TIMEOUT = 7*1000;
    private static final int CONNECT_TIMEOUT = 5*1000;

    private OkGoClient() {
    }


    public static void init() {


        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        //全局的读取超时时间
        builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);

        OkGo.getInstance()
                .init(MyApp.context())
                .setOkHttpClient(builder.build())
                .setCacheMode(CacheMode.NO_CACHE)
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setRetryCount(RETRY_COUNT);

    }

    public static OkHttpClient getOkHttpClient() {
        return OkGo.getInstance().getOkHttpClient();
    }

    public static void cancelAll() {
        OkGo.getInstance().cancelAll();
    }

    public static void cancelAll(OkHttpClient okhttpclient) {
        OkGo.cancelAll(okhttpclient);
    }

    public static void cancelTag(Object tag) {
        OkGo.getInstance().cancelTag(tag);
    }


}
