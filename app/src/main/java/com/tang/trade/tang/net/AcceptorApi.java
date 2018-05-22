package com.tang.trade.tang.net;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.tang.trade.tang.utils.MD5;

import java.util.HashMap;

import static com.tang.trade.tang.net.TangConstant.Accptance_path;
import static com.tang.trade.tang.ui.LaunchActivity.lang;

/**
 * Created by Administrator on 2017/12/14.
 */

public class AcceptorApi {
    private static final String key="t1bmqfndfcje4vt1cv7pt50rlh6zddka";

    public static void acceptantHttp(HashMap<String, String> hashMap, String code, Callback callback) {

        String time=String.valueOf(System.currentTimeMillis()/1000);
        //hashMap.put("lang", lang);
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("prm",JsonCreator.getJsonString(hashMap));
        par.put("time",time);
        par.put("code",code);
        par.put("token","");
        par.put("sign", MD5.md5(key + time + code));
        par.put("lang", lang);
//        String sr=JsonCreator.getJsonString(par);
        OkGo
                .<Object>post(Accptance_path)
                .params(par)

                .execute(callback);
    }


}
