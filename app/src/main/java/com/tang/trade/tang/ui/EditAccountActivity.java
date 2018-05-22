package com.tang.trade.tang.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AcceptanceResponseModel;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class EditAccountActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_save)
    TextView tv_save;



    @BindView(R.id.tv_pay)
    TextView tv_pay;
    @BindView(R.id.tv_pay1)
    TextView tv_pay1;
    @BindView(R.id.ll_pay)
    LinearLayout ll_pay;

    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.ll_name)
    LinearLayout ll_name;
    @BindView(R.id.et_name)
    EditText et_name;


    @BindView(R.id.tv_bank)
    TextView tv_bank;
    @BindView(R.id.ll_bank)
    LinearLayout ll_bank;
    @BindView(R.id.et_bank)
    EditText et_bank;

    @BindView(R.id.tv_banknum)
    TextView tv_banknum;
    @BindView(R.id.ll_banknum)
    LinearLayout ll_banknum;
    @BindView(R.id.et_banknum)
    EditText et_banknum;

    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.ll_type)
    LinearLayout ll_type;
    @BindView(R.id.tv_type1)
    TextView tv_type1;

    private String type = "BankCard";
    private String payway = "CNY";
    private AccountResponseModel.DataBean dataBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_edit;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:

                if (type.equalsIgnoreCase("BankCard")){
                    if (TextUtils.isEmpty(et_name.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_NOTEFillName));
                        return;
                    }
                    if (TextUtils.isEmpty(et_bank.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_NOTEFillBanch));
                        return;
                    }
                    if (TextUtils.isEmpty(et_banknum.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_NOTEFillCard));
                        return;
                    }
                }else if (type.equalsIgnoreCase("Wallet")){
                    if (TextUtils.isEmpty(et_banknum.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_enter_address_or_card));
                        return;
                    }
                }else{
                    if (TextUtils.isEmpty(et_name.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_NOTEFillName));
                        return;
                    }

                    if (type.equalsIgnoreCase("Alipay")){
                        if (TextUtils.isEmpty(et_banknum.getText().toString())){
                            MyApp.showToast(getString(R.string.bds_NOTEFillAlipay));
                            return;
                        }
                    }else {
                        if (TextUtils.isEmpty(et_banknum.getText().toString())){
                            MyApp.showToast(getString(R.string.bds_NOTEFillWeChat));
                            return;
                        }
                    }

                }


                String payWay = "";
                if (TextUtils.isEmpty(tv_type.getText().toString())){
                    payWay = "";
                }else{
                    payWay = tv_type.getText().toString();
                }

                String bank = "";
                if (TextUtils.isEmpty(et_bank.getText().toString())){
                    bank = "";
                }else{
                    bank = et_bank.getText().toString();
                }


                String name = "";
                if (TextUtils.isEmpty(et_name.getText().toString())){
                    name = "";
                }else{
                    name = et_name.getText().toString();
                }

                TangApi.addAccount("tttttt", type,bank ,payWay , name, et_banknum.getText().toString(),
                        new JsonCallBack<ResponseModel>(ResponseModel.class) {
                            @Override
                            public void onSuccess(Response<ResponseModel> response) {
                                if (response.body().getStatus().equalsIgnoreCase("success")){
                                    MyApp.showToast("success");
                                    finish();
                                }else {
                                    MyApp.showToast("fail");
                                }
                            }

                            @Override
                            public void onError(Response<ResponseModel> response) {
                                super.onError(response);
                                MyApp.showToast("fail");
                            }
                        });
                break;


            case R.id.iv_back:
                finish();
                break;

        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        tv_save.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Intent intent =  getIntent();
        dataBean = (AccountResponseModel.DataBean)intent.getSerializableExtra("databean");

//        type = dataBean.getType();
//        payway = dataBean.getTypeWay();
//        et_bank.setText(dataBean.getBankname());
//        et_name.setText(dataBean.getName());
//        et_banknum.setText(dataBean.getAccount());
        tv_type.setText(type);
//        tv_pay.setText(payway);
        setCNYView();
    }




    private void setCNYView(){
        //卡号
        tv_banknum.setVisibility(View.VISIBLE);
        et_banknum.setVisibility(View.VISIBLE);
        ll_banknum.setVisibility(View.VISIBLE);
        tv_banknum.setText(getString(R.string.bds_CreditNum));

        if (payway.equalsIgnoreCase("BankCard")){
            tv_banknum.setText(getString(R.string.bds_CreditNum));

            //户名
            tv_name.setVisibility(View.VISIBLE);
            et_name.setVisibility(View.VISIBLE);
            ll_name.setVisibility(View.VISIBLE);

            //开户行
            tv_bank.setVisibility(View.VISIBLE);
            et_bank.setVisibility(View.VISIBLE);
            ll_bank.setVisibility(View.VISIBLE);

            ll_type.setVisibility(View.VISIBLE);
            tv_type.setVisibility(View.VISIBLE);
            tv_type1.setVisibility(View.VISIBLE);

            tv_pay.setText(getString(R.string.bds_Creditcard));
        }else if (payway.equalsIgnoreCase("Wallet")){

            //卡号
            tv_banknum.setVisibility(View.VISIBLE);
            et_banknum.setVisibility(View.VISIBLE);
            ll_banknum.setVisibility(View.VISIBLE);
            tv_banknum.setText(getString(R.string.bds_walletaddress));

            //户名
            tv_name.setVisibility(View.GONE);
            et_name.setVisibility(View.GONE);
            ll_name.setVisibility(View.GONE);

            //开户行
            tv_bank.setVisibility(View.GONE);
            et_bank.setVisibility(View.GONE);
            ll_bank.setVisibility(View.GONE);

            ll_type.setVisibility(View.VISIBLE);
            tv_type.setVisibility(View.VISIBLE);
            tv_type1.setVisibility(View.VISIBLE);
            tv_pay.setText(getString(R.string.bds_walletaddress));
        }else{
            tv_banknum.setText(getString(R.string.bds_account_name));

            //户名
            tv_name.setVisibility(View.VISIBLE);
            et_name.setVisibility(View.VISIBLE);
            ll_name.setVisibility(View.VISIBLE);

            //开户行
            tv_bank.setVisibility(View.GONE);
            et_bank.setVisibility(View.GONE);
            ll_bank.setVisibility(View.GONE);

            ll_type.setVisibility(View.GONE);
            tv_type.setVisibility(View.GONE);
            tv_type1.setVisibility(View.GONE);

            if (payway.equalsIgnoreCase("Alipay")){
                tv_pay.setText(getString(R.string.bds_Alipay));
            }else {
                tv_pay.setText(getString(R.string.bds_WeChat));
            }

        }
    }

}
