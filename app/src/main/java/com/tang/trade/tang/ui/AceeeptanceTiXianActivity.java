package com.tang.trade.tang.ui;

import android.os.Bundle;
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
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.FeeUtil;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;


public class AceeeptanceTiXianActivity extends BaseActivity {
   // private TextView tv_currency,tv_currency_diya,tv_banlance,tv_keti_balance,tv_order_zhongzhi,tv_order_thixian;
    private TextView tv_currency,tv_keti_balance;
    private TextView tv_fee;
    private Button btn_submit;
    private EditText editText;
    private ImageView ivBack;
    private MyProgressDialog myProgressDialog;
    private String fee = "0.00000";
    private String symbol;
    private String available = "0";//可提取余额


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_aceeeptance_ti_xian;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
            finish();

        }else if (view.getId()==R.id.btn_submit) {
            if (UtilOnclick.isFastClick()) {
                String str = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(str)) {

                    if (str.substring(0, 1).equalsIgnoreCase(".")) {
                        MyApp.showToast(getString(R.string.bds_quantity_err));
                        return;
                    }
                    if (Double.parseDouble(str) == 0) {
                        MyApp.showToast(getString(R.string.bds_note_quantity));
                        return;
                    }

                    if (Double.parseDouble(str) <= Double.parseDouble(available)) {
                        myProgressDialog.show();
                        acceptantWithdraw(SPUtils.getString(Const.USERNAME,""), symbol, str);
                    } else {
                        MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                    }
                } else {
                    MyApp.showToast(getString(R.string.bds_enter_amount));
                }


            }
        }

    }

    @Override
    public void initView() {
        tv_currency=findViewById(R.id.tv_currency);
        tv_keti_balance=findViewById(R.id.tv_keti_balance);
        tv_fee=findViewById(R.id.tv_fee);
        editText=findViewById(R.id.et_revalued_currency);
        btn_submit=findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        ivBack=findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);
    }

    @Override
    public void initData() {
        Bundle bundle = getIntent().getExtras();
        symbol=bundle.getString("symbol");
        available=bundle.getString("available");
        tv_currency.setText(symbol);
        if (TextUtils.isEmpty(available)){
            tv_keti_balance.setText("0.00000 "+symbol);
        }else {
            tv_keti_balance.setText(NumberUtils.formatNumber5(available) + " " + symbol);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                setFee(symbol);
            }
        }).start();

    }

    /**
     * 领取金额(承兑商提现)
     * @param account
     * @param symbol
     * @param money
     */
    private void acceptantWithdraw(String account,String symbol ,String money){
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("coinType",symbol);
        hashMap.put("money",money);

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

        AcceptorApi.acceptantHttp(hashMap,"acceptant_withdraw",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    MyApp.showToast(getString(R.string.bds_Withdraw_successfully));
                    myProgressDialog.dismiss();
                    AcceptanceManagerActivity.Listtype=1;
                    finish();
                }else {
                    MyApp.showToast(response.body().getMsg());
                    myProgressDialog.dismiss();

                }
            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);
                myProgressDialog.show();

            }

            @Override
            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                myProgressDialog.dismiss();

            }
        });
    }

    /**
     * 手续费
     * @param symbol
     */
    private void setFee(final String symbol){
        try {
            final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);

//            if (getIntent().getStringExtra("symbol").equalsIgnoreCase("BTC")){
//                fee = NumberUtils.formatNumber8(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//            }else if(getIntent().getStringExtra("symbol").equalsIgnoreCase("BDS")){
//                fee = NumberUtils.formatNumber(gbdsTransferFee+"");
//            }else {
//                fee = NumberUtils.formatNumber(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//            }
            fee= FeeUtil.getFee(symbol,rate);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_fee.setText(getText(R.string.bds_fee_margin)+fee +" "+symbol);
//
//                    if (!symbol.equalsIgnoreCase("BDS")){
//                        if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH") ||symbol.equalsIgnoreCase("LTC")){
//                            fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                            BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00000001);
//                            String rate1 = NumberUtils.formatNumber8(db.toPlainString());
//                            tv_fee.setText(getText(R.string.bds_fee_margin)+rate1 +" "+symbol);
//                        }else {
//                            fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                            BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00001);
//                            String rate1 = NumberUtils.formatNumber(db.toPlainString());
//                            tv_fee.setText(getText(R.string.bds_fee_margin)+rate1 +" "+symbol);
//                        }
//                    }else {
//                        tv_fee.setText(getText(R.string.bds_fee_margin)+fee +" "+symbol);
//                    }

                }
            });

        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }


}
