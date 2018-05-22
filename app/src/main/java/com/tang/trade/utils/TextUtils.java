package com.tang.trade.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/4/13.
 */

public class TextUtils {

    /**
     * 检验是否是8到16位数字和字母组成
     *
     * @param str
     * @return
     */
    public static boolean isNumberLetter(String str) {
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$";
        return str.matches(regex);
    }

    /**
     * 检验是否是由数字和字母组成
     *
     * @param str
     * @return
     */
    public static boolean checkNumberLetter(String str) {
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{0,16}$";
        return str.matches(regex);
    }


    /**
     * 验证是否包含特殊字符
     *
     * @param str
     * @return
     */
    public static boolean isSpecial(String str) {
        String regEx = "[`~!@#$%^&*()_+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }

    /**
     * 验证手机号
     *
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
        Pattern p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 是否是空格
     *
     * @param str
     * @return
     */
    public static boolean isBlank(CharSequence str) {
        if (" ".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 将手机号加密成 135****4754
     *
     * @param phone
     * @return
     */
    public static String enPhone(String phone) {
        if (android.text.TextUtils.isEmpty(phone)) {
            return "";
        }
        return phone.replaceAll(phone.substring(3, phone.length() - 4), "****");
    }

}
