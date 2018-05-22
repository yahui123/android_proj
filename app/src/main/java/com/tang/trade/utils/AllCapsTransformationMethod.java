package com.tang.trade.utils;

import android.text.method.ReplacementTransformationMethod;

/**
 * Created by daibin on 2018/4/19.
 * EditText英文字母大小写转换
 */

public class AllCapsTransformationMethod extends ReplacementTransformationMethod{

    char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    boolean isUpperToLower;

    public AllCapsTransformationMethod(boolean isUpperToLower) {
        this.isUpperToLower = isUpperToLower;
    }

    @Override
    protected char[] getOriginal() {
        if (isUpperToLower) {
            return upper;
        } else {
            return lower;
        }
    }

    @Override
    protected char[] getReplacement() {
        if (isUpperToLower) {
            return lower;
        } else {
            return upper;
        }
    }
}
