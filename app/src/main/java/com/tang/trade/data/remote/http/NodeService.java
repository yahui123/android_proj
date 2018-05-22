package com.tang.trade.data.remote.http;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/4/8.
 */

public interface NodeService {

    @GET("static-nodes.json")
    Observable<List<String>> getNodes();

}
