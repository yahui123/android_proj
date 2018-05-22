package com.tang.trade.tang.ui.acceptorsfragment;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.tang.trade.tang.net.acceptormodel.AccPhoneModel;
import com.tang.trade.tang.net.acceptormodel.AcceptorOpenModel;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.acceptormodel.ResponseModelBase;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.AcceptanceActivity;
import com.tang.trade.tang.ui.SelectAccountActivity;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.AcceptanceBottomDialog;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tang.trade.tang.ui.SelectAccountActivity.TYPE_Chongzhi;
import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccRechargeFragment extends Fragment implements View.OnClickListener {

    public static final int myresultCode = 100;

    private final int ListSUSESS = 3;
    private final int ListFial = 4;
    private final int ListEerr = 5;
    private final int CHONGZHI_SCUSESS = 0;
    private final int CHONGZHI_FAIL = 1;
    private final int CHONGZHI_Eerr = 2;
    private final int INTMARFIN = 6;
    @BindView(R.id.tv_acceptance_account)
    TextView tv_acceptance_account;
    @BindView(R.id.tv_transaction_reference)
    TextView tv_transaction_reference;
    @BindView(R.id.tv_acceptance_account2)
    TextView tv_acceptance_account2;
    @BindView(R.id.tv_acceptance_name)
    TextView tv_acceptance_name;
    @BindView(R.id.tv_margin)
    TextView tv_margin;
    @BindView(R.id.tv_maximum_deposit)
    TextView tv_maximum_deposit;
    @BindView(R.id.tvWithdrawalAmount)
    TextView tvWithdrawalAmount;
    @BindView(R.id.tvGatewayServiceFee)
    TextView tvGatewayServiceFee;
    @BindView(R.id.tv_phoneNumber)
    TextView tv_phoneNumber;
    @BindView(R.id.line_select_pay)
    LinearLayout line_select_pay;
    @BindView(R.id.line_pay)
    LinearLayout line_pay;
    @BindView(R.id.iv_pay_icon)
    ImageView iv_pay_icon;
    @BindView(R.id.tv_select_pay)
    TextView tv_select_pay;
    @BindView(R.id.tv_pay_mothod)
    TextView tv_pay_mothod;
    @BindView(R.id.tv_pay_name)
    TextView tv_pay_name;
    @BindView(R.id.tv_pay_account)
    TextView tv_pay_account;
    @BindView(R.id.line_copy)
    LinearLayout line_copy;
    @BindView(R.id.btn_confirm)
    TextView tv_confirm;
    @BindView(R.id.btn_contact)
    TextView tv_contact;
    @BindView(R.id.et_amount)
    EditText et_amount;
    @BindView(R.id.iv_visible)
    ImageView iv_visible;
    @BindView(R.id.iv_gone)
    ImageView iv_gone;
    @BindView(R.id.line_phone)
    LinearLayout line_phone;
    @BindView(R.id.line_xiangqing)
    LinearLayout line_xiangqing;
    @BindView(R.id.editPhone)
    TextView tv_phone;
    @BindView(R.id.editCode)
    EditText et_code;
    @BindView(R.id.tvCode)
    TextView tv_code;
    @BindView(R.id.tv_deposit_type)
    TextView tv_deposit_type;
    double cashInLowerLimit = 0;//充值下限
    double cashInLowerUp = 0;//充值上限
    String margin = "0";
    MyProgressDialog myProgressDialog;
    AcceptanceActivity activity;
    AlertDialog.Builder builder;
    private Unbinder unbinder;
    private View view;
    private String mobile;
    private int second = Const.SMS_COUNTDOWN;
    private ClipboardManager cm;
    private int type;
    private String bdsAccount = "";
    private AcceptotXiangqingModel.DataBean dataBean = null;//数据详情
    private AccountResponseModel.DataBean zhiFuDate = null;//支付方式
    private TextView tv_amount;
    private String rate = "0";
    private String dealCode = "";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHONGZHI_SCUSESS:
                    tv_confirm.setEnabled(true);

                    break;
                case CHONGZHI_FAIL:
                    tv_confirm.setEnabled(true);
                    myProgressDialog.dismiss();

                    break;
                case CHONGZHI_Eerr:
                    MyApp.showToast(getContext().getString(R.string.network));
                    tv_confirm.setEnabled(true);
                    myProgressDialog.dismiss();
                    break;
                case ListSUSESS:
                    initData();
                    myProgressDialog.dismiss();
                    break;
                case ListFial:
                    myProgressDialog.dismiss();
                    break;
                case ListEerr:
                    MyApp.showToast(getContext().getString(R.string.network));
                    myProgressDialog.dismiss();
                    break;
                case INTMARFIN:
                    if (tv_margin != null) {
                        tv_margin.setText(margin + " BDS");
                        myProgressDialog.dismiss();
                    }
                    break;

                case 100:
                    if (getActivity() != null) {
                        second--;
                        if (second > 0) {

                            tv_code.setText(getContext().getString(R.string.bds_yi_send) + "(" + second + ")");
                            tv_code.setTextColor(Color.BLACK);
                            tv_code.setClickable(false);
                            handler.postDelayed(thread, 1000);
                            myProgressDialog.dismiss();
                        } else {
                            second = 60;
                            tv_code.setText(getContext().getString(R.string.bds_chongxin_send));
                            tv_code.setTextColor(Color.BLACK);
                            tv_code.setClickable(true);
                            myProgressDialog.dismiss();
                        }
                        break;
                    }

            }


        }
    };
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(100);
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
            bdsAccount = getArguments().getString("bdsAccount");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_accrecharge_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        acceptantListPhone();
    }

    private void init() {


        cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        myProgressDialog = MyProgressDialog.getInstance(getActivity());

        // acceptantList();
        activity = (AcceptanceActivity) getActivity();
        if (activity.dataBean != null) {
            this.dataBean = activity.dataBean;
            myProgressDialog.show();
            initData();
        }

        tv_contact.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        line_copy.setOnClickListener(this);
        iv_visible.setOnClickListener(this);
        iv_gone.setOnClickListener(this);
        tv_phoneNumber.setOnClickListener(this);
        tv_code.setOnClickListener(this);

        et_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String amount = "";
                if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(".")) {
                    amount = s.toString();
                } else {
                    amount = "";
                }

                if (!TextUtils.isEmpty(amount) && !amount.equals(".")) {
                    tvGatewayServiceFee.setText(CalculateUtils.mulScale(amount, rate, 2) + " " + dataBean.getSymbol());
                } else {
                    tvGatewayServiceFee.setText("0.00" + " " + dataBean.getSymbol());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        line_select_pay.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == myresultCode && data != null && data.getSerializableExtra("databean") != null && data.getIntExtra("type", -1) == 1) {
            zhiFuDate = (AccountResponseModel.DataBean) data.getSerializableExtra("databean");
            line_pay.setVisibility(View.VISIBLE);
            tv_select_pay.setVisibility(View.GONE);
            if (zhiFuDate.getTypeCode().equals("AP")) {
                tv_pay_mothod.setVisibility(View.GONE);
                iv_pay_icon.setImageResource(R.mipmap.acaept_alipay_icon);
                tv_pay_name.setText(getString(R.string.bds_full_name) + " " + zhiFuDate.getName() + "");
                tv_pay_account.setText(getString(R.string.bds_apliay_account) + " " + zhiFuDate.getPayAccountID());


            } else if (zhiFuDate.getTypeCode().equals("WC")) {
                tv_pay_mothod.setVisibility(View.GONE);
                iv_pay_icon.setImageResource(R.mipmap.accept_wx_icon);
                tv_pay_name.setText(getString(R.string.bds_weChat_name) + " " + zhiFuDate.getName() + "");
                tv_pay_account.setText(getString(R.string.bds_weChat_account) + " " + zhiFuDate.getPayAccountID());
            } else {
                tv_pay_mothod.setVisibility(View.VISIBLE);
                iv_pay_icon.setImageResource(R.mipmap.accept_bank_icon);
                tv_pay_mothod.setText(zhiFuDate.getBank());
                tv_pay_name.setText(getString(R.string.bds_full_name) + "" + zhiFuDate.getName());
                tv_pay_account.setText(getString(R.string.banknum) + " " + zhiFuDate.getPayAccountID());
            }

        } else if (zhiFuDate != null) {
            zhiFuDate = null;

            line_pay.setVisibility(View.GONE);
            tv_select_pay.setVisibility(View.VISIBLE);
            iv_pay_icon.setImageResource(R.mipmap.accept_accept_nopay_icon);

        }
    }

    private void initData() {
        if (dataBean != null) {
            if (!TextUtils.isEmpty(dataBean.getCashInServiceRate())) {
                rate = dataBean.getCashInServiceRate();
            }


            tv_acceptance_account.setText(bdsAccount);
            tv_acceptance_account2.setText(bdsAccount);

            tv_acceptance_name.setText(dataBean.getAcceptantName() + "");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    margin = getBalance("BDS", dataBean.getBdsAccountCo());
                    if (handler != null) {
                        handler.sendEmptyMessage(INTMARFIN);
                    }


                }
            }).start();


            tv_deposit_type.setText(getString(R.string.bds_revalued_amount) + "(" + dataBean.getSymbol() + ")");


            cashInLowerLimit = 0;//充值下限
            cashInLowerUp = 0;//充值上限
            double acceptLimit = 0;//承兑额度
            double accountBalance = 0;//账户余额accountBalance
            if (!TextUtils.isEmpty(dataBean.getCashInLowerLimit())) {
                cashInLowerLimit = Double.parseDouble(dataBean.getCashInLowerLimit());
            }

            if (!TextUtils.isEmpty(dataBean.getCashInUpperLimit())) {
                cashInLowerUp = Double.parseDouble(dataBean.getCashInUpperLimit());
            }
//
            if (!TextUtils.isEmpty(dataBean.getAcceptLimit())) {
                acceptLimit = Double.parseDouble(dataBean.getAcceptLimit());
            }
            if (!TextUtils.isEmpty(dataBean.getAccountBalance())) {
                accountBalance = Double.parseDouble(dataBean.getAccountBalance());
            }

            if (cashInLowerUp > acceptLimit) {
                cashInLowerUp = acceptLimit;
            }


            if (cashInLowerLimit > cashInLowerUp) {//充值下限，大于等于上限
                cashInLowerUp = 0;
            }
            tv_maximum_deposit.setText(CalculateUtils.round(cashInLowerUp + "") + " " + dataBean.getSymbol());
            tvWithdrawalAmount.setText(CalculateUtils.round(cashInLowerLimit + "") + " " + dataBean.getSymbol());
            tvGatewayServiceFee.setText("0.00 " + dataBean.getSymbol());

            tv_phoneNumber.setText(dataBean.getMobile());

            dealCode = NumberUtils.getStringRandom(6);
            tv_transaction_reference.setText(dealCode);


        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.line_select_pay:
                Intent intent2 = new Intent(getActivity(), SelectAccountActivity.class);
                intent2.putExtra("symbol", dataBean.getSymbol());
                intent2.putExtra("type", TYPE_Chongzhi);
                intent2.putExtra("bdsAccount", bdsAccount);
                if (zhiFuDate != null) {
                    intent2.putExtra("payStyleID", zhiFuDate.getPayStyleID());
                }
                startActivityForResult(intent2, myresultCode);
                break;
            case R.id.btn_confirm:
                if (UtilOnclick.isFastClick()) {
                    tv_confirm.setEnabled(false);
                    String tv_amount = et_amount.getText().toString().trim();
                    if (TextUtils.isEmpty(tv_amount)) {
                        MyApp.showToast(getString(R.string.bds_Deposit_amount_node_0));
                        tv_confirm.setEnabled(true);
                    } else if (zhiFuDate == null) {
                        MyApp.showToast(getString(R.string.bds_select_Deposit_mothod));
                        tv_confirm.setEnabled(true);
                    } else {
                        double dAmount = Double.parseDouble(tv_amount);//输入金额
                        if (dAmount < cashInLowerLimit) {
                            MyApp.showToast(getString(R.string.bds_Deposit_amount_bottom));
                            tv_confirm.setEnabled(true);
                        } else {
                            acceptantChongzhi(tv_amount);

                        }
                    }
                }

                break;

            case R.id.btn_contact:
                IMListener.startChatting(getContext(), dataBean.getAcceptantBdsAccount());
//                    YWIMKit mIMKit = YWAPI.getIMKitInstance(SPUtils.getString(Const.USERNAME,""), MyApp.APP_KEY);
//                    Intent intent = mIMKit.getChattingActivityIntent(dataBean.getAcceptantBdsAccount(), MyApp.APP_KEY);
//                    startActivity(intent);
                break;
            case R.id.line_copy:
                cm.setText(dealCode);
                MyApp.showToast(R.string.copy_ed);
                break;

            case R.id.iv_visible:
                if (line_phone.getVisibility() != View.VISIBLE) {
                    line_xiangqing.setVisibility(View.VISIBLE);
                    line_phone.setVisibility(View.VISIBLE);
                    iv_visible.setVisibility(View.GONE);
                }

                break;

            case R.id.iv_gone:
                if (line_phone.getVisibility() != View.GONE) {
                    line_xiangqing.setVisibility(View.GONE);
                    line_phone.setVisibility(View.GONE);
                    iv_visible.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.tv_phoneNumber:
                if (!TextUtils.isEmpty(dataBean.getMobile())) {
                    showPhoneDialog();
                }
                break;
            case R.id.tvCode:
                tv_code.setClickable(false);
                myProgressDialog.show();
                if (!TextUtils.isEmpty(tv_phone.getText().toString())) {
                    mobile = tv_phone.getText().toString();
                } else {
                    MyApp.showToast(getString(R.string.bds_note_phone_null));
                    myProgressDialog.dismiss();
                    tv_code.setClickable(true);
                    return;
                }

                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("mobile", mobile);
                String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
                if (TextUtils.isEmpty(signMessage)) {
                    MyApp.showToast(getString(R.string.encryption_failed));
                    myProgressDialog.dismiss();
                    tv_code.setClickable(true);
                    return;
                }
                hashMap.put("encmsg", signMessage);
                hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
                AcceptorApi.acceptantHttp(hashMap, "send_verify_sms", new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
                    @Override
                    public void onSuccess(Response<AcceptorOpenModel> response) {
                        if (response.body().getStatus().equalsIgnoreCase("success")) {
                            handler.post(thread);
                        } else {
                            MyApp.showToast(response.body().getMsg());
                            myProgressDialog.dismiss();
                            tv_code.setClickable(true);
                        }

                    }


                    @Override
                    public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                        super.onStart(request);

                    }

                    @Override
                    public void onError(Response<AcceptorOpenModel> response) {
                        super.onError(response);
                        MyApp.showToast(getString(R.string.network));
                        myProgressDialog.dismiss();
                        tv_code.setClickable(true);
                    }
                });

                break;

        }

    }

    private void showPhoneDialog() {
        if (builder == null) {
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.bds_Note);
            builder.setMessage(R.string.bds_agree_contact);
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.bds_hujiao, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {//
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + dataBean.getMobile()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });
        }

        builder.show();
    }

    /*
     * 充值订单提交
     * dealCode
     * */

    private void acceptantChongzhi(final String amount) {
        myProgressDialog.show();
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("memberBdsAccount", SPUtils.getString(Const.USERNAME, ""));//memberBdsAccount
        hashMap.put("acceptantBdsAccount", bdsAccount);//
        hashMap.put("symbol", dataBean.getSymbol());
        hashMap.put("payTypeCode", zhiFuDate.getTypeCode());
        hashMap.put("orderTypeCode", "CI");
        hashMap.put("payStyleID", zhiFuDate.getPayStyleID());
        hashMap.put("dealCode", dealCode);
        hashMap.put("amount", amount);
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            tv_confirm.setEnabled(true);
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg", signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));

        if (TextUtils.isEmpty(et_code.getText().toString())) {
            MyApp.showToast(getString(R.string.bds_note_code_err));
            tv_confirm.setEnabled(true);
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("phonenumber", mobile);
        hashMap.put("sms", et_code.getText().toString());
        AcceptorApi.acceptantHttp(hashMap, "member_acc_create_order_by_sms", new JsonCallBack<ResponseModelBase>(ResponseModelBase.class) {
            @Override
            public void onSuccess(Response<ResponseModelBase> response) {
                if (response.body().getStatus().equals("success")) {
                    //   MyApp.showToast(getString(R.string.bds_order_submitted));
                    tv_confirm.setEnabled(true);
                    handler.removeCallbacks(thread);
                    handler.sendEmptyMessage(CHONGZHI_SCUSESS);
                    showPopupConfirm();

                } else {
                    MyApp.showToast("" + response.body().getMsg());
                    tv_confirm.setEnabled(true);
                    handler.sendEmptyMessage(CHONGZHI_FAIL);
                }

                myProgressDialog.dismiss();

            }

            @Override
            public void onStart(Request<ResponseModelBase, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<ResponseModelBase> response) {
                super.onError(response);
                MyApp.showToast(getActivity().getString(R.string.network));
                tv_confirm.setEnabled(true);
                myProgressDialog.dismiss();

            }
        });
    }


    /**
     * setBalance
     */
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


    private void acceptantListPhone() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("memberBdsAccount", SPUtils.getString(Const.USERNAME, ""));
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            return;
        }
        hashMap.put("encmsg", signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
        AcceptorApi.acceptantHttp(hashMap, "member_get_info", new JsonCallBack<AccPhoneModel>(AccPhoneModel.class) {
            @Override
            public void onSuccess(Response<AccPhoneModel> response) {
                if (response.body().getStatus().equals("success")) {
                    mobile = response.body().getData().get(0).getMobile();
                    tv_phone.setText(mobile);
                } else {
                    //MyApp.showToast(response.body().getMsg());
                }

            }

            @Override
            public void onStart(Request<AccPhoneModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<AccPhoneModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));

            }
        });
    }

    private void showPopupConfirm() {

        final android.app.AlertDialog customizeDialog = new android.app.AlertDialog.Builder(getActivity(), android.app.AlertDialog.THEME_HOLO_LIGHT).create();
        customizeDialog.setInverseBackgroundForced(false);
        customizeDialog.show();
        Window window = customizeDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.confirm_change_password);

        customizeDialog.setCanceledOnTouchOutside(false);

        customizeDialog.setCancelable(false);

        TextView tv_submit = (TextView) window.findViewById(R.id.tv_submit);
        TextView tv_info = (TextView) window.findViewById(R.id.tv_info);
        tv_info.setText(getContext().getString(R.string.bds_order_submitted));
        customizeDialog.setCanceledOnTouchOutside(false);

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeDialog.dismiss();

                setBottomDiolog();

            }
        });


    }


    private void setBottomDiolog() {
        final AcceptanceBottomDialog bottomDialog = new AcceptanceBottomDialog(getActivity());
        bottomDialog.setDialogText(zhiFuDate);
        bottomDialog.show();
        bottomDialog.setOnDialogClick(new AcceptanceBottomDialog.OnDialogClick() {
            @Override
            public void OnSubmitListener() {
                bottomDialog.dismiss();
                getActivity().finish();
            }
        });
    }

}
