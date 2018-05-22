package com.tang.trade.data.remote.http;

import com.flh.framework.net.RetrofitManager;
import com.tang.trade.app.Const;
import com.tang.trade.data.remote.http.old.BorderService;
import com.tang.trade.data.remote.http.old.PostParameterInterceptor;
import com.tang.trade.tang.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by leo on 01/03/2018.
 */

public class HttpApiClient {

    private static int TIME_OUT = 30;

    private static HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (BuildConfig.DEBUG) {

            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }
        return httpLoggingInterceptor;
    }

    //获取节点
//    static NodeService getNodeService() {
//        return RetrofitManager.createRetrofitBuilder(BuildConfig.STATIC_NODES)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConvertFactory(GsonConverterFactory.create())
//                .addCustomInterceptor(getHttpLoggingInterceptor())
//                .addCustomInterceptor(new HttpHeaderInterceptor())
//                .setConnectTimeout(TIME_OUT, TimeUnit.SECONDS)
//                .setReadTimeout(TIME_OUT, TimeUnit.SECONDS)
//                .setWriteTimeout(TIME_OUT, TimeUnit.SECONDS)
//                .build(NodeService.class);
//    }

    static NodeService getNodeService() {
        return RetrofitManager.createRetrofitBuilder(BuildConfig.STATIC_NODES_400)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConvertFactory(GsonConverterFactory.create())
                .addCustomInterceptor(getHttpLoggingInterceptor())
                .addCustomInterceptor(new HttpHeaderInterceptor())
                .setConnectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .setReadTimeout(TIME_OUT, TimeUnit.SECONDS)
                .setWriteTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build(NodeService.class);
    }

    //老的 拦截重构请求参数的方式
    static BorderService getBorderService() {
        return RetrofitManager.createRetrofitBuilder(Const.ACCPTANCE_PATH)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConvertFactory(GsonConverterFactory.create())
                .addCustomInterceptor(new PostParameterInterceptor())
                .addCustomInterceptor(getHttpLoggingInterceptor())
                .build(BorderService.class);
    }

    /**
     * 新注册服务
     *
     * @return
     */
    static BorderService getNewBorderService() {
        return RetrofitManager.createRetrofitBuilder(Const.HTTP_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConvertFactory(GsonConverterFactory.create())
                .addCustomInterceptor(getHttpLoggingInterceptor())
                .addCustomInterceptor(new HttpHeaderInterceptor())
                .setConnectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .setReadTimeout(TIME_OUT, TimeUnit.SECONDS)
                .setWriteTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build(BorderService.class);

    }

    //注册阿里IM
    static IMService getIMService() {
        return RetrofitManager.createRetrofitBuilder("https://api.borderless.vip:1855/api/acceptance/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConvertFactory(GsonConverterFactory.create())
                .addCustomInterceptor(new PostParameterInterceptor())
                .addCustomInterceptor(getHttpLoggingInterceptor())
                .build(IMService.class);
    }
}
