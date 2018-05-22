package com.tang.trade.tang.ui.acceptorsfragment;


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
import com.tang.trade.tang.net.acceptormodel.DeleteOrderModel;
import com.tang.trade.tang.net.acceptormodel.TianXianModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.AcceptanceActivity;
import com.tang.trade.tang.ui.SelectAccountActivity;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.FeeUtil;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;
import static com.tang.trade.tang.ui.SelectAccountActivity.TYPE_TIXIAN;
import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccWithdrawalsFragment extends Fragment implements View.OnClickListener {

    public static final int myresultCode = 101;
    private Unbinder unbinder;
    private View view;

    @BindView(R.id.tv_acceptance_account)
    TextView tv_acceptance_account;
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

    @BindView(R.id.tv_deposit_type)
    TextView tv_deposit_type;

    @BindView(R.id.tv_amount)
    TextView tv_amount;

    @BindView(R.id.editPhone)
    TextView tv_phone;

    @BindView(R.id.editCode)
    EditText et_code;

    @BindView(R.id.tvCode)
    TextView tv_code;

    private String mobile;
    private int second = Const.SMS_COUNTDOWN;

    private AcceptotXiangqingModel.DataBean dataBean = null;//数据详情
    private AccountResponseModel.DataBean zhiFuDate = null;//支付方式


    private final int TIXIAN_SCUSESS = 0;
    private final int ZHUANZHNAG_FAIL = 1;
    private final int TIXIAN_FAIL = 2;

    private final int INTMARGIN = 6;
    private final int INTsAssetAmount = 7;

    private final int ListSUSESS = 3;
    private final int ListFial = 4;
    private final int ListEerr = 5;
    private int type;
    private String bdsAccount = "";
    private String rate = "0";
    private String sAssetAmount = "0";//账户余额
    private String fee = "0";//提现手续费
    private String cashOutLowerLimit = "0.00000";
    private String margin;//保证金
    private AcceptanceActivity activity;


    MyProgressDialog myProgressDialog = null;


    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            margin = getBalance("BDS", dataBean.getBdsAccountCo());
            sAssetAmount = getBalance(dataBean.getSymbol(), SPUtils.getString(Const.USERNAME, ""));
            if (handler != null) {
                handler.sendEmptyMessage(INTMARGIN);
            }

        }
    });
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIXIAN_SCUSESS:
                    MyApp.showToast(getContext().getString(R.string.bds_order_submitted));
                    myProgressDialog.dismiss();
                    handler.removeCallbacks(thread1);
                    getActivity().finish();


                    break;
                case TIXIAN_FAIL:
                    myProgressDialog.dismiss();
                    String msg0 = (String) msg.getData().get("msg");
                    MyApp.showToast(msg0);
                    tv_confirm.setEnabled(true);

                    break;
                case ZHUANZHNAG_FAIL:
                    myProgressDialog.dismiss();
                    MyApp.showToast(getContext().getString(R.string.bds_failed_submit_order));
                    tv_confirm.setEnabled(true);
                    handler.removeCallbacks(thread1);
                    getActivity().finish();
                    break;
                case ListSUSESS:
                    initData();
                    myProgressDialog.dismiss();
                    tv_confirm.setEnabled(true);
                    break;
                case ListFial:
                    myProgressDialog.dismiss();

                    tv_confirm.setEnabled(true);
                    break;
                case ListEerr:
                    MyApp.showToast(getContext().getString(R.string.network));
                    myProgressDialog.dismiss();
                    tv_confirm.setEnabled(true);
                    break;
                case INTMARGIN:
                    if (tv_margin != null) {
                        tv_margin.setText(margin + " BDS");
                        tv_amount.setText(getContext().getString(R.string.bds_account_balance) + ":" + sAssetAmount + " " + dataBean.getSymbol());
                        myProgressDialog.dismiss();
                    }
                    break;
                case INTsAssetAmount:
                    break;

                case 100:
                    if (getActivity() != null) {
                        second--;
                        if (second > 0) {
                            tv_code.setText(getContext().getString(R.string.bds_yi_send) + "(" + second + ")");
                            tv_code.setTextColor(Color.BLACK);
                            tv_code.setClickable(false);
                            handler.postDelayed(thread1, 1000);
                            myProgressDialog.dismiss();
                        } else {
                            second = 60;
                            tv_code.setText(getContext().getString(R.string.bds_chongxin_send));
                            tv_code.setTextColor(Color.BLACK);
                            tv_code.setClickable(true);
                            myProgressDialog.dismiss();
                        }
                    }
                    break;
            }
        }
    };


    Thread thread1 = new Thread(new Runnable() {
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
        view = inflater.inflate(R.layout.fragment_withdrawals_layout, container, false);
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
        myProgressDialog = MyProgressDialog.getInstance(getActivity());

        activity = (AcceptanceActivity) getActivity();
        if (activity.dataBean != null) {
            this.dataBean = activity.dataBean;
            myProgressDialog.show();
            initData();
        }


        tv_contact.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        iv_visible.setOnClickListener(this);
        iv_gone.setOnClickListener(this);
        line_select_pay.setOnClickListener(this);
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


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == myresultCode && data.getSerializableExtra("databean") != null && data.getIntExtra("type", -1) == 1) {
            zhiFuDate = (AccountResponseModel.DataBean) data.getSerializableExtra("databean");
            line_pay.setVisibility(View.VISIBLE);
            tv_select_pay.setVisibility(View.GONE);
            if (zhiFuDate.getTypeCode().equals("AP")) {
                tv_pay_mothod.setVisibility(View.GONE);
                iv_pay_icon.setImageResource(R.mipmap.acaept_alipay_icon);
                tv_pay_name.setText(getString(R.string.bds_full_name) + " " + zhiFuDate.getName() + "");
                tv_pay_account.setText(getString(R.string.bds_apliay_account) + " " + zhiFuDate.getPayAccountID());

            } else if (zhiFuDate.getTypeCode().equals("WC")) {
                iv_pay_icon.setImageResource(R.mipmap.accept_wx_icon);
                tv_pay_mothod.setVisibility(View.GONE);
                tv_pay_name.setText(getString(R.string.bds_weChat_name) + " " + zhiFuDate.getName() + "");
                tv_pay_account.setText(getString(R.string.bds_weChat_account) + " " + zhiFuDate.getPayAccountID());
            } else {
                iv_pay_icon.setImageResource(R.mipmap.accept_bank_icon);
                tv_pay_mothod.setText(zhiFuDate.getBank());
                tv_pay_mothod.setVisibility(View.VISIBLE);
                tv_pay_name.setText(getString(R.string.bds_full_name) + " " + zhiFuDate.getName());
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
        if (null != dataBean) {
            if (!TextUtils.isEmpty(dataBean.getCashOutServiceRate())) {
                rate = dataBean.getCashOutServiceRate();
            }

            if (getActivity() == null) {
                return;
            }

            thread.start();
            tv_acceptance_account.setText(bdsAccount);
            tv_acceptance_account2.setText(bdsAccount);
            tv_acceptance_name.setText(dataBean.getAcceptantName() + "");

            tv_deposit_type.setText(getString(R.string.bds_cash_out1_money) + "(" + dataBean.getSymbol() + ")");


            String cashOutLowerUp = "0";//提现上限
            double acceptLimit = 0;//承兑额度
            double cashOutUncompletedAmount = 0;//未完成提现订单金额
            if (!TextUtils.isEmpty(dataBean.getCashOutLowerLimit())) {
                cashOutLowerLimit = dataBean.getCashOutLowerLimit();
            }

            if (!TextUtils.isEmpty(dataBean.getCashOutUpperLimit())) {
                cashOutLowerUp = dataBean.getCashOutUpperLimit();
            }


            tv_maximum_deposit.setText(CalculateUtils.round(cashOutLowerUp + "") + " " + dataBean.getSymbol());
            tvWithdrawalAmount.setText(CalculateUtils.round(cashOutLowerLimit + "") + " " + dataBean.getSymbol());
            tvGatewayServiceFee.setText("0.00 " + dataBean.getSymbol());

            tv_phoneNumber.setText(dataBean.getMobile());

        } else {

        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thread.interrupt();
        unbinder.unbind();
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.line_select_pay:
                Intent intent2 = new Intent(getActivity(), SelectAccountActivity.class);
                intent2.putExtra("symbol", dataBean.getSymbol());
                intent2.putExtra("type", TYPE_TIXIAN);
                intent2.putExtra("bdsAccount", bdsAccount);
                if (zhiFuDate != null) {
                    intent2.putExtra("payStyleID", zhiFuDate.getPayStyleID());
                }
                startActivityForResult(intent2, myresultCode);
                break;
            case R.id.btn_confirm:
                if (UtilOnclick.isFastClick()) {
                    tv_confirm.setEnabled(false);
                    final String tv_amount = et_amount.getText().toString().trim();//
                    if (TextUtils.isEmpty(tv_amount)) {
                        MyApp.showToast(getString(R.string.please_put_amount_withdraw));
                        tv_confirm.setEnabled(true);
                    } else if (zhiFuDate == null) {
                        MyApp.showToast(getString(R.string.select_withdraw_method));
                        tv_confirm.setEnabled(true);
                    } else {
                        double dAmount = Double.parseDouble(tv_amount);//输入金额
                        double dAssetAmount = Double.parseDouble(sAssetAmount);//账户余额
                        double dFee = Double.parseDouble(fee);//手续费
                        double dBottom = Double.parseDouble(cashOutLowerLimit);//提现下限
                        if (dAmount + dFee > dAssetAmount) {
                            MyApp.showToast(getString(R.string.bds_note_insufficient_funds));
                            tv_confirm.setEnabled(true);
                        } else if (dAmount < dBottom) {
                            MyApp.showToast(getString(R.string.bds_withdraw_minimum_ccc));
                            tv_confirm.setEnabled(true);
                        } else {
                            tv_confirm.setEnabled(false);
                            myProgressDialog.show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    acceptantTIXian(tv_amount);
                                }
                            }, 50);

                        }
                    }
                }


                break;


            case R.id.btn_contact:

                IMListener.startChatting(getContext(),dataBean.getAcceptantBdsAccount());
//                YWIMKit mIMKit = YWAPI.getIMKitInstance(SPUtils.getString(Const.USERNAME, ""), MyApp.APP_KEY);
//                Intent intent = mIMKit.getChattingActivityIntent(dataBean.getAcceptantBdsAccount(), MyApp.APP_KEY);
//                startActivity(intent);

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
                            handler.post(thread1);
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

    AlertDialog.Builder builder;

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
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + dataBean.getMobile()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });
        }

        builder.show();
    }


    private String getBalance(final String symbol, final String bdsAccount) {
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

            final String rate = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(symbol);

            if (bdsAccount.equals(SPUtils.getString(Const.USERNAME, ""))) {
//                if (symbol.equalsIgnoreCase("BTC")){
//                    fee = NumberUtils.formatNumber8(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//                }else if(symbol.equalsIgnoreCase("BDS")){
//                    fee = NumberUtils.formatNumber(gbdsTransferFee+"");
//                }else {
//                    fee = NumberUtils.formatNumber(CalculateUtils.mul(Double.parseDouble(rate),gbdsTransferFee));
//                }
                fee = FeeUtil.getFee(symbol, rate);

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
//                                if (bdsAccount.equals(SPUtils.getString(Const.USERNAME,""))){
//                                    this.sAssetAmount=finalSAssetAmount;
//                                    myProgressDialog.dismiss();
//                                }
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


    private void acceptantTIXian(final String amount) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("memberBdsAccount", SPUtils.getString(Const.USERNAME, ""));//memberBdsAccount
        hashMap.put("acceptantBdsAccount", bdsAccount);//
        hashMap.put("orderTypeCode", "CO");
        hashMap.put("symbol", dataBean.getSymbol());
        hashMap.put("payTypeCode", zhiFuDate.getTypeCode());
        hashMap.put("amount", amount);
        hashMap.put("payStyleID", zhiFuDate.getPayStyleID());
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(et_code.getText().toString())) {
            MyApp.showToast(getString(R.string.bds_note_code_err));
            myProgressDialog.dismiss();
            tv_confirm.setEnabled(true);
            return;
        }
        hashMap.put("encmsg", signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
        hashMap.put("phonenumber", mobile);
        hashMap.put("sms", et_code.getText().toString());
        AcceptorApi.acceptantHttp(hashMap, "member_acc_create_order_by_sms", new JsonCallBack<TianXianModel>(TianXianModel.class) {
            @Override
            public void onSuccess(Response<TianXianModel> response) {

                if (response.body().getStatus().equals("success") && getActivity() != null) {
                    if (!TextUtils.isEmpty(response.body().getData().get(0).getToken().trim())) {

                        String token = response.body().getData().get(0).getToken().trim();
                        String id = response.body().getData().get(0).getId().trim();
                        acceptantRecharge(dataBean.getBdsAccountCo(), dataBean.getSymbol(), amount, fee, token, id);
                    } else {
                        MyApp.showToast(getString(R.string.bds_failed_submit_order));
                    }

                } else if (getActivity() != null) {
                    Message message = Message.obtain();
                    message.what = TIXIAN_FAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", response.body().getMsg());
                    message.setData(bundle);
                    handler.sendMessage(message);

                }

            }

            @Override
            public void onStart(Request<TianXianModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<TianXianModel> response) {
                super.onError(response);
                if (getActivity() != null) {
                    MyApp.showToast(getActivity().getString(R.string.network));
                    myProgressDialog.dismiss();
                    tv_confirm.setEnabled(true);
                }

            }
        });
    }


    /**
     * 承兑商提现
     */

    private void acceptantRecharge(String accepatantAccount, String symbol, final String amount, String fee, String token, final String id) {
        String payAccount = SPUtils.getString(Const.USERNAME, "");

        asset_object object = null;
        signed_transaction signedTransaction = null;
        try {
            object = BitsharesWalletWraper.getInstance().lookup_asset_symbols(symbol);
            if (object != null) {
                signedTransaction = BitsharesWalletWraper.getInstance().transfer(payAccount
                        , accepatantAccount
                        , amount
                        , symbol
                        , token
                        , fee //fee
                        , object.id.toString());

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // MyApp.showToast(getString(R.string.network));
                        //handler.sendEmptyMessage(ZHUANZHNAG_FAIL);
//                        Message message = Message.obtain();
//                        message.what = ZHUANZHNAG_FAIL;
//                        Bundle bundle = new Bundle();
//                        bundle.putString("amount", amount);
//                        message.setData(bundle);
//                        handler.sendMessage(message);

                        deleteOrder(id);
                    }
                });

            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            signedTransaction = null;
        }
        if (signedTransaction != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.showToast(getString(R.string.outsuccess));
                    //handler.sendEmptyMessage(TIXIAN_SCUSESS);
                    Message message = Message.obtain();
                    message.what = TIXIAN_SCUSESS;
                    Bundle bundle = new Bundle();
                    bundle.putString("amount", amount);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            });


        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // MyApp.showToast(getString(R.string.bds_Withdraw_fail));
                    //handler.sendEmptyMessage(ZHUANZHNAG_FAIL);
//                    Message message = Message.obtain();
//                    message.what = ZHUANZHNAG_FAIL;
//                    Bundle bundle = new Bundle();
//                    bundle.putString("amount", amount);
//                    message.setData(bundle);
//                    handler.sendMessage(message);
                    deleteOrder(id);
                }
            });


        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    myProgressDialog.dismiss();
                    tv_confirm.setEnabled(true);
                }

            }
        });

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
                if (getActivity() != null) {
                    if (response.body().getStatus().equals("success")) {
                        mobile = response.body().getData().get(0).getMobile();
                        tv_phone.setText(mobile);
                    } else {
                        //MyApp.showToast(response.body().getMsg());
                    }
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


    private void deleteOrder(String id) {

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("id", id);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME,""));

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            myProgressDialog.dismiss();
            return;
        }
        hashMap.put("encmsg", signMessage);

        AcceptorApi.acceptantHttp(hashMap, "acc_exit_fdig", new JsonCallBack<DeleteOrderModel>(DeleteOrderModel.class) {
            @Override
            public void onSuccess(Response<DeleteOrderModel> response) {

            }
        });

        if (getActivity() != null) {
            MyApp.showToast(getString(R.string.bds_transfer_fail_next));
            myProgressDialog.dismiss();
        }


    }
}
