package com.tang.trade.tang.net;

import org.json.JSONObject;

import java.util.HashMap;

import static com.tang.trade.tang.net.TangConstant.*;

/**
 * Created by dagou on 2017/9/21.
 */

public class JsonCreator {
    public static JSONObject loginJsonCreator(String username, String password) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, ACCOUNT_LOGIN);
        hashMap.put(NAME, username);
        hashMap.put(PASSWORD, password);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

    public static JSONObject RegisterJsonCreator(String username, String password) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, ACCOUNT_CREATE);
        hashMap.put(NAME, username);
        hashMap.put(PASSWORD, password);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }


    public static JSONObject HistoryJsonCreator(String name) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, GET_ACCOUNT_HISTORY);
        hashMap.put(NAME, name);
        return new JSONObject(hashMap);

    }

    public static JSONObject BalanceJsonCreator(String name) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, LIST_ACCOUNT_BANLANCE);
        hashMap.put(NAME, name);
        return new JSONObject(hashMap);
    }

    public static JSONObject sendOutJsonCreator(String from, String to, String amount, String symbol, String memo, String broadcast, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, SENDOUT);
        hashMap.put("from", from);
        hashMap.put("to", to);
        hashMap.put("amount", amount);
        hashMap.put("symbol", symbol);
        hashMap.put("memo", memo);
        hashMap.put("broadcast", broadcast);
        hashMap.put("token", token);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

    public static JSONObject receptionRecordJsonCreator(String username) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, RECEPTION_RECORD);
        hashMap.put(NAME, username);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

    public static JSONObject accountYuEJsonCreator(String username) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, ACCOUNTYUE);
        hashMap.put(NAME, username);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

    public static JSONObject blockChainInfoCreator() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, BLOCCK_CHAIN_INFO);
        return new JSONObject(hashMap);
    }

    public static JSONObject blockInfoCreator(String page) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, BLOCCK_INFO);
        hashMap.put(BLOCCK_NUMBER, page);
        return new JSONObject(hashMap);
    }

    public static JSONObject ServiceChargeJsonCreator(String id) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(API_CODE, SERVICE_CHARGE);
        hashMap.put(FEE_ID, id);
        return new JSONObject(hashMap);
    }


    /**
     * 承兑商json格式化
     * @param prm
     * @param time
     * @param code
     * @return
     */
    public static JSONObject acceptanceJsonCreator(String prm, String time,String code) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("prm", prm);
        hashMap.put("time", time);
        hashMap.put("code", code);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

    /**
     *返回json字符串
     * @return
     */
    public static String getJsonString(HashMap<String, String> hashMap) {
        JSONObject jsonObject = new JSONObject(hashMap);
        return "["+jsonObject.toString()+"]";

    }

    /**
     * 返回json对象
     * @param hashMap
     * @return
     */
    public static JSONObject getJsonObject(HashMap<String, String> hashMap) {
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject;

    }

}
