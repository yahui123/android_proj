package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.AccountAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class SelectAccountActivity extends BaseActivity implements AccountAdapter.onOnButtonViewLister {
    public int myresultCode=100;


    @BindView(R.id.lv_account)
    ListView lv_account;
    @BindView(R.id.tv_add)
    TextView tv_add;

    @BindView(R.id.ll_add)
    LinearLayout ll_add;
    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.loading)
    LoadingView loading;

    @BindView(R.id.iv_add)
    ImageView iv_add;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;



    private View view1;
    private PopupWindow window;

    private TextView tv_note, tv_edit, tv_delete, tv_cancel;

    private String bdsAccount="";

    private AccountAdapter adapter;
    private ArrayList<AccountResponseModel.DataBean> data = new ArrayList<AccountResponseModel.DataBean>();
    private MyProgressDialog myProgressDialog;


    private int position;
    private String isItem = "0";

    public static final String TYPE_TIXIAN="0"; //0 , 1  判断上级页面跳转 提现页面
    public static final String  TYPE_Chongzhi="2";
    public static final String TYPE_GUANLI="1"; //0 , 1  判断上级页面跳转 安全中心 的支付方式管理
    public static String TYPE=""; //0 , 1  判断上级页面跳转 提现页面 和安全中心 的支付方式管理

    private boolean isFrist=true;
    private String symbol="";
    private String accountBdsAccount="";
    private String selectPayStyleID="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    Handler handler = new Handler();
    Thread thread=new Thread(new Runnable() {
        @Override
        public void run() {
            if (TYPE.equals(TYPE_Chongzhi)){
                memberAccountList(accountBdsAccount,"","");

            }else{
                memberAccountList(bdsAccount,"","");
            }
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
       if (isFrist==false){
           data.clear();
           ll_nodata.setVisibility(View.GONE);
           handler.post(thread);
       }
        isFrist=false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_select;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_add:
                int p=adapter.getSelectPosttion();
                if (p>=0&&data.size()>0){
                    Intent resultIntent=new Intent();
                    resultIntent.putExtra("databean", (Serializable)data.get(p));
                    resultIntent.putExtra("type", 1);
                    setResult(myresultCode, resultIntent);
                    finish();
                }
                finish();
                break;
            case R.id.iv_add:
                startActivity(new Intent(this, AddMoneyAddressActivity.class));
                break;

            case R.id.iv_back:
                finish();
                break;

            case R.id.tv_edit:
                Intent intent = new Intent(this, EditAccountActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("databean", data.get(position));
                intent.putExtras(mBundle);
                startActivity(intent);
                window.dismiss();
                break;

            case R.id.tv_delete:
                MyApp.showToast(position + "");
                window.dismiss();
                break;

            case R.id.tv_cancel:
                window.dismiss();
                break;
        }
    }

    @Override
    public void initView() {



        if (getIntent().getStringExtra("isItem") != null) {
            isItem = getIntent().getStringExtra("isItem");
        }


        if (getIntent().getStringExtra("symbol") != null) {
            symbol = getIntent().getStringExtra("symbol");
        }
        bdsAccount= SPUtils.getString(Const.USERNAME,"");

        if (getIntent().getStringExtra("type") != null) {
            TYPE = getIntent().getStringExtra("type");
            if (TYPE.equals(TYPE_TIXIAN)){
                tv_title.setText(getString(R.string.select_withdraw_method));
                ll_add.setVisibility(View.VISIBLE);
                myresultCode=100;
                accountBdsAccount=getIntent().getStringExtra("bdsAccount");
                if (getIntent().getStringExtra("payStyleID") != null) {
                    selectPayStyleID = getIntent().getStringExtra("payStyleID");
                }



            }else if (TYPE.equals(TYPE_GUANLI)){
                ll_add.setVisibility(View.GONE);
                bdsAccount=SPUtils.getString(Const.USERNAME,"");


            }else if (TYPE.equals(TYPE_Chongzhi)){
                myresultCode=101;
                tv_title.setText(getString(R.string.bds_Deposit_options));
                iv_add.setVisibility(View.GONE);
                ll_add.setVisibility(View.VISIBLE);
                accountBdsAccount=getIntent().getStringExtra("bdsAccount");
                if (getIntent().getStringExtra("payStyleID") != null) {
                    selectPayStyleID = getIntent().getStringExtra("payStyleID");
                }

            }

        }



        myProgressDialog=MyProgressDialog.getInstance(this);

        view1 = View.inflate(this, R.layout.edit_popupwindow_item, null);
        tv_note = (TextView) view1
                .findViewById(R.id.tv_note);
        tv_edit = (TextView) view1
                .findViewById(R.id.tv_edit);
        tv_delete = (TextView) view1
                .findViewById(R.id.tv_delete);
        tv_cancel = (TextView) view1
                .findViewById(R.id.tv_cancel);


        adapter = new AccountAdapter(data, this);
        lv_account.setAdapter(adapter);

        iv_back.setOnClickListener(this);
        ll_add.setOnClickListener(this);
        tv_edit.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        adapter.setOnItemViewLister(this);

    }

    @Override
    public void initData() {
            if (TYPE.equals(TYPE_TIXIAN)){
                memberAccountList(bdsAccount,"","");

            }else if (TYPE.equals(TYPE_GUANLI)){
                memberAccountList(bdsAccount,"","");
            }else if (TYPE.equals(TYPE_Chongzhi)){
                memberAccountList(accountBdsAccount,"","");

        }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(thread);
        handler.removeCallbacksAndMessages(null);
    }

    /*从支付管理跳入此页面
    * */
    private void memberAccountList(String bdsAccount,String typeCode,final String typeName) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",bdsAccount);
        if (TYPE.equals(TYPE_TIXIAN)){
            hashMap.put("isAcceptantPay","");

        }else if (TYPE.equals(TYPE_GUANLI)){

            hashMap.put("isAcceptantPay","");

        }else if (TYPE.equals(TYPE_Chongzhi)){
            hashMap.put("isAcceptantPay","1");
        }
        data.clear();

        AcceptorApi.acceptantHttp(hashMap, "get_pay_style", new JsonCallBack<AccountResponseModel>(AccountResponseModel.class) {
            @Override
            public void onSuccess(Response<AccountResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    data.addAll(response.body().getData());

                    if (!TextUtils.isEmpty(symbol)){//数据筛选
                        if (data.size()>0){//集合有数据
                            for ( int i=data.size()-1;i>=0;i--){
                                if (!data.get(i).getSymbol().equals(symbol)){
                                    data.remove(i);
                                }else {
                                    if (!TextUtils.isEmpty(selectPayStyleID)){
                                        if (data.get(i).getPayStyleID().equals(selectPayStyleID)){
                                            adapter.setSelectPosition(i);
                                        }
                                    }
                                }
                            }
                        }

                    }else {

                    }
                    adapter.setList(data);
                    adapter.notifyDataSetChanged();


                } else {

                }
                if (data.size()==0){
                    ll_nodata.setVisibility(View.VISIBLE);
                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onStart(Request<AccountResponseModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AccountResponseModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                loading.setVisibility(View.GONE);
                if (data.size()==0){
                    ll_nodata.setVisibility(View.VISIBLE);
                }


            }
        });
    }

    @Override
    public void onBiLiLister(int position) {

    }

    @Override
    public void onDeleteLister(final int position) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(getString(R.string.bds_delete_zhifu_pay));
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount(data.get(position).getPayStyleID(),position);
                myProgressDialog.show();

                dialog.dismiss();


            }
        });
        builder.show();
    }

    @Override
    public void onMorenLister(final int position, final boolean isflay) {
        if (isflay){
//            myProgressDialog.show();
            updateAccount(data.get(position).getPayStyleID(),position,isflay);

        }else {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.bds_Note);
            builder.setMessage(getString(R.string.bds_delete_account_pay));
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    myProgressDialog.show();
                    updateAccount(data.get(position).getPayStyleID(),position,isflay);
                    dialog.dismiss();

                }
            });
            builder.show();

        }

    }



    private void deleteAccount(final String payStyleID, final int position){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",bdsAccount);
        hashMap.put("payStyleID",payStyleID);

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"delete_pay_style",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    data.remove(position);
                    if (position==adapter.getSelectPosttion()){
                        adapter.setSelectPosition(-1);
                    }
                    adapter.notifyDataSetChanged();

                    if(data.size()==0){
                        ll_nodata.setVisibility(View.VISIBLE);
                    }


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



    private void updateAccount(String payStyleID,final int position,final boolean isFlay){

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount",bdsAccount);
        hashMap.put("payStyleID",payStyleID);
        if (isFlay){
            hashMap.put("isAcceptantPay","1");//设置承兑商可用

        }else {
            hashMap.put("isAcceptantPay","0");

        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"set_pay_style_acceptant",new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")){
                    if (isFlay){
                       data.get(position).setIsAcceptantPay("1");

                    }else {
                        data.get(position).setIsAcceptantPay("0");

                    }
                    adapter.notifyDataSetChanged();

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
                adapter.notifyDataSetChanged();
              //  myProgressDialog.dismiss();

            }
        });
    }






}
