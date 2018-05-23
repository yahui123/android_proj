package com.tang.trade.tang.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.PoolAdapter;
import com.tang.trade.tang.net.AcceptorApi;

import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;

import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.chain.vesting_balance_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.base.BaseActivity;

import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import io.reactivex.disposables.Disposable;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;
import static com.tang.trade.tang.ui.loginactivity.LoginActivity.isLifeMember;

public class OrePoolActivity extends BaseActivity implements PoolAdapter.onOnButtonViewLister{
    private String TAG="OrePoolActivity";
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.lv_pool)
    ListView lv_pool;
    @BindView(R.id.loading)
    LoadingView loading;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;

    TextView tv_hoder_title,iv_baseSymbol,tvAsset;
    PoolAdapter adapter;
    private String fee;    //手续费
    private String poolAmount="0.00000";//总量
    private View hoder;
//    //领取额度
    List<vesting_balance_object> vestObjList = new ArrayList<>();
    List<vesting_balance_object> vestObjList2 = new ArrayList<>();
    private String balance="0";

    private account_object upgradeAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ore_pool;
    }

    @Override
    public void onClick(View v) {
        vesting_balance_object balance_object =null;
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }

    }

    @Override
    public void initView() {
        hoder=View.inflate(this,R.layout.header_my_asster,null);
        tv_hoder_title=hoder.findViewById(R.id.tv_hoder_title);
        iv_baseSymbol=hoder.findViewById(R.id.iv_baseSymbol);
        tvAsset=hoder.findViewById(R.id.tvAsset);
        tv_hoder_title.setText(getText(R.string.bds_ore_pool_amount));
        iv_baseSymbol.setText("( "+"BDS"+" )");
        lv_pool.addHeaderView(hoder);
        iv_back.setOnClickListener(this);
        adapter=new PoolAdapter(vestObjList,this);
        lv_pool.setAdapter(adapter);
        adapter.setOnItemViewLister(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                balance=getBalance("BDS",SPUtils.getString(Const.USERNAME,""));

            }
        }).start();
        startTimer();
    }


    private TimerTask timerTask;
    private Timer timer;
    private void startTimer() {
        stopTimer();

        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                synchronized (this) {
                    try {
                      setData();
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        };

        timer.schedule(timerTask, 200, 5000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            loading.setVisibility(View.GONE);
            tvAsset.setText(poolAmount);
            if (vestObjList.size()==0){
                if (ll_nodata.getVisibility()==View.GONE){
                    ll_nodata.setVisibility(View.VISIBLE);
                }
            }else {
                if (ll_nodata.getVisibility()==View.VISIBLE){
                    ll_nodata.setVisibility(View.GONE);
                }
                adapter.setFee(fee);
                adapter.setList(vestObjList);
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void setData(){
        final String loginUser =  SPUtils.getString(Const.USERNAME,"");
        try {
            upgradeAccount = BitsharesWalletWraper.getInstance().get_account_object(loginUser);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (upgradeAccount != null) {
            vesting_balance_object balance_jianZheng = null; //见证奖励
            vesting_balance_object balance_fee = null;//手续费分润
            try {
                fee = BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_VESTING_WITHDRAW_OPERATION);
                poolAmount = BitsharesWalletWraper.getInstance().get_witness_budget() + "";
                double dou = 100000;
                if (!TextUtils.isEmpty(poolAmount)) {
                    poolAmount = CalculateUtils.div(Double.parseDouble(poolAmount), dou, 5);
                }
                //领取额度
                if (!TextUtils.isEmpty(loginUser)) {
                    vestObjList2 = BitsharesWalletWraper.getInstance().get_vesting_balances(loginUser);
                    vestObjList.clear();
                    if (vestObjList2!=null&&vestObjList2.size()>0){
                        for (int i = 0 ; i < vestObjList2.size() ; i ++){
                            if (Double.parseDouble(vestObjList2.get(i).TotalBalance) > 0){
                                vestObjList.add(vestObjList2.get(i));
                            }
                        }
                    }

                }


            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
        handler.sendEmptyMessage(0);
    }

    @Override
    public void initData() {

    }
    private void  withdarw_vesting(final String amount,String assets_id) {

        //vesting_balance_object balance_object =null;
        if (vestObjList != null && !vestObjList.isEmpty()&&!TextUtils.isEmpty(fee)) {
            //balance_object=vestObjList.get(positon);
        }else {
            MyApp.showToast(getString(R.string.network));
            return;
        }

        BorderlessDataManager.getInstance().withdrawVestingWebSocket(assets_id, SPUtils.getString(Const.USERNAME, ""), amount, "BDS", new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                MyApp.showToast(getString(R.string.bds_extractionFailed));
                progressDialog.dismiss();
            }

            @Override
            public void onSubscribe(Disposable d) {
                progressDialog.show();
            }

            @Override
            public void onNext(Object o) {
                MyApp.showToast(getString(R.string.bds_extraction));
                startTimer();
                progressDialog.dismiss();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @Override
    public void onBtnOnklick(int position) {
        if (vestObjList.size()>0){
            String str =vestObjList.get(position).Available_to_claim;
            if (!TextUtils.isEmpty(str)&&Double.parseDouble(str)>0){
                if (Double.parseDouble(fee)<Double.parseDouble(balance)){
                    stopTimer();
                    if (SPUtils.getBoolean(Const.IS_LIFE_MEMBER,false)) {

                        withdarw_vesting(vestObjList.get(position).Available_to_claim,vestObjList.get(position).id);

                    } else {
                        MyApp.showToast(getString(R.string.bds_extractionFailed));
                    }

                }else {
                    MyApp.showToast(getString(R.string.bds_banlance_buzu));
                }


            }else {
                MyApp.showToast(getString(R.string.bds_bds_null));
            }

        }else {
            MyApp.showToast(getString(R.string.bds_bds_null));
        }

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




}
