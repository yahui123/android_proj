package com.tang.trade.data.remote.http;


import com.tang.trade.data.model.httpRequest.EmptyRequest;

import io.reactivex.Observable;
import io.reactivex.Observer;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/4/2.
 * 无界4.0之后
 */

public interface BdsService {

    @POST(".")
    Observable<HttpResponse> testMy(@Body EmptyRequest request);


}
