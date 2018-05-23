package com.tang.trade.tang.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.gson.Gson;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;

import com.tang.trade.tang.net.model.HttpResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;

import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.CallOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.utils.SPUtils;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;


/**
 * Created by Administrator on 2018/3/21.
 */

public class BorrowUtil {

    public String collateral="0.00";//原有的保证金
    public String collateral2="0.00";//原有的保证金
    public String debt = "0.00";//借入初始值
    public String debt1="0.00";//修改之后
    public String feed_price = "0.00";//喂价
    public double per = 1.5;//比例
    public List<asset_object> objAssets;//资产列表
    public String selectCurrency="CNY";
    private Context mContext;
    public String bdsBalance;
    public String selectBalance;
    public String bds_borrow_Fee;

    public String strProgress="3.00";
    public String minProgress="3.00";
    public String maxProgress="6.00";

    //强平触发价
    public String kyohei = "0.00";




    public void intiData() throws IOException, NetworkStatusException, JSONException {

        try {
            objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        getKyohei(selectCurrency);


        selectBalance =getBalance(selectCurrency, SPUtils.getString(Const.USERNAME,""));
        bdsBalance =getBalance("BDS", SPUtils.getString(Const.USERNAME,""));
        bds_borrow_Fee =getFee();
        strProgress=this.getStrProgress(debt,feed_price,collateral,minProgress);

    }
    public void updateDate(String select) throws NetworkStatusException, JSONException, IOException {

        if (!select.equals(selectCurrency)){
            collateral="0.00";//原有的保证金
            collateral2="0.00";//原有的保证金
            debt = "0.00";//借入初始值
            debt1=debt1;//修改之后
            feed_price = "0.00";//喂价
            per = 1.5;//比例
            selectCurrency=select;
            selectBalance="0.00000";


            getKyohei(selectCurrency);


            selectBalance =getBalance(selectCurrency, SPUtils.getString(Const.USERNAME,""));
            bdsBalance =getBalance("BDS", SPUtils.getString(Const.USERNAME,""));
            bds_borrow_Fee =getFee();
            strProgress=this.getStrProgress(debt,feed_price,collateral,minProgress);

        }




    }
    /**重置
     * */
    public void calnalDate(){
            this.collateral2=collateral;
            this.debt1=debt;
            strProgress=this.getStrProgress(debt,feed_price,collateral,minProgress);

    }


    private String getBalance(final String symbol, final String bdsAccount) throws NetworkStatusException {
        String finalSAssetAmount = "0.00000";
            String sAssetAmount;
            boolean findAssetInAccount = false;

            //获取所有资产列表
            account_object object = BitsharesWalletWraper.getInstance().get_account_object(bdsAccount);
            if (object == null) {
//                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }
            //查询账户id
            object_id<account_object> loginAccountId = object.id;

            //获取账户余额列表
            List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
            if (accountAsset == null) {
               // MyApp.showToast(mContext.getString(R.string.network));
                return finalSAssetAmount;
            }
            //查询资产列表
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (objAssets == null) {
//                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }


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
                return finalSAssetAmount;
            }

        return finalSAssetAmount;
    }

    private String getFee() throws NetworkStatusException {
        return  BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_UPDATE_LMMIT_ORDER_OPERATION);

    }
    private void getKyohei(String symbol) throws NetworkStatusException, JSONException {
        kyohei = "0.00";
        debt = "0.00000";
        collateral = "0.00000";

        String asset_id = "";
        full_account full_account = null;

            full_account = BitsharesWalletWraper.getInstance().get_full_account(SPUtils.getString(Const.USERNAME,""),true);
        if (objAssets != null) {
            for (int j = 0; j < objAssets.size(); j++) {
                if (objAssets.get(j).symbol.equalsIgnoreCase(symbol)) {
                    asset_id = objAssets.get(j).id.toString();
                }
            }
            if (full_account != null){
                List<CallOrder> callOrder = full_account.call_orders;

                if (callOrder != null){
                    for (int i = 0 ; i<callOrder.size();i++){
                        String call_asset_id = "";
                        if (!full_account.call_orders.get(i).getQuote_asset_id().equalsIgnoreCase("1.3.0")){
                            call_asset_id = full_account.call_orders.get(i).getQuote_asset_id();
                        }else {
                            call_asset_id = full_account.call_orders.get(i).getBase_asset_id();
                        }

                        if (asset_id.equalsIgnoreCase(call_asset_id)){
                            String base_asset_id = full_account.call_orders.get(i).getBase_asset_id();
                            String baseAmount = full_account.call_orders.get(i).getBase_amount();
                            String quoteAmount = full_account.call_orders.get(i).getQuote_amount();

                            debt = NumberUtils.formatNumber5((Double.parseDouble(full_account.call_orders.get(i).getDebt()) / 100000) + "");
                            debt1 = debt;
                            collateral = NumberUtils.formatNumber5((Double.parseDouble(full_account.call_orders.get(i).getCollateral()) / 100000) + "");
                            collateral2=collateral;
                            if (base_asset_id.equals("1.3.0")){
                                kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                            }else{
                                kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                            }
                        }
                    }
                }

            }



        }

    }


    public void setCloseSoftKeyboard(String jieru, String balace1, String strBili){
        String type="debt";
        if (Double.parseDouble(jieru)!=Double.parseDouble(debt1)){
            this.debt1=jieru;

            if(!isStrProgress(this.strProgress,this.minProgress,this.maxProgress)){
                strProgress=getStrProgress(strProgress,minProgress,maxProgress);
                collateral2=getEarnestMoney(debt1,feed_price,strProgress);

            }else {
                collateral2=getEarnestMoney(debt1,feed_price,strProgress);
            }

        }else if (Double.parseDouble(balace1)!=Double.parseDouble(collateral2)){
             type="collateral";

            collateral2=balace1;
            strProgress=getStrProgress(debt1,feed_price,balace1,minProgress);

            if(!isStrProgress(this.strProgress,this.minProgress,this.maxProgress)){
                strProgress=getStrProgress(strProgress,minProgress,maxProgress);
                collateral2=getEarnestMoney(debt1,feed_price,strProgress);
            }

        }else if (Double.parseDouble(strBili)!=Double.parseDouble(strProgress)){
             type="progress";
            if(!isStrProgress(strBili,this.minProgress,this.maxProgress)){
                strProgress=getStrProgress(strBili,minProgress,maxProgress);
                collateral2=getEarnestMoney(debt1,feed_price,strProgress);
            }else {
                this.strProgress=strBili;
                collateral2=getEarnestMoney(debt1,feed_price,strProgress);
            }
        }else {

            type="";
            return;
        }

        onUpdateListener.onUpadeChangeString(strProgress,collateral2,type);
    }






    /**
     *
     * **/

    public String getStrProgress(String debt,String feed_price,String collateral,String minProgress,String maxProgress){
       String strProgress=getStrProgress(debt,feed_price,collateral,minProgress);
       double douProgress=CalculateUtils.getDouble(strProgress);
        double douminProgress=CalculateUtils.getDouble(minProgress);
        double doumaxProgress=CalculateUtils.getDouble(maxProgress);

       if (douProgress<douminProgress){
           return minProgress;
       }else if (douProgress>doumaxProgress){
           return maxProgress;
       }
        return strProgress;
    }

    /**
     * 判断比例是否在范围内
     * **/

    public  String getStrProgress(String strProgress,String minProgress,String maxProgress){
        double douProgress=CalculateUtils.getDouble(strProgress);
        double douminProgress=CalculateUtils.getDouble(minProgress);
        double doumaxProgress=CalculateUtils.getDouble(maxProgress);

        if (douProgress<douminProgress){
            return minProgress;
        }else if (douProgress>doumaxProgress){
            return maxProgress;
        }
        return strProgress;
    }

    /**
     * 判断比例是否在范围内
     * **/

    public  boolean isStrProgress(String strProgress,String minProgress,String maxProgress){
        double douProgress=CalculateUtils.getDouble(strProgress);
        double douminProgress=CalculateUtils.getDouble(minProgress);
        double doumaxProgress=CalculateUtils.getDouble(maxProgress);

        if (douProgress<douminProgress){
            return false;
        }else if (douProgress>doumaxProgress){
            return false;
        }
        return true;
    }


    public String getStrProgress(String debt,String feed_price,String collateral,String minProgress){
        String strProgress=null;
        if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
             strProgress= NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(collateral),Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(feed_price))),2));
        }else {
            strProgress=minProgress;
        }
        return strProgress;
    }

    /**
     * 获取保证金
     * **/
    public String getEarnestMoney(String debt,String feed_price,String strProgress){
        if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
            return CalculateUtils.mul(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(strProgress))), Double.parseDouble(feed_price));
        }else {
            return "0.00";
        }
    }

    public void setCollateral(int intProgress){
       String progress=CalculateUtils.div(intProgress,100,2);
       strProgress=new BigDecimal(progress).add(new BigDecimal(minProgress)).setScale(2, BigDecimal.ROUND_HALF_DOWN).toPlainString();
       collateral2=getEarnestMoney(debt1,feed_price,strProgress);
       onUpdateListener.onUpadeChangeSeekbar(strProgress,collateral2);
    }


    OnUpdateListener onUpdateListener;
    public void setOnUpdateListener(OnUpdateListener onUpdateListener){
        this.onUpdateListener=onUpdateListener;
    }

    /**
     *
     * **/

    public int getIntProgress(String strProgress) {
        return Integer.parseInt(CalculateUtils.mulScaleHALF_DOWN(CalculateUtils.sub(Double.parseDouble(strProgress),Double.parseDouble(minProgress))+"","100",0));
    }



    public interface OnUpdateListener {

        void onUpadeChangeString(String strProgress, String collateral,String isFlay);
        void onUpadeChangeSeekbar(String strProgress, String collateral);

    }

    public static void setPricePoint(final EditText editText,final int ACCURACYNUM) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString().trim();
                //
                if (s.toString().startsWith(".")) {
                    editText.setText("");
                }

                int posDot = temp.indexOf(".");
                if (posDot>0&&temp.length() - posDot - 1 > ACCURACYNUM) {
                    s.delete(posDot + ACCURACYNUM + 1, posDot + ACCURACYNUM + 2);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }




}
