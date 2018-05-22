package com.tang.trade.tang.ui.exchangefragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tang.trade.app.Const;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.BuyOrSellAdapter2;
import com.tang.trade.tang.adapter.LatestDealAdapter;
import com.tang.trade.tang.net.model.AllHistoryResult;
import com.tang.trade.tang.net.model.BuyOrSellResponseModel;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.ui.fragment.NewHomeFragment;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.widget.ListViewForScrollView;
import com.tang.trade.tang.widget.TradeLeftView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuyFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final String TAG = "BuyFragment";
    public int mTradeType;
    public String baseAsset;
    public String quoteAsset;
    List<BuyOrSellResponseModel.DataBean> showSellList = new ArrayList<>();
    List<BuyOrSellResponseModel.DataBean> showBuyList = new ArrayList<>();
    List<AllHistoryResult> latestDealList = new ArrayList<>();
    private ListViewForScrollView lv_sell;
    private ListViewForScrollView lv_buy;
    private ViewStub loading_sell;
    private ViewStub loading_buy;
    private ViewStub loading_latest;
    private ListView lv_latest_deal;
    private EditText et_transaction_price;
    private ImageView img_latest_deal;
    private ImageView img_latest_deal_1;
    private TextView tv_latest_deal;
    private TextView tv_latest_deal_1;
    private RelativeLayout rl_latest_deal;
    private ScrollView sv_container;
    private TradeLeftView tradeLeftView;
    private TextView list_price;
    private TextView list_amount;
    private View view;
    private BuyOrSellAdapter2 adapterSell;
    private BuyOrSellAdapter2 adapterBuy;
    private LatestDealAdapter adapterLatest;
    private TextView tv_amount_bds;
    private TextView tv_amount_cny;
    private ReChangeFragment2 reChangeFragment2;
    private boolean isViewCreated = false;
    private boolean isVisible = false;
    private View loadingSellView;
    private View loadingBuyView;
    private View loadingLatestView;
    private BuyOnBalanceChangeListener buyOnBalanceChangeListener = new BuyOnBalanceChangeListener();
    //    private EditText et_charge_bds;
    private BuyOnFeeChangeListener buyOnFeeChangeListener = new BuyOnFeeChangeListener();
    private BuyOnLatestTradeChangeListener buyOnLatestTradeChangeListener = new BuyOnLatestTradeChangeListener();
    private BuyOnOrderChangeListener buyOnOrderChangeListener = new BuyOnOrderChangeListener();
    private OnBuySuccessListener onBuySuccessListener;
    private OnVisibleListener onVisibleListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_buy_sell_2, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        isViewCreated = true;
        MainActivity.mainActivity.registerMyOnTouchListener(tradeLeftView.myOnTouchListener);
        reChangeFragment2 = (ReChangeFragment2) getParentFragment();
        reChangeFragment2.registerOnBalanceChangeListener(buyOnBalanceChangeListener);
        reChangeFragment2.registerOnFeeChangeListener(buyOnFeeChangeListener);
        reChangeFragment2.registerLatestTradeListener(buyOnLatestTradeChangeListener);
        reChangeFragment2.registerOrderChangeListener(buyOnOrderChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reChangeFragment2.unregisterOnBalanceChangeListener(buyOnBalanceChangeListener);
        reChangeFragment2.unregisterOnFeeChangeListener(buyOnFeeChangeListener);
        reChangeFragment2.unregisterLatestTradeListener(buyOnLatestTradeChangeListener);
        reChangeFragment2.unregisterOrderChangeListener(buyOnOrderChangeListener);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            isVisible = true;

            if (null != onVisibleListener) {
                onVisibleListener.onVisible();
            }
        } else {
            isVisible = false;

//            if (tradeLeftView != null) {
//                tradeLeftView.clearTextValue();
//            }
        }
    }

    public void clearTextValue() {
        if (null != et_transaction_price) {
            et_transaction_price.setText("0");
        }

        if (null != tradeLeftView) {
            tradeLeftView.clearTextValue();
        }
    }

    private void initView() {
        tradeLeftView = view.findViewById(R.id.trade_left_view);
        et_transaction_price = view.findViewById(R.id.et_transaction_price);
        list_price = view.findViewById(R.id.list_price);
        list_amount = view.findViewById(R.id.list_amount);

        img_latest_deal = view.findViewById(R.id.img_latest_deal);
        img_latest_deal_1 = view.findViewById(R.id.img_latest_deal_1);
        tv_latest_deal = view.findViewById(R.id.tv_latest_deal);
        tv_latest_deal_1 = view.findViewById(R.id.tv_latest_deal_1);
        lv_latest_deal = view.findViewById(R.id.lv_latest_deal);
        rl_latest_deal = view.findViewById(R.id.rl_latest_deal);
        sv_container = view.findViewById(R.id.sv_container);

        img_latest_deal.setOnClickListener(this);
        img_latest_deal_1.setOnClickListener(this);
        tv_latest_deal.setOnClickListener(this);
        tv_latest_deal_1.setOnClickListener(this);

        adapterLatest = new LatestDealAdapter(latestDealList);
        lv_latest_deal.setAdapter(adapterLatest);

        lv_sell = view.findViewById(R.id.lv_sell);
        lv_buy = view.findViewById(R.id.lv_buy);
        lv_sell.setOnItemClickListener(this);
        lv_buy.setOnItemClickListener(this);

        getAsset();

        list_price.setText(getResources().getString(R.string.price) + "(" + baseAsset + ")");
        list_amount.setText(getResources().getString(R.string.Amount1) + "(" + quoteAsset + ")");

        View headerView = View.inflate(this.getContext(), R.layout.latest_deal_item, null);
        tv_amount_bds = headerView.findViewById(R.id.tv_amount_bds);
        tv_amount_cny = headerView.findViewById(R.id.tv_amount_cny);
        tv_amount_bds.setText(quoteAsset);
        tv_amount_cny.setText(baseAsset);
        lv_latest_deal.addHeaderView(headerView);

        showLoading();
    }

    private void showLoading() {
        if (loadingBuyView != null) {
            loadingBuyView.setVisibility(View.VISIBLE);
        } else {
            loading_buy = view.findViewById(R.id.loading_buy);
            if (loading_buy != null) {
                loadingBuyView = loading_buy.inflate();
            }
        }

        if (loadingSellView != null) {
            loadingSellView.setVisibility(View.VISIBLE);
        } else {
            loading_sell = view.findViewById(R.id.loading_sell);
            if (loading_sell != null) {
                loadingSellView = loading_sell.inflate();
            }
        }

        if (loadingLatestView != null) {
            loadingLatestView.setVisibility(View.VISIBLE);
        } else {
            loading_latest = view.findViewById(R.id.loading_latest);
            if (loading_latest != null) {
                loadingLatestView = loading_latest.inflate();
            }
        }
    }

    private void hideLoading() {
        if (loadingBuyView != null) {
            loadingBuyView.setVisibility(View.GONE);
        }

        if (loadingSellView != null) {
            loadingSellView.setVisibility(View.GONE);
        }

        if (loadingLatestView != null) {
            loadingLatestView.setVisibility(View.GONE);
        }
    }

    private void getAsset() {
        mTradeType = getArguments().getInt(ReChangeFragment2.TRADE_TYPE);
        if (baseAsset == null && quoteAsset == null) {
            if (LaunchActivity.lang.equalsIgnoreCase("en")) {
                baseAsset = getArguments().getString(ReChangeFragment2.BASE, "USD");
            } else {
                baseAsset = getArguments().getString(ReChangeFragment2.BASE, "CNY");
            }
            quoteAsset = getArguments().getString(ReChangeFragment2.SELECT, "BDS");
        }

        if (quoteAsset != null) {
            if (Arrays.asList(Const.Asset.BIG_SCALE_ASSETS).contains(quoteAsset)) {
                setListViewAdapter(Const.Asset.ASSET_SCALE_8);
            } else {
                setListViewAdapter(Const.Asset.ASSET_SCALE_5);
            }
        }


        if (tradeLeftView != null) {
            tradeLeftView.setBuyFragment(this);
        }
    }

    private void setListViewAdapter(String precision) {
        adapterSell = new BuyOrSellAdapter2(showSellList, precision);
        adapterBuy = new BuyOrSellAdapter2(showBuyList, precision);
        lv_sell.setAdapter(adapterSell);
        lv_buy.setAdapter(adapterBuy);
    }

    public void updateAsset(String baseAsset, String quoteAsset) {
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        tv_amount_bds.setText(quoteAsset);
        tv_amount_cny.setText(baseAsset);

        if (list_price != null && list_amount != null) {
            list_price.setText(getResources().getString(R.string.price) + "(" + baseAsset + ")");
            list_amount.setText(getResources().getString(R.string.Amount1) + "(" + quoteAsset + ")");

            if (quoteAsset.equals("ETH") || quoteAsset.equals("BTC") || quoteAsset.equals("LTC")) {
                setListViewAdapter(Const.Asset.ASSET_SCALE_8);
            } else {
                setListViewAdapter(Const.Asset.ASSET_SCALE_5);
            }
        }

        if (isViewCreated) {
            tradeLeftView.setBuyFragment(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_latest_deal:
            case R.id.img_latest_deal:
                reChangeFragment2.anim_down(sv_container, rl_latest_deal);
                tv_latest_deal.setVisibility(View.GONE);
                img_latest_deal.setVisibility(View.GONE);
                break;
            case R.id.tv_latest_deal_1:
            case R.id.img_latest_deal_1:
                reChangeFragment2.anim_up(rl_latest_deal, sv_container);
                tv_latest_deal.setVisibility(View.VISIBLE);
                img_latest_deal.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BuyOrSellResponseModel.DataBean dataBean = (BuyOrSellResponseModel.DataBean) parent.getAdapter().getItem(position);
        BigDecimal bd_price = new BigDecimal(dataBean.getPrice()).setScale(NewHomeFragment.sScale[0], BigDecimal.ROUND_DOWN);
        BigDecimal bd_amount = new BigDecimal(dataBean.getAmount()).setScale(NewHomeFragment.sScale[1], BigDecimal.ROUND_DOWN);

        if (Double.parseDouble(dataBean.getPrice()) >= 10) {
            bd_price = bd_price.setScale(2, BigDecimal.ROUND_DOWN);
        } else {
            bd_price = bd_price.setScale(5, BigDecimal.ROUND_DOWN);
        }

        if (Double.parseDouble(dataBean.getPrice()) >= 1000) {
            bd_amount = bd_amount.setScale(5, BigDecimal.ROUND_DOWN);
        } else if (Double.parseDouble(dataBean.getPrice()) < 1000 && Double.parseDouble(dataBean.getPrice()) >= 1) {
            bd_amount = bd_amount.setScale(2, BigDecimal.ROUND_DOWN);
        } else {
            bd_amount = bd_amount.setScale(0, BigDecimal.ROUND_DOWN);
        }

        tradeLeftView.fillPrice(bd_price.toPlainString());
        tradeLeftView.fillAmount(bd_amount.toPlainString());
//        if (reChangeFragment2.getQuotePrecision() == 8) {
//            tradeLeftView.etd_amount.setText(bd_amount.toPlainString());
//        } else {
//            tradeLeftView.etd_amount.setText(bd_amount.toPlainString());
//        }

        //bd_price = bd_price.add(new BigDecimal("0.00001"));
        tradeLeftView.fillTotal(bd_price.multiply(bd_amount).setScale(NewHomeFragment.sScale[0] + NewHomeFragment.sScale[1], BigDecimal.ROUND_UP).toPlainString());
    }

    public void registerOnBuySuccessListener(OnBuySuccessListener listener) {
        onBuySuccessListener = listener;
    }

    public void unregisterOnBuySuccessListener() {
        if (null != onBuySuccessListener) {
            onBuySuccessListener = null;
        }
    }

    public OnBuySuccessListener getOnBuySuccessListener() {
        if (null != onBuySuccessListener) {
            return onBuySuccessListener;
        }

        return null;
    }

    public void registerOnVisibleListener(OnVisibleListener listener) {
        onVisibleListener = listener;
    }

    public void unregisterOnVisibleListener() {
        onVisibleListener = null;
    }

    public interface OnBuySuccessListener {
        void onBuySuccess();
    }

    public interface OnVisibleListener {
        void onVisible();
    }

    public class BuyOnBalanceChangeListener implements ReChangeFragment2.OnBalanceChangeListener {
        @Override
        public void onBalanceChange(String[] balances) {
            if (null != balances) {
                if (!TextUtils.isEmpty(balances[0])) {
                    tradeLeftView.fillBalance(balances[0]);
                    tradeLeftView.setBalance(Double.valueOf(balances[0]));
                    tradeLeftView.setQuotePrecision(reChangeFragment2.getQuotePrecision());
                    tradeLeftView.setBasePrecision(reChangeFragment2.getBasePrecision());
                } else {
                    tradeLeftView.fillBalance("0");
                }
            }
        }
    }

    public class BuyOnFeeChangeListener implements ReChangeFragment2.OnFeeChangeListener {
        @Override
        public void onFeeChange(double[] fee) {
            BigDecimal bd_fee = new BigDecimal(fee[0]).setScale(reChangeFragment2.getBasePrecision(), BigDecimal.ROUND_UP);
            tradeLeftView.fillFee(bd_fee.toPlainString());
            tradeLeftView.setFee(bd_fee);
        }
    }

    public class BuyOnLatestTradeChangeListener implements ReChangeFragment2.OnLatestTradeChangeListener {
        @Override
        public void onLatestTradeChange(List<AllHistoryResult> allHistoryResults) {
//            if (!isVisible) {
//                Log.e(TAG, "LatestTrade : visible is false!!!!!!");
//                return;
//            }

//            if (allHistoryResults != null) {
//                latestDealList.clear();
//                latestDealList.addAll(allHistoryResults);
//
//                adapterLatest.notifyDataSetChanged();
//
//                if (loading_latest != null) {
//                    loading_latest.setVisibility(View.GONE);
//                }
//            }
        }
    }

    public class BuyOnOrderChangeListener implements ReChangeFragment2.OnOrderChangeListener {
        @Override
        public void onOrderChange(List<List<BuyOrSellResponseModel.DataBean>> buyOrSellList, MarketTicker marketTicker, double bds_laster, List<AllHistoryResult> allHistoryResults, double sellAllCount, double buyAllCount) {
            if (!isVisible) {
                Log.e(TAG, "Order : visible is false!!!!!!");
                return;
            }

            if (null != buyOrSellList && buyOrSellList.size() > 0) {
                showSellList.clear();
                showBuyList.clear();

                adapterBuy.setCount(buyAllCount);
                adapterSell.setCount(sellAllCount);

//                synchronized (buyOrSellList) {
//                    showSellList.addAll(buyOrSellList.get(0));
//                    showBuyList.addAll(buyOrSellList.get(1));
//                }
                showSellList.addAll(buyOrSellList.get(0));
                showBuyList.addAll(buyOrSellList.get(1));
                adapterSell.notifyDataSetChanged();
                adapterBuy.notifyDataSetChanged();

                latestDealList.clear();
//                synchronized (allHistoryResults) {
//                    latestDealList.addAll(allHistoryResults);
//                }
                latestDealList.addAll(allHistoryResults);
                adapterLatest.notifyDataSetChanged();

                hideLoading();

                if (TextUtils.isEmpty(tradeLeftView.getCurrentPrice()) || Double.parseDouble(tradeLeftView.getCurrentPrice()) == 0) {
                    if (showSellList.size() > 0) {
                        tradeLeftView.fillPrice(showSellList.get(showSellList.size() - 1).getPrice());
                    } else {
                        tradeLeftView.fillPrice("0");
                    }
                }
            }

            if (marketTicker != null && marketTicker.latest >= 0) {
                if (marketTicker.base.equals("CNY") || marketTicker.base.equals("USD")) {
                    et_transaction_price.setText(String.format("%.5f", marketTicker.latest));

                } else {

                    String baseSymbol1;
                    if (LaunchActivity.lang.equals("en")) {
                        baseSymbol1 = "USD";
                    } else {
                        baseSymbol1 = "CNY";
                    }
                    et_transaction_price.setText(String.format("%.5f", marketTicker.latest) + "\n" + " ≈ " + CalculateUtils.mulScaleHALF_DOWN(String.format("%.5f", marketTicker.latest), String.format("%.5f", bds_laster), 5) + " " + baseSymbol1);

                }

            }
        }
    }
}
