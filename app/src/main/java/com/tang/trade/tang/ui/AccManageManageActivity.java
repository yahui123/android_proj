package com.tang.trade.tang.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.FeeMinOrMaxModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.MyEditextSeekbar;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;

public class AccManageManageActivity extends BaseActivity {

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.btn_summit)
    Button btn_summit;

    @BindView(R.id.et_chongzhiUp_aquate)
    EditText et_chongzhiUp_aquate;


    @BindView(R.id.et_tianxinaBottom)
    EditText et_tianxinaBottom;

    @BindView(R.id.tv_chongzhiUp_currency)
    TextView tv_chongzhiUp_currency;


    @BindView(R.id.tv_tixianBottom_currentcy)
    TextView tv_tixianBottom_currentcy;

    @BindView(R.id.line_fee)
    LinearLayout line_fee;

    private String symbol="";
    MyProgressDialog myProgressDialog;

    @BindView(R.id.scrollview)
    ScrollView scrollView;
    
    private final int INT10000=10000;
    private final int INT100=100;

    @BindView(R.id.seekbarDeposit)
    MyEditextSeekbar seekbarDeposit;

    @BindView(R.id.seekbarWithdrawal)
    MyEditextSeekbar seekbarWithdrawal;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acc_manage_manage;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
            finish();
        }else if (view.getId()==R.id.btn_summit){
            myProgressDialog.show();

            cashInLowerLimit=et_chongzhiUp_aquate.getText().toString().trim();
            cashOutLowerLimit=et_tianxinaBottom.getText().toString().trim();
            if (TextUtils.isEmpty(cashInLowerLimit)){
                cashInLowerLimit="0";
            }
            if (TextUtils.isEmpty(cashOutLowerLimit)){
                cashOutLowerLimit="0";
            }

            acceptantAccountList();



        }

    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        btn_summit.setOnClickListener(this);
        myProgressDialog=MyProgressDialog.getInstance(this);
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.setFocusable(true);
            }
        });

        seekbarDeposit.setRefreshData(new MyEditextSeekbar.onRefreshData() {
            @Override
            public void onRefresh(String rate) {
                cashInServiceRate=rate;
            }
        });
        seekbarWithdrawal.setRefreshData(new MyEditextSeekbar.onRefreshData() {
            @Override
            public void onRefresh(String rate) {
                cashOutServiceRate=rate;
            }
        });

    }

    private String acceptantBdsAccount="";
    private String cashInLowerLimit="0";//充值下限
    private String cashOutLowerLimit="0";//提现下限
    private String cashInServiceRate="0";//充值fee
    private String cashOutServiceRate="0";//提现fee

    @Override
    public void initData() {

        acceptantBdsAccount= SPUtils.getString(Const.USERNAME,"");
        Bundle bundle=getIntent().getBundleExtra("bundle");
         symbol=bundle.getString("symbol","");
         cashInLowerLimit= NumberUtils.formatNumber0(bundle.getString("cashInLowerLimit","0"));//充值下线
         cashOutLowerLimit=NumberUtils.formatNumber0(bundle.getString("cashOutLowerLimit","0"));//体现下线
         cashInServiceRate=bundle.getString("cashInServiceRate","0");//充值网管手续费
         cashOutServiceRate=bundle.getString("cashOutServiceRate","0");//体现充值网管手续费

        if (TextUtils.isEmpty(cashInLowerLimit)){
            et_chongzhiUp_aquate.setText(0+"");
        }else {
            et_chongzhiUp_aquate.setText(cashInLowerLimit);
        }

        if (TextUtils.isEmpty(cashOutLowerLimit)){
            et_tianxinaBottom.setText(0+"");
        }else {
            et_tianxinaBottom.setText(cashOutLowerLimit);
        }

        if (TextUtils.isEmpty(cashInServiceRate)){
            cashInServiceRate="0";
        }

        if (TextUtils.isEmpty(cashOutServiceRate)){
            cashInServiceRate="0";
        }



        tv_chongzhiUp_currency.setText(symbol);

        tv_tixianBottom_currentcy.setText(symbol);

        acceptantfee();
    }


    private void acceptantAccountList(){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount",acceptantBdsAccount);
        hashMap.put("cashInLowerLimit",cashInLowerLimit);//

        hashMap.put("cashOutLowerLimit",cashOutLowerLimit);
        hashMap.put("cashInServiceRate",cashInServiceRate);//cashInServiceRate
        hashMap.put("cashOutServiceRate",cashOutServiceRate);

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
                    MyApp.showToast(getString(R.string.bds_xiugai_sucess));
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

    private void acceptantfee(){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        AcceptorApi.acceptantHttp(hashMap,"acceptant_set_rate_section",new JsonCallBack<FeeMinOrMaxModel>(FeeMinOrMaxModel.class) {
            @Override
            public void onSuccess(Response<FeeMinOrMaxModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    FeeMinOrMaxModel.DataBean dataBean =response.body().getData().get(0);
                    seekbarDeposit.initData(dataBean.getCashinmax(),dataBean.getCashinmin(),cashInServiceRate);
                    seekbarWithdrawal.initData(dataBean.getCashoutmax(),dataBean.getCashoutmin(),cashOutServiceRate);

                }else {

                }



            }

            @Override
            public void onStart(Request<FeeMinOrMaxModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<FeeMinOrMaxModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));


            }
        });
    }
}
