package com.tang.trade.app;

import com.tang.trade.tang.BuildConfig;

/**
 * Created by Administrator on 2018/4/2.
 */

public class Const {

    //写死的钱包密码
    public static final String WALLET_PWD = "123456";

    /*QR_CONTENT，DIRECT_WORDS，PHONE 都必须加用户名前缀 因为每个用户都不一样*/

    /* USERNAME--加密速记词   */
    public static final String PRIVATEKEY = "privateKey";
    public static final String IS_LIFE_MEMBER = "isLifeMember";
    public static final String QR_CONTENT = "qr_content";
    public static final String NODES = "nodes";
    public static final String DIRECT_WORDS = "direct_words";
    public static final String USERNAME = "userName";
    public static final String CURRTENT_BIN = "current_bin";
    public static final String PHONE = "phone";
    public static final String RECOVER_SUCCESS = "recover_success";
    public static final String FIRST_MASK = "first_mask";

    public static String HTTP_API_BDS = BuildConfig.URL_BORDERLESS;
    public static String ACCPTANCE_PATH = BuildConfig.Accptance_path;
    public static String HTTP_BASE_URL = BuildConfig.HTTP_BASE_URL;

    public static final int SMS_COUNTDOWN = 60;

    public class SP {
        public static final String SP_ACCEPTENCE_REQUEST_TIME = "sp_acceptence_request_time";
        public static final String SP_ACCEPTENCE_REQUEST_LIST_JSON = "sp_acceptence_request_list_json";
        public static final String ROOM_VERSION_ID = "room_version_id";
        public static final String IM_LOGIN_SUCCESS = "im_login_success";
    }

    public static class Asset {
        public static final String BIG_SCALE_ASSETS[] = {"ETH", "BTC", "LTC"};
        public static final String ASSET_SCALE_2 = "0.00";
        public static final String ASSET_SCALE_5 = "0.00000";
        public static final String ASSET_SCALE_8 = "0.00000000";


    }
}
