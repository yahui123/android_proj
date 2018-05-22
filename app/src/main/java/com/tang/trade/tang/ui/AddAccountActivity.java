package com.tang.trade.tang.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AddAccountActivity extends BaseActivity {

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

    private String [] pay_ways;

    private String [] cardStr ;
    private String [] addressStr ;

    private String type = "BankCard";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_add;
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

            case R.id.ll_pay:
                showSingleChoicePayDialog(pay_ways);
                break;

            case R.id.ll_type:
                if (tv_pay.getText().toString().equalsIgnoreCase(getString(R.string.bds_Creditcard))){
                    showSingleChoiceDialog(cardStr);
                }else if (tv_pay.getText().toString().equalsIgnoreCase(getString(R.string.bds_walletaddress))){
                    showSingleChoiceDialog(addressStr);
                }

                break;

            case R.id.iv_back:
                finish();
                break;

        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        ll_pay.setOnClickListener(this);
        ll_type.setOnClickListener(this);
        tv_save.setOnClickListener(this);
    }

    @Override
    public void initData() {
        pay_ways = new String[4];
        pay_ways[0] = getString(R.string.bds_Creditcard);
        pay_ways[1] = getString(R.string.bds_WeChat);
        pay_ways[2] = getString(R.string.bds_Alipay);
        pay_ways[3] = getString(R.string.bds_walletaddress);

        tv_pay.setText(pay_ways[0]);

        cardStr = getCardType();
        addressStr = getAddressType();

        setCNYView();
    }



    /**
     * 选择交易类型
     */
    private int position = 0;
    private void showSingleChoiceDialog(final String[] items){
        final
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle(getString(R.string.checked));
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                        tv_type.setText(items[which]);
                        dialog.dismiss();
                    }
                });

        singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel),null);
        singleChoiceDialog.show();
    }

    /**
     * 选择支付方式类型
     */
    private int position1 = 0;
    private void showSingleChoicePayDialog(final String[] items){
        final
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle(getString(R.string.bds_payment));
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, position1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position1 = which;
                        tv_pay.setText(items[which]);
                        setCNYView();
                        position = 0;
                        dialog.dismiss();
                    }
                });

        singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel),null);
        singleChoiceDialog.show();
    }

    private void setCNYView(){
        //卡号
        tv_banknum.setVisibility(View.VISIBLE);
        et_banknum.setVisibility(View.VISIBLE);
        ll_banknum.setVisibility(View.VISIBLE);
        tv_banknum.setText(getString(R.string.bds_CreditNum));

        if (tv_pay.getText().toString().equalsIgnoreCase(getString(R.string.bds_Creditcard))){
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
            tv_type.setText(cardStr[0]);

            type = "BankCard";
        }else if (tv_pay.getText().toString().equalsIgnoreCase(getString(R.string.bds_walletaddress))){

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
            tv_type.setText(addressStr[0]);
            type = "Wallet";
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

            if (tv_pay.getText().toString().equalsIgnoreCase(getString(R.string.bds_Alipay))){
                type = "Alipay";
            }else {
                type = "WeChat";
            }

        }
    }


    private String [] getCardType(){
        String [] str = new String[2];
        str[0] = "CNY";
        str[1] = "USD";
        return str;
    }


    private String [] getAddressType(){
        List<asset_object> objAssets = null;

        ArrayList<String> list = new ArrayList<>();
        try {
             objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        if (objAssets != null){
            if (objAssets.size()>0){
                for (int i =  0 ;i<objAssets.size() ; i++ ){
                    if (!objAssets.get(i).symbol.equalsIgnoreCase("BDS") &&  !objAssets.get(i).symbol.equalsIgnoreCase("USD") && !objAssets.get(i).symbol.equalsIgnoreCase("CNY")){
                        list.add(objAssets.get(i).symbol);
                    }
                }
            }
        }
        String [] str = new String[list.size()];
        for (int i =  0 ;i<list.size() ; i++ ){
            str[i] = list.get(i);
        }

        return str;
    }

}
