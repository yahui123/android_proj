package com.tang.trade.data.remote.http.old;

import com.tang.trade.data.model.entity.ChangePhoneBean;
import com.tang.trade.data.model.entity.ConfigMessage;
import com.tang.trade.data.model.entity.HelpCenter;
import com.tang.trade.data.model.entity.PhoneBean;
import com.tang.trade.data.model.entity.PhoneResult;
import com.tang.trade.data.model.entity.QrCodeBean;
import com.tang.trade.data.model.entity.RandomEncryptResult;
import com.tang.trade.data.model.entity.VerifyBean;
import com.tang.trade.data.model.httpRequest.MsgConfigRequest;
import com.tang.trade.data.model.httpRequest.PhoneRequest;
import com.tang.trade.data.model.httpRequest.RandomEncryptRequest;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.HttpBaseResponse;
import com.tang.trade.module.register.create.RegisterBean;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/4/2.
 * 无界3.0之前方式
 */

public interface BorderService {
    String CODE_GET_CHAIN_NODES = "code_get_chain_nodes";

    String HTTP_FIXED = "";

//    @FormUrlEncoded
//    @POST(".")
//    Observable<HttpBaseResponse<Object>> register(@Header("bds_code") String code, @Field("prm") String prm);

    @POST("register")
    Observable<HttpBaseResponse<Object>> register(@Body RegisterBean registerRequest);  //@Header("bds_code") String code

    @POST("send/code")
    Observable<HttpBaseResponse<SMSResult>> sendSMS(@Body PhoneBean bean);

    @POST("validate/code")
    Observable<HttpBaseResponse<Object>> verifySMS(@Body VerifyBean bean);

    @POST("qrCode/phone")
    Observable<HttpBaseResponse<PhoneResult>> correspondingPhone(@Body PhoneRequest request);


    @POST("qrCode/save")
    Observable<HttpBaseResponse<Object>> saveQrCode(@Body QrCodeBean prm);

    @POST("qrCode/changePhone")
    Observable<HttpBaseResponse<Object>> modifyPhone(@Body ChangePhoneBean prm);

    @POST("qrCode/encryptKey")
    Observable<HttpBaseResponse<RandomEncryptResult>> getRandomEncrypt(@Body RandomEncryptRequest request);

    @POST("init/message")
    Observable<HttpBaseResponse<ConfigMessage>> getMessageConfig(@Body MsgConfigRequest request);

    @GET("settings/help")
    Observable<HttpBaseResponse<HelpCenter>> getHelpCenter();
}
