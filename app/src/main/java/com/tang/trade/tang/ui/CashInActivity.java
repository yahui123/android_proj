package com.tang.trade.tang.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.CashInModel;
import com.tang.trade.tang.net.acceptormodel.CenterWlletModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.QRCodeUtil;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class CashInActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.iv_vr)
    ImageView iv_vr;

    String str_vr="";

    @BindView(R.id.tv_copy)
    Button tv_copy;

    @BindView(R.id.tv_transfer_address)
    TextView tv_transfer_address;

    @BindView(R.id.iv_record)
    ImageView iv_record;

    @BindView(R.id.tv_name)
    TextView tv_name;


    private String symbol="";
    private CenterWlletModel.DataBean dataBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cash_in;
    }


    public void initView() {
        tv_name.setText(SPUtils.getString(Const.USERNAME,""));

        tv_copy.setOnClickListener(this);
        iv_record.setOnClickListener(this);
        iv_back.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getSerializableExtra("dataBean") != null) {
            dataBean = (CenterWlletModel.DataBean) getIntent().getSerializableExtra("dataBean");
            if (!TextUtils.isEmpty(dataBean.getType())) {
                createURL(dataBean.getType().trim().toLowerCase());
            }
        }
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.iv_back){
            finish();
        }else if (view.getId()==R.id.tv_copy){
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(tv_transfer_address.getText().toString());
            Toast.makeText(this, getString(R.string.bds_copied_to_clipboard), Toast.LENGTH_LONG).show();


        }else if (view.getId()==R.id.iv_record){
            Intent intent=new Intent(this,NumberOrderActivity.class);
            intent.putExtra("type","CI");
            startActivity(intent);

        }
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


    private void createURL(String url){
        progressDialog.show();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            progressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"member_"+url+"_wallet_address",new JsonCallBack<CashInModel>(CashInModel.class) {
            @Override
            public void onSuccess(Response<CashInModel> response) {
                if (response.body().getStatus().equals("success")){

                    str_vr=response.body().getData().get(0).getAddress();
                    tv_transfer_address.setText(str_vr);
                    iv_vr.setImageBitmap(QRCodeUtil.getQRBitMap(str_vr));
                }else {
                    MyApp.showToast(response.body().getMsg());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onStart(Request<CashInModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<CashInModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();


            }
        });
    }


}
