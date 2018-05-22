package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;

import butterknife.BindView;

public class AddMoneyAddressActivity extends BaseActivity {

    @BindView(R.id.tv_cash_in_currency)
    TextView tv_cash_in_currency; // 充值币种

    @BindView(R.id.iv_select_cash_in_currency)
    LinearLayout iv_select_cash_in_currency; // 选择充值币种

    @BindView(R.id.tv_cash_in_type)
    TextView tv_cash_in_type; // 充值类型

    @BindView(R.id.iv_select_cash_in_type)
    LinearLayout iv_select_cash_in_type; // 选择充值类型

    @BindView(R.id.tv_next)
    TextView tv_next; // 下一步按钮

    @BindView(R.id.iv_back)
    ImageView iv_back; // 返回按钮

    // 选择充值币种用列表
    private String[] currencyList;
    private int currencyListIndex;

    // 选择充值方式用列表
    private String[] typeList;
    private int typeListIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_money_address;
    }

    public void initView() {
        iv_select_cash_in_currency.setOnClickListener(this);
        iv_select_cash_in_type.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    public void initData() {
        currencyList = new String[]{"CNY", "USD"};
        typeList = new String[]{getString(R.string.bds_WeChat), getString(R.string.bds_Alipay), getString(R.string.bds_Creditcard)};

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_select_cash_in_currency:

                // 点击【充值币种】的场合，弹出单选对话框，选择币种
                final AlertDialog.Builder currencyBuilder = new AlertDialog.Builder(this);
                currencyBuilder.setTitle(getString(R.string.bds_select_currency));
                currencyBuilder.setSingleChoiceItems(currencyList, currencyListIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currencyListIndex = which;
                        if (currencyListIndex==0){
                            tv_cash_in_currency.setText(currencyList[currencyListIndex]);
                            typeList = new String[]{getString(R.string.bds_WeChat), getString(R.string.bds_Alipay), getString(R.string.bds_Creditcard)};
                        }else {
                            tv_cash_in_currency.setText(currencyList[currencyListIndex]);
                            typeList = new String[]{getString(R.string.bds_Creditcard)};
                            typeListIndex=0;
                            tv_cash_in_type.setText(typeList[typeListIndex]);
                        }

                        dialog.dismiss();
                    }
                });

                currencyBuilder.setNegativeButton(getString(R.string.bds_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });
                currencyBuilder.create().show();
                break;

            case R.id.iv_select_cash_in_type:

                // TODO:对话框仅为测试场合使用，正式场合应改为下级菜单界面
                // 点击【充值方式】的场合，弹出单选对话框，选择方式
                AlertDialog.Builder typeBuilder = new AlertDialog.Builder(this);
                typeBuilder.setTitle(getString(R.string.bds_select));
                typeBuilder.setSingleChoiceItems(typeList, typeListIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        typeListIndex = which;
                        tv_cash_in_type.setText(typeList[typeListIndex]);
                        dialog.dismiss();
                    }
                });

                typeBuilder.setNegativeButton(getString(R.string.bds_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });
                typeBuilder.create().show();
                break;

            case R.id.tv_next:

                // 按下【下一步】按钮的场合，启动后画面，并传递选择的充值币种和充值方式
                String cashInType = tv_cash_in_type.getText().toString();
                String cashInCurrency = tv_cash_in_currency.getText().toString();
                if (cashInCurrency.equals(currencyList[0])){
                    if (cashInType.equals(typeList[0])){
                        cashInType="WC";
                        Intent intent = new Intent(this, CashInAddressByWAActivity.class);
                        intent.putExtra("cashInType", cashInType);
                        intent.putExtra("cashInCurrency", cashInCurrency);
                        intent.putExtra("bianji",false);
                        startActivity(intent);

                    }else if(cashInType.equals(typeList[1])) {
                        cashInType="AP";
                        Intent intent = new Intent(this, CashInAddressByWAActivity.class);
                        intent.putExtra("cashInType", cashInType);
                        intent.putExtra("cashInCurrency", cashInCurrency);
                        intent.putExtra("bianji",false);
                        startActivity(intent);

                    }else if (cashInType.equals(typeList[2])) {
                        Intent intent = new Intent(this, CashInAddressByCardActivity.class);
                        intent.putExtra("cashInCurrency", cashInCurrency);
                        intent.putExtra("bianji",false);
                        startActivity(intent);

                    }
                    finish();

                }else {
//                    Intent intent = new Intent(this, CashInAddressByCardActivity.class);
//                    intent.putExtra("cashInCurrency", cashInCurrency);
//                    intent.putExtra("bianji",false);
//                    startActivity(intent);
                    MyApp.showToast(getString(R.string.bds_note_usd_id_card));

                }






                break;

            case R.id.iv_back:
                finish();
                break;
        }
    }
}