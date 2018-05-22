package com.tang.trade.data.remote.http;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by leo on 02/03/2018.
 */

public class HttpHeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {

        return chain.proceed(chain.request());
    }
}
