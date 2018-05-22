package com.tang.trade.tang.net;

import android.os.Environment;

import com.tang.trade.tang.BuildConfig;

/**
 * Created by dagou on 2017/9/21.
 */

public class TangConstant {

    public static String URL = "https://tap.blacoin.cc/api/v1/";
    public static String URL_UPDATE = "https://tap.borderless.vip:39685/update.json";

    /**
     * k char
     */
    public static String K_CHAR = "https://api.borderless.vip:1855/chart/kline.ashx";

    /**
     * 无需手动切换正式、测试，Jenkins会根据打包类型结合build.gradle中的配置自动切换
     */
    public static String URL_BORDERLESS = BuildConfig.URL_BORDERLESS;
    public static String Accptance_path = BuildConfig.Accptance_path;


    /**
     * 承兑商所用正式
     */
//    public static String URL_BORDERLESS = "https://acc.borderless.vip:1117/Api/";
//    public static String Accptance_path ="https://api.borderless.vip:1855/api/acceptance/";

    /**
     * 承兑商测试
     */
//   public static String URL_BORDERLESS = "http://222.186.173.51:1117/api/";
//   public static String Accptance_path ="https://acc.borderless.vip:5557/api/acceptance/";



    public static String MARKET_PAHT="https://tap.borderless.vip:39685/api/trade/";//集市正式
//
//    //升级正式
    public static String UPGRADE_PATH="https://acc.borderless.vip:1117/api/";

    //k-char
    public static String K_CHART="https://tap.borderless.vip:39685/chart/coinkline.html?symbol=";

    //CoinType
    public static String COINTYPE="https://tap.borderless.vip:39685/img/";

    public static String ACCOUNT_CREATE = "account_create";
    public static String API_CODE = "api_code";
    public static String ACCOUNT_LOGIN = "account_login";
    public static String TRANSFER = "transfer";

    public static String SENDOUT = "transfer";
    public static String NAME = "name";

    public static String PASSWORD = "password";
    public static String GET_ACCOUNT_HISTORY = "get_account_history";
    public static String LIST_ACCOUNT_BANLANCE = "list_account_balances";
    public static String RECEPTION_RECORD = "get_receive_exchange";
    public static String ACCOUNTYUE = "list_account_balances";
    public static String BLOCCK_CHAIN_INFO = "get_blockchain_infor";
    public static String BLOCCK_INFO = "get_block_infor";
    public static String BLOCCK_NUMBER = "blocknumber";
    public static String SERVICE_CHARGE = "get_fees";
    public static String FEE_ID = "feeid";

    public static String FILE_APK_PATH = Environment.getExternalStorageDirectory().getPath();

    public static String PATH = Environment.getExternalStorageDirectory() + "/bds/v4/bin/";
    public static String QR_PATH = Environment.getExternalStorageDirectory() + "/bds/v4/capture/";

    public static String PATH_BACKUP = Environment.getExternalStorageDirectory() + "/bds/v4/backup/";

    public static String PATH_CLI_BACKUP = Environment.getExternalStorageDirectory() + "/bds/v4/cli_backup/";

    public static String NODE_JSON = "[\"ws://47.104.109.91:12357\",\"ws://118.190.157.46:12357\",\"ws://47.104.82.7:11117\",\"ws://47.104.13.65:12357\",\"ws://61.14.210.3:11117\",\"ws://185.224.168.90:32357\",\"ws://172.246.128.66:12357\",\"ws://211.75.1.216:22357\"]";

}
