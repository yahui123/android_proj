package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextUtils;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.acceptormodel.AccountBorrowModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BorrowUtil;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.BottomDialog;
import com.tang.trade.utils.SPUtils;


import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;


public class JieRuActivity extends BaseActivity implements BorrowUtil.OnUpdateListener {


    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.line_fee)
    LinearLayout line_fee;

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

    @BindView(R.id.tv_baozheng_quede)
    TextView tv_baozheng_quede;

    @BindView(R.id.et_baozheng_quate)
    EditText et_baozheng_quate;

    @BindView(R.id.tv_qingxingpingcang)
    TextView tv_qingxingpingcang;

    @BindView(R.id.tv_qing_symloy)
    TextView tv_qing_symloy;

    @BindView(R.id.tv_bili)
    EditText tv_bili;

    @BindView(R.id.tv_wei_price)
    TextView tv_wei_price;

    @BindView(R.id.tv_wei_symbol)
    TextView tv_wei_symbol;

    @BindView(R.id.seekbar)
    AppCompatSeekBar seekBar;

    @BindView(R.id.tv_minSumber)
    TextView tv_minSumber;

    @BindView(R.id.tv_maxSumber)
    TextView tv_maxSumber;


    String[] strType;
    int currencyListIndex = 0;
    private String selectCurrency = null;
    private boolean isUpdate = false; //seekbar 是否可以联动
    private boolean isFirst = true;
    private final static int INNIDATA = 1;//初始化数据
    private final static int UPDATADATA = 2;//初始化数据

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            et_jieru_aquate.setText(NumberUtils.formatNumber2(borrowUtil.debt));

            if (Double.parseDouble(borrowUtil.kyohei) != 0) {
                tv_qingxingpingcang.setText(CalculateUtils.div(1, Double.parseDouble(borrowUtil.kyohei), 5));
            } else {
                tv_qingxingpingcang.setText("0.00000");
            }


            if (Double.parseDouble(borrowUtil.kyohei) == 0) {
                tv_wei_price.setText("0.00000");
            } else {
                tv_wei_price.setText(CalculateUtils.div(1, Double.parseDouble(borrowUtil.kyohei), 5));
            }
            if (Double.parseDouble(borrowUtil.collateral) == 0) {
                et_baozheng_quate.setText("0.00000");
            } else {
                et_baozheng_quate.setText(NumberUtils.formatNumber2(borrowUtil.collateral));
            }
            tv_bili.setText(NumberUtils.formatNumber2(borrowUtil.strProgress));
            String strProgress = borrowUtil.strProgress;
            if (!borrowUtil.isStrProgress(borrowUtil.strProgress, borrowUtil.minProgress, borrowUtil.maxProgress)) {
                strProgress = borrowUtil.getStrProgress(borrowUtil.strProgress, borrowUtil.minProgress, borrowUtil.maxProgress);
            }
            int progress = Integer.parseInt(CalculateUtils.mulScaleHALF_DOWN(CalculateUtils.sub(strProgress, borrowUtil.minProgress), "100", 0));
            seekBar.setProgress(progress);


            tv_keyong_quede.setText(borrowUtil.selectBalance + selectCurrency);
            tv_baozheng_quede.setText(borrowUtil.bdsBalance + " BDS");
            progressDialog.dismiss();


            if (msg.what == INNIDATA) {
                isFirst = true;
                line_fee.getViewTreeObserver().addOnGlobalLayoutListener(ViewTreeObserver);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_jie_ru;
    }

    @Override
    public void onClick(View view) {
        if (UtilOnclick.isFastClick()) {
            if (view.getId() == R.id.ivBack) {
                finish();
            } else if (view.getId() == R.id.iv_select_cash_in_currency) {
                setSelectCurrenty();

            } else if (view.getId() == R.id.btn_cancel) {

                borrowUtil.calnalDate();
                //line_fee.getViewTreeObserver().removeOnGlobalLayoutListener(ViewTreeObserver);
                isUpdate = false;
                handler.sendEmptyMessage(2);

            } else if (view.getId() == R.id.btn_confirm) {
                if (!TextUtils.isEmpty(borrowUtil.collateral2)) {
                    if (TextUtils.isEmpty(et_jieru_aquate.getText().toString())) {
                        MyApp.showToast(getString(R.string.bds_input_jieru_amount));
                        return;
                    }
                    if (Double.parseDouble(borrowUtil.collateral2) == 0 && Double.parseDouble(et_jieru_aquate.getText().toString()) == 0) {
                        MyApp.showToast(getString(R.string.bds_fill_borrowed_amount));
                        return;
                    }
                    if (Double.parseDouble(borrowUtil.feed_price) == 0) {
                        MyApp.showToast(getString(R.string.bds_no_feed_price));
                        return;
                    }
                    setBottomDialog();
                } else {
                    MyApp.showToast(getString(R.string.bds_input_margin_amount));
                }
            }
        }
    }

    BorrowUtil borrowUtil = new BorrowUtil();


    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isUpdate) {
                borrowUtil.setCollateral(progress);
            } else {
                isUpdate = true;
            }


        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    ViewTreeObserver.OnGlobalLayoutListener ViewTreeObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            line_fee.getWindowVisibleDisplayFrame(r);
            int screenHeight = line_fee.getRootView()
                    .getHeight();
            int heightDifference = screenHeight - (r.bottom);
            if (heightDifference > 200) {
                isUpdate = false;

                //软键盘显示
            } else {
                //软键盘隐藏
                isUpdate = true;
                if (!isFirst) {
                    String jieru = et_jieru_aquate.getText().toString().trim();
                    String balace1 = et_baozheng_quate.getText().toString().trim();
                    String strBili = tv_bili.getText().toString().trim();
                    if (TextUtils.isEmpty(jieru)) {
                        jieru = "0.00";
                    }
                    if (TextUtils.isEmpty(balace1)) {
                        balace1 = "0.00";
                    }
                    if (TextUtils.isEmpty(strBili)) {
                        strBili = "0.00";
                    }

                    borrowUtil.setCloseSoftKeyboard(jieru, balace1, strBili);
                } else {
                    isFirst = false;
                }


            }
        }


    };


    @Override
    public void initView() {
        borrowUtil.setOnUpdateListener(this);
        tv_bili.setText("3.00");

        ivBack.setOnClickListener(this);
        iv_select_currency.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
        BorrowUtil.setPricePoint(et_jieru_aquate, 2);
        BorrowUtil.setPricePoint(et_baozheng_quate, 2);
        BorrowUtil.setPricePoint(tv_bili, 2);


        strType = new String[]{"CNY", "USD"};
        selectCurrency = strType[0];
        borrowUtil.selectCurrency = selectCurrency;


    }


    @Override
    public void initData() {

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("encmsg", "");
        AcceptorApi.acceptantHttp(hashMap, "account_borrow", new JsonCallBack<AccountBorrowModel>(AccountBorrowModel.class) {
            @Override
            public void onSuccess(Response<AccountBorrowModel> response) {
                if (response.body().getStatus().equals("success")) {
                    AccountBorrowModel.DataBean dataBean = response.body().getData().get(0);
                    String minProgress = dataBean.getMinBorrow();
                    String maxPorgress = dataBean.getMaxBorrow();
                    String strProgress = CalculateUtils.sub(maxPorgress, minProgress);
                    borrowUtil.minProgress = dataBean.getMinBorrow();
                    borrowUtil.maxProgress = dataBean.getMaxBorrow();
                    tv_minSumber.setText(NumberUtils.formatNumber2(minProgress));
                    tv_maxSumber.setText(NumberUtils.formatNumber2(maxPorgress));
                    tv_bili.setText(NumberUtils.formatNumber2(minProgress));
                    seekBar.setMax(Integer.parseInt(CalculateUtils.mulScaleHALF_DOWN(strProgress, "100", 0)));
                    seekBar.setProgress(0);
                    seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

                    try {
//                        if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                            getFeedPrice(selectCurrency);
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    try {
//                                        borrowUtil.intiData();
//                                        handler.sendEmptyMessage(INNIDATA);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                            }).start();
//
//                        } else {
                            getFeedPrice2(selectCurrency);
//                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    seekBar.setProgress(0);
                    seekBar.setMax(300);
                    seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                }


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

    private void setSelectCurrenty() {
        // 点击【充值币种】的场合，弹出单选对话框，选择币种
        final AlertDialog.Builder currencyBuilder = new AlertDialog.Builder(this);
        currencyBuilder.setTitle(getString(R.string.bds_select_currency_type));
        currencyBuilder.setSingleChoiceItems(strType, currencyListIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currencyListIndex != which) {
                    currencyListIndex = which;
                    selectCurrency = strType[currencyListIndex];
                    tv_cash_in_currency.setText(selectCurrency);
                    tv_select_currency.setText(selectCurrency);
                    progressDialog.show();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                                    getFeedPrice(selectCurrency);
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//
//                                            try {
//                                                borrowUtil.updateDate(selectCurrency);
//                                                line_fee.getViewTreeObserver().removeOnGlobalLayoutListener(ViewTreeObserver);
//                                                isUpdate = false;
//                                                handler.sendEmptyMessage(INNIDATA);
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//
//                                        }
//                                    }).start();
//
//                                } else {
                                    getFeedPrice2(selectCurrency);
//                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 100);

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

    @Override
    public void onUpadeChangeString(String strProgress, String collateral, String isFlay) {
        if (isUpdate && !TextUtils.isEmpty(isFlay)) {
            if (isFlay.equals("debt")) {
                tv_bili.setText(strProgress);
                this.et_baozheng_quate.setText(collateral);

            } else if (isFlay.equals("collateral")) {
                tv_bili.setText(strProgress);
                seekBar.setProgress(borrowUtil.getIntProgress(strProgress));
                this.et_baozheng_quate.setText(collateral);
            } else {
                seekBar.setProgress(borrowUtil.getIntProgress(strProgress));
                this.et_baozheng_quate.setText(collateral);

            }


        }

        isUpdate = true;

    }

    @Override
    public void onUpadeChangeSeekbar(String strProgress, String collateral) {
        if (isUpdate) {
            this.et_baozheng_quate.setText(collateral);
            tv_bili.setText(strProgress);
        }

    }


    public void getFeedPrice(String selectCurrency) throws NetworkStatusException, JSONException {

        String data = BitsharesWalletWraper.getInstance().cli_get_bitasset_data(selectCurrency);
        if (!TextUtils.isEmpty(data)) {
            borrowUtil.feed_price = data.split(" ")[0];
            borrowUtil.per = Double.parseDouble(data.split(" ")[1]) / 1000;
        }
        if (TextUtils.isEmpty(borrowUtil.feed_price) || Double.parseDouble(borrowUtil.feed_price) == 0) {
            borrowUtil.feed_price = "0.00000";
        }

    }

    private void getFeedPrice2(String symbol) throws IOException {


        //执行命令
        String command = String.format("get_bitasset_data %s", symbol);
        //执行命令
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type", "1");
        hashMap.put("name", "");
        hashMap.put("1", "");
        hashMap.put("2", "");
        hashMap.put("command", command);
        AcceptorApi.acceptantHttp(hashMap, "and_run_command", new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
            @Override
            public void onSuccess(Response<HttpResponseModel> response) {
                HttpResponseModel body = response.body();
                if (body.getStatus().contains("success")) {
                    String quote_asset_id = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getQuote().getAsset_id();
                    String baseAmount = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getBase().getAmount();
                    String quoteAmount = body.getData().get(0).getCurrent_feed().getCore_exchange_rate().getQuote().getAmount();
                    if (quote_asset_id.equals("1.3.0")) {
                        borrowUtil.feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount), Double.parseDouble(baseAmount), 2));
                    } else {
                        borrowUtil.feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount), Double.parseDouble(quoteAmount), 2));
                    }
                    try {
                        borrowUtil.intiData();
                        handler.sendEmptyMessage(INNIDATA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void setBottomDialog() {
        String bds_debt = "0.00000";
        String bds_collateral = "0.00000";
//        if (!TextUtils.isEmpty(et_jieru_aquate.getText().toString())){
//            bds_debt = et_jieru_aquate.getText().toString();
//        }
        bds_debt = borrowUtil.debt1;

        bds_collateral = borrowUtil.collateral2;

        //变化量
        final String debtChange = NumberUtils.formatNumber5(CalculateUtils.sub(Double.parseDouble(borrowUtil.debt1), Double.parseDouble(borrowUtil.debt)) + "");
        final String collateralChange = NumberUtils.formatNumber5(CalculateUtils.sub(Double.parseDouble(bds_collateral), Double.parseDouble(borrowUtil.collateral2)) + "");

        final BottomDialog bottomDialog = new BottomDialog(this);
        bottomDialog.setDialogText(SPUtils.getString(Const.USERNAME, ""), collateralChange + " BDS", debtChange + " " + selectCurrency, borrowUtil.bds_borrow_Fee + " BDS");
        bottomDialog.show();
        bottomDialog.setOnDialogClick(new BottomDialog.OnDialogClick() {

            @Override
            public void OnSubmitListener() {

                if (Double.parseDouble(debtChange) == 0 && Double.parseDouble(collateralChange) == 0) {
                    MyApp.showToast(getString(R.string.bds_please_adjust_amount));
                } else {
                    if (Double.parseDouble(collateralChange) > 0) {
                        if (Double.parseDouble(collateralChange) + Double.parseDouble(borrowUtil.bds_borrow_Fee) > Double.parseDouble(borrowUtil.bdsBalance)) {
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            return;
                        }
                    }

                    if (Double.parseDouble(debtChange) < 0) {
                        if (-Double.parseDouble(debtChange) > Double.parseDouble(borrowUtil.selectBalance)) {
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            return;
                        }
                    }


//                    if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                        new JieRuActivity.borrowTask().execute(debtChange, collateralChange);
//                    } else {
                        httpBorrow(debtChange, collateralChange);
//                    }

                }
                bottomDialog.dismiss();
            }
        });

    }


    private void httpBorrow(String debtChange, String collateralChange) {
        progressDialog.show();
        account_object myAccount = null;
        //获取账户 private key
        try {
            myAccount = BitsharesWalletWraper.getInstance().get_account_object(SPUtils.getString(Const.USERNAME, ""));
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        //网络异常
        if (null == myAccount) {
            MyApp.showToast(getString(R.string.network));
            progressDialog.dismiss();
            return;
        }
        types.public_key_type publicKeyType = myAccount.owner.get_keys().get(0);
        String strPublicKey = publicKeyType.toString();
        types.private_key_type privateKey = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);
        String strPrivateKey = null;

        if (privateKey != null) {
            strPrivateKey = privateKey.toString();
        } else {
            BitsharesWalletWraper.getInstance().clear();
            BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + CURRTEN_BIN, PASSWORD);
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
            return;
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

        String command = String.format("borrow_asset %s %s %s %s true", SPUtils.getString(Const.USERNAME, ""), debtChange, selectCurrency, collateralChange);

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("type", "2");
        hashMap.put("name", SPUtils.getString(Const.USERNAME, ""));
        hashMap.put("1", encryptoPrivateKey);
        hashMap.put("2", strPublicKey);
        hashMap.put("command", command);
        //执行命令
        AcceptorApi.acceptantHttp(hashMap, "and_run_command", new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
            @Override
            public void onSuccess(Response<HttpResponseModel> response) {
                HttpResponseModel httpResponseModel = response.body();
                if (httpResponseModel.getStatus().contains("success")) {
                    MyApp.showToast(getString(R.string.bds_borrowed_successfully));
                    finish();
                    progressDialog.dismiss();
                } else {
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

    private int borrow(String debtChange, String collateralChange) {
        int i = 1;
        String result = "";
        try {
            result = BitsharesWalletWraper.getInstance().cli_borrow_asset(SPUtils.getString(Const.USERNAME, ""), debtChange, selectCurrency, collateralChange);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        Log.i("result", result);
        if (TextUtils.isEmpty(result)) {
            i = 1;
        } else if (result.contains("ref_block_num") && result.contains("signatures")) {
            i = 0;
        } else if (result.contains("exception")) {
            i = 1;
        } else if (result.contains("timeout_exception")) {
            i = 2;
        }
        return i;
    }


    public class borrowTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            return borrow(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if (i == 1) {
                MyApp.showToast(getString(R.string.bds_borrow_fail));
            } else if (i == 0) {
                MyApp.showToast(getString(R.string.bds_borrow_successfully));
                finish();
            } else if (i == 2) {
                MyApp.showToast(getString(R.string.network));
            }
            progressDialog.dismiss();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        line_fee.getViewTreeObserver().removeOnGlobalLayoutListener(ViewTreeObserver);

    }


}
