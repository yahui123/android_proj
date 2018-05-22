package com.tang.trade.tang.net;

import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.tang.BuildConfig;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HideTradeResponseModel;
import com.tang.trade.tang.net.model.IsAccResponseModel;
import com.tang.trade.tang.net.model.MarketListResponseModel;
import com.tang.trade.tang.net.model.MarketRecordResponseModel;
import com.tang.trade.tang.net.model.RechargResponseModel;
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.net.model.UpdateResponseModel;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.MD5;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.tang.trade.tang.net.TangConstant.Accptance_path;
import static com.tang.trade.tang.net.TangConstant.K_CHAR;
import static com.tang.trade.tang.net.TangConstant.MARKET_PAHT;
import static com.tang.trade.tang.net.TangConstant.URL;
import static com.tang.trade.tang.net.TangConstant.URL_BORDERLESS;
import static com.tang.trade.tang.net.TangConstant.URL_UPDATE;
import static com.tang.trade.tang.ui.LaunchActivity.lang;

/**
 * Created by dagou on 2017/9/21.
 */

public class TangApi {
    private static final String key="t1bmqfndfcje4vt1cv7pt50rlh6zddka";


    public static void login(String username, String password, Callback callback) {
        OkGo
                .<String>post(URL)
                .upJson(JsonCreator.loginJsonCreator(username, password))
                .execute(callback);
    }

    public static void register(String username, String password, Callback callback) {
        OkGo.<String>post(URL)
                .upJson(JsonCreator.RegisterJsonCreator(username, password))
                .execute(callback);
    }




    /**
     * charge
     * @param accid
     * @param type
     * @param cointypeway
     * @param name
     * @param count
     * @param callback
     */
    public static void charge(String accid, String type,String cointypeway, String name,String count,String acc,String typeid,String remark,String cointypewayid, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("accid",accid);
        hashMap.put("type",type);
        hashMap.put("cointypeway",cointypeway);
        hashMap.put("name",name);
        hashMap.put("count",count);
        hashMap.put("cointypewayid",cointypewayid);
        hashMap.put("acc",acc);
        hashMap.put("typeid",typeid);
        hashMap.put("remark",remark);

        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","api_acceptance_cashin");
        par.put("lang",lang);
        OkGo
                .<RechargResponseModel>post(URL_BORDERLESS)
                .params(par)

                .execute(callback);
    }





    public static void evaluate(String name, String id ,String type,String evaluate, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name",name);
        hashMap.put("id",id);
        hashMap.put("type",type);
        hashMap.put("evaluate",evaluate);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","api_acceptance_evaluate");
        par.put("lang",lang);
        OkGo
                .<ResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(callback);
    }


    public static void creatAccount(String accountname ,String publickey,String referreraccount,String code ,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("accountname",accountname);
        hashMap.put("publickey",publickey);
        hashMap.put("referreraccount",referreraccount);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("lang",lang);
        par.put("code",code);
        OkGo
                .<ResponseModel>post(Accptance_path)
                .params(par)
                .execute(callback);
    }

    //升级账户
    public static void upgradeMember(String name,String token,String token2,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name",name);
        hashMap.put("1",token);
        hashMap.put("2",token2);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("lang",lang);
        par.put("code","member_upgrade");
        OkGo
                .<ResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(callback);
    }

    public static void isAcc(String name,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name",name);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","api_is_acceptance");
        OkGo
                .<IsAccResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(callback);
    }

    public static void getNodes(final MyBaseViewCallBack baseViewCallback){
        OkGo.<ArrayList<String>>get(BuildConfig.STATIC_NODES).execute(new JsonCallBack<ArrayList<String>>(ArrayList.class) {
            @Override
            public void onSuccess(Response<ArrayList<String>> response) {
                baseViewCallback.setData(response.body());
            }

            @Override
            public void onStart(Request<ArrayList<String>, ? extends Request> request) {
                super.onStart(request);
                baseViewCallback.start();
            }

            @Override
            public void onError(Response<ArrayList<String>> response) {
                super.onError(response);
                baseViewCallback.onEnd();
            }
        });
    }

    public static void getUpdateInfo(final MyBaseViewCallBack baseViewCallback) {
        OkGo.<UpdateResponseModel>get(URL_UPDATE)
                .execute(new JsonCallBack<UpdateResponseModel>(UpdateResponseModel.class) {

                    @Override
                    public void onSuccess(Response<UpdateResponseModel> response) {
                        if (response.body()!= null) {
                            baseViewCallback.setData(response.body());
                        }
                    }

                    @Override
                    public void onError(Response<UpdateResponseModel> response) {
                        super.onError(response);
                        baseViewCallback.onEnd();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
//                        baseViewCallback.onEnd();
                    }

                });
    }

    public static void downloadApk(String url ,final BaseViewCallBackWithProgress baseViewCallback) {
        if (Device.sdcardExit()) {
            OkGo.<File>get(url).execute(new FileCallback(TangConstant.FILE_APK_PATH, "/Borderless.apk"){
                @Override
                public void onSuccess(Response<File> response) {
                    if (response.body() != null) {
                        baseViewCallback.setData(response.body());
                    }
                }

                @Override
                public void downloadProgress(Progress progress) {
                    super.downloadProgress(progress);
                    baseViewCallback.setProgress(progress);
                }

                @Override
                public void onStart(Request<File, ? extends Request> request) {
                    super.onStart(request);
                    baseViewCallback.onStart(request);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    baseViewCallback.onFinish();
                }
            });
        }

    }

   // "{"code":"member_push_list","prm":"[{\"type\":\"1\"}]","lang":"en","time":"1509527122"}

    /**
     * 集市交易list
     * marketList
     * @param type
     * @param callback
     */
    public static void marketList(String type, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type",type);

        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_list");
        par.put("lang",lang);
        OkGo
                .<MarketListResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }




    /**
     * 集市交易 买卖 获取token
     * @param callback
     * @param pushid
     * @param member
     * @param count
     */
    public static void marketSellOrBuy(String pushid,String member,String count, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put("nodeid","2");
        hashMap.put("dealname",member);
        hashMap.put("pushid",pushid);
        hashMap.put("count",count);

        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_check_tran_token");
        par.put("lang",lang);
//        Log.e("eeeeeeeeee", "marketCommission: "+JsonCreator.getJsonString(par));
        OkGo
                .<RechargResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }


    /**
     * 集市交易 获取token 委托挂单
     * @param type //类型
     * @param callback  //类型
     * @param member  //用户名
     * @param count  //数量
     * @param legal   //钱币 类型 USD CNY
     *
     */
    public static void marketCommission(String type,String member,String price,String count,String legal, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type",type);
        hashMap.put("member",member);
        hashMap.put("price",price);
        hashMap.put("count",count);
        hashMap.put("legal",legal);

        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_sell");
        par.put("lang",lang);
//        Log.e("eeeeeeeeee", "marketCommission: "+JsonCreator.getJsonString(par));
        OkGo
                .<RechargResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }

    /**
     * 记录 挂单记录
     * @param callback  //类型
     * @param member  //用户名
     *
     */
    public static void marketOrderRecord(String member, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("Member",member);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_list_self");
        par.put("lang",lang);
        Log.e("eeeeeeeeee", "marketCommission: "+JsonCreator.getJsonString(par));
        OkGo
                .<MarketRecordResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }

    /**
     *     记录 交易记录
     * @param callback  //类型
     * @param dealname  //用户名
     *
     */
    public static void marketRecord(String dealname, Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("dealname",dealname);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_list_self_deal");
        par.put("lang",lang);
        Log.e("eeeeeeeeee", "marketCommission: "+JsonCreator.getJsonString(par));
        OkGo
                .<MarketRecordResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }

    /**
     *     记录 取消挂单
     * @param callback  //类型
     * @param cancelname  //用户名
     * @param pushid
     * @param token
     *
     */
    public static void marketRecordQUxai(String cancelname,String pushid,String token,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("cancelname",cancelname);
        hashMap.put("pushid",pushid );
        hashMap.put("token",token );
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_cancel");
        par.put("lang",lang);
        OkGo
                .<ResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }


    /**
     *     记录 取消挂单 获取token
     * @param callback  //类型
     * @param cancelname  //用户名
     * @param pushid
     *
     */
    public static void marketRecordQUxaiToken(String cancelname,String pushid,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("cancelname",cancelname);
        hashMap.put("pushid",pushid );
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_push_cancel_token");
        par.put("lang",lang);
        OkGo
                .<RechargResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }


    /**
     *     记录 取消挂单 获取token//$count$ nodeid  member token pushid

     * @param callback  //类型
     * @param count
     * @param member
     * @param token//用户名
     * @param pushid
     *
     */
    public static void marketSellOrBuyJiaoYan(String count,String member,String token,String pushid,Callback callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("count",count);
        hashMap.put("pushid",pushid);
        hashMap.put("nodeid","2");
        hashMap.put("member",member);
        hashMap.put("token",token);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",String.valueOf(System.currentTimeMillis()));
        par.put("code","member_check_tran_buy_sell");
        par.put("lang",lang);
        OkGo
                .<ResponseModel>post(MARKET_PAHT)
                .params(par)

                .execute(callback);
    }



    /**
     * Add Account
     * @param borderlessname
     * @param typeway
     * @param bankname
     * @param type
     * @param name
     * @param bankaccount
     * @param baseViewCallback
     */
    public static void addAccount(String borderlessname ,String typeway,String bankname,String type,String name,String bankaccount, final Callback<ResponseModel> baseViewCallback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("borderlessname", borderlessname);
        hashMap.put("typeway", typeway);
        hashMap.put("bankname", bankname);
        hashMap.put("type", type);
        hashMap.put("name", name);
        hashMap.put("bankaccount", bankaccount);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm", JsonCreator.getJsonString(hashMap));
        par.put("time", String.valueOf(System.currentTimeMillis()));
        par.put("code", "api_account_list_add");
        par.put("lang", lang);
        OkGo
                .<ResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(baseViewCallback);

    }


    public static void getHideTrade( final Callback<HideTradeResponseModel> baseViewCallback) {
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm","");
        par.put("time", String.valueOf(System.currentTimeMillis()));
        par.put("code", "api_trade_hide");
        par.put("lang", lang);
        OkGo
                .<HideTradeResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(baseViewCallback);

    }

    public static void getHideTransfer( final Callback<HideTradeResponseModel> baseViewCallback) {
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm","");
        par.put("time", String.valueOf(System.currentTimeMillis()));
        par.put("code", "api_transfer_hide");
        par.put("lang", lang);
        OkGo
                .<HideTradeResponseModel>post(Accptance_path)
                .params(par)
                .execute(baseViewCallback);

    }





    /**
     * 检验见证人
     * @param name
     * @param baseViewCallback
     */
    public static void witnessCheck(String name ,final Callback<ResponseModel> baseViewCallback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name", name);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm", JsonCreator.getJsonString(hashMap));
//        par.put("time", String.valueOf(System.currentTimeMillis()));
//        par.put("code", "member_to_witness_check");
        par.put("lang", lang);
        String time =String.valueOf(System.currentTimeMillis());
        String code="member_to_witness_check";
        par.put("time",time);
        par.put("code",code);
        par.put("token","");
        par.put("sign", MD5.md5(key + time + code));
        OkGo
                .<ResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(baseViewCallback);

    }

    /**
     * 成为见证人
     * @param name
     * @param baseViewCallback
     */
    public static void setWitness(String name ,String token , String token1, final Callback<ResponseModel> baseViewCallback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name", name);
        hashMap.put("1", token);
        hashMap.put("2", token1);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm", JsonCreator.getJsonString(hashMap));
//        par.put("time", String.valueOf(System.currentTimeMillis()));
//        par.put("code", "member_to_witness");

        String time =String.valueOf(System.currentTimeMillis());
        String code="member_to_witness";
        par.put("time",time);
        par.put("code",code);
        par.put("token","");
        par.put("sign", MD5.md5(key + time + code));
        par.put("lang", lang);
        OkGo
                .<ResponseModel>post(URL_BORDERLESS)
                .params(par)
                .execute(baseViewCallback);

    }


    public interface BaseViewCallback<T> {
        void setData(T t);
    }



    public interface BaseViewCallBackWithProgress<T>extends BaseViewCallback<T>{
        void setProgress(Progress progress);

        void onStart(Request<File, ? extends Request> request);

        void onFinish();

    }


    public interface MyBaseViewCallBack<T>extends BaseViewCallback<T>{
        void start();
        void onEnd();
    }




    /**
     * get k char data
     * @param type
     * @param baseSymbol
     * @param qouetSymbol
     * @param baseViewCallback
     */
    /**
     * get k char data
     * @param type
     * @param baseSymbol
     * @param qouetSymbol
     * @param baseViewCallback
     */
    public static void getKCharData(String type,String baseSymbol,String qouetSymbol ,final MyBaseViewCallBack baseViewCallback) {

        OkGo.<String>get(K_CHAR+"?type="+type+"&symbol="+qouetSymbol+"_"+baseSymbol)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        baseViewCallback.setData(response.body());
                    }

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        baseViewCallback.start();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        baseViewCallback.onEnd();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        baseViewCallback.onEnd();
                    }
                });

    }

}
