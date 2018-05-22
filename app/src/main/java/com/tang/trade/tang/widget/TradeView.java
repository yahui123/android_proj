package com.tang.trade.tang.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TradeView extends FrameLayout {


    private final BigDecimal m0 = new BigDecimal(0.0);
    @BindView(R.id.tv_balance)
    TextView mTvBalance;
    @BindView(R.id.et_balance)
    EditText mEtBalance;
    @BindView(R.id.tv_price)
    TextView mTvPrice;
    @BindView(R.id.etd_price)
    EditTextDrawableClick mEtdPrice;
    @BindView(R.id.tv_amount)
    TextView mTvAmount;
    @BindView(R.id.etd_amount)
    EditTextDrawableClick mEtdAmount;
    @BindView(R.id.tv_25_percent)
    TextView mTv25Percent;
    @BindView(R.id.tv_50_percent)
    TextView mTv50Percent;
    @BindView(R.id.tv_75_percent)
    TextView mTv75Percent;
    @BindView(R.id.tv_100_percent)
    TextView mTv100Percent;
    @BindView(R.id.tv_total)
    TextView mTvTotal;
    @BindView(R.id.et_total)
    EditText mEtTotal;
    @BindView(R.id.btn_buy)
    Button mBtnBuy;
    @BindView(R.id.btn_sell)
    Button mBtnSell;
    @BindView(R.id.tv_fee)
    TextView mTvFee;
    @BindView(R.id.et_fee)
    EditText mEtFee;
    private BigDecimal mBalance;
    private double mFee;
    private BigDecimal mPrice;
    private BigDecimal mAmount;
    private BigDecimal mTurnover;
    private boolean mIsSell;
    private double mPriceScale;
    private double mAmountScale;
    private Context mContext;
    private String mBaseAsset;
    private String mQuoteAsset;

    public TradeView(@NonNull Context context) {
        this(context, null);
    }

    public TradeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public TradeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    private void initData() {
        SpannableString styledText = new SpannableString("");
        if (LaunchActivity.lang.equals("en")) {
            if (mBaseAsset.equals("USD")) {
                styledText = new SpannableString(getResources().getString(R.string.price) + ":" + "BDSUSD/" + mQuoteAsset);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 6, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (mBaseAsset.equals("BDS")) {
                if (mQuoteAsset.equals("CNY")) {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + mBaseAsset + "/BDSUSD");
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 13, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + mBaseAsset + "/" + mQuoteAsset);
                }
            }
        } else {
            if (mBaseAsset.equals("CNY")) {
                styledText = new SpannableString(getResources().getString(R.string.price) + ":" + "BDSCNY/" + mQuoteAsset);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 6, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (mBaseAsset.equals("BDS")) {
                if (mQuoteAsset.equals("CNY")) {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + mBaseAsset + "/BDSCNY");
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style2), 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style3), 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    styledText = new SpannableString(getResources().getString(R.string.price) + ":" + mBaseAsset + "/" + mQuoteAsset);
                }
            }
        }
        mTvPrice.setText(styledText, TextView.BufferType.SPANNABLE);

        if (isSell()) {
            mBtnBuy.setVisibility(View.GONE);
            mBtnSell.setVisibility(View.VISIBLE);

            mTvBalance.setText(getResources().getString(R.string.Balance) + mQuoteAsset);
            mTvAmount.setText(getResources().getString(R.string.Amount) + mQuoteAsset);
            mTvFee.setText(getResources().getString(R.string.change) + mQuoteAsset);
            TextViewUtils.setTextView1(mTvTotal, mContext, mBaseAsset, getResources().getString(R.string.gmv));
        } else {
            mBtnBuy.setVisibility(View.VISIBLE);
            mBtnSell.setVisibility(View.GONE);

            TextViewUtils.setTextView1(mTvBalance, mContext, mBaseAsset, getResources().getString(R.string.bds_balance));
            TextViewUtils.setTextView1(mTvTotal, mContext, mBaseAsset, getResources().getString(R.string.gmv));
            TextViewUtils.setTextView1(mTvFee, mContext, mBaseAsset, getResources().getString(R.string.change1));
            mTvAmount.setText(getResources().getString(R.string.Amount) + mQuoteAsset);
        }
    }

    public double getPriceScale() {
        return mPriceScale;
    }

    public void setPriceScale(double priceScale) {
        mPriceScale = priceScale;
    }

    public double getAmountScale() {
        return mAmountScale;
    }

    public void setAmountScale(double amountScale) {
        mAmountScale = amountScale;
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.trade_view, this, true);
        ButterKnife.bind(this);

        mEtdPrice.setOnDrawableClickListener(new EditTextDrawableClick.OnDrawableClickListener() {
            @Override
            public void onDrawableBottomClick(View view) {

            }

            @Override
            public void onDrawableLeftClick(View view) {
                mPrice = mPrice.subtract(BigDecimal.valueOf(Math.pow(10d, (mPriceScale * -1))));

                if (mPrice.compareTo(m0) >= 0) {
                    mEtdPrice.setText(mPrice.toPlainString());
                } else {
                    mEtdPrice.setText("0");
                }
            }

            @Override
            public void onDrawableTopClick(View view) {

            }

            @Override
            public void onDrawableRightClick(View view) {
                mPrice = mPrice.add(BigDecimal.valueOf(Math.pow(10d, (mPriceScale * -1))));
                mEtdPrice.setText(mPrice.toPlainString());
            }
        });

        mEtdAmount.setOnDrawableClickListener(new EditTextDrawableClick.OnDrawableClickListener() {
            @Override
            public void onDrawableBottomClick(View view) {

            }

            @Override
            public void onDrawableLeftClick(View view) {
                mAmount = mAmount.subtract(BigDecimal.valueOf(Math.pow(10d, (mAmountScale * -1))));

                if (mAmount.compareTo(m0) >= 0) {
                    mEtdAmount.setText(mAmount.toPlainString());
                } else {
                    mEtdAmount.setText("0");
                }
            }

            @Override
            public void onDrawableTopClick(View view) {

            }

            @Override
            public void onDrawableRightClick(View view) {
                mAmount = mAmount.add(BigDecimal.valueOf(Math.pow(10d, (mAmountScale * -1))));
                mEtdAmount.setText(mAmount.toPlainString());
            }
        });
    }

    public void putTradeInfo() {

    }

    public void clearTradeInfo() {
        mEtdPrice.setText("0");
        mEtdAmount.setText("0");
        mEtTotal.setText("0");
    }

    public void fillBalance(String balance) {
        mBalance = BigDecimal.valueOf(Double.parseDouble(balance));
        mEtBalance.setText(String.valueOf(mBalance));
    }

    public void fillFee(String fee) {
        mFee = Double.parseDouble(fee);
        mEtFee.setText(String.valueOf(mFee));
    }

    public void fillPirce(String price) {
        mPrice = new BigDecimal(price);

        if (mPrice.compareTo(BigDecimal.valueOf(10d)) < 0) {

            mEtdPrice.setText(NumberUtils.formatNumber5(String.valueOf(mPrice), RoundingMode.DOWN));
        } else {
            mEtdPrice.setText(NumberUtils.formatNumber2(String.valueOf(mPrice), RoundingMode.DOWN));
        }
    }

    public void fillAmount(String amount) {
        mAmount = BigDecimal.valueOf(Double.parseDouble(amount));

        if (mAmount.compareTo(BigDecimal.valueOf(get100PercentAmount())) > 0) {
            mAmount = BigDecimal.valueOf(get100PercentAmount());
        }

        if (mPrice.compareTo(BigDecimal.valueOf(1d)) < 0) {
            mEtdAmount.setText(NumberUtils.formatNumber0(String.valueOf(mAmount), RoundingMode.DOWN));
        } else if (mPrice.compareTo(BigDecimal.valueOf(1d)) >= 0 && mPrice.compareTo(BigDecimal.valueOf(1000d)) < 0) {
            mEtdAmount.setText(NumberUtils.formatNumber2(String.valueOf(mAmount), RoundingMode.DOWN));
        } else {
            mEtdAmount.setText(NumberUtils.formatNumber5(String.valueOf(mAmount), RoundingMode.DOWN));
        }
    }

    public void fillTurnover(String turnover) {
        mTurnover = BigDecimal.valueOf(Double.parseDouble(turnover));

        if (mPrice.compareTo(BigDecimal.valueOf(10d)) < 0) {

            mEtTotal.setText(NumberUtils.formatNumber5(String.valueOf(mTurnover), RoundingMode.DOWN));
        } else {

            mEtTotal.setText(NumberUtils.formatNumber2(String.valueOf(mTurnover), RoundingMode.DOWN));
        }
    }

    public boolean isSell() {
        return mIsSell;
    }

    public void setSell(boolean sell) {
        mIsSell = sell;
    }

    private double get100PercentAmount() {

        if (isSell()) {
            return mBalance.subtract(BigDecimal.valueOf(mFee)).divide(mPrice, 5, BigDecimal.ROUND_DOWN).doubleValue();
        } else {
            return mBalance.subtract(BigDecimal.valueOf(mFee)).doubleValue();
        }
    }

    private double get75PercentAmount() {
        return get100PercentAmount() * 0.75;
    }

    private double get50PercentAmount() {
        return get100PercentAmount() * 0.5;
    }

    private double get25PercentAmount() {
        return get100PercentAmount() * 0.25;
    }


    @OnClick(R.id.tv_25_percent)
    public void onTv25PercentClicked() {
        fillAmount(String.valueOf(get25PercentAmount()));
        fillTurnover(String.valueOf(mAmount.multiply(mPrice)));
    }

    @OnClick(R.id.tv_50_percent)
    public void onTv50PercentClicked() {
        fillAmount(String.valueOf(get50PercentAmount()));
        fillTurnover(String.valueOf(mAmount.multiply(mPrice)));
    }

    @OnClick(R.id.tv_75_percent)
    public void onTv75PercentClicked() {
        fillAmount(String.valueOf(get75PercentAmount()));
        fillTurnover(String.valueOf(mAmount.multiply(mPrice)));
    }

    @OnClick(R.id.tv_100_percent)
    public void onTv100PercentClicked() {
        fillAmount(String.valueOf(get100PercentAmount()));
        fillTurnover(String.valueOf(mAmount.multiply(mPrice)));
    }

    @OnClick(R.id.btn_buy)
    public void onBtnBuyClicked() {

    }

    @OnClick(R.id.btn_sell)
    public void onBtnSellClicked() {

    }
}
