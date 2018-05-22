package com.tang.trade.data.remote.http;

import com.tang.trade.data.remote.http.old.Response;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/4/9.
 */

public interface IMService {

    @FormUrlEncoded
    @POST(".")
    Observable<Response<Object>> regIM(@Header("bds_code") String code, @Field("prm") String prm);


}
