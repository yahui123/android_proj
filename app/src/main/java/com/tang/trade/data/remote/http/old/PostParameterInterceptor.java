package com.tang.trade.data.remote.http.old;


import com.tang.trade.tang.utils.MD5;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Created by leo on 07/01/2018.
 */

public class PostParameterInterceptor implements Interceptor {

    private static String TAG = PostParameterInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (BorderService.CODE_GET_CHAIN_NODES.equals(originalRequest.header("bds_code"))) {
            return chain.proceed(originalRequest);
        }

        String code = originalRequest.header("bds_code");
        String time = getTime();
        String sign = getSign(time, code);
        String token = "";

        requestBuilder.removeHeader("bds_code");

        FormBody formBody = new FormBody.Builder()
                .add("token", token)
                .add("code", code)
                .add("time", time)
                .add("sign", sign)
                .add("lang", "zh_CN")
                .build();

        String postBodyString = bodyToString(originalRequest.body());
        postBodyString += ((postBodyString.length() > 0) ? "&" : "") + bodyToString(formBody);
        Request request = requestBuilder
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString))
                .build();

        return chain.proceed(request);
    }

    private String bodyToString(RequestBody body) {
        try {
            final RequestBody copy = body;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private String getTime() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    private String getSign(String time, String code) {
//        try {
//            if (BuildConfig.DEBUG && code.equals(BorderlessApiService.CODE_REGISTER_ACCOUNT)) {
//
//                return MD5.getMD5(BuildConfig.HTTP_BORDERLESS_SALT + time + code);
//            }

            return MD5.md5("t1bmqfndfcje4vt1cv7pt50rlh6zddka" + time + code);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return "";
    }
}
