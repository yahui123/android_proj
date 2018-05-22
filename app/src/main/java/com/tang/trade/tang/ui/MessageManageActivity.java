package com.tang.trade.tang.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;

public class MessageManageActivity extends BaseActivity{


    @BindView(R.id.et_advice)
    EditText et_advice;

    @BindView(R.id.advice_limit)
    TextView tv_sum;
    int num=20;

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.tv_wancheng)
    TextView tv_wancheng;

    @BindView(R.id.btn_submit)
    Button btn_submit;
    String acceptantBdsAccount=null;
    String introduce;
    String symbol;
    MyProgressDialog myProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    protected int getLayoutId() {
        return R.layout.activity_message_manage;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
            finish();
        }else if (view.getId()==R.id.tv_wancheng){

        }else if (view.getId()==R.id.btn_submit){

            introduce=et_advice.getText().toString().trim();
            if (!TextUtils.isEmpty(introduce)){
                myProgressDialog.show();
                acceptantAccountList();
            }else {
                MyApp.showToast(getString(R.string.bds_input_manage));
            }

        }

    }

    @Override
    public void initView() {
        myProgressDialog=MyProgressDialog.getInstance(this);
        Bundle bundle=getIntent().getBundleExtra("bundle");
        acceptantBdsAccount= SPUtils.getString(Const.USERNAME,"");
        introduce=bundle.getString("introduce");

        symbol=bundle.getString("symbol");

        ivBack.setOnClickListener(this);
        btn_submit.setOnClickListener(this);



        et_advice.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
                tv_sum.setText("" + s.length()+"/"+num);
                selectionStart = et_advice.getSelectionStart();
                selectionEnd = et_advice.getSelectionEnd();
                if (temp.length() > num) {
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    et_advice.setText(s);
                    et_advice.setSelection(tempSelection);//设置光标在最后
                }

            }

        });

    }



    @Override
    public void initData() {
        if (!TextUtils.isEmpty(introduce)){
            et_advice.setText(introduce);
        }

    }

    private void acceptantAccountList(){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount",acceptantBdsAccount);//acceptantBdsAccount
        hashMap.put("introduce",introduce);//introduce

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
        AcceptorApi.acceptantHttp(hashMap,"set_acceptant_info",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    MyApp.showToast(getString(R.string.bds_setting_manage_success));
                    myProgressDialog.dismiss();
                    AcceptanceManagerActivity.Listtype=4;
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
