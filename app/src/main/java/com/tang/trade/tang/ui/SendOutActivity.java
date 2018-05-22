package com.tang.trade.tang.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HideTradeResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FeeUtil;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.StringUtil;
import com.tang.trade.tang.utils.TLog;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.EditTextDialog;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.tang.trade.tang.utils.NumberUtils.setPricePoint;

/**
 * Created by Administrator on 2017/9/22.
 */

public class SendOutActivity extends BaseActivity {

    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final int REQURE_CODE_SCAN = 100;

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.et_balance1)
    TextView etBalance1;
    @BindView(R.id.et_balance2)
    EditText etBalance2;
    @BindView(R.id.et_amount)
    EditText etAmount;
    @BindView(R.id.tv_send_out)
    TextView tvSendOut;
    @BindView(R.id.servicecharge)
    TextView tvServiceCharge;

    @BindView(R.id.line_select_currency)
    LinearLayout line_select_currency;

    @BindView(R.id.tv_type)
    TextView tv_type;

    @BindView(R.id.et_memo)
    TextView et_memo;

    @BindView(R.id.qr)
    ImageView qr;
    private String username;

    int i = 0;
    MyProgressDialog progressDialog;


    private String[] coinType;
    private String fee = "0";

    private double feeReally = 0;

    private String sAssetAmount;

    String memo = "";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                sendout();
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    });
    Thread thread1 = new Thread(new Runnable() {
        @Override
        public void run() {
            setBalance(tv_type.getText().toString());

        }
    });
    private AlertDialog tradeSuccessDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sendout;
    }

    @Override
    public void initView() {
        setPricePoint(etAmount);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  //打开相机权限
                    != PackageManager.PERMISSION_GRANTED) {

                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        2);

            }
        }

    }

    @Override
    public void initData() {
        getCoinType();

        qr.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvSendOut.setOnClickListener(this);
        line_select_currency.setOnClickListener(this);
        username = SPUtils.getString(Const.USERNAME, "");
        progressDialog = MyProgressDialog.getInstance(this);

//        tvServiceCharge.setText(getString(R.string.bds_fee)+": "+NumberUtils.formatNumber(gbdsTransferFee+"")+" BDS");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.line_select_currency:
//                if (Device.pingIpAddress()){
                showSingleChoiceDialog(coinType);
//                }else {
//                    MyApp.showToast(getString(R.string.network));
//                }

                break;

            case R.id.tv_send_out:
                progressDialog.show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!Device.pingIpAddress()) {
                            MyApp.showToast(R.string.network);
                            progressDialog.dismiss();
                        } else if (TextUtils.isEmpty(etBalance2.getText().toString().trim())) {
                            MyApp.showToast(R.string.bds_accoun_is_empty);
                            progressDialog.dismiss();
                        } else if (TextUtils.isEmpty(etAmount.getText().toString().trim())) {
                            MyApp.showToast(R.string.bds_enter_amount);
                            progressDialog.dismiss();
                        } else if (Double.parseDouble(etAmount.getText().toString().trim()) <= 0) {
                            MyApp.showToast(R.string.bds_enter_amount_err);
                            progressDialog.dismiss();
                        } else if (Double.parseDouble(etAmount.getText().toString().trim()) <= 0) {
                            MyApp.showToast(R.string.bds_enter_amount_err);
                            progressDialog.dismiss();
                        } else if (BitsharesWalletWraper.getInstance().is_suggest_brain_key() == false) {
                            MyApp.showToast(R.string.bds_restrat_app);
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            showDialog();
                        }

                    }
                }, 50);

                break;
            case R.id.qr:
                if (UtilOnclick.isFastClick()) {
                    Intent i = new Intent(this, CaptureActivity.class);
                    startActivityForResult(i, REQURE_CODE_SCAN);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    // CameraManager必须在这里初始化，而不是在onCreate()中。
                    // 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
                    // 当扫描框的尺寸不正确时会出现bug

                } else {
                    if (i == 0) {
                        MyApp.showToast(R.string.permissionsError);
                        i++;
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQURE_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            TLog.log("qr success");
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                if (StringUtil.getStringDelimiter(content)) {
                    try {
                        String[] arr = StringUtil.getStringaArr(content);
                        etBalance2.setText(arr[0]);
                        et_memo.setText(arr[1]);
                    } catch (Exception e) {

                    }


                } else {
                    etBalance2.setText(content);
                }


            }
        }
    }

    /**
     * SHA加密
     *
     * @param strSrc 明文
     * @return 加密之后的密文
     */
    public static String shaEncrypt(String strSrc) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");// 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    /**
     * byte数组转换为16进制字符串
     *
     * @param bts 数据源
     * @return 16进制字符串
     */
    public static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    // 加密
    public static String getBase64(String str) {
        String result = "";
        if (str != null) {
            try {
                result = new String(Base64.encode(str.getBytes("utf-8"), Base64.NO_WRAP), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    private void showDialog() {
        if (!TextUtils.isEmpty(et_memo.getText().toString())) {
            memo = et_memo.getText().toString();
        } else {
            memo = "";
        }
        String str = getString(R.string.isconfirm) + etBalance2.getText().toString().trim() + "\n" + getString(R.string.Amount) + etAmount.getText().toString().trim();
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(str);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                double transferMoney = CalculateUtils.add(Double.parseDouble(etAmount.getText().toString().trim()), feeReally);

                if (Double.parseDouble(sAssetAmount) >= transferMoney) {
                    if (!username.equals(etBalance2.getText().toString().trim())) {
                        dialog.dismiss();
                        setEdTextDiolog();
                    } else {
                        MyApp.showToast(R.string.bds_no_transfer_money_to_yourself);
                    }

                } else {
                    MyApp.showToast(R.string.priceInfo);
                }

            }
        });
        builder.show();
    }

    public void sendout() {

        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(tv_type.getText().toString());
            if (object != null) {
                if (!tv_type.getText().toString().equalsIgnoreCase("BDS")) {
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(username
                            , etBalance2.getText().toString().trim()
                            , etAmount.getText().toString().trim()
                            , tv_type.getText().toString(), memo, fee, object.id.toString());
                } else {
                    signedTransaction = BitsharesWalletWraper.getInstance().transfer(username
                            , etBalance2.getText().toString().trim()
                            , etAmount.getText().toString().trim()
                            , "BDS", memo, "", "");
                }


            } else {
                MyApp.showToast(getString(R.string.network));
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            signedTransaction = null;
        }
        progressDialog.dismiss();
        if (signedTransaction != null) {
//            MyApp.showToast(getString(R.string.outsuccess));
//            finish();
            showTradeSuccessDialog();

        } else {
            //     MyApp.showToast(getString(R.string.network));
            MyApp.showToast(getString(R.string.bds_AccountNoExists));


        }

    }

    /**
     * 交易成功弹窗
     */
    private void showTradeSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("转账成功");
        builder.setMessage("转账记录可能会有延迟，如无法查到请稍后查看");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tradeSuccessDialog.dismiss();
                finish();

            }
        });
//        builder.setNegativeButton("查看转账记录", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                tradeSuccessDialog.dismiss();
//                TransferRecordctivity.start(SendOutActivity.this);
//                finish();
//            }
//        });
        tradeSuccessDialog = builder.create();
        if (!isFinishing()) {
            tradeSuccessDialog.show();
        }
    }


    private void setBalance(final String symbol) {
        try {
            if (!SPUtils.getString(Const.USERNAME, "").equalsIgnoreCase("")) {

                boolean findAssetInAccount = false;
                //获取所有资产列表
                account_object object = BitsharesWalletWraper.getInstance().get_account_object(SPUtils.getString(Const.USERNAME, ""));
                if (object == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                            sAssetAmount = "0.00000";
                            etBalance1.setText("0.00000 " + symbol);
                        }
                    });
                    return;
                }
                //查询账户id
                object_id<account_object> loginAccountId = object.id;

                //获取账户余额列表
                List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                if (accountAsset == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                            sAssetAmount = "0.00000";
                            etBalance1.setText("0.00000 " + symbol);
                        }
                    });

                    return;
                }
                //查询资产列表
                List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                if (objAssets == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                            sAssetAmount = "0.00000";
                            etBalance1.setText("0.00000 " + symbol);
                        }
                    });
                    return;
                }

                final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);
                fee = FeeUtil.getFee(tv_type.getText().toString(), rate);
                feeReally = Double.parseDouble(fee);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        if (!tv_type.getText().toString().equalsIgnoreCase("BDS")){
//                            if (tv_type.getText().toString().equalsIgnoreCase("BTC") || tv_type.getText().toString().equalsIgnoreCase("ETH") || tv_type.getText().toString().equalsIgnoreCase("LTC")){
//                                fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                                BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00000001);
//                                String rate1 = NumberUtils.formatNumber8(db.toPlainString());
//                                tvServiceCharge.setText(getString(R.string.bds_fee)+": "+rate1+symbol);
//                                feeReally = Double.parseDouble(rate1);
//                            }else {
//                                fee = CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee);
//                                BigDecimal db = new BigDecimal(Double.parseDouble(fee)+0.00001);
//                                String rate1 = NumberUtils.formatNumber(db.toPlainString());
//                                tvServiceCharge.setText(getString(R.string.bds_fee)+": "+rate1+symbol);
//                                feeReally = Double.parseDouble(rate1);
//                            }
//                        }else {
//                            tvServiceCharge.setText(getString(R.string.bds_fee)+": "+ NumberUtils.formatNumber(gbdsTransferFee+"")+symbol);
//                            feeReally = gbdsTransferFee;
//                        }

                        tvServiceCharge.setText(getString(R.string.bds_fee) + ": " + fee + symbol);
                    }
                });

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

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    etBalance1.setText(NumberUtils.formatNumber5(sAssetAmount) + " " + symbol);
                                                }
                                            });

                                        }
                                    });


                                }
                            }
                        }

                    }
                }
                if (!findAssetInAccount) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sAssetAmount = "0.00000";

                            etBalance1.setText("0.00000 " + symbol);
                        }
                    });

                }

            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sAssetAmount = "0.00000";
                    etBalance1.setText("0.00000 " + symbol);
                }
            });
        }
    }


    /**
     * 选择交易类型
     */
    private int position = 0;

    private void showSingleChoiceDialog(final String[] items) {
        final AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle(getString(R.string.checked));
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                        tv_type.setText(items[which]);
//                        handler.post(thread1);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setBalance(tv_type.getText().toString());
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });

        singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel), null);
        singleChoiceDialog.show();
    }


    private void getCoinType() {
        final ArrayList<String> list = new ArrayList<>();


        TangApi.getHideTransfer(new JsonCallBack<HideTradeResponseModel>(HideTradeResponseModel.class) {
            @Override
            public void onSuccess(Response<HideTradeResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    if (response.body().getData().size() > 0) {
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            list.add(response.body().getData().get(i).getName());
                        }
                    }

                    coinType = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        coinType[i] = list.get(i);
                    }
                    tv_type.setText(coinType[0]);
//                    handler.post(thread1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setBalance(tv_type.getText().toString());
                        }
                    }).start();

                }
            }
        });

    }

    private void setEdTextDiolog() {
        final EditTextDialog editTextDialog = new EditTextDialog(this);
        editTextDialog.setOnDialogOnClick(pwd -> {
            if (TextUtils.isEmpty(pwd)) {
                MyApp.showToast(getString(R.string.bds_note_null_pwd));
            } else if (!pwd.equals(ChooseWalletActivity.PASSWORD)) {
                MyApp.showToast(getString(R.string.bds_note_ncorrecti_pwd));
            } else {
                editTextDialog.dismiss();
                progressDialog.show();
                handler.post(thread);

            }
        });
        editTextDialog.isVisible();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}



