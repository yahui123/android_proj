package com.tang.trade.tang.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.tang.trade.tang.net.acceptormodel.CenterWlletModel;
import com.tang.trade.tang.net.acceptormodel.DeleteOrderModel;
import com.tang.trade.tang.net.acceptormodel.TianXianModel;
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
import com.tang.trade.tang.utils.TLog;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

import static com.tang.trade.tang.ui.CaptureActivity.EXTRA_RESULT;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class CashOutActivity extends BaseActivity {

    private int REQURE_CODE_SCAN=101;

    @BindView(R.id.et_cash_out_money)
    EditText et_cash_out_money;

    @BindView(R.id.tv_wallet_address)
    EditText tv_wallet_address;

    @BindView(R.id.iv_scan_address)
    ImageView iv_scan_address;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_account_balance)
    TextView tv_account_balance;


    @BindView(R.id.tv_account_balance_currency)
    TextView tv_account_balance_currency;

    @BindView(R.id.tv_cash_out_money_currency)
    TextView tv_cash_out_money_currency;

    @BindView(R.id.iv_record)
    ImageView iv_record;
    private static int ACCURACYNUM = 5;

    @BindView(R.id.tv_cash_out)
    Button tv_cash_out;

    @BindView(R.id.tv_rate)
    TextView tv_rate;

    private String strURL="";
    private CenterWlletModel.DataBean dataBean;
    private String finalBalance="0.0000";

    private String bdsAccount="";
    private Handler handler =new Handler(){};

    MyProgressDialog myProgressDialog;

    int i =0;


    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            setBalance(dataBean.getType(),bdsAccount);
        }
    };
    private String myFee="0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cash_out;
    }


    public void initView() {


        iv_scan_address.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        tv_cash_out.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);
        iv_record.setOnClickListener(this);
        et_cash_out_money.addTextChangedListener(watcher);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  //打开相机权限
                    != PackageManager.PERMISSION_GRANTED ) {

                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        2);

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    if (i==0) {
                        MyApp.showToast(R.string.permissionsError);
                        i++;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void initData() {

        bdsAccount= SPUtils.getString(Const.USERNAME,"");

        if (getIntent().getSerializableExtra("dataBean") != null) {
            dataBean = (CenterWlletModel.DataBean) getIntent().getSerializableExtra("dataBean");
            tv_account_balance_currency.setText(dataBean.getType());
            tv_cash_out_money_currency.setText(dataBean.getType());

                if (dataBean.getType().toString().equalsIgnoreCase("BTC") || dataBean.getType().toString().equalsIgnoreCase("ETH") ||dataBean.getType().toString().toString().equalsIgnoreCase("LTC")){
                    tv_account_balance.setText("0.00000000");
                    ACCURACYNUM=8;
                    tv_rate.setText(getString(R.string.bds_fee_cashout)+": 0.00000000"+dataBean.getType());

                }else {
                    tv_account_balance.setText("0.00000");
                    tv_rate.setText(getString(R.string.bds_fee_cashout)+": 0.00000"+dataBean.getType());
                    ACCURACYNUM=5;
                }

            tv_rate.setText(getString(R.string.bds_fee_cashout)+": "+CalculateUtils.div(Double.parseDouble(dataBean.getRate()),1,ACCURACYNUM)+dataBean.getType());



          new Thread(runnable).start();
        }



    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.iv_scan_address){
            Intent i = new Intent(this, CaptureActivity.class);
            startActivityForResult(i, REQURE_CODE_SCAN);
            
        }else if (view.getId()==R.id.iv_back){
            finish();
        }else if (view.getId()==R.id.tv_cash_out){
           if( UtilOnclick.isFastClick()){
               tv_cash_out.setEnabled(false);
               String amount=et_cash_out_money.getText().toString().trim();
               strURL=tv_wallet_address.getText().toString().trim();

               if (TextUtils.isEmpty(amount)){
                   MyApp.showToast(getString(R.string.bds_quantity_must_than_0));
                   myProgressDialog.dismiss();
                   tv_cash_out.setEnabled(true);
                   return;
               }


               if (amount.substring(0,1).equalsIgnoreCase(".")){
                   MyApp.showToast(getString(R.string.bds_quantity_err));
                   myProgressDialog.dismiss();
                   tv_cash_out.setEnabled(true);
                   return;
               }

               if (!TextUtils.isEmpty(dataBean.getMinAmount())){
                   double minAmount=Double.parseDouble(dataBean.getMinAmount());
                   if (minAmount>Double.parseDouble(amount)){
                       MyApp.showToast(getString(R.string.bds_amount_minAmount)+" "+dataBean.getMinAmount());
                       myProgressDialog.dismiss();
                       tv_cash_out.setEnabled(true);
                       return;
                   }
               }
               if (!TextUtils.isEmpty(dataBean.getRate())){
                   double rate=Double.parseDouble(dataBean.getRate());
                   if (rate+Double.parseDouble(amount)>Double.parseDouble(finalBalance)){
                       MyApp.showToast(getString(R.string.bds_banlance_buzu));
                       myProgressDialog.dismiss();
                       tv_cash_out.setEnabled(true);
                       return;
                   }
               }


               if (TextUtils.isEmpty(strURL)){
                   MyApp.showToast(getString(R.string.bds_input_address));
                   tv_cash_out.setEnabled(true);

               }else {
                   double str_et=Double.parseDouble(amount);//输入金额
                   double str_fee=Double.parseDouble(myFee);//fee
                   double str_b=Double.parseDouble(finalBalance);//账户余额
                   if (str_et+str_fee>str_b){
                       tv_cash_out.setEnabled(true);
                       MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                   }else {

                       showPopupConfirm(amount,strURL);

                   }
               }
           }

        }else if (view.getId()==R.id.iv_record){
            Intent intent=new Intent(this,NumberOrderActivity.class);
            intent.putExtra("type","CO");
            startActivity(intent);


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQURE_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            TLog.log("qr success");
            if (data != null) {
                strURL= data.getStringExtra(EXTRA_RESULT);
                tv_wallet_address.setText(strURL);

            }
        }
    }


    private void setBalance(final String symbol, final String account){
        String balance = "0.00000";
        try {
            if (!account.equals("")) {
                String sAssetAmount;
                boolean findAssetInAccount = false;

                //获取所有资产列表
                account_object object = BitsharesWalletWraper.getInstance().get_account_object(account);
                if (object == null) {
                    balance = "0.00000";
                    return;
                }
                //查询账户id
                object_id<account_object> loginAccountId = object.id;

                //获取账户余额列表
                List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                if (accountAsset == null) {
                    balance = "0.00000";
                    return;
                }
                //查询资产列表
                List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                if (objAssets == null) {
                    balance = "0.00000";
                    return;
                }
                //fee
                final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);
                myFee= FeeUtil.getFee(symbol,rate);

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!symbol.equalsIgnoreCase("BDS")){
//                            if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH") || symbol.equalsIgnoreCase("LTC")){
//                                myFee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                            }else {
//                                myFee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                            }
//                        }else {
//                            myFee= NumberUtils.formatNumber(gbdsTransferFee+"");
//                        }

//                    }
//                });

                //查询基础货币BDS,和所有智能资产

                for (asset_object objAsset : objAssets){
                    if (objAsset.symbol.equalsIgnoreCase(symbol)){
                        if (accountAsset.size() > 0) {
                            for (int j = 0; j < accountAsset.size(); j++) {
                                asset account_asset = accountAsset.get(j);
                                String sAccount_sid = account_asset.asset_id.toString();
                                if (sAccount_sid.equals(objAsset.id.toString())) {
                                    //计算账户余额
                                    asset_object.asset_object_legible myasset = objAsset.get_legible_asset_object(account_asset.amount);
                                    sAssetAmount = myasset.count;
                                    balance = sAssetAmount;
                                    ACCURACYNUM=objAsset.precision;
                                }
                            }
                        }

                    }
                }

            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            balance = "0.00000";
        }

        finalBalance = balance;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (symbol.equals("BDS")){
                    tv_account_balance.setText(CalculateUtils.div(Double.parseDouble(finalBalance),1,ACCURACYNUM));
                }else {
                    tv_account_balance.setText(CalculateUtils.div(Double.parseDouble(finalBalance),1,ACCURACYNUM));
                }
                myProgressDialog.dismiss();

            }


        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

    }



        /*
    *创建数字货币充值订单
    * member_create_digital_order
    * orderTypeCode String 是 订单类型代码 DI：充值，DO：提现
    * memberBdsAccount String 是 会员的无界账号
    * symbol String 是 币种
    * payTypeCode String 是 付款类型代码
    * payStyleID String 是 付款方式ID
    * amount

    * */


    private void createOrder(final String amount,String address){
        myProgressDialog.show();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("memberBdsAccount",SPUtils.getString(Const.USERNAME,""));
        hashMap.put("orderTypeCode","DO");
        hashMap.put("symbol",dataBean.getType());
        hashMap.put("amount",amount);
        hashMap.put("address",address);

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            tv_cash_out.setEnabled(true);
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"member_create_digital_order",new JsonCallBack<TianXianModel>(TianXianModel.class) {
            @Override
            public void onSuccess(Response<TianXianModel> response) {
                if (response.body().getStatus().equals("success")){
                    String token=response.body().getData().get(0).getToken();
                    String id=response.body().getData().get(0).getId();
                    acceptantRecharge(dataBean.getBdsAccount(),dataBean.getType(),amount,myFee,token,id);
                }else {
                    MyApp.showToast(response.body().getMsg());
                    myProgressDialog.dismiss();

                }

                tv_cash_out.setEnabled(true);

            }

            @Override
            public void onStart(Request<TianXianModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<TianXianModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                myProgressDialog.dismiss();
                tv_cash_out.setEnabled(true);


            }
        });
    }


    /**
     * 承兑商提现
     * */
    private void acceptantRecharge(String accepatantAccount ,String symbol ,String amount,String fee,String token,final String id){
        String payAccount=SPUtils.getString(Const.USERNAME,"");
        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(symbol);
            if (object != null) {
                signedTransaction = BitsharesWalletWraper.getInstance().transfer(payAccount
                        , accepatantAccount
                        ,amount
                        ,symbol
                        ,token
                        ,fee+""//
                        ,object.id.toString());

            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApp.showToast(getString(R.string.network));
                        myProgressDialog.dismiss();
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
                    MyApp.showToast(getString(R.string.bds_OrderSubmitted));
                    myProgressDialog.dismiss();
                }
            });


        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //MyApp.showToast(getString(R.string.bds_Withdraw_fail));
                    deleteOrder(id);

                }
            });

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_cash_out.setEnabled(true);
                myProgressDialog.dismiss();
                finish();

            }
        });

    }


    /***
     *取消订单
     * acc_exit_fdig
     * id
     * **/
    private void deleteOrder(String id){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("id",id);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));


        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            tv_cash_out.setEnabled(true);
            return;
        }
        hashMap.put("encmsg",signMessage);


        AcceptorApi.acceptantHttp(hashMap,"acc_exit_dig", new JsonCallBack<DeleteOrderModel>(DeleteOrderModel.class) {
            @Override
            public void onSuccess(Response<DeleteOrderModel> response) {
               String str=response.body().getStatus();

            }

                    @Override
                    public void onError(Response<DeleteOrderModel> response) {
                        super.onError(response);
                    }
                }
        );
        MyApp.showToast(getString(R.string.bds_transfer_fail_next));
        myProgressDialog.dismiss();
        tv_cash_out.setEnabled(true);

    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            String temp = s.toString().trim();
            //
            if (s.toString().startsWith(".")) {
                et_cash_out_money.setText("");
            }

            int posDot = temp.indexOf(".");//.的下标
            if (posDot>0&&temp.length() - posDot - 1 > ACCURACYNUM) {
                s.delete(posDot + ACCURACYNUM + 1, posDot + ACCURACYNUM + 2);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    };


    private void showPopupConfirm(final String amount,final String strURL) {

        final android.app.AlertDialog customizeDialog = new android.app.AlertDialog.Builder(this, android.app.AlertDialog.THEME_HOLO_LIGHT).create();
        customizeDialog.setInverseBackgroundForced(false);
        customizeDialog.show();
        Window window = customizeDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.confirm_xn_deposit_node);

        customizeDialog.setCanceledOnTouchOutside(false);

        customizeDialog.setCancelable(false);

        TextView tv_submit = (TextView) window.findViewById(R.id.tv_submit);
        TextView tv_info = (TextView) window.findViewById(R.id.tv_info);
        tv_info.setText(getText(R.string.bds_deposit_amount)+amount+ dataBean.getType());

        TextView tv_info2 = (TextView) window.findViewById(R.id.tv_info2);
        tv_info2.setText(getString(R.string.bds_fee_transef)+CalculateUtils.div(Double.parseDouble(myFee),1,ACCURACYNUM)+dataBean.getType());

        ImageView iv_cancel=window.findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeDialog.dismiss();
                tv_cash_out.setEnabled(true);
            }
        });
        customizeDialog.setCanceledOnTouchOutside(false);

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createOrder(amount,strURL);
                customizeDialog.dismiss();


            }
        });


    }


}
