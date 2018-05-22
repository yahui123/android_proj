package com.tang.trade.tang.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.tang.trade.tang.utils.QRCodeUtil;
import com.tang.trade.tang.utils.TLog;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class CashInAddressByWAActivity extends BaseActivity {
    private static final int REQURE_CODE_SCAN = 101;
    public static final String EXTRA_RESULT = "codedContent";
    private static String cashInTypeWC="WC";
    private static String cashInTypeAlipay="AP";
    private AccountResponseModel.DataBean dataBean=null;
    private String cashInType="";//判断是支付宝或者微信
    private String PLIPAY_CODE="HTTPS://QR.ALIPAY.COM";
    private String WEIXIN_CODE="wxp://";

    @BindView(R.id.iv_qrcode)
    ImageView iv_qrcode;

    @BindView(R.id.et_weChat_alipay_name)
    EditText et_weChat_alipay_name;

    @BindView(R.id.et_weChat_account)
    EditText et_weChat_account;

    @BindView(R.id.tv_account)
    TextView tv_account;

    @BindView(R.id.tv_niCheng)
    TextView tv_niCheng;

    @BindView(R.id.tv_submit)
    TextView tv_submit;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_qr)
    TextView tv_qr;
    String cashInCurrency="";
    String typeCode="";
    String payCode="";
    MyProgressDialog myProgressDialog;

    @BindView(R.id.tv_node)
    TextView tv_node;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cash_in_address_by_wa;
    }


    public void initView() {
        iv_qrcode.setOnClickListener(this);
        tv_submit.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);

        Intent intent =getIntent();
        cashInType=intent.getStringExtra("cashInType");
        cashInCurrency=getIntent().getStringExtra("cashInCurrency");

        if (cashInType.equals(cashInTypeWC)){
            tv_niCheng.setText(getString(R.string.bds_weChat_name));
            tv_account.setText(getString(R.string.bds_weChat_account));
            if (TextUtils.isEmpty(typeCode)){
                typeCode="WC";

            }


        }else if (cashInType.equals(cashInTypeAlipay)){
            tv_niCheng.setText(getString(R.string.full_name));//alipay
            tv_account.setText(getString(R.string.bds_apliay_account));
            if (TextUtils.isEmpty(typeCode)){
                typeCode="AP";

            }
            tv_node.setText(getString(R.string.bds_cash_in_description_apliay));
        }
        et_weChat_alipay_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        et_weChat_account.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
//        MyTextTuils.setEditTextInhibitInputSpeChat(et_weChat_account);

    }

    @Override
    public void initData() {


    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.iv_qrcode){
            Intent i = new Intent(this, CaptureActivity.class);
            startActivityForResult(i, REQURE_CODE_SCAN);
        }else if (view.getId()==R.id.tv_submit){

            String name=et_weChat_alipay_name.getText().toString().trim();
            String accountId=et_weChat_account.getText().toString().trim();
            if (TextUtils.isEmpty(name)){

                if (cashInType.equals(cashInTypeWC)) {
                    MyApp.showToast(getString(R.string.bds_null_name));
                }else {
                    MyApp.showToast(getString(R.string.bds_null_name_apliay));
                }

            }else if (TextUtils.isEmpty(accountId)){
                if (cashInType.equals(cashInTypeWC)) {
                    MyApp.showToast(getString(R.string.bds_null_account));
                }else {
                    MyApp.showToast(getString(R.string.bds_null_account_apliay));
                }

            }else if (TextUtils.isEmpty(payCode)){
                if (cashInType.equals(cashInTypeWC)) {
                    MyApp.showToast(getString(R.string.bds_null_qr_code));
                }else {
                    MyApp.showToast(getString(R.string.bds_null_qr_ap));
                }

            }else {
               // if (!accountId.matches("^[^\\u4e00-\\u9fa5]{0,}$") == true||!MyTextTuils.containsEmoji(accountId)) {
                    addAccount(name, accountId, payCode);

            }

        }else if (view.getId()==R.id.iv_back){
            finish();
        }

    }

    private void addAccount(String name,String payAccountID,String payCode){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        hashMap.put("symbol",cashInCurrency);
        hashMap.put("typeCode",typeCode);
        hashMap.put("payAccountID",payAccountID);
        hashMap.put("name",name);
        hashMap.put("payCode",payCode);
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME,""));

        AcceptorApi.acceptantHttp(hashMap,"add_pay_style",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    MyApp.showToast(getString(R.string.bds_add_successfully));
                    finish();

                }else {
                    MyApp.showToast(response.body().getMsg()+"");

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQURE_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            TLog.log("qr success");
            if (data != null) {
                String code= data.getStringExtra(EXTRA_RESULT);
                tv_qr.setText(code);
                Drawable drawable =null;
                if (setCodeHefa(code)){
                    payCode=code;
                    if (cashInType.equals(cashInTypeWC)){
                        drawable = getResources().getDrawable(R.mipmap.accept_wx_icon);
                    }else {
                         drawable = getResources().getDrawable(R.mipmap.acaept_alipay_icon);
                    }
                    BitmapDrawable bd = (BitmapDrawable) drawable;
                    final Bitmap logo = bd.getBitmap();
                    iv_qrcode.setImageBitmap(QRCodeUtil.getLogoQRBitMap(payCode,logo));
                }else {
                    if (cashInType.equals(cashInTypeWC)){
                        MyApp.showToast(getString(R.string.bds_not_whar_code));
                    }else {
                        MyApp.showToast(getString(R.string.bds_not_ap_code));
                    }


                }



            }
        }
    }
    //判断二维码是否合法
    private boolean setCodeHefa(String payCode){

        if (cashInType.equals(cashInTypeWC)){
            if (payCode.contains(WEIXIN_CODE)){
                return true;
            }
        }else {
            if (payCode.contains(PLIPAY_CODE)){
                return true;
            }
        }

        return false;

    }

}
