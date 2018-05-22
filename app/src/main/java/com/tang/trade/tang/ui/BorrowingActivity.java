package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.acceptormodel.AccountBorrowModel;
import com.tang.trade.tang.net.acceptormodel.GongGaoModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;
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
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.BottomDialog;
import com.tang.trade.tang.widget.MyEditextSeekbar;
import com.tang.trade.tang.widget.MyJieRuditextSeekbar;
import com.tang.trade.utils.SPUtils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

/**
 * Created by Administrator on 2018/2/5.
 */

public class BorrowingActivity extends BaseActivity {

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.iv_select_cash_in_currency)
    LinearLayout iv_select_currency;

    @BindView(R.id.tv_cash_in_currency)
    TextView tv_cash_in_currency;

    @BindView(R.id.tv_select_currency)
    TextView tv_select_currency;

    @BindView(R.id.btn_cancel)
    Button btn_cancel;//重置

    @BindView(R.id.btn_confirm)
    Button btn_confirm;//提取

    @BindView(R.id.tv_keyong_quede)
    TextView tv_keyong_quede;

    @BindView(R.id.et_jieru_aquate)
    EditText et_jieru_aquate;

    @BindView(R.id.tv_qingxingpingcang)
    TextView tv_qingxingpingcang;

    @BindView(R.id.tv_qing_symloy)
    TextView tv_qing_symloy;


    @BindView(R.id.tv_wei_price)
    TextView tv_wei_price;

    @BindView(R.id.tv_wei_symbol)
    TextView tv_wei_symbol;

    @BindView(R.id.myJieRuditextSeekbar)
    MyJieRuditextSeekbar myJieRuditextSeekbar;



    String[] strType;
    int currencyListIndex=0;
    private String selectCurrency=null;



    //强平触发价
    private String kyohei = "0.00";


    List<asset_object> objAssets = null;//资产

    private String feed_price = "0.00";//喂价


    private String debt = "0.00";//借入
    private String debt1 = "0.00";//变化后的借入借入
    private String collateral = "0.00";//保证金

    private String finalBalance = "0.00000";//bds余额
    private String finalBalanceSymbol = "0.00000";//借入币种余额
    private String bds_borrow_Fee = "0.00000";//接入手续费
    private int i = 0;//

    private double per = 1.5;//比例
    private final static int ACCURACYNUM=2;

//    private String strProgress="0.00";
    private String collateral2="0.00";//变化后的保证金

    Handler handler = new Handler(){};

    Runnable runnable =new Runnable() {
        @Override
        public void run() {
            try {
                String data = BitsharesWalletWraper.getInstance().cli_get_bitasset_data(selectCurrency);
                if (!TextUtils.isEmpty(data)) {
                    feed_price = data.split(" ")[0];
                    per = Double.parseDouble(data.split(" ")[1])/1000;
                }
                if (TextUtils.isEmpty(feed_price)){
                    feed_price = "0.00";
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Double.parseDouble(feed_price) == 0){
                        tv_wei_price.setText("0.00000");
                    }else {
                        tv_wei_price.setText(CalculateUtils.div(1,Double.parseDouble(feed_price),5));
                    }
                }
            });


        }
    };

    Runnable runnable2=new Runnable() {
        @Override
        public void run() {
            getKyohei(selectCurrency);
            setBalance(selectCurrency, SPUtils.getString(Const.USERNAME,""));
            setBalance("BDS", SPUtils.getString(Const.USERNAME,""));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }
    };

    @Override
    public void onClick(View view) {
        if (UtilOnclick.isFastClick()){
            if (view.getId()==R.id.ivBack){
                finish();
            }else if (view.getId()==R.id.iv_select_cash_in_currency){
                myJieRuditextSeekbar.setType(true);
                setSelectCurrenty();

            }else if (view.getId()==R.id.btn_cancel){
                myJieRuditextSeekbar.setType(true);

                if (!Device.pingIpAddress()){
                    MyApp.showToast(getString(R.string.network));
                    return;

                }
                progressDialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resettingData(selectCurrency);
                    }
                },100);




            }else if (view.getId()==R.id.btn_confirm){
                if (!TextUtils.isEmpty(collateral2)){
                    if (TextUtils.isEmpty(et_jieru_aquate.getText().toString())){
                        MyApp.showToast(getString(R.string.bds_input_jieru_amount));
                        return;
                    }
                    if (Double.parseDouble(collateral2) == 0 && Double.parseDouble(et_jieru_aquate.getText().toString()) == 0){
                        MyApp.showToast(getString(R.string.bds_fill_borrowed_amount));
                        return;
                    }
                    if (Double.parseDouble(feed_price) == 0){
                        MyApp.showToast(getString(R.string.bds_no_feed_price));
                        return;
                    }
                    setBottomDialog();
                }else {
                    MyApp.showToast(getString(R.string.bds_input_margin_amount));
                }
            }

        }


    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_borrowing;
    }

    @Override
    public void initView() {

        ivBack.setOnClickListener(this);
        iv_select_currency.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

        et_jieru_aquate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String temp = editable.toString().trim();
                if (editable.toString().startsWith(".")) {
                    et_jieru_aquate.setText("");
                    return;
                }
                int posDot = temp.indexOf(".");
                if (posDot>0&&temp.length() - posDot - 1 > ACCURACYNUM) {
                    editable.delete(posDot + ACCURACYNUM + 1, posDot + ACCURACYNUM + 2);
                    return;
                }

                debt1 = editable.toString().trim();

                if (!TextUtils.isEmpty(debt1) && !debt1.equals("0")&& Double.parseDouble(debt1) != 0 && Double.parseDouble(feed_price) != 0) {
                }else {
                    debt1="0.00";
                }
                if (myJieRuditextSeekbar.getType()==1){
                    myJieRuditextSeekbar.initDataDebt(debt1);
                }


            }
        });
        myJieRuditextSeekbar.setRefreshData(new MyJieRuditextSeekbar.onRefreshData() {
            @Override
            public void onRefresh(String strProgres, String collateral2s) {
                collateral2=collateral2s;
                kyohei = CalculateUtils.div(Double.parseDouble(collateral2s),Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt1),per)),2);
                if (Double.parseDouble(kyohei) != 0) {
                    tv_qingxingpingcang.setText(CalculateUtils.div(1, Double.parseDouble(kyohei), 5));
                }else {
                    tv_qingxingpingcang.setText("0.00000");
                }
            }
        });

    }

    @Override
    public void initData() {

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("encmsg","");
        AcceptorApi.acceptantHttp(hashMap,"account_borrow",new JsonCallBack<AccountBorrowModel>(AccountBorrowModel.class) {
            @Override
            public void onSuccess(Response<AccountBorrowModel> response) {
                if (response.body().getStatus().equals("success")){
                    AccountBorrowModel.DataBean dataBean=response.body().getData().get(0);
                    myJieRuditextSeekbar.setSeekBar(dataBean.getMinBorrow(),dataBean.getMaxBorrow());
                }else {
                    myJieRuditextSeekbar.setSeekBar("3","6");
                }
                new LoadDataTask().execute();


            }

            @Override
            public void onStart(Request<AccountBorrowModel, ? extends Request> request) {
                super.onStart(request);
                progressDialog.show();


            }

            @Override
            public void onError(Response<AccountBorrowModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();

            }
        });



    }

    private void resettingData(String selectCurrency){

        et_jieru_aquate.setText("0");

        getFeedPrice(selectCurrency);

        new Thread(runnable2).start();

        tv_wei_symbol.setText(selectCurrency+"/BDS");
        tv_qing_symloy.setText(selectCurrency+"/BDS");

    }


    public class LoadDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Integer doInBackground(String... params) {
            if (Device.pingIpAddress()) {
                try {
                    objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }

                strType = new String[]{"CNY", "USD"};
                selectCurrency = strType[0];

                getFeedPrice(selectCurrency);

                getKyohei(selectCurrency);
                setBalance(selectCurrency, SPUtils.getString(Const.USERNAME,""));
                setBalance("BDS", SPUtils.getString(Const.USERNAME,""));
                try {
                    bds_borrow_Fee = BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_UPDATE_LMMIT_ORDER_OPERATION);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
            i = 1;
            return null;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            progressDialog.dismiss();
        }
    }


    private void getKyohei(String symbol){
        kyohei = "0.00";
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

        if (objAssets != null) {
            for (int j = 0; j < objAssets.size(); j++) {
                if (objAssets.get(j).symbol.equalsIgnoreCase(symbol)) {
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
                            debt1=debt;
                            collateral = NumberUtils.formatNumber5((Double.parseDouble(full_account.call_orders.get(i).getCollateral()) / 100000) + "");
                            collateral2=collateral;
                            if (base_asset_id.equals("1.3.0")){
                                kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                            }else{
                                kyohei = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                            }
                        }
                    }
                }

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {



                    if (Double.parseDouble(kyohei) == 0){
                        tv_qingxingpingcang.setText("0.00000");
                    }else {
                        tv_qingxingpingcang.setText(CalculateUtils.div(1,Double.parseDouble(kyohei),5));
                    }

                    et_jieru_aquate.setText(debt);
                    if (!myJieRuditextSeekbar.isFirst()){
                        myJieRuditextSeekbar.initData(debt,collateral,feed_price);
                    }else {
                        myJieRuditextSeekbar.initUpdateData(debt,collateral,feed_price);
                    }
                }
            });
        }

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

        final String finalBalance1 = balance;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(symbol)) {
                    if (symbol.equals("BDS")) {

                        finalBalance = finalBalance1;
                        myJieRuditextSeekbar.updateBDSBlance(finalBalance);
                    } else {
                        finalBalanceSymbol = finalBalance1;
                        tv_keyong_quede.setText(getString(R.string.bds_keyong_quate) + NumberUtils.formatNumber5(finalBalance1) + " " + symbol);
                    }
                }
            }
        });

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
                    String quote_asset_id = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getQuote().getAsset_id();
                    String baseAmount = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getBase().getAmount();
                    String quoteAmount = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getQuote().getAmount();
                    if (quote_asset_id.equals("1.3.0")){
                        feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                    }else{
                        feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                    }
                }
                if (Double.parseDouble(feed_price) == 0){
                    tv_wei_price.setText("0.00000");
                }else {
                    tv_wei_price.setText(CalculateUtils.div(1,Double.parseDouble(feed_price),5));
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


    private void setSelectCurrenty(){
        // 点击【充值币种】的场合，弹出单选对话框，选择币种
        final AlertDialog.Builder currencyBuilder = new AlertDialog.Builder(this);
        currencyBuilder.setTitle(getString(R.string.bds_select_currency_type));
        currencyBuilder.setSingleChoiceItems(strType, currencyListIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currencyListIndex!=which){
                    currencyListIndex = which;
                    selectCurrency=strType[currencyListIndex];
                    tv_cash_in_currency.setText(selectCurrency);
                    tv_select_currency.setText(selectCurrency);
//
//                    if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()){
//                        handler.post(runnable);
//                    }else {
//                        getFeedPrice(selectCurrency);
//                    }
//
//                    et_jieru_aquate.setText("0.00");
//
//                    new Thread(runnable2).start();
//                    tv_wei_symbol.setText(selectCurrency+"/BDS");
//                    tv_qing_symloy.setText(selectCurrency+"/BDS");
                    progressDialog.show();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resettingData(selectCurrency);
                        }
                    },100);

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

    private void setBottomDialog() {
        String bds_debt = "0.00000";
        String bds_collateral = "0.00000";
        if (!TextUtils.isEmpty(et_jieru_aquate.getText().toString())){
            bds_debt = et_jieru_aquate.getText().toString();
        }

        bds_collateral=collateral2;

        //变化量
        final String debtChange = NumberUtils.formatNumber5(CalculateUtils.sub(Double.parseDouble(bds_debt), Double.parseDouble(debt)) + "");
        final String collateralChange = NumberUtils.formatNumber5(CalculateUtils.sub(Double.parseDouble(bds_collateral), Double.parseDouble(collateral)) + "");

        final BottomDialog bottomDialog = new BottomDialog(this);
        bottomDialog.setDialogText(SPUtils.getString(Const.USERNAME,""),collateralChange+" BDS",debtChange+" "+selectCurrency,bds_borrow_Fee+" BDS");
        bottomDialog.show();
        bottomDialog.setOnDialogClick(new BottomDialog.OnDialogClick() {

            @Override
            public void OnSubmitListener() {

                if (Double.parseDouble(debtChange) == 0 && Double.parseDouble(collateralChange) == 0){
                    MyApp.showToast(getString(R.string.bds_please_adjust_amount));
                }else {
                    if (Double.parseDouble(collateralChange)>0){
                        if (Double.parseDouble(collateralChange)+Double.parseDouble(bds_borrow_Fee)>Double.parseDouble(finalBalance)){
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            return;
                        }
                    }

                    if (Double.parseDouble(debtChange)<0){
                        if (-Double.parseDouble(debtChange)>Double.parseDouble(finalBalanceSymbol)){
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            return;
                        }
                    }

                    httpBorrow(debtChange,collateralChange);


                }
                bottomDialog.dismiss();
            }
        });

    }


    private void httpBorrow(String debtChange,String collateralChange){
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
                    MyApp.showToast(getString(R.string.bds_borrowed_successfully));
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

    private int borrow(String debtChange,String collateralChange){
        int i = 1;
        String result = "";
        try {
            result = BitsharesWalletWraper.getInstance().cli_borrow_asset(SPUtils.getString(Const.USERNAME,""),debtChange,selectCurrency,collateralChange);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        Log.i("result",result);
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

    public class borrowTask extends AsyncTask<String, Void,Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            return borrow(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if (i == 1){
                MyApp.showToast(getString(R.string.bds_borrow_fail));
            }else if ( i == 0){
                MyApp.showToast(getString(R.string.bds_borrow_successfully));
                finish();
            }else if ( i == 2){
                MyApp.showToast(getString(R.string.network));
            }
            progressDialog.dismiss();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
