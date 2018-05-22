package com.tang.trade.tang.utils;

import java.math.BigDecimal;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.gbdsTransferFee;

/**
 * Created by Administrator on 2018/2/23.
 */

public class FeeUtil {
    public static String getFee(String symbol,String rate ) {
        String fee="0";
        if (!symbol.equalsIgnoreCase("BDS")){
            fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
            if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH") ||symbol.equalsIgnoreCase("LTC")){
               fee = new BigDecimal(fee).setScale(8, BigDecimal.ROUND_UP).toPlainString();
            }else {
                fee = new BigDecimal(fee).setScale(5, BigDecimal.ROUND_UP).toPlainString();
            }
        }else {
            fee = NumberUtils.formatNumber5(gbdsTransferFee + "");
        }
        return fee;
    }

}
