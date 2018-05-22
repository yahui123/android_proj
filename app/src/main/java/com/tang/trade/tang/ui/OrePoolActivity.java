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
    private void  withdarw_vesting(final String amount,account_object upgradeAccount,String assets_id) {

        progressDialog.show();
        //vesting_balance_object balance_object =null;
        if (vestObjList != null && !vestObjList.isEmpty()&&!TextUtils.isEmpty(fee)) {
            //balance_object=vestObjList.get(positon);
        }else {
            MyApp.showToast(getString(R.string.network));
            progressDialog.dismiss();
            return;
        }

        //获取账户 private key
        types.public_key_type publicKeyType =  upgradeAccount.owner.get_keys().get(0);
        String strPublicKey = publicKeyType.toString();
        types.private_key_type privateKey = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);
        String strPrivateKey = null;

        if (privateKey != null) {
            strPrivateKey = privateKey.toString();
        } else{
            BitsharesWalletWraper.getInstance().clear();
            BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH+CURRTEN_BIN,PASSWORD);
            BitsharesWalletWraper.getInstance().unlock(PASSWORD);
            privateKey = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);
            if (privateKey != null) {
                strPrivateKey = privateKey.toString();
            }
        }

        if (strPrivateKey == null){
            progressDialog.dismiss();
            return;
        }


        //请求public key

        memo_data memo = new memo_data();
        memo.from = upgradeAccount.options.memo_key;
        memo.to = upgradeAccount.options.memo_key;

        try {
            Address address = new Address(BuildConfig.strPubWifKey);
            public_key publicKey = new public_key(address.getPublicKey().toBytes());
            //加密
            memo.set_message(
                    privateKey.getPrivateKey(),
                    publicKey,
                    strPrivateKey,
                    1
            );

            String encryptoData = memo.get_message_data();
            //升级
            String command = String.format("withdraw_vesting \"%s\" \"%s\" \"BDS\" true",assets_id,amount);

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("type", "2");
            hashMap.put("name", SPUtils.getString(Const.USERNAME,""));
            hashMap.put("1", encryptoData);
            hashMap.put("2", strPublicKey);
            hashMap.put("command",command);
            //执行命令
            AcceptorApi.acceptantHttp(hashMap,"and_run_command",new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
                @Override
                public void onSuccess(Response<HttpResponseModel> response) {
                    HttpResponseModel httpResponseModel = response.body();
                    if (httpResponseModel.getStatus().contains("success")){
                        MyApp.showToast(getString(R.string.bds_extraction));
                      startTimer();
                        progressDialog.dismiss();
                    }else {
                        MyApp.showToast(httpResponseModel.getMsg());
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onStart(Request<HttpResponseModel, ? extends Request> request) {
                    super.onStart(request);
                }

                @Override
                public void onError(Response<HttpResponseModel> response) {
                    super.onError(response);
                    MyApp.showToast(getString(R.string.network));
                    progressDialog.dismiss();
                }
            });

        } catch (MalformedAddressException e) {
            e.printStackTrace();
            MyApp.showToast(getString(R.string.bds_extractionFailed));
            progressDialog.dismiss();

        }

    }

    @Override
    public void onBtnOnklick(int position) {
        if (vestObjList.size()>0){
            String str =vestObjList.get(position).Available_to_claim;
            if (!TextUtils.isEmpty(str)&&Double.parseDouble(str)>0){
                if (Double.parseDouble(fee)<Double.parseDouble(balance)){
                    stopTimer();
                    progressDialog.show();
                    if (SPUtils.getBoolean(Const.IS_LIFE_MEMBER,false)) {
                        //cli
//                        if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                            new OrePoolTask().execute(vestObjList.get(position).id,vestObjList.get(position).Available_to_claim);
//                        } else {
                            withdarw_vesting(vestObjList.get(position).Available_to_claim, upgradeAccount,vestObjList.get(position).id);
//                        }
                    } else {
                        MyApp.showToast(getString(R.string.bds_extractionFailed));
                        progressDialog.dismiss();
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


    public class OrePoolTask extends AsyncTask<String, Void,Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int result =  BitsharesWalletWraper.getInstance().cli_withdraw_vesting(params[0],params[1]);
            Log.i("register_account",result+"");

            return result;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if (i == 1){
                MyApp.showToast(getString(R.string.bds_bds_lingqu));
                startTimer();
            }else if ( i == 0){
                MyApp.showToast(getString(R.string.bds_bds_fail));
            }else if ( i == -1){
                MyApp.showToast(getString(R.string.network));
            }
            progressDialog.dismiss();

        }
    }


}
