package com.tang.trade.tang.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;

import com.tang.trade.module.profile.generateqrcode.GenerateQrCodeActivity;
import com.tang.trade.module.profile.login.LoginActivity;
import com.tang.trade.module.profile.saveqrcode.SaveQrCodeActivity;
import com.tang.trade.module.profile.security.ModifyPasswordActivity;
import com.tang.trade.module.showword.ShowWordActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.net.model.UpdateResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.socket.websocket_api;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.loginactivity.ChangePasswordActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.TLog;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.SPUtils;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

public class MeActivity extends BaseActivity {
    public static final String TYPE_JIANZHENGREN = "1";
    public static final String TYPE_ANQUAN = "2";
    public static final String TYPE_SHEZHI = "3";
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    ///type1
    @BindView(R.id.line_type1)
    LinearLayout line_type1;
    @BindView(R.id.ll_witness)
    LinearLayout ll_witness;
    @BindView(R.id.v_witness)
    View v_witness;
    @BindView(R.id.tv_vip)
    LinearLayout tv_vip;
    @BindView(R.id.ll_nuggets_pool)
    LinearLayout ll_nuggets_pool;

    @BindView(R.id.line_shezhi_type)
    LinearLayout line_shezhi_type;
    @BindView(R.id.ll_back_up)
    LinearLayout ll_back_up;
    @BindView(R.id.ll_mykey)
    LinearLayout ll_mykey;
    @BindView(R.id.line_anquan_type)
    LinearLayout line_anquan_type;
    @BindView(R.id.tv_check_version)
    LinearLayout tvCkeck;

    //type3
    @BindView(R.id.tv_languages)
    LinearLayout tvLanguages;
    @BindView(R.id.ll_exit)
    LinearLayout ll_exit;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.bg)
    FrameLayout frameLayout;
    @BindView(R.id.ll_phone_manage)
    LinearLayout ll_phone_manage;
    @BindView(R.id.ll_pay_manage)
    LinearLayout ll_pay_manage;
    @BindView(R.id.ll_new_account)
    LinearLayout ll_new_account;
    @BindView(R.id.ll_change_pwd)
    LinearLayout ll_change_pwd;
    @BindView(R.id.ll_qiehuan)
    LinearLayout ll_qiehuan;
    @BindView(R.id.tv_modify_pwd)
    TextView tvModifyPwd;
    @BindView(R.id.tv_show_word)
    TextView tvShowWord;
    @BindView(R.id.tv_qrcode_save)
    TextView tvQrCodeSave;
    @BindView(R.id.ll_WeChat)
    LinearLayout ll_WeChat;
    @BindView(R.id.ll_download_address)
    LinearLayout ll_download_address;

    private PopupWindow popWnd;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private PopupWindow popUpProgress;
    private String username;
    private String encryptoData = "";
    private String strPublicKey = "";
    private MyProgressDialog myProgressDialog;
    Handler handler = new Handler();
    private String type = "1";
    private String VOLLEY = "type";

    Runnable runnableExit = new Runnable() {
        @Override
        public void run() {

            if (Device.pingIpAddress()) {

                if (mIMKit != null) {
                    IYWLoginService loginService = mIMKit.getIMCore().getLoginService();

                    loginService.logout(new IWxCallback() {

                        @Override
                        public void onSuccess(Object... arg0) {
                            finishActivity("1");
                        }

                        @Override
                        public void onProgress(int arg0) {

                        }

                        @Override
                        public void onError(int errCode, String description) {
                            MyApp.showToast(getString(R.string.network));
                            myProgressDialog.dismiss();
                            finishActivity("1");
                        }
                    });

                } else {
                    finishActivity("1");
                }
            } else {
                MyApp.showToast(getString(R.string.network));
                myProgressDialog.dismiss();
                finishActivity("1");

            }
        }
    };
    Runnable runnableQiehuan = new Runnable() {
        @Override
        public void run() {
            if (Device.pingIpAddress()) {

                if (mIMKit != null) {
                    IYWLoginService loginService = mIMKit.getIMCore().getLoginService();
                    loginService.logout(new IWxCallback() {

                        @Override
                        public void onSuccess(Object... arg0) {
                            finishActivity("0");
                        }

                        @Override
                        public void onProgress(int arg0) {
                        }

                        @Override
                        public void onError(int errCode, String description) {
                            MyApp.showToast(getString(R.string.network));
                            myProgressDialog.dismiss();
                            finishActivity("0");

                        }
                    });

                } else {
                    finishActivity("0");

                }
            } else {
                MyApp.showToast(getString(R.string.network));
                myProgressDialog.dismiss();
                finishActivity("0");
            }
        }
    };

    public static void start(Context context, String type) {
        Intent starter = new Intent(context, MeActivity.class);
        starter.putExtra("type", type);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_me;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_vip:
                startActivity(new Intent(this, MemberUpgradeActivity.class));

                break;
            case R.id.ll_nuggets_pool:
                startActivity(new Intent(this, OrePoolActivity.class));

                break;
            case R.id.ll_witness:
                boolean isflay = MyApp.get(username + "member_to_witness", false);
                if (isflay == false) {
                    showWitnessDialog();
                } else {
                    Toast.makeText(MeActivity.this, getString(R.string.bds_sumbit), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.ll_mykey:
                startActivity(new Intent(this, MyInfoActivity.class));

                break;
            case R.id.ll_back_up:
                startActivity(new Intent(this, BackUpActivity.class));

                break;
            case R.id.tv_languages:
                startActivity(new Intent(this, ChangeLanguageActivity.class));

                break;
            case R.id.tv_check_version:
                checkUpdate();
                break;
            case R.id.ll_exit:
                if (UtilOnclick.isFastClick()) {
                    showDialogEdit();
                }
                break;
            case R.id.ll_qiehuan:
                if (UtilOnclick.isFastClick()) {
                    showDialogQiehuan();
                }
                break;
            case R.id.ll_phone_manage:
                startActivity(new Intent(this, PhoneAdministrationActivity.class));
                break;
            case R.id.ll_pay_manage:
                Intent selectI = new Intent(this, SelectAccountActivity.class);
                selectI.putExtra("type", SelectAccountActivity.TYPE_GUANLI);
                startActivity(selectI);
                break;
            case R.id.ll_new_account:
                startActivity(new Intent(this, CreateAccountActivity.class));
                break;
            case R.id.ll_change_pwd:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.tv_modify_pwd:
                ModifyPasswordActivity.start(this);
                break;
            case R.id.tv_show_word:
                ShowWordActivity.start(this);
                break;
            case R.id.tv_qrcode_save:
                String userName = SPUtils.getString(Const.USERNAME, "");
                String qr_content = SPUtils.getString(userName + Const.QR_CONTENT, "");
                if (TextUtils.isEmpty(qr_content)) {
                    GenerateQrCodeActivity.start(this);
                } else {
                    String phone = SPUtils.getString(userName + Const.PHONE, "");
                    SaveQrCodeActivity.start(this, phone);
                }
                break;

            case R.id.ll_WeChat:
                startActivity(new Intent(this, WeCharPublicNumberActivity.class));
                break;

            case R.id.ll_download_address:
                startActivity(new Intent(this, NewDownloadAddressActivity.class));
                break;
        }

    }

    @Override
    public void initView() {
        myProgressDialog = MyProgressDialog.getInstance(MeActivity.this);
        username = SPUtils.getString(Const.USERNAME, "");
        type = getIntent().getStringExtra(VOLLEY);

        iv_back.setOnClickListener(this);

        tv_vip.setOnClickListener(this);
        ll_nuggets_pool.setOnClickListener(this);
        ll_witness.setOnClickListener(this);
        tvQrCodeSave.setOnClickListener(this);
        ll_mykey.setOnClickListener(this);
        ll_back_up.setOnClickListener(this);
        tvModifyPwd.setOnClickListener(this);
        tvShowWord.setOnClickListener(this);
        tvLanguages.setOnClickListener(this);
        tvCkeck.setOnClickListener(this);
        ll_exit.setOnClickListener(this);
        ll_phone_manage.setOnClickListener(this);
        ll_pay_manage.setOnClickListener(this);
        ll_new_account.setOnClickListener(this);
        ll_change_pwd.setOnClickListener(this);
        ll_qiehuan.setOnClickListener(this);

        ll_WeChat.setOnClickListener(this);
        ll_download_address.setOnClickListener(this);

    }

    @Override
    public void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (type.equals(TYPE_JIANZHENGREN)) {
            getKey();
            line_type1.setVisibility(View.VISIBLE);
            line_shezhi_type.setVisibility(View.GONE);
            line_anquan_type.setVisibility(View.GONE);

            if (SPUtils.getBoolean(Const.IS_LIFE_MEMBER, false)) {
                ll_new_account.setVisibility(View.GONE);
                ll_nuggets_pool.setVisibility(View.VISIBLE);

            } else {
                ll_new_account.setVisibility(View.GONE);
                ll_nuggets_pool.setVisibility(View.GONE);
            }


            tv_title.setText(getString(R.string.bds_witness));
        } else if (type.equals(TYPE_SHEZHI)) {
            line_type1.setVisibility(View.GONE);
            line_shezhi_type.setVisibility(View.VISIBLE);
            line_anquan_type.setVisibility(View.GONE);
            tv_title.setText(getString(R.string.bds_shezhi));
            getTv_version();

        } else if (type.equals(TYPE_ANQUAN)) {
            line_type1.setVisibility(View.GONE);
            line_shezhi_type.setVisibility(View.GONE);
            line_anquan_type.setVisibility(View.VISIBLE);

            tv_title.setText(getString(R.string.bds_anquan_center));

        }

    }

    private void showDialogEdit() {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(R.string.exit);

        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            dialog.dismiss();

            if (mIMKit != null) {
                IYWLoginService loginService = mIMKit.getIMCore().getLoginService();

                loginService.logout(new IWxCallback() {

                    @Override
                    public void onSuccess(Object... arg0) {

                    }

                    @Override
                    public void onProgress(int arg0) {

                    }

                    @Override
                    public void onError(int errCode, String description) {

                    }
                });

            }

            BitsharesWalletWraper.getInstance().close();

//            try {
//                CliCmdExecutor.DistoryCli();
                websocket_api.WEBSOCKET_CONNECT_INVALID = -1;
//            } catch (JNIException e) {
//                e.printStackTrace();
//            }

            finishActivity("1");

        });
        builder.show();
    }

    private void showDialogQiehuan() {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(R.string.bds_qiehuan_account);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                handler.postDelayed(runnableQiehuan, 100);

            }
        });
        builder.show();
    }

    public void getTv_version() {
        String versionName;
        try {
            versionName = MyApp.context()
                    .getPackageManager()
                    .getPackageInfo(MyApp.context().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            versionName = "1.0.0";
        }
        tv_version.setText(versionName);
    }


    private void getKey() {
        account_object upgradeAccount = null;
        try {
            upgradeAccount = BitsharesWalletWraper.getInstance().get_account_object(username);
            if (null == upgradeAccount) {
                MyApp.showToast(getString(R.string.network));
                return;
            }
            types.public_key_type publicKeyType = upgradeAccount.owner.get_keys().get(0);
            strPublicKey = publicKeyType.toString();
            types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(publicKeyType);
            String strPrivateKey = null;

            if (privateKeyType != null) {
                strPrivateKey = privateKeyType.toString();
            } else {
                BitsharesWalletWraper.getInstance().clear();
                BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH + CURRTEN_BIN, PASSWORD);
                BitsharesWalletWraper.getInstance().unlock(PASSWORD);
                privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(strPublicKey);
                if (privateKeyType != null) {
                    strPrivateKey = privateKeyType.toString();
                }
            }

            if (privateKeyType == null || privateKeyType.getPrivateKey() == null) {
                return;
            }
            //请求public key

            memo_data memo = new memo_data();
            memo.from = upgradeAccount.options.memo_key;
            memo.to = upgradeAccount.options.memo_key;

            Address address = new Address(BuildConfig.CLI_StrPubWifKey);
            public_key publicKey = new public_key(address.getPublicKey().toBytes());
            //加密
            memo.set_message(
                    privateKeyType.getPrivateKey(),
                    publicKey,
                    strPrivateKey,
                    1
            );

            encryptoData = memo.get_message_data();
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        } catch (MalformedAddressException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查版本更新
     */
    private void checkUpdate() {
        TangApi.getUpdateInfo(new TangApi.MyBaseViewCallBack<UpdateResponseModel>() {
            @Override
            public void start() {
            }

            @Override
            public void onEnd() {
            }

            @Override
            public void setData(UpdateResponseModel updateResponseModel) {
                int i = Device.compareVersion(updateResponseModel.getAndversion(), String.valueOf(Device.getVersionCode()));
                TLog.log("Device.compareVersion(updateResponseModel." + i);
                if (i == 1) {
                    Random random = new Random();
                    int nextInt = random.nextInt(updateResponseModel.getAndurl().size());
                    showPopup(updateResponseModel, nextInt);
                    frameLayout.setAlpha(0.6f);
                    frameLayout.setVisibility(View.VISIBLE);

                } else {
                    MyApp.showToast(getString(R.string.bds_already_new_version));
                }
            }
        });
    }

    private void showPopup(final UpdateResponseModel updateResponseModel, final int i) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.download_alert, null);
        popWnd = new PopupWindow(this);
        popWnd.setContentView(contentView);
        popWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
        popWnd.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popWnd.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popWnd.setOutsideTouchable(false);
        popWnd.showAtLocation(View.inflate(this, R.layout.activity_launch, null), Gravity.CENTER, 0, 0);
        TextView tvContent = (TextView) contentView.findViewById(R.id.content);
        if (!MyApp.get(BuildConfig.LANGUAGE, "").equals("")) {
            if (MyApp.get(BuildConfig.LANGUAGE, "").equalsIgnoreCase("繁體中文")) {
                tvContent.setText(updateResponseModel.getContents_tw());
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").equalsIgnoreCase("简体中文")) {
                tvContent.setText(updateResponseModel.getContents_cn());
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").equalsIgnoreCase("English")) {
                tvContent.setText(updateResponseModel.getContents());
            }
        } else {

            tvContent.setText(updateResponseModel.getContents_cn());
        }

        contentView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWnd.dismiss();

                TangApi.downloadApk(
                        updateResponseModel.getAndurl().get(i)
                        , new TangApi.BaseViewCallBackWithProgress<File>() {
                            @Override
                            public void setData(File file) {
                                Device.openFile(MeActivity.this, file);
                            }

                            @Override
                            public void setProgress(Progress progress) {
                                TLog.log("progress.currentSize/progress.totalSize)" + (float) progress.currentSize / (float) progress.totalSize);
                                TLog.log("(int) ((progress.currentSize/progress.totalSize)*100)===" + (((float) progress.currentSize / (float) progress.totalSize) * 100));
                                TLog.log("((int) ((progress.currentSize/progress.totalSize)*100))+\"%\"===" + ((float) ((progress.currentSize / progress.totalSize) * 100)) + "%");
                                progressBar.setProgress((int) ((progress.currentSize / (float) progress.totalSize) * 100));
                                tvProgress.setText((int) ((((float) progress.currentSize / (float) progress.totalSize) * 100)) + "%");
                            }

                            @Override
                            public void onStart(Request<File, ? extends Request> request) {
                                showPopupProgress();
                            }

                            @Override
                            public void onFinish() {
                                frameLayout.setVisibility(View.INVISIBLE);
                                popUpProgress.dismiss();
                            }
                        });
            }
        });

        contentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameLayout.setVisibility(View.INVISIBLE);
                popWnd.dismiss();
            }
        });
    }

    private void showPopupProgress() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_progress, null);
        progressBar = (ProgressBar) contentView.findViewById(R.id.progressbar);
        progressBar.setMax(100);
        tvProgress = (TextView) contentView.findViewById(R.id.text_progressbar);

        popUpProgress = new PopupWindow(this);
        popUpProgress.setContentView(contentView);
        popUpProgress.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
        popUpProgress.setWidth(850);
        popUpProgress.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popUpProgress.setOutsideTouchable(false);
        popUpProgress.showAtLocation(View.inflate(this, R.layout.activity_me, null), Gravity.CENTER, 0, 0);
    }

    /**
     * 成为见证人
     */
    private void showWitnessDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.bds_Note);
        builder.setMessage(R.string.bds_confirm_witness);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(R.string.bds_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (username.equals("")) {
                    MyApp.showToast(getString(R.string.network));
                    return;
                }
                //校验
                TangApi.witnessCheck(username, new JsonCallBack<ResponseModel>(ResponseModel.class) {
                    @Override
                    public void onSuccess(Response<ResponseModel> response) {
                        if (response.body().getStatus().contains("success")) {
                            Toast.makeText(MeActivity.this, getString(R.string.bds_sumbit), Toast.LENGTH_SHORT).show();
                            MyApp.set(username + "member_to_witness", true);
                        } else {
                            //申请
                            TangApi.setWitness(username, encryptoData, strPublicKey, new JsonCallBack<ResponseModel>(ResponseModel.class) {
                                @Override
                                public void onSuccess(Response<ResponseModel> response) {
                                    if (response.body().getStatus().contains("success")) {
                                        MyApp.set(username + "member_to_witness", true);
                                        Toast.makeText(MeActivity.this, getString(R.string.bds_sumbit_success), Toast.LENGTH_SHORT).show();
                                    } else {
                                        MyApp.showToast(response.body().getMsg());
                                    }
                                }

                                @Override
                                public void onStart(Request<ResponseModel, ? extends
                                        Request> request) {
                                    super.onStart(request);
                                }

                                @Override
                                public void onError(Response<ResponseModel> response) {
                                    super.onError(response);
                                    MyApp.showToast(getString(R.string.network));

                                }
                            });

                        }
                    }

                    @Override
                    public void onStart(Request<ResponseModel, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onError(Response<ResponseModel> response) {
                        super.onError(response);
                        MyApp.showToast(getString(R.string.network));
                    }
                });

            }
        });
        builder.show();
    }

    /*
     * finish all activity to ChooseWalletActivity or LoginActivity
     * **/

    private void finishActivity(String type) {
        //切换
        if (type.equals("0")) {
//            SPUtils.put(Const.USERNAME, "");
            MyApp.set(BuildConfig.ID, "");
            MyApp.set(BuildConfig.MEMOKEY, "");
//
            myProgressDialog.dismiss();
//            ActivityManagerUrils.getActivityManager().popAllActivityExceptOne(MeActivity.class);
//            Intent intent = new Intent(MeActivity.this, com.tang.trade.module.profile.login.LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
            LoginActivity.start(this); //CLEAR_TASK
            finish();
        } else {//退出

//            SPUtils.put(Const.USERNAME, "");
            BitsharesWalletWraper.getInstance().reset_400();

            MyApp.set(BuildConfig.ID, "");
            MyApp.set(BuildConfig.MEMOKEY, "");
            myProgressDialog.dismiss();
//            ActivityManagerUrils.getActivityManager().popAllActivityExceptOne(MeActivity.class);
//            Intent intent = new Intent(MeActivity.this, com.tang.trade.module.profile.login.LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
            LoginActivity.start(this);//CLEAR_TASK
            finish();

            System.exit(0);
        }


    }

}
