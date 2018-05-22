package com.tang.trade.tang.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
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
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class CashInAddressByCardActivity extends BaseActivity {

    private AccountResponseModel.DataBean dataBean=null;
    private String cashInCurrency="";
    private final String CNY="CNY";
    private final String USD="USD";

    @BindView(R.id.et_real_name)
    EditText et_real_name;

    @BindView(R.id.et_bank_name)
    EditText et_bank_name;

    @BindView(R.id.et_credit_num)
    EditText et_credit_num;

    @BindView(R.id.tv_submit)
    TextView tv_submit;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    MyProgressDialog myProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cash_in_address_by_card;

    }


    public void initView() {
        tv_submit.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);
        et_real_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        et_bank_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});


    }

    @Override
    public void initData() {
        Intent intent =getIntent();
        cashInCurrency=intent.getStringExtra("cashInCurrency");
            if (cashInCurrency.equals(CNY)){

            }else if(cashInCurrency.equals(USD)){

            }
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.tv_submit){
            String name=et_real_name.getText().toString().trim();
            String back=et_bank_name.getText().toString().trim();
            String credit=et_credit_num.getText().toString().trim();
            if (TextUtils.isEmpty(name)){
                MyApp.showToast(getString(R.string.bds_null_name_apliay));

            }else if (TextUtils.isEmpty(back)){
                MyApp.showToast(getString(R.string.bds_bank_name_null));

            }else if (TextUtils.isEmpty(credit)){
                MyApp.showToast(getString(R.string.bds_bank_id_number));

            }else {
                addAccount(credit,name,back);
            }

        }else if (R.id.iv_back==view.getId()){
            finish();
        }
    }

    private void addAccount(String payAccountID,String name,String bank){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME,""));
        hashMap.put("symbol",cashInCurrency);
        hashMap.put("typeCode","CARD");
        hashMap.put("payAccountID",payAccountID);
        hashMap.put("name",name);
        hashMap.put("bank",bank);

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"add_pay_style",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    MyApp.showToast(getString(R.string.bds_add_successfully));
                    finish();

                }else {
                    MyApp.showToast(response.body().getMsg());

                }
                myProgressDialog.dismiss();

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

            }
        });
    }
}
