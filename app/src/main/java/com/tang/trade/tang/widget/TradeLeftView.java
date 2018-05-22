package com.tang.trade.tang.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.ui.exchangefragment.BuyFragment;
import com.tang.trade.tang.ui.exchangefragment.SellFragment;
import com.tang.trade.tang.ui.fragment.NewHomeFragment;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;

import java.math.BigDecimal;

public class TradeLeftView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = "TradeLeftView";
    private final BigDecimal bd_0 = new BigDecimal(0.0);
    private final BigDecimal bd_scale_price = new BigDecimal(0.00001).setScale(5, BigDecimal.ROUND_DOWN);
    private final BigDecimal bd_scale_price_8 = new BigDecimal(0.00000001).setScale(8, BigDecimal.ROUND_DOWN);
    private final BigDecimal bd_scale_amount = new BigDecimal(1).setScale(0, BigDecimal.ROUND_DOWN);
    private EditText et_total;
    private EditTextDrawableClick etd_price;
    private EditTextDrawableClick etd_amount;
    private int mScale_base = 5;
    private int mScale_quote = 5;
    private Button btn_buy;
    private Button btn_sell;
    private EditText et_balance;
    private EditText et_fee;
    private TextView tv_25_percent;
    private TextView tv_50_percent;
    private TextView tv_75_percent;
    private TextView tv_100_percent;
    private TextView tv_balance;
    private TextView tv_price;
    private TextView tv_amount;
    private TextView tv_charge;
    private TextView tv_total;
    private MyProgressDialog progressDialog;
    private BigDecimal bd_price;
    private BigDecimal bd_amount;
    private BigDecimal bd_total;
    private String mCurrentPrice;
    private String mCurrentAmount;
    public MainActivity.MyOnTouchListener myOnTouchListener = new MainActivity.MyOnTouchListener() {
        @Override
        public void onTouch(MotionEvent ev) {
            mCurrentPrice = etd_price.getText().toString();
            mCurrentAmount = etd_amount.getText().toString();
            String sTotal = et_total.getText().toString();

            if (!TextUtils.isEmpty(mCurrentPrice)) {
                bd_price = new BigDecimal(mCurrentPrice).setScale(NewHomeFragment.sScale[0], BigDecimal.ROUND_DOWN);
                etd_price.setText(bd_price.toPlainString());
            }

            if (!TextUtils.isEmpty(mCurrentAmount)) {
                bd_amount = new BigDecimal(mCurrentAmount).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_DOWN);
                etd_amount.setText(bd_amount.toPlainString());
            }

//            if (!TextUtils.isEmpty(mCurrentPrice) && !TextUtils.isEmpty(mCurrentAmount)) {
//                calculateTotalMoney();
//            }
//            else {
//                if (!TextUtils.isEmpty(sTotal)) {
//                    if (Double.parseDouble(sTotal) > 0) {
//                        et_total.setText("0");
//                    }
//                }
//            }
        }
    };
    private BigDecimal bd_balances;
    private BuyFragment mBuyFragment;
    private SellFragment mSellFragment;
    private int mTradeType;
    private String baseAsset;
    private String quoteAsset;
    private Context mContext;
    private double dBalances = 0;
    private BigDecimal bd_fee = new BigDecimal(0);

    public TradeLeftView(@NonNull Context context, AttributeSet attr) {
        super(context, attr);
        LayoutInflater.from(context).inflate(R.layout.trade_left_view, this);

        initView();
    }

    public String getCurrentPrice() {
        return etd_price.getText().toString();
    }

    public String getCurrentAmount() {
        return etd_amount.getText().toString();
    }

    public void fillBalance(String balance) {
        et_balance.setText(balance);
    }

    public void fillFee(String fee) {
        et_fee.setText(fee);
    }

    public void fillPrice(String price) {
        etd_price.setText(price);
    }

    public void fillAmount(String amount) {
        etd_amount.setText(amount);
    }

    public void fillTotal(String total) {
        et_total.setText(total);
    }

    private void initView() {
        btn_buy = this.findViewById(R.id.btn_buy);
        btn_sell = this.findViewById(R.id.btn_sell);
        tv_25_percent = this.findViewById(R.id.tv_25_percent);
        tv_50_percent = this.findViewById(R.id.tv_50_percent);
        tv_75_percent = this.findViewById(R.id.tv_75_percent);
        tv_100_percent = this.findViewById(R.id.tv_100_percent);
        et_balance = this.findViewById(R.id.et_balance);
        etd_price = this.findViewById(R.id.etd_price);
        etd_amount = this.findViewById(R.id.etd_amount);
        et_total = this.findViewById(R.id.et_total);
        et_fee = this.findViewById(R.id.et_fee);
        tv_balance = this.findViewById(R.id.tv_balance);
        tv_charge = this.findViewById(R.id.tv_charge);
        tv_total = this.findViewById(R.id.tv_total);
        tv_price = this.findViewById(R.id.tv_price);
        tv_amount = this.findViewById(R.id.tv_amount);

        tv_25_percent.setOnClickListener(this);
        tv_50_percent.setOnClickListener(this);
        tv_75_percent.setOnClickListener(this);
        tv_100_percent.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        btn_sell.setOnClickListener(this);

        setPriceAndAmountOnClick();
    }

    public void setBuyFragment(BuyFragment fragment) {
        mBuyFragment = fragment;
        mTradeType = ReChangeFragment2.BUY;
        baseAsset = mBuyFragment.baseAsset;
        quoteAsset = mBuyFragment.quoteAsset;

        if (baseAsset == null || quoteAsset == null) {
            return;
        }

        mContext = mBuyFragment.getContext();
        initData();

        clearTextValue();
    }

    public void setSellFragment(SellFragment fragment) {
        mSellFragment = fragment;
        mTradeType = ReChangeFragment2.SELL;
        baseAsset = mSellFragment.baseAsset;
        quoteAsset = mSellFragment.quoteAsset;

        if (baseAsset == null || quoteAsset == null) {
            return;
        }

        mContext = mSellFragment.getContext();
        initData();

        clearTextValue();
    }

    public void setBalance(double balance) {
        dBalances = balance;
    }

    public void setFee(BigDecimal fee) {
        bd_fee = fee;
    }

    public void setBasePrecision(int precision) {
        mScale_base = precision;
    }

    public void setQuotePrecision(int precision) {
        mScale_quote = precision;
    }

    private void initData() {
        SpannableString styledText = new SpannableString("");
        if (LaunchActivity.lang.equals("en")) {
            if (baseAsset.equals("USD")) {
                styledText = new SpannableString(getResources().getString(R.string.price) + ":" + "BDSUSD/" + quoteAsset);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 6, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (baseAsset.equals("BDS")) {
                if (quoteAsset.equals("CNY")) {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + baseAsset + "/BDSUSD");
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 13, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + baseAsset + "/" + quoteAsset);
                }
            }
        } else {
            if (baseAsset.equals("CNY")) {
                styledText = new SpannableString(getResources().getString(R.string.price) + ":" + "BDSCNY/" + quoteAsset);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 6, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (baseAsset.equals("BDS")) {
                if (quoteAsset.equals("CNY")) {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + baseAsset + "/BDSCNY");
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + baseAsset + "/" + quoteAsset);
                }
            }
        }
        tv_price.setText(styledText, TextView.BufferType.SPANNABLE);

        if (ReChangeFragment2.BUY == mTradeType) {
            btn_buy.setVisibility(View.VISIBLE);
            btn_sell.setVisibility(View.GONE);

            TextViewUtils.setTextView1(tv_balance, mContext, baseAsset, getResources().getString(R.string.bds_balance));
            TextViewUtils.setTextView1(tv_total, mContext, baseAsset, getResources().getString(R.string.gmv));
            TextViewUtils.setTextView1(tv_charge, mContext, baseAsset, getResources().getString(R.string.change1));
            tv_amount.setText(getResources().getString(R.string.Amount) + quoteAsset);
        } else if (ReChangeFragment2.SELL == mTradeType) {
            btn_buy.setVisibility(View.GONE);
            btn_sell.setVisibility(View.VISIBLE);

            tv_balance.setText(getResources().getString(R.string.Balance) + quoteAsset);
            tv_amount.setText(getResources().getString(R.string.Amount) + quoteAsset);
            tv_charge.setText(getResources().getString(R.string.change) + quoteAsset);
            TextViewUtils.setTextView1(tv_total, mContext, baseAsset, getResources().getString(R.string.gmv));
        }
    }

    private void setPriceAndAmountOnClick() {
        if (TextUtils.isEmpty(etd_price.getText().toString())) {
            bd_price = BigDecimal.valueOf(0);
        } else {
            bd_price = BigDecimal.valueOf(Double.valueOf(etd_price.getText().toString()));
        }

        if (TextUtils.isEmpty(etd_amount.getText().toString())) {
            bd_amount = BigDecimal.valueOf(0);
        } else {
            bd_amount = BigDecimal.valueOf(Double.valueOf(etd_amount.getText().toString()));
        }

        etd_price.setOnDrawableClickListener(new EditTextDrawableClick.OnDrawableClickListener() {
            @Override
            public void onDrawableBottomClick(View view) {

            }

            @Override
            public void onDrawableLeftClick(View view) {
                bd_price = bd_price.subtract(bd_scale_price).setScale(NewHomeFragment.sScale[0], BigDecimal.ROUND_UP);

                if (bd_price.compareTo(bd_0) >= 0) {
                    etd_price.setText(bd_price.toPlainString());
                    calculateTotalMoney();
                } else {
                    etd_price.setText("0");
                    bd_price = new BigDecimal("0");
                }
            }

            @Override
            public void onDrawableTopClick(View view) {

            }

            @Override
            public void onDrawableRightClick(View view) {
                bd_price = bd_price.add(bd_scale_price).setScale(NewHomeFragment.sScale[0], BigDecimal.ROUND_UP);
                etd_price.setText(bd_price.toPlainString());
                calculateTotalMoney();
            }
        });

        etd_amount.setOnDrawableClickListener(new EditTextDrawableClick.OnDrawableClickListener() {
            @Override
            public void onDrawableBottomClick(View view) {

            }

            @Override
            public void onDrawableLeftClick(View view) {
                bd_amount = bd_amount.subtract(bd_scale_amount).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);

                if (bd_amount.compareTo(bd_0) >= 0) {
                    etd_amount.setText(bd_amount.toPlainString());
                    calculateTotalMoney();
                } else {
                    etd_amount.setText("0");
                    bd_amount = new BigDecimal("0");
                }
            }

            @Override
            public void onDrawableTopClick(View view) {

            }

            @Override
            public void onDrawableRightClick(View view) {
                bd_amount = bd_amount.add(bd_scale_amount).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                etd_amount.setText(bd_amount.toPlainString());

                calculateTotalMoney();
            }
        });
        etd_price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotalMoney();
            }
        });
        etd_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotalMoney();
            }
        });
    }

    private void calculateTotalMoney() {
        String sPrice = etd_price.getText().toString();
        String sAmount = etd_amount.getText().toString();

        if (!TextUtils.isEmpty(sPrice) && !TextUtils.isEmpty(sAmount)) {
            bd_price = BigDecimal.valueOf(Double.valueOf(sPrice));
            bd_amount = BigDecimal.valueOf(Double.valueOf(sAmount));

//            if (ReChangeFragment2.BUY == mTradeType) {
//                bd_price = bd_price.add(new BigDecimal("0.00001"));
//            } else if (ReChangeFragment2.SELL == mTradeType) {
//                bd_price = bd_price.subtract(new BigDecimal("0.00001"));
//            }
            if (NewHomeFragment.sScale != null) {
                bd_total = bd_price.multiply(bd_amount).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                et_total.setText(bd_total.toPlainString());
            }
//            if (bd_price.compareTo(bd_0) > 0 && bd_amount.compareTo(bd_0) > 0) {
//                bd_total = bd_price.multiply(bd_amount).setScale(sScale_base, BigDecimal.ROUND_UP);
//                et_total.setText(bd_total.toPlainString());
//            }
        }
    }

    private void calculatePrice() {
        String sTotal = et_total.getText().toString();
        String sAmount = etd_amount.getText().toString();

        if (!TextUtils.isEmpty(sTotal) && !TextUtils.isEmpty(sAmount)) {
            bd_total = BigDecimal.valueOf(Double.valueOf(sTotal));
            bd_amount = BigDecimal.valueOf(Double.valueOf(sAmount));

            if (bd_total.compareTo(bd_0) > 0 && bd_amount.compareTo(bd_0) > 0) {
                bd_price = bd_total.divide(bd_amount, mScale_base, BigDecimal.ROUND_HALF_UP);
                //etd_price.setText(bd_price.toPlainString());
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_25_percent:
                switchState(0);
                break;
            case R.id.tv_50_percent:
                switchState(1);
                break;
            case R.id.tv_75_percent:
                switchState(2);
                break;
            case R.id.tv_100_percent:
                switchState(3);
                break;
            case R.id.btn_buy:
                if (checkData()) {
                    showTradeInfo();
                }
                break;
            case R.id.btn_sell:
                if (checkData()) {
                    showTradeInfo();
                }
                break;
            default:
                break;
        }
    }

    private void showTradeInfo() {
        String amount = etd_amount.getText().toString();
        String total = et_total.getText().toString();

        if (!TextUtils.isEmpty(total) && !TextUtils.isEmpty(amount)) {
            bd_total = BigDecimal.valueOf(Double.valueOf(total));
            bd_amount = BigDecimal.valueOf(Double.valueOf(amount));

            if (bd_total.compareTo(bd_0) > 0 && bd_amount.compareTo(bd_0) > 0) {
                bd_price = bd_total.divide(bd_amount, NewHomeFragment.sScale[0], BigDecimal.ROUND_HALF_UP);
            }
        }

        String price = bd_price.toString();

        price = price + baseAsset + "/" + quoteAsset;
        amount = amount + quoteAsset;
        total = total + baseAsset;

        final BottomDialog bottomDialog = new BottomDialog(mContext);
        bottomDialog.setExchargeDalogText(price, amount, total);
        bottomDialog.show();
        bottomDialog.setOnDialogClick(new BottomDialog.OnDialogClick() {

            @Override
            public void OnSubmitListener() {
                if (ReChangeFragment2.BUY == mTradeType) {
                    buy();
                } else if (ReChangeFragment2.SELL == mTradeType) {
                    sell();
                }

                bottomDialog.dismiss();
            }
        });
    }

    private void buy() {
        progressDialog = MyProgressDialog.getInstance(mContext);
        progressDialog.show();

        new BuyOrSellTask().execute(Integer.valueOf(mTradeType));
    }

    private void sell() {
        progressDialog = MyProgressDialog.getInstance(mContext);
        progressDialog.show();

        new BuyOrSellTask().execute(Integer.valueOf(mTradeType));
    }

    private boolean doBuyOrSell(int type) {
        if (type == 0) {
            return doBuy();
        } else if (type == 1) {
            return doSell();
        }

        return false;
    }

    private boolean doBuy() {
        String total = et_total.getText().toString();
        String mAmount = etd_amount.getText().toString();
        signed_transaction signed_transaction_buy = null;

        if (TextUtils.isEmpty(mAmount) || TextUtils.isEmpty(total)) {
            return false;
        }

        try {
            asset_object obj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseAsset);
            if (obj == null || obj.id == null) {
                return false;
            }

            String baseAssetId = obj.id.toString();
            if (baseAsset.equalsIgnoreCase("BDS")) {
                signed_transaction_buy = BitsharesWalletWraper.getInstance().buy(
                        quoteAsset,
                        baseAsset,
                        Double.parseDouble(total),
                        Double.parseDouble(mAmount),
                        "0",
                        "",
                        MyApp.get(BuildConfig.INDEX, 0)
                );
            } else {
                signed_transaction_buy = BitsharesWalletWraper.getInstance().buy(
                        quoteAsset,
                        baseAsset,
                        Double.parseDouble(total),
                        Double.parseDouble(mAmount),
                        bd_fee + "",
                        baseAssetId,
                        MyApp.get(BuildConfig.INDEX, 0)
                );
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (signed_transaction_buy != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean doSell() {
        String total = et_total.getText().toString();
        String mAmount = etd_amount.getText().toString();
        signed_transaction signed_transaction_sell = null;

        if (TextUtils.isEmpty(mAmount) || TextUtils.isEmpty(total)) {
            return false;
        }

        try {
            asset_object obj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(quoteAsset);
            if (obj == null || obj.id == null) {
                return false;
            }

            String baseAssetId = obj.id.toString();
            if (quoteAsset.equalsIgnoreCase("BDS")) {
                signed_transaction_sell = BitsharesWalletWraper.getInstance().sell(
                        quoteAsset,
                        baseAsset,
                        Double.parseDouble(total),
                        Double.parseDouble(mAmount),
                        "0",
                        "",
                        MyApp.get(BuildConfig.INDEX, 0));
            } else {
                signed_transaction_sell = BitsharesWalletWraper.getInstance().sell(
                        quoteAsset,
                        baseAsset,
                        Double.parseDouble(total),
                        Double.parseDouble(mAmount),
                        bd_fee + "",
                        baseAssetId,
                        MyApp.get(BuildConfig.INDEX, 0));
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (signed_transaction_sell != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkData() {
        try {
            String sPrice = etd_price.getText().toString();
            String sAmount = etd_amount.getText().toString();
            String sTotal = et_total.getText().toString();

            String sBalance = et_balance.getText().toString();
            if (TextUtils.isEmpty(sBalance)) {
                MyApp.showToast(getResources().getString(R.string.bds_note_insufficient_funds));
                return false;
            }

            if (TextUtils.isEmpty(sPrice) || TextUtils.isEmpty(sAmount) || TextUtils.isEmpty(sTotal)) {
                MyApp.showToast(getResources().getString(R.string.bds_node_null_priceOrsumber));
                return false;
            }

            double amounts = Double.parseDouble(et_total.getText().toString());
            double fee = Double.parseDouble(et_fee.getText().toString());
            double total_amounts = CalculateUtils.add(amounts, fee);

            if (TextUtils.isEmpty(sPrice) || TextUtils.isEmpty(sAmount)) {
                MyApp.showToast(getResources().getString(R.string.bds_node_null_priceOrsumber));
                return false;
            }

            double dou_price = Double.parseDouble(sPrice);
            double dou_sumber = Double.parseDouble(sAmount);
            if (dou_price == 0 || dou_sumber == 0) {
                MyApp.showToast(getResources().getString(R.string.bds_node_0_priceOrsumber));
                return false;
            }
            if (ReChangeFragment2.BUY == mTradeType) {
                if (dBalances > amounts && dBalances < amounts + fee) {
                    showDialogFail();
                    return false;
                }

            }
            if (ReChangeFragment2.SELL == mTradeType) {
                if (dBalances > dou_sumber && dBalances < dou_sumber + fee) {
                    showDialogFail();
                    return false;
                }
            }

            if (ReChangeFragment2.BUY == mTradeType) {
                if (total_amounts > dBalances) {
                    MyApp.showToast(getResources().getString(R.string.priceInfo));
                    return false;
                }
            }

            if (ReChangeFragment2.SELL == mTradeType) {
                if (dou_sumber > dBalances) {
                    MyApp.showToast(getResources().getString(R.string.priceInfo));
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void switchState(int state) {
        if (state != 4) {
            scaleAmount(state);
        }

        switch (state) {
            case 0:
                tv_25_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                tv_25_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_blue));
                tv_50_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_50_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_75_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_75_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_100_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_100_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                break;
            case 1:
                tv_25_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_25_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_50_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                tv_50_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_blue));
                tv_75_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_75_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_100_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_100_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                break;
            case 2:
                tv_25_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_25_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_50_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_50_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_75_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                tv_75_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_blue));
                tv_100_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_100_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                break;
            case 3:
                tv_25_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_25_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_50_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_50_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_75_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_75_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_100_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                tv_100_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_blue));
                break;
            case 4:
                tv_25_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_25_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_50_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_50_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_75_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_75_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
                tv_100_percent.setTextColor(getResources().getColor(R.color.rechange_switch_text));
                tv_100_percent.setBackground(getResources().getDrawable(R.drawable.bg_rechange_rect_gray));
            default:
                break;
        }
    }

    private void scaleAmount(int state) {
        String str = et_balance.getText().toString();
        if (TextUtils.isEmpty(str)) {
            str = "0";
        }
        bd_balances = new BigDecimal(str);

        switch (state) {
            case 0:
                setScaleAmount(bd_balances, new BigDecimal(0.25));
                break;
            case 1:
                setScaleAmount(bd_balances, new BigDecimal(0.50));
                break;
            case 2:
                setScaleAmount(bd_balances, new BigDecimal(0.75));
                break;
            case 3:
                setScaleAmount(bd_balances, new BigDecimal(1.00));
                break;
            default:
                break;
        }
    }

    private void setScaleAmount(BigDecimal balance, BigDecimal scale) {
        BigDecimal one = new BigDecimal(1.00);
        BigDecimal bd_total;
        if (ReChangeFragment2.BUY == mTradeType) {
            if (scale.compareTo(one) == 0) {
                if (balance.compareTo(bd_fee) <= 0) {
                    MyApp.showToast(R.string.bds_note_insufficient_funds);
                    clearTextValue();
                    return;
                }

                bd_total = balance.subtract(bd_fee);
            } else {
                BigDecimal scale_temp = one.subtract(scale);
                BigDecimal balance_scale = balance.multiply(scale).setScale(mScale_base, BigDecimal.ROUND_UP);
                BigDecimal balance_scale_temp = balance.multiply(scale_temp).setScale(mScale_base, BigDecimal.ROUND_UP);

                if (balance_scale.compareTo(bd_fee) <= 0) {
                    MyApp.showToast(R.string.bds_note_insufficient_fee);
                    clearTextValue();
                    return;
                }

                if (balance_scale_temp.compareTo(bd_fee) <= 0) {
                    bd_total = balance.subtract(bd_fee).multiply(scale).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                } else {
                    bd_total = balance.multiply(scale).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                }
            }

            String price = etd_price.getText().toString();
            if (!TextUtils.isEmpty(price)) {
                BigDecimal bd_price = new BigDecimal(price);
                if (bd_price.compareTo(bd_0) > 0) {
                    BigDecimal bd_amount = bd_total.divide(bd_price, NewHomeFragment.sScale[1], BigDecimal.ROUND_DOWN);
                    bd_total = bd_price.multiply(bd_amount).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                    etd_amount.setText(bd_amount.toPlainString());
                    et_total.setText(bd_total.toPlainString());
                }
            }
        } else if (ReChangeFragment2.SELL == mTradeType) {
            BigDecimal bd_amount;
            if (scale.compareTo(one) == 0) {
                if (balance.compareTo(bd_fee) <= 0) {
                    MyApp.showToast(R.string.bds_note_insufficient_funds);
                    clearTextValue();
                    return;
                }

                bd_amount = balance.subtract(bd_fee).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                etd_amount.setText(bd_amount.toPlainString());
            } else {
                BigDecimal scale_temp = one.subtract(scale);
                BigDecimal balance_scale = balance.multiply(scale).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                BigDecimal balance_scale_temp = balance.multiply(scale_temp).setScale(mScale_quote, BigDecimal.ROUND_UP);

                if (balance_scale_temp.compareTo(bd_fee) < 0) {
                    MyApp.showToast(R.string.bds_note_insufficient_fee);
                    clearTextValue();
                    return;
                }

                bd_amount = balance_scale;
                etd_amount.setText(bd_amount.toPlainString());
            }

            String price = etd_price.getText().toString();
            if (!TextUtils.isEmpty(price)) {
                BigDecimal bd_price = new BigDecimal(price);
                if (bd_price.compareTo(bd_0) <= 0 || bd_amount.compareTo(bd_0) <= 0) {
                    return;
                }

                bd_total = bd_price.multiply(bd_amount).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP);
                et_total.setText(bd_total.toPlainString());
            }
        }
    }

    public void clearTextValue() {
        etd_price.setText("0");
        etd_amount.setText("0");
        et_total.setText("0");
        //et_charge_bds.setText("0");
        switchState(4);
    }

    private void showDialogFail() {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle(R.string.prompt);
        builder.setMessage(R.string.guadan_fail);
        builder.setNegativeButton(R.string.bds_sure, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public class BuyOrSellTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... type) {
            return doBuyOrSell(type[0].intValue());
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                MyApp.showToast(getResources().getString(R.string.bds_OrderSubmitted));
                clearTextValue();

                if (ReChangeFragment2.BUY == mTradeType) {
                    mBuyFragment.getOnBuySuccessListener().onBuySuccess();
                } else if (ReChangeFragment2.SELL == mTradeType) {
                    mSellFragment.getOnSellSuccessListener().onSellSuccess();
                }
//                ReChangeFragment2.getReChangeFragment().new GetBalancesTask().execute(baseAsset, quoteAsset, SPUtils.getString(Const.USERNAME,""));
//                new GetBalancesTask().execute(baseAsset, quoteAsset, SPUtils.getString(Const.USERNAME,""));
            } else {
                MyApp.showToast(getResources().getString(R.string.bds_FailedToSubmitOrder));
            }

            progressDialog.dismiss();
        }

    }

}
