package com.tang.trade.tang.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2017/11/29.
 */

public class MyTextTuils {

    public static boolean containsEmoji(String source){
        int len = source.length();
            for(int i = 0; i < len; i++){
                char codePoint = source.charAt(i);
                if(!isEmojiCharacter(codePoint)){//如果不能匹配,则该字符是Emoji表情  
                    return true;
                }
            }
        return false;
    }

    private static boolean isEmojiCharacter(char codePoint) {
       return(codePoint==0x0)||(codePoint==0x9)||(codePoint==0xA)||
            (codePoint==0xD)||((codePoint>=0x20)&&(codePoint<=0xD7FF))||
            ((codePoint>=0xE000)&&(codePoint<=0xFFFD))||((codePoint>=0x10000)
               &&(codePoint<=0x10FFFF));
    }


    /**
     * 禁止EditText输入特殊字符
     * @param editText
     */
    public static void setEditTextInhibitInputSpeChat(EditText editText){

        InputFilter filter=new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String speChat="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if(matcher.find())return "";
                else return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

}
