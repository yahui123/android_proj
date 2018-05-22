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
        progressDialog.show();
        final String loginUser = SPUtils.getString(Const.USERNAME,"");
        //获取账户 private key
        try {
            account_object upgradeAccount = BitsharesWalletWraper.getInstance().get_account_object(loginUser);
            if (null == upgradeAccount) {
                MyApp.showToast(getString(R.string.network));
                return;
            }
            types.public_key_type publicKeyType = upgradeAccount.owner.get_keys().get(0);
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

                String command = String.format("upgrade_account %s true", loginUser);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("type", "2");
                hashMap.put("name", SPUtils.getString(Const.USERNAME,""));
                hashMap.put("1", encryptoData);
                hashMap.put("2", strPublicKey);
                hashMap.put("command", command);
                //执行命令
                AcceptorApi.acceptantHttp(hashMap, "and_run_command", new JsonCallBack<HttpResponseModel>(HttpResponseModel.class) {
                    @Override
                    public void onSuccess(Response<HttpResponseModel> response) {
                        HttpResponseModel httpResponseModel = response.body();
                        if (httpResponseModel.getStatus().contains("success")) {
                            MyApp.showToast(getString(R.string.bds_upgrade_successful));
//                            isLifeMember = "1";
                            SPUtils.put(Const.IS_LIFE_MEMBER, true);

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

            } catch (MalformedAddressException e) {
                e.printStackTrace();
                MyApp.showToast(getString(R.string.bds_UpgradeFailed));
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            MyApp.showToast(getString(R.string.bds_UpgradeFailed));
        }

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
                        //cli
//                        if (BitsharesWalletWraper.getInstance().getCliUsedSwitch()) {
//                            new MemberUpgradeTask().execute();
//                        } else {
                            upgrade_account_to_lifttime();
//                        }
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


    public class MemberUpgradeTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int result = BitsharesWalletWraper.getInstance().cli_upgrade_account(SPUtils.getString(Const.USERNAME,""));
            Log.i("register_account", result + "");

            return result;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if (i == 1) {
                MyApp.showToast(getString(R.string.bds_upgrade_successful));
//                isLifeMember = "1";
                SPUtils.put(Const.IS_LIFE_MEMBER,true);

                finish();
            } else if (i == 0) {
                MyApp.showToast(getString(R.string.bds_UpgradeFailed));
            } else if (i == -1) {
                MyApp.showToast(getString(R.string.network));
            }
            progressDialog.dismiss();

        }
    }

}
