package com.tang.trade.tang.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.IsResponseModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.FeeUtil;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;


public class AcceptanceDyorCzorShActivity extends BaseActivity {
    public static final String type_diya="2";
    public static final String type_chizhi="1";
    private String type_select="1";
    private TextView tvTitle,tv_revalued_quate,tv_quate_number,tv_mindiya,tv_fee,tv_node;
    private EditText et_revalued_currency;
    private Button btn_summit;
    private ImageView ivBack;
    private MyProgressDialog myProgressDialog;
    private String fee = "0.00000";

    private String finalSAssetAmount = "0.00000";
    private String symbol;
    private String payAccount;
    private String xuDiyaQuate;
    private String mortgage;//保证金
    private String minMortgage;//最小抵押额
    private String blance;//余额
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){

            }

        }
    };
    Thread thread=new Thread(new Runnable() {
        @Override
        public void run() {
            mortgage=  getBalance(symbol,payAccount);

            if (handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Double.parseDouble(mortgage)>Double.parseDouble(minMortgage)){
                            tv_mindiya.setVisibility(View.GONE);
                        }else {
                            xuDiyaQuate = NumberUtils.formatNumber5(CalculateUtils.sub(Double.parseDouble(minMortgage), Double.parseDouble(mortgage)) + "");
                            tv_mindiya.setText(getString(R.string.bds_mortgage)+xuDiyaQuate+symbol);

                        }
                    }
                });
            }

        }
    });

    Thread thread2=new Thread(new Runnable() {
        @Override
        public void run() {
            finalSAssetAmount=getBalance(symbol, SPUtils.getString(Const.USERNAME,""));
            if (handler!=null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_quate_number.setText(getString(R.string.bds_balance) + ":" + NumberUtils.formatNumber5(finalSAssetAmount) + " " + symbol);

                    }
                });
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acceptance_dyor_czor_sh;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_summit:
                if (UtilOnclick.isFastClick()) {
                    btn_summit.setClickable(false);
                    String amount = et_revalued_currency.getText().toString().trim();
                    if (!TextUtils.isEmpty(amount)) {
                        double total = CalculateUtils.add(Double.parseDouble(amount), Double.parseDouble(fee));
                        if (Double.parseDouble(finalSAssetAmount) >= total) {
                            myProgressDialog.show();
                            if (type_select.equals(type_chizhi)) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (et_revalued_currency.getText().toString().substring(0, 1).equalsIgnoreCase(".")) {
                                            MyApp.showToast(getString(R.string.bds_quantity_err));
                                            myProgressDialog.dismiss();
                                            btn_summit.setClickable(true);
                                            return;
                                        }
                                        if (Double.parseDouble(et_revalued_currency.getText().toString()) == 0) {
                                            MyApp.showToast(getString(R.string.bds_note_quantity));
                                            myProgressDialog.dismiss();
                                            btn_summit.setClickable(true);
                                            return;
                                        }
                                        acceptantRecharge();
                                    }
                                }).start();

                            } else if (type_select.equals(type_diya)) {
                                if (et_revalued_currency.getText().toString().substring(0, 1).equalsIgnoreCase(".")) {
                                    MyApp.showToast(getString(R.string.bds_quantity_err));
                                    myProgressDialog.dismiss();
                                    btn_summit.setClickable(true);
                                    return;
                                }
                                if (Double.parseDouble(et_revalued_currency.getText().toString()) == 0) {
                                    MyApp.showToast(getString(R.string.bds_note_quantity));
                                    myProgressDialog.dismiss();
                                    btn_summit.setClickable(true);
                                    return;
                                }
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        acceptantMortgage();
                                    }
                                },100);

                            }
                        } else {
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            myProgressDialog.dismiss();
                            btn_summit.setClickable(true);
                        }
                    } else {
                        MyApp.showToast(getString(R.string.bds_enter_amount));
                        myProgressDialog.dismiss();
                        btn_summit.setClickable(true);
                    }
                }

                break;
            case R.id.ivBack:
                finish();
                break;
        }


    }

    @Override
    public void initView() {
        myProgressDialog=MyProgressDialog.getInstance(this);
        type_select=getIntent().getStringExtra("type");
        symbol=getIntent().getStringExtra("symbol");
        payAccount=getIntent().getStringExtra("payAccount");

        if (TextUtils.isEmpty(type_select)){
            type_select=type_chizhi;
        }
        tvTitle=(TextView) findViewById(R.id.tvTitle);
        tv_revalued_quate=(TextView) findViewById(R.id.tv_revalued_quate);
        tv_quate_number=(TextView) findViewById(R.id.tv_quate_number);
        tv_mindiya=(TextView) findViewById(R.id.tv_mindiya);
        tv_fee=(TextView) findViewById(R.id.tv_fee);
        tv_node=(TextView) findViewById(R.id.tv_node);

        et_revalued_currency= (EditText) findViewById(R.id.et_revalued_currency);
        btn_summit =(Button)findViewById(R.id.btn_summit);
        ivBack=(ImageView) findViewById(R.id.ivBack);
        btn_summit.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        setType(type_select);



    }
    private void setType(String type){

        if (type.equals(type_chizhi)){
            tvTitle.setText(getText(R.string.bds_deposit));
            tv_revalued_quate.setText(getText(R.string.bds_revalued_amount));
            tv_mindiya.setVisibility(View.GONE);

            thread2.start();
        }else if (type.equals(type_diya)){
            tvTitle.setText(getText(R.string.bds_margin2));

            tv_revalued_quate.setText(getText(R.string.bds_margin_amount));
            tv_mindiya.setText(getString(R.string.bds_mortgage)+" 0.00000 BDS");
            tv_node.setText(getText(R.string.bds_diya_note));
            symbol="BDS";
            thread2.start();
            setMinDiyaQuate();
        }
    }

    @Override
    public void initData() {

    }


    /**
     * 抵押
     * */
    private void acceptantMortgage(){

        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(getIntent().getStringExtra("symbol"));
            if (object != null) {
                signedTransaction = BitsharesWalletWraper.getInstance().transfer(SPUtils.getString(Const.USERNAME,"")
                        , getIntent().getStringExtra("payAccount")
                        , et_revalued_currency.getText().toString().trim()
                        , "BDS","","","");

            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApp.showToast(getString(R.string.network));
                        myProgressDialog.dismiss();
                        btn_summit.setClickable(true);
                    }
                });
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            signedTransaction = null;
        }
        if (signedTransaction != null){
            HashMap<String, String> hashMap = new HashMap<String, String>();
            if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
                MyApp.showToast(getString(R.string.encryption_failed));
                btn_summit.setClickable(true);
                myProgressDialog.dismiss();
                return;
            }
            String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
            if (TextUtils.isEmpty(signMessage)){
                MyApp.showToast(getString(R.string.encryption_failed));
                btn_summit.setClickable(true);
                myProgressDialog.dismiss();
                return;
            }
            hashMap.put("encmsg",signMessage);
            hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
            AcceptorApi.acceptantHttp(hashMap,"acceptant_add_bond",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
                @Override
                public void onSuccess(Response<ResponseModelBase> response) {
                    if (response.body().getStatus().equalsIgnoreCase("success")){
                        MyApp.showToast(getString(R.string.bds_Submited));
                        myProgressDialog.dismiss();
                        btn_summit.setClickable(true);
                        AcceptanceManagerActivity.Listtype=2;
                        finish();
                    }else {
                        MyApp.showToast(response.body().getMsg());
                        myProgressDialog.dismiss();
                        btn_summit.setClickable(true);
                    }
                }

                @Override
                public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                    super.onStart(request);

                }

                @Override
                public void onError(Response<ResponseModelBase> response) {
                    super.onError(response);
                    MyApp.showToast(getString(R.string.network));
                    myProgressDialog.dismiss();
                    btn_summit.setClickable(true);
                }
            });

        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.showToast(getString(R.string.network));
                    myProgressDialog.dismiss();
                    btn_summit.setClickable(true);
                }
            });

        }

    }



    /**
     * 承兑商充值
     * */
    private void acceptantRecharge(){
        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(getIntent().getStringExtra("symbol"));
            if (object != null) {
                if (!getIntent().getStringExtra("symbol").equalsIgnoreCase("BDS")){
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(SPUtils.getString(Const.USERNAME,"")
                            , getIntent().getStringExtra("payAccount")
                            , et_revalued_currency.getText().toString().trim()
                            , getIntent().getStringExtra("symbol"),"",fee,object.id.toString());
                }else {
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(SPUtils.getString(Const.USERNAME,"")
                            , getIntent().getStringExtra("payAccount")
                            , et_revalued_currency.getText().toString().trim()
                            , "BDS","","","");
                }

            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApp.showToast(getString(R.string.network));
                        btn_summit.setClickable(true);
                    }
                });
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            signedTransaction = null;
        }
        if (signedTransaction != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.showToast(getString(R.string.bds_deposit_successfully));
                    AcceptanceManagerActivity.Listtype=1;
                    finish();
                }
            });


        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.showToast(getString(R.string.network));
                }
            });

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myProgressDialog.dismiss();
                btn_summit.setClickable(true);
            }
        });

    }


    private String getBalance(final String symbol, final String bdsAccount) {
        String finalSAssetAmount = "0.00000";
        try {
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
                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }
            //查询资产列表
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (objAssets == null) {
//                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }
            //fee
            final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);
            fee= FeeUtil.getFee(getIntent().getStringExtra("symbol"),rate);

//            if (getIntent().getStringExtra("symbol").equalsIgnoreCase("BTC")){
//                fee = NumberUtils.formatNumber8(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//            }else if(getIntent().getStringExtra("symbol").equalsIgnoreCase("BDS")){
//                fee = NumberUtils.formatNumber(gbdsTransferFee+"");
//            }else {
//                fee = NumberUtils.formatNumber(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bdsAccount.equals(SPUtils.getString(Const.USERNAME,""))) {

//                        if (!symbol.equalsIgnoreCase("BDS")){
//                            if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH") ||symbol.equalsIgnoreCase("LTC")){
//                                fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                                BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00000001);
//                                String rate1 = NumberUtils.formatNumber8(db.toPlainString());
//                                tv_fee.setText(getText(R.string.bds_fee_margin)+rate1 +" "+symbol);
//                            }else {
//                                fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                                BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00001);
//                                String rate1 = NumberUtils.formatNumber(db.toPlainString());
//                                tv_fee.setText(getText(R.string.bds_fee_margin)+rate1 +" "+symbol);
//                            }
//                        }else {
//                            tv_fee.setText(getText(R.string.bds_fee_margin)+fee +" "+symbol);
//                        }
                        tv_fee.setText(getText(R.string.bds_fee_margin)+fee +" "+symbol);

                    }
                }
            });


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
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return finalSAssetAmount;
        }
        return finalSAssetAmount;
    }


    private void setMinDiyaQuate(){
        HashMap<String, String> hashMap = new HashMap<String, String>();
        AcceptorApi.acceptantHttp(hashMap,"get_acceptant_global_config",new JsonCallBack<IsResponseModel>(IsResponseModel.class) {
            @Override
            public void onSuccess(Response<IsResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    minMortgage = response.body().getData().get(0).getMortgageLowerLimit();
                    thread.start();
                }
            }

            @Override
            public void onStart(Request<IsResponseModel, ? extends Request> request) {
                super.onStart(request);
            }
            @Override
            public void onError(Response<IsResponseModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler=null;
    }
}
