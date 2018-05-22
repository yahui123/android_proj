package com.tang.trade.tang.ui.loginactivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.YWLoginParam;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.login.YWLoginCode;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.OrderSumberModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AccountModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

public class LoginActivity extends BaseActivity {


    private final int SHOW = 0;
    private final int NETWORK = 1;
    private final int NOUSER = 2;
    private final int SELECT_USER = 3;
    private final int SUCCESS = 4;
    private final int PROGRESSBOR = 5;

    @BindView(R.id.line_qiehuan_wallet)
    LinearLayout line_qiehuan_wallet;

    @BindView(R.id.ll_select_user)
    LinearLayout ll_select_user;

    @BindView(R.id.tv_account)
    TextView tv_account;

    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    @BindView(R.id.tv_import_account)
    TextView tv_import_account;

    @BindView(R.id.tv_register_account)
    TextView tv_register_account;

    @BindView(R.id.line_hoder)
    LinearLayout line_holder;

    @BindView(R.id.line_hoder_index)
    LinearLayout line_holder_index;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    private int position = 0;
    private String[] accounts;
    private ArrayList<AccountModel> data = new ArrayList<AccountModel>();

    public static object_id<account_object> loginAccountId = null;
    public static double gbdsTransferFee;
    public static String isLifeMember = "0";
    private int index=-1;//是不是第一次进入 -1不是 1是
    private int progress=60;

    private YWIMKit mIMKit;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case SHOW:
                    progressDialog.show();
                    break;
                case NETWORK:
                    MyApp.showToast(getString(R.string.network));
                    progressDialog.dismiss();
                    break;
                case NOUSER:
                    MyApp.showToast(getString(R.string.nousername));
                    progressDialog.dismiss();
                    break;
                case SELECT_USER:
                    MyApp.showToast(getString(R.string.bds_please_select_account));
                    progressDialog.dismiss();
                    break;
                case SUCCESS:
                    SPUtils.put(Const.USERNAME,data.get(position).getName());
                    MyApp.set("defaultAccount",data.get(position).getName());
                    MyApp.set(BuildConfig.ID,data.get(position).getId());
                    MyApp.set(BuildConfig.MEMOKEY,data.get(position).getMemokey());

                    MyApp.set(BuildConfig.INDEX,position);
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));

                    finish();
                    progressDialog.dismiss();
                    break;
                case PROGRESSBOR:
                    if (progress <91){
                        progress_bar.setProgress(progress);
                        progress++;
                        handler.postDelayed(thread2,30);
                    }
                    break;
            }
        }
    };


    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            global_property_object object = null;
            try {
                object = BitsharesWalletWraper.getInstance().get_global_properties();
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

            if (object == null){
                try {
                    object = BitsharesWalletWraper.getInstance().get_global_properties();
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
                if (object == null){
                    handler.sendEmptyMessage(NETWORK);
                    return;
                }
            }

            //查询账户id
            try {
                account_object account_object = BitsharesWalletWraper.getInstance().get_account_object(tv_account.getText().toString());
                if (account_object != null){
                    loginAccountId = account_object.id;
                    gbdsTransferFee =  Double.parseDouble(BitsharesWalletWraper.getInstance().get_Fee("2.0.0", operations.ID_TRANSER_OPERATION));
                }else{
                    handler.sendEmptyMessage(NETWORK);
                    return;
                }

            } catch (NetworkStatusException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(NETWORK);
                return;

            }

            account_object account = null;
            try {
                account = BitsharesWalletWraper.getInstance().get_account_object(loginAccountId.toString());
            } catch (NetworkStatusException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(NETWORK);
                return;
            }
            if (account != null) {
                String strAccountId = account.id.toString();
//                if (strAccountId.equals(account.lifetime_referrer)) {
                if (account.membership_expiration_date.contains("1969")){
                    isLifeMember = "1";

                } else {
                    isLifeMember = "0";
                }
            }


            handler.sendEmptyMessage(SUCCESS);
        }
    });


    Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(PROGRESSBOR);

        }
    });


    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View v) {
        if (UtilOnclick.isFastClick()) {
            switch (v.getId()) {
                case R.id.line_qiehuan_wallet:
                    startActivity(new Intent(this, ChooseWalletActivity.class));
                    finish();
                    break;
                case R.id.ll_select_user:
                    if (!Device.pingIpAddress()) {
                        MyApp.showToast(getString(R.string.network));
                    } else {
                        if (null == accounts) {
                            accounts = new String[0];
                        }

                        if (singleChoiceDialog == null){
                            showSingleAccountDialog(accounts);
                        }else {
                            if (!singleChoiceDialog.create().isShowing()){
                                showSingleAccountDialog(accounts);
                            }
                        }

                    }

                    break;
                case R.id.btn_confirm:
                    progressDialog.show();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!Device.pingIpAddress()) {
                                    MyApp.showToast(getString(R.string.network));
                                    progressDialog.dismiss();
                                } else {
                                    final String strAccount = tv_account.getText().toString().trim();
                                    if (!strAccount.equalsIgnoreCase(getString(R.string.bds_select_account)) && !TextUtils.isEmpty(strAccount)) {
                                        aliLogin(strAccount);
                                    } else {
                                        handler.sendEmptyMessage(SELECT_USER);
                                    }
                                }

                            }
                        },100);

                    break;
                case R.id.tv_import_account:
                    startActivity(new Intent(this, ImportUserActivity.class));
                    break;
                case R.id.tv_register_account:
                    startActivity(new Intent(this, RegisterActivity.class));
                    break;
            }
        }

    }

    @Override
    public void initView() {
        line_qiehuan_wallet.setOnClickListener(this);
        ll_select_user.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
        tv_import_account.setOnClickListener(this);
        tv_register_account.setOnClickListener(this);

        index=getIntent().getIntExtra("index",-1);
        if (index==1){
            line_holder.setVisibility(View.GONE);
            line_holder_index.setVisibility(View.VISIBLE);
        }

    }
    AlertDialog.Builder singleChoiceDialog;
    private void showSingleAccountDialog(final String[] items){
        if (singleChoiceDialog==null){
            singleChoiceDialog =
                new AlertDialog.Builder(this);
            singleChoiceDialog.setTitle(getString(R.string.bds_select_account));
            singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel),null);
        }
        singleChoiceDialog.setSingleChoiceItems(items, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                        tv_account.setText(items[position]);
                        dialog.dismiss();
                    }
                });
        singleChoiceDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initAccount();
        handler.post(thread2);

    }

    @Override
    public void initData() {

    }

    private void initAccount(){
        ll_select_user.setClickable(false);
        data.clear();
        try {
            String str = FileUtils.readSDFile(MyApp.get("wallet_path","")).trim();
            String str1 = str.replace("\n","");
            if (!str1.equalsIgnoreCase("")){
                JSONObject data1 = new JSONObject(str1);
                JSONArray jsonArray = data1.optJSONArray("my_accounts");
                accounts = new String[jsonArray.length()];
                for (int i = 0; i<jsonArray.length() ; i++){
                    AccountModel accountModel = new AccountModel();
                    JSONObject data2 = jsonArray.optJSONObject(i);
                    accountModel.setId(data2.optString("id"));
                    accountModel.setName(data2.optString("name"));
                    accountModel.setMemokey(data2.optJSONObject("options").optString("memo_key"));
                    data.add(accountModel);
                    accounts[i] = accountModel.getName();

                    if (!TextUtils.isEmpty(MyApp.get("defaultAccount",""))){
                        if (accounts[i].equals(MyApp.get("defaultAccount",""))){
                            tv_account.setText(MyApp.get("defaultAccount",""));
                            position = i;
                        }else {
                            tv_account.setText(accounts[0]);
                            position = 0;
                        }
                    }else {
                        tv_account.setText(accounts[0]);
                        position = 0;
                    }
                }
                ll_select_user.setClickable(true);
            }


        } catch (JSONException e) {
            e.printStackTrace();
            ll_select_user.setClickable(true);
        }

    }

    private void aliLogin(final String tvAccount){
            //开始登录
            String userid = tvAccount;
            mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);

            String password = MD5.md5(tvAccount+"borderless");
            IYWLoginService loginService = mIMKit.getLoginService();
            YWLoginParam loginParam = YWLoginParam.createLoginParam(userid, password);
            loginService.login(loginParam, new IWxCallback() {
                @Override
                public void onSuccess(Object... arg0) {
                    handler.post(thread);
                }

                @Override
                public void onProgress(int arg0) {

                }

                @Override
                public void onError(int errCode, String description) {
                    if (errCode == YWLoginCode. LOGON_FAIL_INVALIDUSER){
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("bdsaccount",tvAccount);
                        AcceptorApi.acceptantHttp(hashMap,"member_im_account_add",new JsonCallBack<OrderSumberModel>(OrderSumberModel.class) {
                            @Override
                            public void onSuccess(final Response<OrderSumberModel> response) {
                                if (response.body().getStatus().equalsIgnoreCase("success")){
                                    aliLogin(tvAccount);
                                }else {
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
                                MyApp.showToast(getString(R.string.network));
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
