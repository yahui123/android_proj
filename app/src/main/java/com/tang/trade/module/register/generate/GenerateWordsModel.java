package com.tang.trade.module.register.generate;

import com.tang.trade.base.AbsBaseModel;

/**
 * Created by Administrator on 2018/4/9.
 */

public class GenerateWordsModel extends AbsBaseModel implements GenerateWordsContract.Model {

    //调用c代码
    private native String getWord(int type);

    @Override
    public String generateWord(int wordType) {

        return getWord(wordType);
    }
}
