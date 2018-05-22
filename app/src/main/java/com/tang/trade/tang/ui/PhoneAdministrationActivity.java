package com.tang.trade.tang.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.tang.trade.tang.net.acceptormodel.AccPhoneModel;
import com.tang.trade.tang.net.acceptormodel.AcceptorOpenModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class PhoneAdministrationActivity extends BaseActivity {
    private EditText editUsername,editIdCard,editPhone,editCode;
    private ImageView ivBack;
    private TextView tvCode;
    private LinearLayout linearCode;
    private String TAG="PhoneAdministrationActivity";
    private Button btn_summit;
    private Button btn_bianji;
    private AccPhoneModel.DataBean dataBean=null;
    private String name = "";
    private String mobile = "";
    private String idNumber = "";
    private String code = "";

    private String verify = "0";
    private int second = Const.SMS_COUNTDOWN;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                second--;
                if (second > 0 ){
                    tvCode.setText(getString(R.string.bds_yi_send)+"("+second+")");
                    tvCode.setTextColor(0xff00A2F2);
                    tvCode.setClickable(false);
                    handler.postDelayed(thread,1000);
                    progressDialog.dismiss();
                }else {
                    second = 60;
                    tvCode.setText(getString(R.string.bds_chongxin_send));
                    tvCode.setTextColor(Color.BLACK);
                    tvCode.setClickable(true);
                    progressDialog.dismiss();
                }
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_phone_administration;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.iv_back){
            finish();
        }else if (view.getId()==R.id.tvCode){
            tvCode.setClickable(false);
            progressDialog.show();
            if (!TextUtils.isEmpty(editPhone.getText().toString())){
                mobile = editPhone.getText().toString();
            }else {
                MyApp.showToast(getString(R.string.bds_note_phone_err));
                progressDialog.dismiss();
                tvCode.setClickable(true);
                return;
            }

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("mobile",mobile);
            String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
            if (TextUtils.isEmpty(signMessage)){
                MyApp.showToast(getString(R.string.encryption_failed));
                progressDialog.dismiss();
                tvCode.setClickable(true);
                return;
            }
            hashMap.put("encmsg",signMessage);
            hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME,""));
            AcceptorApi.acceptantHttp(hashMap,"send_verify_sms",new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
                @Override
                public void onSuccess(Response<AcceptorOpenModel> response) {
                    if (response.body().getStatus().equalsIgnoreCase("success")){
                        handler.post(thread);
                    }else {
                        MyApp.showToast(response.body().getMsg());
                        progressDialog.dismiss();
                        tvCode.setClickable(true);
                    }

                }


                @Override
                public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                    super.onStart(request);

                }

                @Override
                public void onError(Response<AcceptorOpenModel> response) {
                    super.onError(response);
                    MyApp.showToast(getString(R.string.network));
                    progressDialog.dismiss();
                    tvCode.setClickable(true);
                }
            });

        }else if (R.id.btn_summit==view.getId()){
            if (UtilOnclick.isFastClick()) {
                if (!TextUtils.isEmpty(editUsername.getText().toString())) {
                    name = editUsername.getText().toString();
                } else {
                    MyApp.showToast(getString(R.string.bds_NOTEFillName));
                    return;
                }
                if (!TextUtils.isEmpty(editIdCard.getText().toString())) {
                    idNumber = editIdCard.getText().toString();
                }
                if (!TextUtils.isEmpty(editPhone.getText().toString())) {
                    mobile = editPhone.getText().toString();
                } else {
                    MyApp.showToast(getString(R.string.bds_note_phone_err));
                    return;
                }
                if (!TextUtils.isEmpty(editCode.getText().toString())) {
                    code = editCode.getText().toString();
                } else {
                    MyApp.showToast(getString(R.string.bds_note_code_err));
                    return;
                }
                acceptantOpen(name, idNumber, mobile, code);
            }

        }else if (R.id.btn_bianji==view.getId()){
            btn_bianji.setVisibility(View.GONE);
            linearCode.setVisibility(View.VISIBLE);
            editUsername.setEnabled(true);
            editIdCard.setEnabled(true);
            editPhone.setEnabled(true);
        }

    }

    @Override
    public void initView() {

        editUsername=(EditText) findViewById(R.id.editUsername);
        editIdCard=(EditText) findViewById(R.id.editIDCard);
        editPhone=(EditText) findViewById(R.id.editPhone);
        editCode=(EditText) findViewById(R.id.editCode);
        ivBack=(ImageView) findViewById(R.id.iv_back);
        linearCode=(LinearLayout) findViewById(R.id.linearCode);
        tvCode=(TextView) findViewById(R.id.tvCode);
        btn_bianji=findViewById(R.id.btn_bianji);
        btn_bianji.setOnClickListener(this);
        btn_summit=findViewById(R.id.btn_summit);
        ivBack.setOnClickListener(this);
        tvCode.setOnClickListener(this);
        btn_summit.setOnClickListener(this);
        if(getIntent().getStringExtra("type") != null){
            linearCode.setVisibility(View.VISIBLE);
        }else {
            acceptantListPhone();
        }

    }

    @Override
    public void initData() {

    }
    public void initData(AccPhoneModel.DataBean dataBean) {
        editUsername.setText(dataBean.getMemberName());
        editIdCard.setText(dataBean.getIdNumber());
        editPhone.setText(dataBean.getMobile());
        btn_bianji.setVisibility(View.VISIBLE);
        linearCode.setVisibility(View.GONE);

        editUsername.setEnabled(false);
        editIdCard.setEnabled(false);
        editPhone.setEnabled(false);
    }


    /**
     * 修改实名信息
     * */
    private void acceptantOpen(String name, String idNumber, String mobile, final String verfyCode){
        progressDialog.show();

        if (verify.equals("0")){
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
            hashMap.put("name",name);
            hashMap.put("idNumber",idNumber);
            hashMap.put("mobile",mobile);
            hashMap.put("smsVerifyMobile",mobile);
            hashMap.put("smsVerifyCode",verfyCode);

            String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
            if (TextUtils.isEmpty(signMessage)){
                MyApp.showToast(getString(R.string.encryption_failed));
                progressDialog.dismiss();
                return;
            }
            hashMap.put("encmsg",signMessage);
            hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));

            AcceptorApi.acceptantHttp(hashMap,"set_verify_info",new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
                @Override
                public void onSuccess(Response<AcceptorOpenModel> response) {
                    if (response.body().getStatus().equalsIgnoreCase("success")){
                        MyApp.showToast(getString(R.string.bds_xiugai_sucess));
                       verify = "1";
                        if(getIntent().getStringExtra("type") != null){
                            acceptantOpen();
                        }else {
//                            handler.removeCallbacks(thread);
//
//                            tvCode.setText(getString(R.string.bds_send));
//                            tvCode.setTextColor(Color.BLACK);
//                            tvCode.setClickable(true);
                            //acceptantListPhone();
                            progressDialog.dismiss();
                            finish();
                        }

                    }else {
                        MyApp.showToast(response.body().getMsg());
                        progressDialog.dismiss();
                    }

                }

                @Override
                public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                    super.onStart(request);
                    progressDialog.show();

                }

                @Override
                public void onError(Response<AcceptorOpenModel> response) {
                    super.onError(response);
                    MyApp.showToast(getString(R.string.network));
                    progressDialog.dismiss();

                }
            });
        }else {
            if(getIntent().getStringExtra("type") != null) {
                progressDialog.show();
                acceptantOpen();
            }
        }

    }


    private void acceptantListPhone(){
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("memberBdsAccount",SPUtils.getString(Const.USERNAME,""));
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            progressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"member_get_info",new JsonCallBack<AccPhoneModel>(AccPhoneModel.class) {
            @Override
            public void onSuccess(Response<AccPhoneModel> response) {
                if (response.body().getStatus().equals("success")){
                    dataBean=response.body().getData().get(0);
                    initData(dataBean);
                }else {
                    //MyApp.showToast(response.body().getMsg());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onStart(Request<AccPhoneModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<AccPhoneModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();

            }
        });
    }


    /**
     * 开通承兑商  "bdsAccount": "BDS1001"
     * */
    private void acceptantOpen(){
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
        AcceptorApi.acceptantHttp(hashMap,"member_acceptor_creat",new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
            @Override
            public void onSuccess(Response<AcceptorOpenModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    startActivity(new Intent(PhoneAdministrationActivity.this, AcceptantOpenActivity.class).putExtra("payAccount",response.body().getData().get(0).getBdsaccountCo()));
                    progressDialog.dismiss();
                    finish();
                }else {
                    MyApp.showToast(response.body().getMsg());
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AcceptorOpenModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
