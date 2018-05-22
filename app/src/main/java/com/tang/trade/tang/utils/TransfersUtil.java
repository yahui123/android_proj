package com.tang.trade.tang.utils;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/5.
 */

public class TransfersUtil {

    /**
     *转账
     * */
    public static boolean receiptsUtil(String payAccount,String receiptsAccount ,
                                            String symbol ,final String amount,String fee
            ,String token){
        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(symbol);
            if (object != null) {
                if (!symbol.equals("BDS")){
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(payAccount
                            , receiptsAccount
                            ,amount
                            ,symbol
                            ,token
                            ,fee
                            ,object.id.toString());

                }else {
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(payAccount
                            , receiptsAccount
                            , amount
                            , "BDS","","","");

                }



            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            signedTransaction=null;
        }


        if (signedTransaction != null){
            return true;
        }
            return false;

    }




    public static Map<String,String> getBalance(final String symbol, final String bdsAccount) throws NetworkStatusException {
        String finalSAssetAmount = "0.00000";
            String sAssetAmount;
            boolean findAssetInAccount = false;
            Map<String,String> map = new HashMap<>();            //获取所有资产列表
            account_object object = BitsharesWalletWraper.getInstance().get_account_object(bdsAccount);
            if (object == null) {
//                MyApp.showToast(getString(R.string.network));
                throw new NetworkStatusException("NetworkStatusException");
            }
            //查询账户id
            object_id<account_object> loginAccountId = object.id;

            //获取账户余额列表
            List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
            if (accountAsset == null) {
                throw new NetworkStatusException("NetworkStatusException");
            }
            //查询资产列表
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (objAssets == null) {
//                MyApp.showToast(getString(R.string.network));
                throw new NetworkStatusException("NetworkStatusException");
            }
            //fee
        String fee=getFee(symbol);
            map.put("fee",fee);



            //查询基础货币BDS,和所有智能资产

            for (asset_object objAsset : objAssets) {
                if (objAsset.symbol.equalsIgnoreCase(symbol)) {
                    findAssetInAccount = false;
                    if (accountAsset.size() > 0) {
                        for (int j = 0; j < accountAsset.size(); j++) {
                            asset account_asset = accountAsset.get(j);
                            String sAccount_sid = account_asset.asset_id.toString();
                            if (sAccount_sid.equals(objAsset.id.toString())) {
                                findAssetInAccount = true;
                                //计算账户余额
                                asset_object.asset_object_legible myasset = objAsset.get_legible_asset_object(account_asset.amount);
                                sAssetAmount = myasset.count;
                                finalSAssetAmount = sAssetAmount;
                            }
                        }
                    }

                }
            }
            if (!findAssetInAccount) {
                map.put("account",finalSAssetAmount);
                return map;
            }else {
                throw new NetworkStatusException("NetworkStatusException ");
            }

    }
    public static String getFee(String symbol) throws NetworkStatusException {
        final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);
        return FeeUtil.getFee(symbol,rate);
    }
}
