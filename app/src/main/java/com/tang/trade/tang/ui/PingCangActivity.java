package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.PingCangAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;
import com.tang.trade.tang.net.model.RechargeResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.CallOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.BottomDialog;
import com.tang.trade.utils.SPUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

public class PingCangActivity extends BaseActivity {
    private ArrayList<RechargeResponseModel> listDate= new ArrayList<RechargeResponseModel>();

    @BindView(R.id.lv_pingcang)
    ListView listView;

    @BindView(R.id.iv_select_cash_in_currency)
    LinearLayout iv_select_currency;

    @BindView(R.id.tv_cash_in_currency)
    TextView tv_cash_in_currency;

    @BindView(R.id.ivBack)
    ImageView ivBack;
    PingCangAdapter adapter;
    View foorView=null;
    private Button but_summit;
    private TextView tv_noda;


    String[] strType;
    int currencyListIndex=0;
    private String selectCurrency=null;

    //强平触发价
    private String kyohei = "0.00";


    List<asset_object> objAssets = null;

    private String feed_price = "0.00";


    private String debt = "0.00000";
    private String collateral = "0.00000";

    //保证金比例
    private String margin_level = "0.00";

    private String finalBalanceSymbol = "0.00000";
    private String bds_borrow_Fee = "0.00000";


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            String data = null;
            try {
                data = BitsharesWalletWraper.getInstance().cli_get_bitasset_data(selectCurrency);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(data)) {
                feed_price = data.split(" ")[0];
            }
            if (TextUtils.isEmpty(feed_price)){
                feed_price = "0.00";
            }

        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ping_cang;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivBack){
         finish();
        }else if (view.getId()==R.id.btn_submit){
            setBottomDialog();

        }else if (view.getId()==R.id.iv_select_cash_in_currency){
            setSelectCurrenty();
        }


    }

    @Override
    public void initView() {

        ivBack.setOnClickListener(this);
        adapter=new PingCangAdapter(listDate,this);
        listView.setAdapter(adapter);
        foorView = View.inflate(this,R.layout.piangcang_list_footer,null);

        but_summit=foorView.findViewById(R.id.btn_submit);
        tv_noda=foorView.findViewById(R.id.tv_node);
        but_summit.setOnClickListener(this);
        iv_select_currency.setOnClickListener(this);

    }

    @Override
    public void initData() {
        listDate.add(new RechargeResponseModel(getString(R.string.bds_jie_quate), ""));
        listDate.add(new RechargeResponseModel(getString(R.string.bds_margin2), ""));
        listDate.add(new RechargeResponseModel(getString(R.string.bds_call_Price), ""));

        listView.addFooterView(foorView);
        adapter.notifyDataSetChanged();

        new LoadDataTask().execute("");

    }

    private void setSelectCurrenty(){
        // 点击【充值币种】的场合，弹出单选对话框，选择币种
        final AlertDialog.Builder currencyBuilder = new AlertDialog.Builder(this);
        currencyBuilder.setTitle(getString(R.string.bds_sel_deposit));
        currencyBuilder.setSingleChoiceItems(strType, currencyListIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currencyListIndex!=which){
                    currencyListIndex = which;
                    selectCurrency=strType[currencyListIndex];
                    tv_cash_in_currency.setText(selectCurrency);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setBalance(selectCurrency, SPUtils.getString(Const.USERNAME,""));
                            getKyohei(selectCurrency);
                        }
                    }).start();

                }

                dialog.dismiss();
            }


        });
        currencyBuilder.setNegativeButton(getString(R.string.bds_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        currencyBuilder.create().show();

    }


    private void getKyohei(String symbol){
        kyohei = "0.00000";
        debt = "0.00000";
        collateral = "0.00000";

        String asset_id = "";

        full_account full_account = null;
        try {
            full_account = BitsharesWalletWraper.getInstance().get_full_account(SPUtils.getString(Const.USERNAME,""),true);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (feed_price.equals("")) {
            feed_price = "0";
        }
        for (int j = 0 ; j < objAssets.size() ; j++){
            if (objAssets.get(j).symbol.equalsIgnoreCase(symbol)){
                asset_id = objAssets.get(j).id.toString();
            }
        }
        if (full_account != null){
            List<CallOrder> callOrder = full_account.call_orders;
            if (callOrder != null){
                for (int i = 0 ; i<callOrder.size();i++){
                    String call_asset_id = "";
                    if (!full_account.call_orders.get(i).getQuote_asset_id().equalsIgnoreCase("1.3.0")){
                        call_asset_id = full_account.call_orders.get(i).getQuote_asset_id();
                    }else {
                        call_asset_id = full_account.call_orders.get(i).getBase_asset_id();
                    }

                    if (asset_id.equalsIgnoreCase(call_asset_id)){
                        String base_asset_id = full_account.call_orders.get(i).getBase_asset_id();
                        String baseAmount = full_account.call_orders.get(i).getBase_amount();
                        String quoteAmount = full_account.call_orders.get(i).getQuote_amount();

                        debt = NumberUtils.formatNumber5((Double.parseDouble(full_account.call_orders.get(i).getDebt()) / 100000) + "");
                        collateral = NumberUtils.formatNumber5((Double.parseDouble(full_account.call_orders.get(i).getCollateral()) / 100000) + "");
                        if (base_asset_id.equals("1.3.0")){
                           // kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                            kyohei=CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),10);
                            if (Double.parseDouble(kyohei) != 0){
                                kyohei = CalculateUtils.div(1,Double.parseDouble(kyohei),5);
                            }

                        }else{
                           // kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                            kyohei=CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),10);
                            if (Double.parseDouble(kyohei) != 0){
                                kyohei = CalculateUtils.div(1,Double.parseDouble(kyohei),5);
                            }

                        }

                        if (Double.parseDouble(feed_price) != 0) {
                            margin_level = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(collateral), Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt), Double.parseDouble(feed_price))), 2));
                        }else {
                            margin_level = "0.00";
                        }
                    }

                }
            }

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ww11",debt+" "+collateral + " "+margin_level + " "+kyohei);

                for (int i= 0 ; i<listDate.size() ; i++){
                    if (listDate.get(i).getKey().equals(getString(R.string.bds_jie_quate))){
                        listDate.get(i).setValue(debt+" "+selectCurrency);
                        //tv_noda.setText("你至少需要"+debt+" "+selectCurrency+"进行平仓");

                    }else if (listDate.get(i).getKey().equals(getString(R.string.bds_margin2))){
                        listDate.get(i).setValue(collateral+" BDS");
                    }
//                    else if (listDate.get(i).getKey().equals("保证金比例")){
//                        listDate.get(i).setValue(margin_level);
                    //}
                    else if (listDate.get(i).getKey().equals(getString(R.string.bds_call_Price))){
                        listDate.get(i).setValue(kyohei);
                    }
                }
                adapter.setList(listDate);
                adapter.notifyDataSetChanged();
            }

        });

    }


    private void setBalance(final String symbol, final String account){
        String balance = "0.00000";
        try {
            if (!account.equals("")) {
                String sAssetAmount;
                boolean findAssetInAccount = false;

                //获取所有资产列表
                account_object object = BitsharesWalletWraper.getInstance().get_account_object(account);
                if (object == null) {
                    balance = "0.00000";
                }else {
                    //查询账户id
                    object_id<account_object> loginAccountId = object.id;
                    //获取账户余额列表
                    List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                    if (accountAsset == null) {
                        balance = "0.00000";
                    }
                    //查询资产列表
                    List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                    if (objAssets == null) {
                        balance = "0.00000";
                    }

                    //查询基础货币BDS,和所有智能资产

                    for (asset_object objAsset : objAssets){
                        if (objAsset.symbol.equalsIgnoreCase(symbol)){
                            if (accountAsset.size() > 0) {
                                for (int j = 0; j < accountAsset.size(); j++) {
                                    asset account_asset = accountAsset.get(j);
                                    String sAccount_sid = account_asset.asset_id.toString();
                                    if (sAccount_sid.equals(objAsset.id.toString())) {
                                        //计算账户余额
                                        asset_object.asset_object_legible myasset = objAsset.get_legible_asset_object(account_asset.amount);
                                        sAssetAmount = myasset.count;
                                        balance = sAssetAmount;
                                    }
                                }
                            }

                        }
                    }

                }
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            balance = "0.00000";
        }

        finalBalanceSymbol = balance;

    }


    private void setBottomDialog() {

        String debtChange = "0.00000";
        String collateralChange ="0.00000";
        String finalDebt = "0.00000";

        for (int i = 0;i<listDate.size();i++){
            if (listDate.get(i).getKey().equals(getString(R.string.bds_jie_quate))){
                if (!TextUtils.isEmpty(listDate.get(i).getValue())) {
                    if (Double.parseDouble(listDate.get(i).getValue().split(" ")[0]) == 0) {
                        debtChange = listDate.get(i).getValue().split(" ")[0];
                    } else {
                        debtChange = "-" + listDate.get(i).getValue().split(" ")[0];
                    }
                    finalDebt = listDate.get(i).getValue().split(" ")[0];
                }
            }else  if (listDate.get(i).getKey().equals(getString(R.string.bds_margin2))){
                if (!TextUtils.isEmpty(listDate.get(i).getValue())) {
                    if (Double.parseDouble(listDate.get(i).getValue().split(" ")[0]) == 0) {
                        collateralChange = listDate.get(i).getValue().split(" ")[0];
                    } else {
                        collateralChange = "-" + listDate.get(i).getValue().split(" ")[0];
                    }
                }
            }
        }

        final BottomDialog bottomDialog = new BottomDialog(this);
        bottomDialog.setDialogText(SPUtils.getString(Const.USERNAME,""),collateralChange+" BDS",debtChange+" "+selectCurrency,bds_borrow_Fee+" BDS");
        final String finalDebtChange = debtChange;
        final String finalCollateralChange = collateralChange;
        final String finalDebt1 = finalDebt;
        bottomDialog.setOnDialogClick(new BottomDialog.OnDialogClick() {

            @Override
            public void OnSubmitListener() {
                if (Double.parseDouble(finalDebtChange) == 0 && Double.parseDouble(finalCollateralChange) == 0){
                    MyApp.showToast(getString(R.string.bds_no_close));
                }else {

                   if (Double.parseDouble(finalDebt1)<=Double.parseDouble(finalBalanceSymbol)){


                       httpEveningUp(finalDebtChange,finalCollateralChange);

                   } else {
                       MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                   }

                }
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }


    private void httpEveningUp(String debtChange,String collateralChange){
        progressDialog.show();
        account_object myAccount = null;
        //获取账户 private key
        try {
            myAccount = BitsharesWalletWraper.getInstance().get_account_object(SPUtils.getString(Const.USERNAME,""));
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        //网络异常
        if (null == myAccount) {
            MyApp.showToast(getString(R.string.network));
            progressDialog.dismiss();
            return ;
        }
        types.public_key_type publicKeyType =  myAccount.owner.get_keys().get(0);
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

        //请求public key

        memo_data memo = new memo_data();
        memo.from = myAccount.options.memo_key;
        memo.to = myAccount.options.memo_key;


        Address address = null;
        try {
            address = new Address(BuildConfig.strPubWifKey);
        } catch (MalformedAddressException e) {
            e.printStackTrace();
            MyApp.showToast(getString(R.string.network));
            progressDialog.dismiss();
            return ;
        }
        public_key publicKey = new public_key(address.getPublicKey().toBytes());
        //加密
        memo.set_message(
                privateKey.getPrivateKey(),
                publicKey,
                strPrivateKey,
                1
        );

        String encryptoPrivateKey = memo.get_message_data();

        String command = String.format("borrow_asset %s %s %s %s true",SPUtils.getString(Const.USERNAME,""),debtChange,selectCurrency,collateralChange);

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type", "2");
        hashMap.put("name", SPUtils.getString(Const.USERNAME,""));
        hashMap.put("1", encryptoPrivateKey);
        hashMap.put("2", strPublicKey);
        hashMap.put("command",command);
        //执行命令
        AcceptorApi.acceptantHttp(hashMap,"and_run_command",new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
            @Override
            public void onSuccess(Response<HttpResponseModel> response) {
                HttpResponseModel httpResponseModel = response.body();
                if (httpResponseModel.getStatus().contains("success")){
                    MyApp.showToast(getString(R.string.bds_close_sucessfully));
                    finish();
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

    }


    private int eveningUp(String finalDebtChange,String finalCollateralChange){
        int i = 1;
        String result = "";
        try {
            result = BitsharesWalletWraper.getInstance().cli_borrow_asset(SPUtils.getString(Const.USERNAME,""), finalDebtChange,selectCurrency, finalCollateralChange);

        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(result)){
            i = 1;
        }else if (result.contains("ref_block_num") && result.contains("signatures")){
            i = 0;
        }else  if (result.contains("exception")){
            i = 1;
        }else  if (result.contains("timeout_exception")){
            i = 2;
        }
        return i;
    }


    public void getFeedPrice(String symbol) {
        String command = String.format("get_bitasset_data %s",symbol);
        //执行命令
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type", "1");
        hashMap.put("name", "");
        hashMap.put("1", "");
        hashMap.put("2", "");
        hashMap.put("command",command);
        AcceptorApi.acceptantHttp(hashMap,"and_run_command",new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
            @Override
            public void onSuccess(Response<HttpResponseModel> response) {
                HttpResponseModel body = response.body();
                if (body.getStatus().contains("success")){
                    String quote_asset_id = body.getData().get(0).getCurrent_feed().getSettlement_price().getQuote().getAsset_id();
                    String baseAmount = body.getData().get(0).getCurrent_feed().getSettlement_price().getBase().getAmount();
                    String quoteAmount = body.getData().get(0).getCurrent_feed().getSettlement_price().getQuote().getAmount();
                    if (quote_asset_id.equals("1.3.0")){
                        feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                    }else{
                        feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                    }
                }

            }

            @Override
            public void onStart(Request<HttpResponseModel, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<HttpResponseModel> response) {
                super.onError(response);
            }
        });

    }


    public class LoadDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            if ( Device.pingIpAddress()) {
                try {
                    objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }

                strType = new String[]{"CNY", "USD"};
                selectCurrency = strType[0];

                getFeedPrice(selectCurrency);


                setBalance(selectCurrency,SPUtils.getString(Const.USERNAME,""));
                getKyohei(selectCurrency);
                try {
                    bds_borrow_Fee = BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_UPDATE_LMMIT_ORDER_OPERATION);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
