package com.tang.trade.tang.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.app.Const;
import com.tang.trade.kchar.KLineActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.BaseViewPagerAdapter;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.model.AllHistory;
import com.tang.trade.tang.net.model.AllHistoryResult;
import com.tang.trade.tang.net.model.AssetResponseModel;
import com.tang.trade.tang.net.model.BuyOrSellResponseModel;
import com.tang.trade.tang.net.model.CoinList;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.utils;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.socket.market.Order;
import com.tang.trade.tang.socket.market.OrderBook;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.ui.exchangefragment.BuyFragment;
import com.tang.trade.tang.ui.exchangefragment.OrderFragment;
import com.tang.trade.tang.ui.exchangefragment.SellFragment;
import com.tang.trade.tang.ui.exchangefragment.TradeHistoryFragment;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.TimeUtils;
import com.tang.trade.tang.widget.TextViewUtils;
import com.tang.trade.utils.SPUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class ReChangeFragment2 extends Fragment implements TangApi.BaseViewCallback<AssetResponseModel>, View.OnClickListener{
    private static final String TAG = "ReChangeFragment2";

    public static final int BUY = 0;
    public static final int SELL = 1;
    private static final String STRSELL = "sell";
    private static final String STRBUY = "buy";
    public static final String TYPE = "type";
    public static final String BASE = "base";
    public static final String SELECT = "select";
    public static final String TRADE_TYPE = "tradeType";

    private View view;
    private BaseViewPagerAdapter mPagerAdapter;
    private ArrayList<Fragment> fragments = new ArrayList();
    private ArrayList<String> listType = new ArrayList();
    private String typeBase;
    private String typeSelect;

    private ImageView iv_kchar;
    private ViewPager vp_exchange;
    private TextView tv_principal_subordinate_currency;
    private TabLayout tablayout;

    private BuyFragment buyFragment = new BuyFragment();
    private SellFragment sellFragment = new SellFragment();
    private OrderFragment orderFragment = new OrderFragment();
    private TradeHistoryFragment tradeHistoryFragment = new TradeHistoryFragment();


    private double sellAllCount = 0;
    private double buyAllCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rechange2,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        listType.add(getString(R.string.bds_Buy));
        listType.add(getString(R.string.bds_Sell));
        listType.add(getString(R.string.bds_entrust_2));
        listType.add(getString(R.string.bds_history_2));

        for (int i = 0; i < listType.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putString(TYPE, listType.get(i));
            bundle.putString(BASE, typeBase);
            bundle.putString(SELECT, typeSelect);

            switch (i) {
                case 0:
                    bundle.putInt(TRADE_TYPE, BUY);
                    buyFragment.setArguments(bundle);
                    fragments.add(buyFragment);
                    break;
                case 1:
                    bundle.putInt(TRADE_TYPE, SELL);
                    sellFragment.setArguments(bundle);
                    fragments.add(sellFragment);
                    break;
                case 2:
                    orderFragment.setArguments(bundle);
                    fragments.add(orderFragment);
                    break;
                case 3:
                    tradeHistoryFragment.setArguments(bundle);
                    fragments.add(tradeHistoryFragment);
                    break;
                default:
                    break;
            }
        }

        mPagerAdapter = new BaseViewPagerAdapter(getChildFragmentManager(), fragments, listType);
        vp_exchange.setOffscreenPageLimit(1);
        vp_exchange.setAdapter(mPagerAdapter);

        if (mTradeType == BUY) {
            vp_exchange.setCurrentItem(0);
        } else if (mTradeType == SELL) {
            vp_exchange.setCurrentItem(1);
        }

        tablayout.setupWithViewPager(vp_exchange);
        if(tablayout.getTabCount()>4)
        {
            tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tablayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LinearLayout linearLayout = (LinearLayout) tablayout.getChildAt(0);
        linearLayout.setPadding(0,30,0,30);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        orderFragment.registerOnCancelSuccessListener(onCancelListener);
        buyFragment.registerOnBuySuccessListener(onBuySuccessListener);
        sellFragment.registerOnSellSuccessListener(onSellSuccessListener);
        buyFragment.registerOnVisibleListener(onVisibleListener);
        sellFragment.registerOnVisibleListener(onVisibleListener);
        orderFragment.registerOnVisibleListener(onVisibleListener);
        tradeHistoryFragment.registerOnVisibleListener(onVisibleListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        orderFragment.unregisterOnCancelSuccessListener();
        buyFragment.unregisterOnBuySuccessListener();
        sellFragment.unregisterOnSellSuccessListener();
        buyFragment.unregisterOnVisibleListener();
        sellFragment.unregisterOnVisibleListener();
        orderFragment.unregisterOnVisibleListener();
        tradeHistoryFragment.unregisterOnVisibleListener();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.e(TAG, "enter setUserVisibleHint!!!!!!!!");
        if (isVisibleToUser) {
            Log.e(TAG, "is show");
        } else {
            Log.e(TAG, "is not show");
        }
    }

    public void initView() {
        iv_kchar = view.findViewById(R.id.iv_kchar);
        vp_exchange = view.findViewById(R.id.vp_exchange);
        tv_principal_subordinate_currency = view.findViewById(R.id.tv_principal_subordinate_currency);
        tablayout = view.findViewById(R.id.tabLayout);
        iv_kchar.setOnClickListener(this);
        tv_principal_subordinate_currency.setOnClickListener(this);
        tablayout.setSelectedTabIndicatorHeight(3);

        getDefaultAcceptType();

        getMetrics();
    }

    private void getDefaultAcceptType() {
        if (TextUtils.isEmpty(typeBase) || TextUtils.isEmpty(typeSelect)) {
            if (HomePageFragment.coinList1 != null && HomePageFragment.coinList1.getData().size() > 0) {
                if (LaunchActivity.lang.equalsIgnoreCase("en")) {
                    for (int i = 0; i < HomePageFragment.coinList1.getData().size(); i++) {
                        if (HomePageFragment.coinList1.getData().get(i).getCoins().equalsIgnoreCase("USD")) {
                            typeBase = HomePageFragment.coinList1.getData().get(i).getCoins();
                            typeSelect = HomePageFragment.coinList1.getData().get(i).getDeputycoin();
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < HomePageFragment.coinList1.getData().size(); i++) {
                        if (HomePageFragment.coinList1.getData().get(i).getCoins().equalsIgnoreCase("CNY")) {
                            typeBase = HomePageFragment.coinList1.getData().get(i).getCoins();
                            typeSelect = HomePageFragment.coinList1.getData().get(i).getDeputycoin();
                            break;
                        }
                    }
                }
            } else {
                if (LaunchActivity.lang.equalsIgnoreCase("en")) {
                    typeBase = "USD";
                } else {
                    typeBase = "CNY";
                }
                typeSelect = "BDS";
            }
        }

        setTitle();
    }

    public void initChildVisible(int type) {
        Log.e(TAG, "from index to : startAsyncTask......");
        startAsyncTask();
        if (vp_exchange != null) {
            if (type == vp_exchange.getCurrentItem()) {
                if (BUY == type) {
                    buyFragment.setUserVisibleHint(true);
                }
                if (SELL == type) {
                    sellFragment.setUserVisibleHint(true);
                }

                buyFragment.clearTextValue();
                sellFragment.clearTextValue();
            }
        }
    }

    public void setChildVisible() {
        if (null == typeBase || null == typeSelect) {
            getDefaultAcceptType();
        }

        Log.e(TAG, "setChildVisible : startAsyncTask......");
        startAsyncTask();
        if (vp_exchange != null) {
            Log.e(TAG, vp_exchange.getCurrentItem() + " ： visible");
            switch (vp_exchange.getCurrentItem()) {
                case 0:
                    buyFragment.setUserVisibleHint(true);
                    break;
                case 1:
                    sellFragment.setUserVisibleHint(true);
                    break;
                case 2:
                    orderFragment.setUserVisibleHint(true);
                    break;
                case 3:
                    tradeHistoryFragment.setUserVisibleHint(true);
                    break;
                default:
                    break;
            }
        }
    }

    public void setChildHidden() {
        Log.e(TAG, "setChildHidden : stopAsyncTask......");
        stopAsyncTask();
        if (vp_exchange != null) {
            Log.e(TAG, vp_exchange.getCurrentItem() + " ：hidden");
            switch (vp_exchange.getCurrentItem()) {
                case 0:
                    buyFragment.setUserVisibleHint(false);
                    break;
                case 1:
                    sellFragment.setUserVisibleHint(false);
                    break;
                case 2:
                    orderFragment.setUserVisibleHint(false);
                    break;
                case 3:
                    tradeHistoryFragment.setUserVisibleHint(false);
                    break;
                default:
                    break;
            }
        }
    }

    private int mTradeType;
    public void setAcceptType(int type, String typeBase, String typeSelect) {
        this.mTradeType = type;
        this.typeBase = typeBase;
        this.typeSelect = typeSelect;

        if (TextUtils.isEmpty(typeBase)){
            this.typeBase = "CNY";
        }
        if (TextUtils.isEmpty(typeBase)){
            this.typeSelect = "BDS";
        }
        if (view != null) {
            buyFragment.updateAsset(typeBase, typeSelect);
            sellFragment.updateAsset(typeBase, typeSelect);
            orderFragment.updateAsset(typeBase, typeSelect);
            tradeHistoryFragment.updateAsset(typeBase, typeSelect);

            setTitle();

            if (mTradeType == BUY) {
                vp_exchange.setCurrentItem(0);
            } else if (mTradeType == SELL) {
                vp_exchange.setCurrentItem(1);
            }
        }
    }

    private void setTitle() {
        if (tv_principal_subordinate_currency != null) {
            TextViewUtils.setTextView1(tv_principal_subordinate_currency, getActivity(), typeBase, typeSelect);
        }
    }

    @Override
    public void setData(AssetResponseModel assetResponseModel) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_kchar:
                Intent intent = new Intent(this.getContext(), KLineActivity.class);
                intent.putExtra("baseSymbol",typeBase).putExtra("qouteSymbol",typeSelect);
                startActivity(intent);
                break;
            case R.id.tv_principal_subordinate_currency:
                stopAsyncTask();
                Log.e(TAG, "back to index : stopAsyncTask......");

                HomePageFragment homeFragment=  MainActivity.mainActivity.getHomeFragment();
                MainActivity.mainActivity.addFragment(R.id.container, homeFragment);
                MainActivity.mainActivity.switchSelectState(MainActivity.home);
                break;
            default:
                break;
        }
    }

    public void anim_up(View viewToShow, View viewToHide) {
        TranslateAnimation anim_up_hide = new TranslateAnimation (0,0,0,-value[1]);
        TranslateAnimation anim_up_show = new TranslateAnimation (0,0,value[1],0);
        AnimationSet anim_set_hide = new AnimationSet(true);
        AnimationSet anim_set_show = new AnimationSet(true);
        anim_set_hide.addAnimation(anim_up_hide);
        anim_set_show.addAnimation(anim_up_show);

        anim_up_hide.setDuration(500);
        anim_up_hide.setFillAfter(false);
        viewToHide.startAnimation(anim_set_hide);
        viewToHide.setVisibility(View.GONE);

        anim_up_show.setDuration(500);
        anim_up_show.setFillAfter(true);
        viewToShow.startAnimation(anim_set_show);
        viewToShow.setVisibility(View.VISIBLE);
    }

    public void anim_down(View viewToShow, View viewToHide) {
        TranslateAnimation anim_up_hide = new TranslateAnimation (0,0,0,value[1]);
        TranslateAnimation anim_up_show = new TranslateAnimation (0,0,-value[1],0);
        AnimationSet anim_set_hide = new AnimationSet(true);
        AnimationSet anim_set_show = new AnimationSet(true);
        anim_set_hide.addAnimation(anim_up_hide);
        anim_set_show.addAnimation(anim_up_show);

        anim_up_hide.setDuration(500);
        anim_up_hide.setFillAfter(false);
        viewToHide.startAnimation(anim_set_hide);
        //viewToHide.setVisibility(View.GONE);

        anim_up_show.setDuration(500);
        anim_up_show.setFillAfter(true);
        viewToShow.startAnimation(anim_set_show);
        viewToShow.setVisibility(View.VISIBLE);
    }

    private int[] value = new int[2];
    private void getMetrics() {
        WindowManager wm = (WindowManager) MyApp.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        value[0] = dm.widthPixels;
        value[1] = dm.heightPixels;
    }

    /**
     * 以下方法都是为实现该机制代码
     * 父类中执行异步任务，提供回调接口给子Fragment，根据需求进行回调。
     */

    private void startAsyncTask() {
        stopAsyncTask();

        startTimer();

        mGetBalancesTask = new GetBalancesTask();
        mGetFeeTask = new GetFeeTask();
        //mGetAllHistoryTask = new GetAllHistoryTask();

        mGetBalancesTask.execute(typeBase, typeSelect, SPUtils.getString(Const.USERNAME,""));
        mGetFeeTask.execute();
//        mGetBalancesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, typeBase, typeSelect, SPUtils.getString(Const.USERNAME,""));
//        mGetFeeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //mGetAllHistoryTask.execute();
    }

    private void stopAsyncTask() {
        stopTimer();

        if (mGetBalancesTask != null) {
            mGetBalancesTask.cancel(true);
            mGetBalancesTask = null;
        }
        if (mGetFeeTask != null) {
            mGetFeeTask.cancel(true);
            mGetFeeTask = null;
        }
//        if (mGetAllHistoryTask != null) {
//            mGetAllHistoryTask.cancel(true);
//            mGetAllHistoryTask = null;
//        }
    }

    private Timer timer = null;
    private TimerTask timerTask = null;
//    private List<List<BuyOrSellResponseModel.DataBean>> buyOrSellList = Collections.synchronizedList(new ArrayList<List<BuyOrSellResponseModel.DataBean>>());
//    private MarketTicker marketTicker;
//    private List<AllHistoryResult> allHistoryResults = Collections.synchronizedList(new ArrayList<AllHistoryResult>());
    public void startTimer() {
        stopTimer();

        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    final List<List<BuyOrSellResponseModel.DataBean>> buyOrSellList = getData();
                    final MarketTicker marketTicker = getMarketTicker(typeBase, typeSelect);
                    String baseSymbol1;
                    if (LaunchActivity.lang.equals("en")){
                        baseSymbol1 = "USD";
                    }else {
                        baseSymbol1 = "CNY";
                    }
                    double bds_laster=0.00000;
                    if (typeBase.equals("USD")||typeBase.equals("CNY")){

                    }else if (null==marketTicker||marketTicker.latest==0){
//                        bds_laster=0.00000;
                    }else {
                        final MarketTicker marketTicker2 = getMarketTicker(baseSymbol1, "BDS");
                        if (marketTicker2!=null){
                            bds_laster=marketTicker2.latest;
                        }
                    }
                    final double bds_laster1=bds_laster;
                    final List<AllHistoryResult> allHistoryResults = getAllHistory(typeBase, typeSelect, HISTORY_LIMIT);

                    if (null != buyOrSellList && null != marketTicker && buyOrSellList.size() > 0 && null != allHistoryResults) {
                        mRefreshHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (OnOrderChangeListener listener : orderListener) {
                                    if (null != listener) {
                                        listener.onOrderChange(buyOrSellList, marketTicker, bds_laster1,allHistoryResults, sellAllCount, buyAllCount);
                                    }
                                }
                            }
                        });
                    }
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(timerTask, 200, 3000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private GetBalancesTask mGetBalancesTask;
    private GetFeeTask mGetFeeTask;
//    private GetAllHistoryTask mGetAllHistoryTask;
    private static final int HISTORY_LIMIT = 300;
    public Handler mRefreshHandler = new Handler();

//    Runnable refreshUIRunnable = new Runnable() {
//        @Override
//        public void run() {
//            for (OnOrderChangeListener listener : orderListener) {
//                if (null != listener) {
//                    listener.onOrderChange(buyOrSellList, marketTicker, allHistoryResults, sellAllCount, buyAllCount);
//                }
//            }
//        }
//    };

    public class GetBalancesTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (isCancelled()) {
                return new String[2];
            }
            return getBalance(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(String[] balances) {
            super.onPostExecute(balances);

            if (isCancelled()) {
                return;
            }

            if (TextUtils.isEmpty(balances[0])) {
                balances[0] = "0";
            }

            if (TextUtils.isEmpty(balances[1])) {
                balances[1] = "0";
            }

            for (OnBalanceChangeListener listener : balanceListener) {
                if (null != listener) {
                    listener.onBalanceChange(balances);
                }
            }
        }
    }

    public class GetFeeTask extends AsyncTask<Void, Void, double[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected double[] doInBackground(Void... params) {
            if (isCancelled()) {
                return new double[2];
            }
            return getFee();
        }

        @Override
        protected void onPostExecute(double[] fee) {
            super.onPostExecute(fee);

            Log.e(TAG, " the fee is : " + fee);

            if (isCancelled()) {
                return;
            }

            if (fee[0] == 0 || fee[1] == 0) {
                MyApp.showToast(getString(R.string.network));
                return;
            }

            for (OnFeeChangeListener listener : feeListener) {
                if (null != listener) {
                    listener.onFeeChange(fee);
                }
            }
        }
    }

//    public class GetAllHistoryTask extends AsyncTask<Void, Object, List<AllHistoryResult>> {
//
//        @Override
//        protected void onPreExecute() {}
//
//        @Override
//        protected List<AllHistoryResult> doInBackground(Void... params) {
//            try {
//                return getAllHistory(typeBase, typeSelect, HISTORY_LIMIT);
//            } catch (NetworkStatusException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(List<AllHistoryResult> allHistoryResults) {
//            super.onPostExecute(allHistoryResults);
//
//            for (OnLatestTradeChangeListener listener : latestTradeListener) {
//                if (null != listener) {
//                    listener.onLatestTradeChange(allHistoryResults);
//                }
//            }
//        }
//    }

    /**
     *
     * @param baseSymbol 基础货币
     * @param quoteSymbol 从币
     * @param nLimit 条数
     * @return
     */
    private List<AllHistoryResult> getAllHistory(String baseSymbol, String quoteSymbol, int nLimit) throws NetworkStatusException {
        List<AllHistoryResult> datas = new ArrayList<>();
        AllHistory allHistory = null;
        asset_object baseAsset = null;
        asset_object quoteAsset = null;

        try {
            baseAsset = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseSymbol);
            if (baseAsset == null || baseAsset.id == null || TextUtils.isEmpty(baseAsset.id.toString())) {
                return null;
            }

            quoteAsset = BitsharesWalletWraper.getInstance().lookup_asset_symbols(quoteSymbol);
            if (quoteAsset == null || quoteAsset.id == null  || TextUtils.isEmpty(quoteAsset.id.toString())) {
                return null;
            }

            allHistory = BitsharesWalletWraper.getInstance().get_all_history(baseAsset.id.toString(), quoteAsset.id.toString(), nLimit);//("1.3.3","1.3.0",300);

        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (allHistory != null && allHistory.getResult() != null && allHistory.getResult().size() > 0){
            for (int i = 0 ; i < allHistory.getResult().size() ; i ++){
                if (i % 2 != 0){
                    String date = TimeUtils.utc2Local(allHistory.getResult().get(i).getTime().replace("T"," "));
                    String time = date.replace(" ","\n");
                    String strAmount;
                    String strValue;
                    String strPrice;
                    //buy
                    if (allHistory.getResult().get(i).getOp().getPays().getAsset_id().equals(baseAsset.id.toString())){
                        strAmount = baseAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getPays().getAmount()).count;
                        strValue =  quoteAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getReceives().getAmount()).count;
                        strPrice =  CalculateUtils.div(Double.parseDouble(strAmount),Double.parseDouble(strValue),5);
                        if (!TextUtils.isEmpty(strPrice)&&Double.parseDouble(strPrice)!=0){
                            datas.add(new AllHistoryResult(time,strPrice,STRBUY,strAmount,strValue));
                        }

                    } else {//sell
                        strAmount = quoteAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getPays().getAmount()).count;
                        strValue =  baseAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getReceives().getAmount()).count;
                        strPrice =  CalculateUtils.div(Double.parseDouble(strValue),Double.parseDouble(strAmount),5);
                        if (!TextUtils.isEmpty(strPrice)&&Double.parseDouble(strPrice)!=0){
                            datas.add(new AllHistoryResult(time,strPrice,STRSELL,strValue,strAmount));
                        }

                    }
                }
            }
        }

        return datas;
    }

    private String[] getBalance(final String baseAsset, final String quoteAsset, final String accountId){
        String [] balances = new String [2];

        try {
            if (!SPUtils.getString(Const.USERNAME,"").equalsIgnoreCase("")) {

                //获取所有资产列表
                account_object object = BitsharesWalletWraper.getInstance().get_account_object(accountId);
                if (object == null) {
                    return balances;
                }

                //查询账户id
                object_id<account_object> loginAccountId = object.id;

                //获取账户余额列表
                List<asset> accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                if (accountAsset == null) {
                    return balances;
                }

                //查询资产列表
                List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                if (objAssets == null) {
                    return balances;
                }

                //查询基础货币BDS,和所有智能资产
                for (asset_object objAsset : objAssets){
                    if (accountAsset.size() > 0) {
                        if (objAsset.symbol.equalsIgnoreCase(baseAsset)) {
                            for (int j = 0; j < accountAsset.size(); j++) {
                                if (accountAsset.get(j).asset_id.toString().equals(objAsset.id.toString())) {
                                    balances[0] = objAsset.get_legible_asset_object(accountAsset.get(j).amount).count;
                                    break;
                                }
                            }
                        }

                        if (objAsset.symbol.equalsIgnoreCase(quoteAsset)) {
                            for (int j = 0; j < accountAsset.size(); j++) {
                                if (accountAsset.get(j).asset_id.toString().equals(objAsset.id.toString())) {
                                    balances[1] = objAsset.get_legible_asset_object(accountAsset.get(j).amount).count;
                                    break;
                                }
                            }
                        }

                        if (!TextUtils.isEmpty(balances[0]) && !TextUtils.isEmpty(balances[1])) {
                            return balances;
                        }
                    }
                }
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        return balances;
    }

    private asset_object baseAssetObj;
    private asset_object quoteAssetObj;
    private global_property_object globalPropertyObject;
    private int mPrecision_base = 5;
    private int mPrecision_quote = 5;

    public int getBasePrecision() {
        return mPrecision_base;
    }

    public int getQuotePrecision() {
        if (typeSelect.equals("BTC") || typeSelect.equals("LTC") || typeSelect.equals("ETH")) {
            mPrecision_quote = 8;
        } else {
            mPrecision_quote = 5;
        }
        return mPrecision_quote;
    }

    private double[] getFee() {
        double[] dFees = new double[2];
        String sRateBuy;
        String sRateSell;

        if (globalPropertyObject == null) {
            try {
                globalPropertyObject = BitsharesWalletWraper.getInstance().get_global_properties();
            } catch (NetworkStatusException e) {
                e.printStackTrace();
                return dFees;
            }
        }

        try {
            baseAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(typeBase);
            if (baseAssetObj == null) {
                return dFees;
            }
            mPrecision_base = baseAssetObj.precision;
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return dFees;
        }

        try {
            if (typeBase.equals("CNY") && !typeSelect.equals("USD"))  {
                quoteAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols("USD");
            } else if (typeBase.equals("USD") && !typeSelect.equals("CNY")) {
                quoteAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols("CNY");
            } else {
                quoteAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(typeSelect);
            }

            if (quoteAssetObj == null) {
                return dFees;
            }

            if (typeSelect.equals("BTC") || typeSelect.equals("LTC") || typeSelect.equals("ETH")) {
                mPrecision_quote = 8;
            } else {
                mPrecision_quote = quoteAssetObj.precision;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return dFees;
        }

        if (globalPropertyObject == null || baseAssetObj == null || quoteAssetObj == null) {
            return dFees;
        }

        try {
            sRateBuy = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(typeBase);
            sRateSell = BitsharesWalletWraper.getInstance().lookup_asset_symbols_rate(typeSelect);

            if (TextUtils.isEmpty(sRateBuy) || TextUtils.isEmpty(sRateSell)) {
                return dFees;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return dFees;
        }

        dFees[0] = Double.parseDouble(CalculateUtils.mul(calculateBuyFee(baseAssetObj, quoteAssetObj, 1, 1), Double.parseDouble(sRateBuy)));
        dFees[1] = Double.parseDouble(CalculateUtils.mul(calculateSellFee(baseAssetObj, quoteAssetObj, 1, 1), Double.parseDouble(sRateSell)));

        return dFees;
    }

    private double calculateBuyFee(asset_object symbolToReceive, asset_object symbolToSell, double rate, double amount) {
        asset buy_fee = null;

        if (symbolToReceive != null && symbolToSell != null) {
            buy_fee = BitsharesWalletWraper.getInstance().calculate_buy_fee(symbolToReceive, symbolToSell, rate, amount, globalPropertyObject);
        }

        if (buy_fee == null || buy_fee.asset_id == null || baseAssetObj.id == null) {
            return 0;
        }

        if (buy_fee.asset_id.equals(baseAssetObj.id)) {
            return utils.get_asset_amount(buy_fee.amount, baseAssetObj);
        } else {
            return utils.get_asset_amount(buy_fee.amount, quoteAssetObj);
        }
    }

    private double calculateSellFee(asset_object symbolToSell, asset_object symbolToReceive, double rate, double amount) {
        asset sell_fee = null;

        if (symbolToReceive != null && symbolToSell != null) {
            sell_fee = BitsharesWalletWraper.getInstance().calculate_sell_fee(symbolToSell, symbolToReceive, rate, amount, globalPropertyObject);
        }

        if (sell_fee == null || sell_fee.asset_id == null || baseAssetObj.id == null) {
            return 0;
        }

        if (sell_fee.asset_id.equals(baseAssetObj.id)) {
            return utils.get_asset_amount(sell_fee.amount, baseAssetObj);
        } else {
            return utils.get_asset_amount(sell_fee.amount, quoteAssetObj);
        }
    }

    private List<List<BuyOrSellResponseModel.DataBean>> getData() throws NetworkStatusException {
        List<List<BuyOrSellResponseModel.DataBean>> arrayList = new ArrayList<>();
        List<BuyOrSellResponseModel.DataBean> sellList = new ArrayList<>();
        List<BuyOrSellResponseModel.DataBean> buyList = new ArrayList<>();

        arrayList.clear();
        sellList.clear();
        buyList.clear();

        //获取买入，卖出交易数据
        OrderBook orderBook = BitsharesWalletWraper.getInstance().get_order_book(typeBase, typeSelect, 50);
        if (orderBook != null) {
            Order tmpOrder;
            Order nextOrder;

            sellAllCount = 0;
            buyAllCount = 0;
            if (orderBook.asks != null && orderBook.asks.size() > 0) {
                for (int i = 0; i < orderBook.asks.size(); i++) {
                    sellAllCount += orderBook.asks.get(i).quote;
                }
            }

            if (orderBook.bids != null && orderBook.bids.size() > 0) {
                for (int i = 0; i < orderBook.bids.size(); i++) {
                    buyAllCount += orderBook.bids.get(i).quote;
                }
            }

            if (orderBook.asks != null && orderBook.asks.size() > 0) {
                BuyOrSellResponseModel.DataBean dataBean;

                for (int i = 0; i < orderBook.asks.size(); i++) {
                    dataBean = new BuyOrSellResponseModel.DataBean();

                    String price;
                    String priceNext;

                    tmpOrder = orderBook.asks.get(i);
                    price = new BigDecimal(tmpOrder.price).setScale(mPrecision_base, BigDecimal.ROUND_HALF_UP).toPlainString();

                    int k = i+1;
                    if (k < orderBook.asks.size()) {
                        for (; k < orderBook.asks.size(); k++) {
                            nextOrder = orderBook.asks.get(k);
                            priceNext = new BigDecimal(nextOrder.price).setScale(mPrecision_base, BigDecimal.ROUND_HALF_UP).toPlainString();

                            if (price.equals(priceNext)) {
                                tmpOrder.quote = CalculateUtils.add(tmpOrder.quote,nextOrder.quote);
                                orderBook.asks.remove(k);
                                k--;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(price)&&Double.parseDouble(price)!=0){
                        dataBean.setAmount(tmpOrder.quote + "");
                        double dGmv = tmpOrder.quote * tmpOrder.price;
                        dataBean.setGmv(dGmv +"");
                        dataBean.setPrice(price);

                        sellList.add(dataBean);
                    }

                    if (sellList.size()==8) {
                        break;
                    }
                }
            }

            Collections.sort(sellList, new Comparator<BuyOrSellResponseModel.DataBean>() {
                @Override
                public int compare(BuyOrSellResponseModel.DataBean o1, BuyOrSellResponseModel.DataBean o2) {
                    Double id1 = 0.0;
                    Double id2 = 0.0;

                    if (!TextUtils.isEmpty(o1.getPrice())) {
                        id1 = Double.parseDouble(o1.getPrice());
                    }

                    if (!TextUtils.isEmpty(o2.getPrice())) {
                        id2 = Double.parseDouble(o2.getPrice());
                    }

                    return (id2.compareTo(id1));
                }
            });

            for (int i = sellList.size(); i > 0; i--) {
                if (isAdded()){
                    if (sellList.size() > 0) {
                        sellList.get(i-1).setType(getString(R.string.bds_sell) + (sellList.size()-(i-1)));
                    }
                }
            }

            arrayList.add(sellList);

            if (orderBook.bids != null && orderBook.bids.size() > 0) {
                BuyOrSellResponseModel.DataBean dataBean;

                for (int i = 0; i < orderBook.bids.size(); i++) {
                    dataBean = new BuyOrSellResponseModel.DataBean();

                    String price;
                    String priceNext;

                    tmpOrder = orderBook.bids.get(i);
                    price = new BigDecimal(tmpOrder.price).setScale(mPrecision_base, BigDecimal.ROUND_HALF_UP).toPlainString();

                    if (i+1<orderBook.bids.size()){
                        int k = i+1;
                        for (; k < orderBook.bids.size(); k++) {
                            nextOrder = orderBook.bids.get(k);
                            priceNext = new BigDecimal(nextOrder.price).setScale(mPrecision_base, BigDecimal.ROUND_HALF_UP).toPlainString();

                            if (price.equals(priceNext)) {
                                tmpOrder.quote = CalculateUtils.add(tmpOrder.quote,nextOrder.quote);
                                orderBook.bids.remove(k);
                                k--;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(price)&&Double.parseDouble(price)!=0) {
                        dataBean.setAmount(tmpOrder.quote + "");
                        double dGmv = tmpOrder.quote * tmpOrder.price;
                        dataBean.setGmv(dGmv + "");
                        dataBean.setPrice(price);
                        buyList.add(dataBean);
                    }
                    if (buyList.size()==8) {
                        break;
                    }
                }
            }

            for (int i = 0; i < buyList.size(); i++) {
                if (isAdded()) {
                    buyList.get(i).setType(getString(R.string.bds_buy) + (i+1));
                }
            }

            arrayList.add(buyList);
        }

        return arrayList;
    }

    private MarketTicker getMarketTicker(String baseSymbol, String selectSymbol) {
        try {
            return BitsharesWalletWraper.getInstance().get_ticker(baseSymbol, selectSymbol);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        return null;
    }


//    public static final String FEE = "fee";
//    public static final String ORDER = "order";
//    public static final String BALANCE = "balance";
//    public static final String LATESTTRADE = "latestTrade";
//    public static final String LATESTPRICE = "latestPrice";
//    private ConcurrentHashMap listenerMap = new ConcurrentHashMap();

    private Vector<OnFeeChangeListener> feeListener = new Vector<>();
    private Vector<OnOrderChangeListener> orderListener = new Vector<>();
    private Vector<OnBalanceChangeListener> balanceListener = new Vector<>();
    private Vector<OnLatestPriceChangeListener> latestPriceListener = new Vector<>();
    private Vector<OnLatestTradeChangeListener> latestTradeListener = new Vector<>();

    public interface OnBalanceChangeListener {
        void onBalanceChange(String[] balances);
    }

    public interface OnFeeChangeListener {
        void onFeeChange(double[] fee);
    }

    public interface OnOrderChangeListener {
        void onOrderChange(List<List<BuyOrSellResponseModel.DataBean>> buyOrSellList, MarketTicker marketTicker,double bds_laster,List<AllHistoryResult> allHistoryResults, double sellAllCount, double buyAllCount);
    }

    public interface OnLatestPriceChangeListener {
        void onLatestPriceChange();
    }

    public interface OnLatestTradeChangeListener {
        void onLatestTradeChange(List<AllHistoryResult> allHistoryResult);
    }

    public void registerOnFeeChangeListener(OnFeeChangeListener listener) {
        feeListener.add(listener);
    }

    public void unregisterOnFeeChangeListener(OnFeeChangeListener listener) {
        feeListener.remove(listener);
    }

    public void registerOrderChangeListener(OnOrderChangeListener listener) {
        orderListener.add(listener);
    }

    public void unregisterOrderChangeListener(OnOrderChangeListener listener) {
        orderListener.remove(listener);
    }

    public void registerOnBalanceChangeListener(OnBalanceChangeListener listener) {
        balanceListener.add(listener);
    }

    public void unregisterOnBalanceChangeListener(OnBalanceChangeListener listener) {
        balanceListener.remove(listener);
    }

    public void registerLatestPriceChangeListener(OnLatestPriceChangeListener listener) {
        latestPriceListener.add(listener);
    }

    public void unregisterLatestPriceListener(OnLatestPriceChangeListener listener) {
        latestPriceListener.remove(listener);
    }

    public void registerLatestTradeListener(OnLatestTradeChangeListener listener) {
        latestTradeListener.add(listener);
    }

    public void unregisterLatestTradeListener(OnLatestTradeChangeListener listener) {
        latestTradeListener.remove(listener);
    }

    private OnCancelListener onCancelListener = new OnCancelListener();
    public class OnCancelListener implements OrderFragment.OnCancelListener{
        @Override
        public void onCancelSuccess() {
            new GetBalancesTask().execute(typeBase, typeSelect, SPUtils.getString(Const.USERNAME,""));
        }
    }

    private OnBuySuccessListener onBuySuccessListener = new OnBuySuccessListener();
    public class OnBuySuccessListener implements BuyFragment.OnBuySuccessListener {
        @Override
        public void onBuySuccess() {
            new GetBalancesTask().execute(typeBase, typeSelect, SPUtils.getString(Const.USERNAME,""));
            //new GetAllHistoryTask().execute();
        }
    }

    private OnSellSuccessListener onSellSuccessListener = new OnSellSuccessListener();
    public class OnSellSuccessListener implements SellFragment.OnSellSuccessListener {
        @Override
        public void onSellSuccess() {
            new GetBalancesTask().execute(typeBase, typeSelect, SPUtils.getString(Const.USERNAME,""));
            //new GetAllHistoryTask().execute();
        }
    }

    private OnVisibleListener onVisibleListener = new OnVisibleListener();
    public class OnVisibleListener implements BuyFragment.OnVisibleListener, SellFragment.OnVisibleListener, OrderFragment.OnVisibleListener, TradeHistoryFragment.OnVisibleListener {
        @Override
        public void onVisible() {
            if (vp_exchange == null) {
                return;
            }

            if (vp_exchange.getCurrentItem() == 0 || vp_exchange.getCurrentItem() == 1) {
                if (null != timer) {
                    stopTimer();
                    Log.e(TAG, vp_exchange.getCurrentItem() + " : is visible and timer is not null stop timer!!!!!!");
                }

                startTimer();
                Log.e(TAG, vp_exchange.getCurrentItem() + " : is visible and timer is null start timer!!!!!!");
            } else {
                if (null != timer) {
                    stopTimer();
                    Log.e(TAG, vp_exchange.getCurrentItem() + " : is visible and timer is not null stop timer!!!!!!");
                } else {
                    Log.e(TAG, vp_exchange.getCurrentItem() + " : is visible and timer is null do nothing!!!!!!");
                }
            }
        }
    }

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SAVE_IS_HIDDEN, true);
    }

}
