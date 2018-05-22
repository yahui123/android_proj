package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.acceptormodel.OrderSumberModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.BadgeView;
import com.tang.trade.tang.widget.TimePickerDialog;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;


public class AcceptanceManagerActivity extends BaseActivity {
    public static int Listtype=-1;//-1,不刷新数据0 初次进入，1 充值提现  2 保证金,  3交易，4留言管理和费用管理

    private LinearLayout line_diya, line_chongzhi, line_shuhui, line_tixian, line_fee_manage, line_massage_manage, line_jiaoyi,line_payway,line_transfer;
    private ImageView ivBack, togglebutton;
    private TextView tv_currency, tv_currency_margin, tv_banlance, tv_keti_balance, tv_order_zhongzhi, tv_order_thixian, tv_jiaoyi;
    private TextView tv_time;

    @BindView(R.id.line_evening_up)
    LinearLayout line_evening_up;

    @BindView(R.id.line_borrow)
    LinearLayout line_borrow;
//
    @BindView(R.id.iv_time_guanli)
    ImageView iv_time_guanli;

    private boolean tooggleOff = false;
    private String symbole = "CNY";
    private ImageView ivTimeSelect;
    private int cashInCount = 0;
    private int cashOutCount = 0;
    
    private String margin="0.00000";//保证金
    private String banlance="0.00000";//承兑账户余额
    private double available = 0.00000;
    String inLock = "0.00000";
    String outLock = "0.00000";

    private AcceptotXiangqingModel.DataBean dataBean = null;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
            }else if (msg.what==2){
                progressDialog.dismiss();
            }
        }
    };


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            margin=getBalance("BDS", dataBean.getBdsAccountCo());
            banlance=getBalance(dataBean.getSymbol(), dataBean.getBdsAccountCo());
            if (handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_currency_margin.setText( margin+ " BDS");
                        tv_banlance.setText(banlance + " " + dataBean.getSymbol());
                        //可提取金额计算
                        available = CalculateUtils.sub(Double.parseDouble(banlance), CalculateUtils.add(Double.parseDouble(inLock), Double.parseDouble(outLock)));
                        if (available>=0){
                            tv_keti_balance.setText(NumberUtils.formatNumber5(available + "") + " " + dataBean.getSymbol());
                        }else {
                            tv_keti_balance.setText("0.00000"+" "+dataBean.getSymbol());
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        }
    };

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler=null;

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acceptance_manager;
    }

    @Override
    public void onClick(View view) {
        if (dataBean != null) {
            if (view.getId() == R.id.line_evening_up) {
                startActivity(new Intent(this, PingCangActivity.class));
            }
            if (view.getId() == R.id.iv_time_guanli||view.getId()==R.id.tv_time) {
                setTimePicker(dataBean.getOnlineStartTime() + "", dataBean.getOnlineEndTime() + "");
            } else if (view.getId() == R.id.line_borrow) {
                startActivity(new Intent(this, BorrowingActivity.class));
            } else if (view.getId() == R.id.line_diya) {
                Intent intent = new Intent(this, AcceptanceDyorCzorShActivity.class);
                intent.putExtra("type", AcceptanceDyorCzorShActivity.type_diya);
                intent.putExtra("symbol", dataBean.getSymbol());
                intent.putExtra("payAccount", dataBean.getBdsAccountCo());
                startActivity(intent);

            } else if (view.getId() == R.id.line_chongzhi) {
                Intent intent = new Intent(this, AcceptanceDyorCzorShActivity.class);
                intent.putExtra("type", AcceptanceDyorCzorShActivity.type_chizhi);
                intent.putExtra("symbol", dataBean.getSymbol());
                intent.putExtra("payAccount", dataBean.getBdsAccountCo());
                startActivity(intent);

            } else if (view.getId() == R.id.line_shuhui) {//注销承兑商
                showDialog();

            } else if (view.getId() == R.id.line_tixian) {

                Intent intent = new Intent(this, AceeeptanceTiXianActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("symbol", dataBean.getSymbol() + "");
                bundle.putString("available", available + "");
      ;
                intent.putExtras(bundle);
                startActivity(intent);

            } else if (view.getId() == R.id.ivBack) {
                finish();
            } else if (view.getId() == R.id.togglebutton) {
                if (tooggleOff) {
                    tooggleOff = false;
                    setToggleButtonState(tooggleOff);
                } else {
                    tooggleOff = true;
                    setToggleButtonState(tooggleOff);
                }
                acceptanTime("", "", tooggleOff + "");

            } else if (view.getId() == R.id.line_jiaoyi) {
                Intent intent = new Intent(this, AcceptanceRecordActivity.class);
                intent.putExtra("cashInCount", cashInCount);
                intent.putExtra("cashOutCount", cashOutCount);
                startActivity(intent);

            } else if (view.getId() == R.id.line_fee_manage) {
                Intent intent = new Intent(this, AccManageManageActivity.class);
                intent.putExtra("symbol", dataBean.getSymbol() + "");

                Bundle bundle = new Bundle();//      "acceptantBdsAccount": "BDS2001",
                bundle.putString("symbol", dataBean.getSymbol() + "");
                bundle.putString("cashInLowerLimit", dataBean.getCashInLowerLimit() + "");//充值下线
                bundle.putString("cashOutLowerLimit", dataBean.getCashOutLowerLimit() + "");//体现下线
                bundle.putString("cashInServiceRate", dataBean.getCashInServiceRate() + "");//充值网管手续费
                bundle.putString("cashOutServiceRate", dataBean.getCashOutServiceRate() + "");//体现充值网管手续费
                intent.putExtra("bundle", bundle);
                startActivity(intent);

            } else if (view.getId() == R.id.line_massage_manage) {
                Intent intent = new Intent(this, MessageManageActivity.class);
                Bundle bundle = new Bundle();//      "acceptantBdsAccount": "BDS2001",
                bundle.putString("introduce", "" + dataBean.getIntroduce());
                bundle.putString("symbol", dataBean.getSymbol() + "");
                bundle.putString("acceptantBdsAccount", SPUtils.getString(Const.USERNAME,""));
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }else if (view.getId()==R.id.line_payway){
                Intent selectI=new Intent(this, SelectAccountActivity.class);
                selectI.putExtra("type",SelectAccountActivity.TYPE_GUANLI);
                startActivity(selectI);
            }else if (view.getId()==R.id.line_transfer){
                Intent selectI=new Intent(this, TransferRecordctivity.class);
                selectI.putExtra("username",dataBean.getBdsAccountCo());
                startActivity(selectI);
            }
        } else {
            if (view.getId() == R.id.ivBack) {
                finish();
            } else {
                MyApp.showToast(getString(R.string.network));
            }

        }
    }

    @Override
    public void initView() {
        MyApp.set(SPUtils.getString(Const.USERNAME,"") + "isAccportant", true);
        line_diya = findViewById(R.id.line_diya);
        line_chongzhi = findViewById(R.id.line_chongzhi);
        line_shuhui = findViewById(R.id.line_shuhui);
        line_tixian = findViewById(R.id.line_tixian);
        line_jiaoyi = findViewById(R.id.line_jiaoyi);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        togglebutton = findViewById(R.id.togglebutton);

        tv_currency = findViewById(R.id.tv_currency);
        tv_currency_margin = findViewById(R.id.tv_currency_margin);
        tv_banlance = findViewById(R.id.tv_banlance);
        tv_keti_balance = findViewById(R.id.tv_keti_balance);
        tv_order_zhongzhi = findViewById(R.id.tv_order_zhongzhi);
        tv_order_thixian = findViewById(R.id.tv_order_thixian);
        ivTimeSelect = findViewById(R.id.iv_time_select);
        tv_jiaoyi = findViewById(R.id.tv_jiaoyi);
        tv_time = findViewById(R.id.tv_time);
        line_fee_manage = findViewById(R.id.line_fee_manage);
        line_massage_manage = findViewById(R.id.line_massage_manage);
        line_payway=findViewById(R.id.line_payway);
        line_transfer = findViewById(R.id.line_transfer);





    }

    @Override
    public void initData() {

        progressDialog.show();
        Listtype=0;
        acceptantList(Listtype);
        line_jiaoyi.setOnClickListener(this);

        tv_time.setOnClickListener(this);
        line_payway.setOnClickListener(this);
        ivTimeSelect.setOnClickListener(this);
        iv_time_guanli.setOnClickListener(this);
        togglebutton.setOnClickListener(this);
        line_diya.setOnClickListener(this);
        line_chongzhi.setOnClickListener(this);
        line_shuhui.setOnClickListener(this);
        line_tixian.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        line_fee_manage.setOnClickListener(this);
        line_massage_manage.setOnClickListener(this);

        line_evening_up.setOnClickListener(this);
        line_borrow.setOnClickListener(this);
        line_transfer.setOnClickListener(this);


    }

    private void initData(final AcceptotXiangqingModel.DataBean dataBean) {
        tv_currency.setText(dataBean.getSymbol() + "");
        
        if (!TextUtils.isEmpty(dataBean.getCashInUncompletedAmount())) {
            inLock = dataBean.getCashInUncompletedAmount();
        }
        tv_order_zhongzhi.setText(inLock + " " + dataBean.getSymbol());
        if (!TextUtils.isEmpty(dataBean.getCashOutUncompletedAmount())) {
            outLock = dataBean.getCashOutUncompletedAmount();
        }
        tv_order_thixian.setText(outLock + " " + dataBean.getSymbol());

        new Thread(runnable).start();
        if (!TextUtils.isEmpty(dataBean.getOnlineStartTime()) && !TextUtils.isEmpty(dataBean.getOnlineEndTime())) {
            String onlineTimeState = dataBean.getOnlineTimeState();
            tv_time.setText(dataBean.getOnlineStartTime() + "~" + dataBean.getOnlineEndTime());
            tv_time.setVisibility(View.VISIBLE);
            togglebutton.setVisibility(View.VISIBLE);
            iv_time_guanli.setVisibility(View.GONE);
            if (onlineTimeState.equals("True")) {
                tooggleOff = true;
                setToggleButtonState(tooggleOff);

            } else {
                tooggleOff = false;
                setToggleButtonState(tooggleOff);
            }
        }

        String InCount = dataBean.getCashInUncompletedCount();
        String OutCount = dataBean.getCashOutUncompletedCount();
        if (!TextUtils.isEmpty(InCount) && !TextUtils.isEmpty(OutCount)) {
            cashInCount = Integer.parseInt(InCount);
            cashOutCount = Integer.parseInt(OutCount);
            BadgeView badge = new BadgeView(this);
            badge.setTargetView(tv_jiaoyi);
            badge.setBadgeCount(cashInCount + cashOutCount);
        }



    }

    private void initDataUpdateChangzhiTiXian(final AcceptotXiangqingModel.DataBean dataBean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                banlance=getBalance(dataBean.getSymbol(), dataBean.getBdsAccountCo());
                if (handler!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_banlance.setText(banlance + " " + dataBean.getSymbol());
                            //可提取金额计算
                            available = CalculateUtils.sub(Double.parseDouble(banlance), CalculateUtils.add(Double.parseDouble(inLock), Double.parseDouble(outLock)));
                            if (available>=0){
                                tv_keti_balance.setText(NumberUtils.formatNumber5(available + " ") + dataBean.getSymbol());
                            }else {
                                tv_keti_balance.setText("0.00000"+" "+dataBean.getSymbol());
                            }
                        }
                    });
                }


            }
        }).start();

    }

    private void initDataUpdateBaozhengjin(final AcceptotXiangqingModel.DataBean dataBean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                margin=getBalance("BDS", dataBean.getBdsAccountCo());
                if (handler!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_currency_margin.setText( margin+ " BDS");
                        }
                    });
                }
            }
        }).start();

    }

    private void acceptantList(final int type) {
        Listtype=-1;
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("acceptantBdsAccount", SPUtils.getString(Const.USERNAME,""));
        hashMap.put("selfacc","1");
        AcceptorApi.acceptantHttp(hashMap, "get_acceptant_info", new JsonCallBack<AcceptotXiangqingModel>(AcceptotXiangqingModel.class) {
            @Override
            public void onSuccess(Response<AcceptotXiangqingModel> response) {
                if (response.body().getStatus().equals("success")) {
                    dataBean = response.body().getData().get(0);
                    if (!TextUtils.isEmpty(response.body().getData().get(0).getBsdcopubkey())) {
                        MyApp.BDS_CO_PUBLICKEY = response.body().getData().get(0).getBsdcopubkey();
                    }
                    if (type==0||type==3){
                        initData(dataBean);

                    }else if (type==1){
                        initDataUpdateChangzhiTiXian(dataBean);
                    }else if (type==2){
                       initDataUpdateBaozhengjin(dataBean);
                    }else if (type==4){
                        
                    }

                } else {
                    handler.sendEmptyMessage(2);
                }
            }

            @Override
            public void onStart(Request<AcceptotXiangqingModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AcceptotXiangqingModel> response) {
                super.onError(response);
                handler.sendEmptyMessage(1);
            }
        });
    }

    private String getBalance(final String symbol, String bdsAccount) {
        String finalSAssetAmount = "0.00000";
        try {
            String sAssetAmount;
            boolean findAssetInAccount = false;

            //获取所有资产列表
            account_object object = BitsharesWalletWraper.getInstance().get_account_object(bdsAccount);
            if (object == null) {
//                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }
            //查询账户id
            object_id<account_object> loginAccountId = object.id;

            //获取账户余额列表
            List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
            if (accountAsset == null) {
                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }
            //查询资产列表
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (objAssets == null) {
//                MyApp.showToast(getString(R.string.network));
                return finalSAssetAmount;
            }


            //查询基础货币BDS,和所有智能资产

            for (asset_object objAsset : objAssets) {
                if (objAsset.symbol.equalsIgnoreCase(symbol)) {
                    findAssetInAccount = false;
                    if (accountAsset.size() > 0) {
                        for (int j = 0; j < accountAsset.size(); j++) {
                            asset account_asset = accountAsset.get(j);
                            String sAccount_sid = account_asset.asset_id.toString();
                            if (sAccount_sid.equals(objAsset.id.toString())) {
                                findAssetInAccount = true;
                                //计算账户余额
                                asset_object.asset_object_legible myasset = objAsset.get_legible_asset_object(account_asset.amount);
                                sAssetAmount = myasset.count;
                                finalSAssetAmount = sAssetAmount;
                            }
                        }
                    }

                }
            }
            if (!findAssetInAccount) {
                return finalSAssetAmount;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return finalSAssetAmount;
        }
        return finalSAssetAmount;
    }

    /**
     * 注销承兑商
     */
    private void showDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(getString(R.string.bds_acceptor_zhuxiao_comfirm));
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                progressDialog.show();
                HashMap<String, String> hashMap = new HashMap<String, String>();

                if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
                    MyApp.showToast(getString(R.string.encryption_failed));
                    progressDialog.dismiss();
                    return;
                }
                String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
                if (TextUtils.isEmpty(signMessage)){
                    MyApp.showToast(getString(R.string.encryption_failed));
                    progressDialog.dismiss();
                    return;
                }
                hashMap.put("encmsg",signMessage);
                hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
                AcceptorApi.acceptantHttp(hashMap, "acceptant_sign_out", new JsonCallBack<OrderSumberModel>(OrderSumberModel.class) {
                    @Override
                    public void onSuccess(Response<OrderSumberModel> response) {
                        if (response.body().getStatus().equalsIgnoreCase("success")) {
                            progressDialog.dismiss();
                            MyApp.showToast(getString(R.string.bds_zhuxiao_success));
                            finish();
                        } else {
                            MyApp.showToast(response.body().getMsg());
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onStart(Request<OrderSumberModel, ? extends Request> request) {
                        super.onStart(request);

                    }

                    @Override
                    public void onError(Response<OrderSumberModel> response) {
                        super.onError(response);
                        progressDialog.dismiss();
                        MyApp.showToast(getString(R.string.network));
                    }
                });

            }
        });
        builder.show();
    }

    /**
     * 设置承兑商时间
     */
    private void acceptanTime(final String onlineStartTime, final String onlineEndTime, final String onlineTimeState) {
        progressDialog.show();

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount", SPUtils.getString(Const.USERNAME,""));
        if (!TextUtils.isEmpty(onlineStartTime)) {
            hashMap.put("onlineStartTime", onlineStartTime);
        }
        if (!TextUtils.isEmpty(onlineEndTime)) {
            hashMap.put("onlineEndTime", onlineEndTime);
        }

        if (!TextUtils.isEmpty(onlineTimeState)) {
            hashMap.put("onlineTimeState", onlineTimeState + "");
        }

        if (TextUtils.isEmpty(MyApp.BDS_CO_PUBLICKEY)){
            MyApp.showToast(getString(R.string.encryption_failed));
            progressDialog.dismiss();
            return;
        }
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(MyApp.BDS_CO_PUBLICKEY);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getString(R.string.encryption_failed));
            progressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap, "set_acceptant_info", new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    MyApp.showToast(getString(R.string.bds_acceptor_ziaxian_time));
                    if (tv_time.getVisibility()==View.GONE){
                        tv_time.setVisibility(View.VISIBLE);
                        togglebutton.setVisibility(View.VISIBLE);
                       // ivTimeSelect.setVisibility(View.VISIBLE);
                       iv_time_guanli.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(onlineTimeState)) {
                        setToggleButtonState(Boolean.parseBoolean(onlineTimeState));
                    }
                    if (!TextUtils.isEmpty(onlineStartTime)) {
                        tv_time.setText(onlineStartTime + "-" + onlineEndTime);
                    }

                } else {
                    MyApp.showToast(response.body().getMsg());

                }
                progressDialog.dismiss();

            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timePickerDialog != null) {
            timePickerDialog.dismiss();
        }
        progressDialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Listtype!=-1){
            acceptantList(Listtype);
        }


    }

    private TimePickerDialog timePickerDialog = null;

    private void setTimePicker(String startTime, String endTime) {
        if (timePickerDialog == null) {
            timePickerDialog = new TimePickerDialog(this);
            timePickerDialog.setOnDialogOnClick(new TimePickerDialog.OnDialogOnClick() {
                @Override
                public void OnSumbmitListener(String startTime, String endTime) {
                    if (!startTime.equals(endTime)){
                        acceptanTime(startTime, endTime, true + "");
                    }else {
                        MyApp.showToast(getString(R.string.bds_time_cannot_same));//Time cannot be the same
                    }

                }
            });
        }
        timePickerDialog.setTime("","");
        timePickerDialog.show();
    }

    private void setToggleButtonState(boolean state) {

        tooggleOff=state;
        if (state) {
            togglebutton.setImageResource(R.mipmap.mine_accept_timeswitch_open);
        } else {
            togglebutton.setImageResource(R.mipmap.mine_accept_timeswitch_shut);

        }

    }

}
