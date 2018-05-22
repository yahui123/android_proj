package com.tang.trade.tang.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.NumberOrderAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.NumberOrderModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.acceptormodel.TianXianModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

/**
 * Created by Administrator on 2017/12/29.
 */

public class NumberOrderActivity extends BaseActivity {
    private String type="";
    private String TYPE_CI="CI";
    private String TYPE_CO="CO";
    private String bdsAccount;
    private String CODE_CASHIN="acceptant_digital_cashin_list";
    private String CODE_CASHOUT="acceptant_digital_cashout_list";





    @BindView(R.id.iv_back)
    ImageView iv_back;


    @BindView(R.id.lv_record)
    ListView lv_record;

    @BindView(R.id.loading)
    LoadingView loadingView;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;


    private NumberOrderAdapter adapter;
    private List<NumberOrderModel.DataBean> listDate= new ArrayList<>();


    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_number;
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void initView() {


        bdsAccount= SPUtils.getString(Const.USERNAME,"");
        if (getIntent().getStringExtra("type") != null) {
            type = getIntent().getStringExtra("type");
        }
        if (type.equals(TYPE_CI)){
            tv_title.setText(getString(R.string.bds_Deposits));
        }else {
            tv_title.setText(getString(R.string.bds_Withdrawals));
        }

        iv_back.setOnClickListener(this);

        adapter=new NumberOrderAdapter(listDate,this,type);
        lv_record.setAdapter(adapter);




    }

    @Override
    public void initData() {

        if (type.equals(TYPE_CI)){
            cashOrderList(bdsAccount,CODE_CASHIN);
        }else {
            cashOrderList(bdsAccount,CODE_CASHOUT);
        }


    }

    /*
    * 获取订单列表
    * */
    private void cashOrderList(final String bdsAccount,String code){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bdsAccount",bdsAccount);

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            loadingView.setVisibility(View.GONE);
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,code,new JsonCallBack<NumberOrderModel>(NumberOrderModel.class) {
            @Override
            public void onSuccess(Response<NumberOrderModel> response) {
                if (response.body().getStatus().equals("success")){
                    listDate.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                }
                if (listDate.size()==0){
                    ll_nodata.setVisibility(View.VISIBLE);
                }
                loadingView.setVisibility(View.GONE);


            }

            @Override
            public void onStart(Request<NumberOrderModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<NumberOrderModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                loadingView.setVisibility(View.GONE);
                if (listDate.size()==0){
                    ll_nodata.setVisibility(View.VISIBLE);
                }


            }
        });
    }
}
