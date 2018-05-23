package com.tang.trade.tang.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import io.reactivex.disposables.Disposable;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.loginAccountId;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

public class MemberUpgradeActivity extends BaseActivity {

    private String strFee = null;
    private String strToken = null;


    @BindView(R.id.tv_balance)
    TextView tv_balance;
    @BindView(R.id.tv_money)
    TextView tv_money;
    @BindView(R.id.tv_ok)
    TextView tv_ok;
    @BindView(R.id.iv_back)
    ImageView iv_back;

    private String balance = "0";


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                getAccountBalance("BDS");
                String strBalance = NumberUtils.formatNumber5(balance) + " BDS";
                tv_balance.setText(strBalance);
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(1);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void getAccountBalance(String baseSymbol) {
        try {
            if (loginAccountId != null) {
                asset_object baseAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseSymbol);
                List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                if (accountAsset == null) {
                    MyApp.showToast(getString(R.string.network));
                    return;
                }
                for (int i = 0; i < accountAsset.size(); i++) {
                    asset a = accountAsset.get(i);
                    if (a != null && baseAssetObj != null) {
                        String sourceAssetId = a.asset_id.toString();
                        String destAssetId = baseAssetObj.id.toString();
                        if (sourceAssetId.equals(destAssetId)) {
                            asset_object.asset_object_legible assetobj = baseAssetObj.get_legible_asset_object(a.amount);
                            balance = assetobj.count;
                            break;
                        }
                    }
                }
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void upgrade_account_to_lifttime() {

        final String loginUser = SPUtils.getString(Const.USERNAME,"");
        //获取账户 private key

        account_object upgradeAccount = null;
        try {
            upgradeAccount = BitsharesWalletWraper.getInstance().get_account_object(loginUser);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        if (null == upgradeAccount) {
                MyApp.showToast(getString(R.string.network));
                return;
        }


        BorderlessDataManager.getInstance().upgradeAccountWebSocket(loginUser, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                MyApp.showToast(getString(R.string.bds_UpgradeFailed));
                progressDialog.dismiss();
            }

            @Override
            public void onSubscribe(Disposable d) {
                progressDialog.show();
            }

            @Override
            public void onNext(Object o) {
                MyApp.showToast(getString(R.string.bds_upgrade_successful));
//                            isLifeMember = "1";
                SPUtils.put(Const.IS_LIFE_MEMBER, true);

                finish();
                progressDialog.dismiss();
            }

            @Override
            public void onComplete() {

            }
        });



    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_member_upgrade;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_ok:
                //升级
                if (SPUtils.getBoolean(Const.IS_LIFE_MEMBER, false)) {

                    MyApp.showToast(getString(R.string.bds_already_lifemember));
                } else {

                    if (Double.parseDouble(strFee) <= Double.parseDouble(balance)) {

                        upgrade_account_to_lifttime();
//
                    } else {
                        MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                    }
                }
                break;
        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        tv_ok.setOnClickListener(this);
        handler.post(thread);

        boolean b = SPUtils.getBoolean(Const.IS_LIFE_MEMBER, false);

        if (!b) {
            tv_ok.setText(getString(R.string.bds_confirm_upgrade));
            tv_ok.setBackgroundResource(R.drawable.btn_sendout);
            tv_ok.setTextColor(Color.parseColor("#ffffffff"));
            tv_ok.setClickable(true);
        } else {
            tv_ok.setText(getString(R.string.bds_already_lifemember));
            tv_ok.setBackgroundResource(R.drawable.bg_shape_eaeaea);
            tv_ok.setTextColor(Color.parseColor("#ff000000"));
            tv_ok.setClickable(false);
        }

        //get fee
        try {
            strFee = BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_UPGRADE_ACCOUNT_OPERATION);
            tv_money.setText(strFee + " BDS");
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initData() {
    }


}
