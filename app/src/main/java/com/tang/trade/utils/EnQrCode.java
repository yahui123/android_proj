package com.tang.trade.utils;

import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.utils.BuildConfig;

import static com.tang.trade.tang.utils.MD5.md5;

/**
 * 用于加密二维码
 * Created by Administrator on 2018/4/26.
 */

public class EnQrCode {

    private static String randomStr;
    private static String md5Key;

    public static String getRandomStr() {
        return randomStr;
    }

    public static String getMd5Key() {
        return md5Key;
    }

    public static void enQrCode(String userName) {
        // 1.生成速记词和用户名的字符串*/（判断有速记词传速记词，没有速记词用私钥）
        String words = SPUtils.getString(userName + Const.DIRECT_WORDS, "");

        String qrStr;
        if (android.text.TextUtils.isEmpty(words)) {
            qrStr = "{\"username\":\"" + userName + "\",\"privateKey\":\"" + SPUtils.getString(userName + Const.PRIVATEKEY, "") + "\"}";
        } else {
            qrStr = "{\"username\":\"" + userName + "\",\"privateKeyWord\":\"" + words + "\"}";
        }

        /*2.生成16位的随机加密字符串*/
        randomStr = RandomUtil.getRandomCharacterAndNumber();

        /*3.AES加密 二维码内容*/
        String qrAesStr = null;
        try {
            qrAesStr = "borderless400AccountInfo:" + AESCipher.aesEncryptString(qrStr, randomStr);

            SPUtils.put(userName + Const.QR_CONTENT, qrAesStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
        /*4.MD5加密二维码内容  与手机号和随机加密字符串上传到服务器*/
        md5Key = md5(qrAesStr);
        System.out.println();

    }


}
