package com.tang.trade.tang.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AcceptorOpenModel;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.acceptormodel.IsResponseModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.gbdsTransferFee;

public class AcceptantOpenActivity extends BaseActivity {
    private TextView tvTitle,tv_account_balance,tv_mindiya_quate,tv_fee,tv_currency_select,tv_diya_quata;
    private LinearLayout line_currency_select;
    private EditText et_revalued_currency;
    private Button btn_submit;
    private ImageView ivBack;
    private String currency_select;
    private MyProgressDialog myProgressDialog;

    private int position = 0;
    private String[] accounts=new String[]{"CNY","USD"};

    private String minMortgage = "0.00000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acceptant_open;
    }

    @Override
    public void onClick(View view) {
     if (view.getId()==R.id.btn_submit){
//        double revalued_currency = 0;
//         if (!TextUtils.isEmpty(et_revalued_currency.getText().toString())){
//             revalued_currency = Double.parseDouble(et_revalued_currency.getText().toString());
//         }else {
//             MyApp.showToast(getString(R.string.bds_enter_amount));
//         }
//         if (revalued_currency<Double.parseDouble(minMortgage)){
//             MyApp.showToast("输入金额太小");
//             return;
//         }
         acceptantSetInfo();
        }else if(view.getId()==R.id.ivBack){
         finish();
        }else if (view.getId()==R.id.line_currency_select){
         showSingleAccountDialog(accounts);

        }


    }

    @Override
    public void initView() {
        tvTitle=(TextView) findViewById(R.id.tvTitle);
        tv_account_balance=(TextView) findViewById(R.id.tv_account_balance);
        tv_mindiya_quate=(TextView) findViewById(R.id.tv_mindiya_quate);
        tv_fee=(TextView) findViewById(R.id.tv_fee);
        tv_currency_select=(TextView) findViewById(R.id.tv_currency_select);
        line_currency_select=(LinearLayout) findViewById(R.id.line_currency_select);
        et_revalued_currency=(EditText) findViewById(R.id.et_revalued_currency);
        btn_submit=(Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        ivBack=(ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        line_currency_select.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        currency_select=accounts[0];
        myProgressDialog=MyProgressDialog.getInstance(this);

    }

    @Override
    public void initData() {
        tv_fee.setText(getText(R.string.bds_fee) + ": " + NumberUtils.formatNumber5(gbdsTransferFee + "") + " BDS");
        tv_mindiya_quate.setText(getText(R.string.bds_min_mortgage)+" "+minMortgage+" BDS");
        tv_currency_select.setText(accounts[position]);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getBalance(accounts[0], SPUtils.getString(Const.USERNAME,""));
            }
        }).start();

        HashMap<String, String> hashMap = new HashMap<String, String>();
        AcceptorApi.acceptantHttp(hashMap,"get_acceptant_config",new JsonCallBack<IsResponseModel>(IsResponseModel.class) {
            @Override
            public void onSuccess(Response<IsResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    minMortgage = NumberUtils.formatNumber5(response.body().getData().get(0).getMortgageLowerLimit());
                    tv_mindiya_quate.setText(getText(R.string.bds_min_mortgage)+" "+minMortgage+" BDS");
                }
            }

            @Override
            public void onStart(Request<IsResponseModel, ? extends Request> request) {
                super.onStart(request);
            }
            @Override
            public void onError(Response<IsResponseModel> response) {
                super.onError(response);

            }
        });


        HashMap<String, String> hashMap1 = new HashMap<String, String>();
        hashMap1.put("acceptantBdsAccount", SPUtils.getString(Const.USERNAME,""));
        hashMap1.put("selfacc","1");
        AcceptorApi.acceptantHttp(hashMap1,"get_acceptant_info", new JsonCallBack<AcceptotXiangqingModel>(AcceptotXiangqingModel.class) {
            @Override
            public void onSuccess(Response<AcceptotXiangqingModel> response) {
                if (response.body().getStatus().equals("success")) {
                    if (!TextUtils.isEmpty(response.body().getData().get(0).getBsdcopubkey())) {
                        MyApp.BDS_CO_PUBLICKEY = response.body().getData().get(0).getBsdcopubkey();
                    }
                }
            }

            @Override
            public void onStart(Request<AcceptotXiangqingModel, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<AcceptotXiangqingModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
            }

        });
    }


    private void showSingleAccountDialog(final String[] items){
        final AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle(getString(R.string.bds_select_currency));
        singleChoiceDialog.setSingleChoiceItems(items, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                        tv_currency_select.setText(items[position]);
                        currency_select=accounts[position];
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getBalance(items[position],SPUtils.getString(Const.USERNAME,""));
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });

        singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel),null);
        singleChoiceDialog.show();
    }


    private void acceptantSetInfo(){
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("acceptantBdsAccount",SPUtils.getString(Const.USERNAME,""));
        hashMap.put("symbol",tv_currency_select.getText().toString());

        if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"set_acceptant_info",new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
            @Override
            public void onSuccess(Response<AcceptorOpenModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
//                    acceptantRecharge();
                    startActivity(new Intent(AcceptantOpenActivity.this, AcceptanceManagerActivity.class));
                    myProgressDialog.dismiss();
                    finish();
                }else {
                    MyApp.showToast(response.body().getMsg());
                    myProgressDialog.dismiss();
                }
            }

            @Override
            public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                super.onStart(request);
                myProgressDialog.show();

            }

            @Override
            public void onError(Response<AcceptorOpenModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                myProgressDialog.dismiss();
            }
        });
    }

    private void getBalance(final String symbol,String bdsAccount){
        String finalSAssetAmount = "0.00000";
        try {
            String sAssetAmount;
            boolean findAssetInAccount = false;

            //获取所有资产列表
            account_object object = BitsharesWalletWraper.getInstance().get_account_object(bdsAccount);
            if (object == null) {
//                MyApp.showToast(getString(R.string.network));

            }
            //查询账户id
            object_id<account_object> loginAccountId = object.id;

            //获取账户余额列表
            List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
            if (accountAsset == null) {
                MyApp.showToast(getString(R.string.network));
            }
            //查询资产列表
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (objAssets == null) {
//                MyApp.showToast(getString(R.string.network));
            }


            //查询基础货币BDS,和所有智能资产

            for (asset_object objAsset : objAssets){
                if (objAsset.symbol.equalsIgnoreCase(symbol)){
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

        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        final String finalSAssetAmount1 = finalSAssetAmount;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_account_balance.setText(getText(R.string.bds_balance)+": "+ finalSAssetAmount1 +" "+symbol);

            }
        });
    }


//    /**
//     * 承兑商充值
//     * */
//    private void acceptantRecharge(){
//        asset_object object = null;
//        signed_transaction signedTransaction = null;
//        try {
//            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(getIntent().getStringExtra("symbol"));
//            if (object != null) {
//                signedTransaction = BitsharesWalletWraper.getInstance().transfer(SPUtils.getString(Const.USERNAME,"")
//                        , getIntent().getStringExtra("payAccount")
//                        , et_revalued_currency.getText().toString().trim()
//                        , "BDS","","","");
//
//            }else {
//                showDialog();
//            }
//
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            signedTransaction = null;
//        }
//        if (signedTransaction != null){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    MyApp.showToast(getString(R.string.outsuccess));
//                    myProgressDialog.dismiss();
//                }
//            });
//            startActivity(new Intent(AcceptantOpenActivity.this, AcceptanceManagerActivity.class));
//            finish();
//
//        }else{
//            showDialog();
//
//        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                myProgressDialog.dismiss();
//            }
//        });
//
//    }


    /**
     * information
     */
    private void showDialog() {
        myProgressDialog.dismiss();
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AcceptantOpenActivity.this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(R.string.bds_haohou_chongshi);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(AcceptantOpenActivity.this, AcceptanceManagerActivity.class));
                finish();
            }
        });
        builder.show();
    }

}
