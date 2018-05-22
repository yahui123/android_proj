package com.tang.trade.utils;

import java.util.UUID;

/**
 * Created by daibin on 2018/4/23.
 */

public class RandomUtil {
    //  public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 生成字母和数字的随机字符串 长度为length
     *
     * @return
     */
    public static String getRandomCharacterAndNumber() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");
        return uuidStr.substring(8, 24);

//   /*  *
//     * @param length
//     * @return
//     */
//    public static String getRandomCharacterAndNumber(int length) {
//        String val = "";
//        Random random = new Random();
//        for (int i = 0; i < length; i++) {
//            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字
//
//            if ("char".equalsIgnoreCase(charOrNum)) {// 字符串
//
//                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
//
//                val += (char) (choice + random.nextInt(26));
//                // int choice = 97; // 指定字符串为小写字母
//                val += (char) (choice + random.nextInt(26));
//
//            } else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
//
//                val += String.valueOf(random.nextInt(10));
//            }
//
//        }
    }

}
