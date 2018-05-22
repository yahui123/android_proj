package com.tang.trade.tang.ui.exchangefragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tang.trade.app.Const;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.OrdersAdapter;
import com.tang.trade.tang.net.model.OrdersResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.FullAccountOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.chain.utils;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.tang.widget.TextViewUtils;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.LimitOrder;
import de.bitsharesmunich.graphenej.Price;

public class OrderFragment extends Fragment implements View.OnClickListener{
    public static final String TAG ="OrderFragment";
    public static final String SELL = "sell";
    public static final String BUY = "buy";

    private View view;
    private Unbinder unbinder;

    private OrdersAdapter adapter;

    private DownTask downTask;

    @BindView(R.id.lv_order)
    ListView lv_order;

    @BindView(R.id.quote)
    TextView title_quote;

    @BindView(R.id.base)
    TextView title_base;

    @BindView(R.id.loading)
    LoadingView loadingView;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;

    @BindView(R.id.btn_buy)
    Button btn_buy;

    @BindView(R.id.btn_sell)
    Button btn_sell;

    @BindView(R.id.ll_title)
    LinearLayout ll_title;

    private String typeBase,typeSelect;
    private String sLoginAccount;

    private boolean isViewCreated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_order,container,false);
        }

        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();

        isViewCreated = true;
        sLoginAccount = SPUtils.getString(Const.USERNAME,"");

        if (isVisible) {
            downTask = new DownTask();
            downTask.execute();
//            downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        adapter = new OrdersAdapter(new ArrayList<OrdersResponseModel.DataBean>(), getActivity());
        lv_order.setAdapter(adapter);
        adapter.setOrderFragment(this);
    }

    private void initData(){
        if (typeBase == null && typeSelect == null) {
            if (LaunchActivity.lang.equalsIgnoreCase("en")) {
                typeBase = getArguments().getString(ReChangeFragment2.BASE,"USD");
            } else {
                typeBase = getArguments().getString(ReChangeFragment2.BASE,"CNY");
            }
            typeSelect = getArguments().getString(ReChangeFragment2.SELECT,"BDS");
        }

        TextViewUtils.setTextView(title_quote, getContext(), typeSelect);
        TextViewUtils.setTextView(title_base, getContext(), typeBase);

        btn_buy.setOnClickListener(this);
        btn_sell.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (view != null) {
            // ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private boolean isVisible = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser == true) {
            isVisible = true;
            if (isViewCreated) {
                if (null != onVisibleListener) {
                    onVisibleListener.onVisible();
                }

                downTask = new DownTask();
                downTask.execute();
//                downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            isVisible = false;
            if (null != downTask && downTask.getStatus() == AsyncTask.Status.RUNNING) {
                downTask.cancel(true);
                downTask = null;
            }
        }
    }

    public void updateAsset(String baseAsset, String quoteAsset) {
        this.typeBase = baseAsset;
        this.typeSelect = quoteAsset;

        if (title_base != null && title_quote != null) {
            TextViewUtils.setTextView(title_quote, getContext(), typeSelect);
            TextViewUtils.setTextView(title_base, getContext(), typeBase);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_buy:
                orderType = BUY;
                btn_buy.setTextColor(getResources().getColor(R.color.common_white));
                btn_buy.setBackground(getResources().getDrawable(R.drawable.rechange_order_btn_selected));
                btn_sell.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                btn_sell.setBackground(getResources().getDrawable(R.drawable.rechange_order_btn_normal));
                downTask = new DownTask();
                downTask.execute();
//                downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.btn_sell:
                orderType = SELL;
                btn_buy.setTextColor(getResources().getColor(R.color.rechange_switch_text_selected));
                btn_buy.setBackground(getResources().getDrawable(R.drawable.rechange_order_btn_normal));
                btn_sell.setTextColor(getResources().getColor(R.color.common_white));
                btn_sell.setBackground(getResources().getDrawable(R.drawable.rechange_order_btn_selected));
                downTask = new DownTask();
                downTask.execute();
//                downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    public class DownTask extends AsyncTask<Void, Void, ArrayList<OrdersResponseModel.DataBean>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingView.setVisibility(View.VISIBLE);
            ll_nodata.setVisibility(View.INVISIBLE);
            ll_title.setVisibility(View.INVISIBLE);
            lv_order.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ArrayList<OrdersResponseModel.DataBean> doInBackground(Void... params) {
            return getOrderDatas(typeBase,typeSelect);
        }

        @Override
        protected void onPostExecute(ArrayList<OrdersResponseModel.DataBean> dataBeen) {
            super.onPostExecute(dataBeen);

            loadingView.setVisibility(View.INVISIBLE);

            if (dataBeen == null || dataBeen.size() == 0) {
                ll_nodata.setVisibility(View.VISIBLE);
                ll_title.setVisibility(View.INVISIBLE);
                lv_order.setVisibility(View.INVISIBLE);
                return;
            }

            if (dataBeen.size() > 0) {
                ArrayList<OrdersResponseModel.DataBean> arrayList = new ArrayList<>();
                arrayList.clear();
                arrayList.addAll(dataBeen);

                ll_nodata.setVisibility(View.INVISIBLE);
                ll_title.setVisibility(View.VISIBLE);
                lv_order.setVisibility(View.VISIBLE);

                adapter.setList(arrayList);
                adapter.setType(orderType);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private String orderType = BUY;
    private ArrayList<OrdersResponseModel.DataBean> getOrderDatas(String baseSymbol, String quoteSymbol) {
        ArrayList<OrdersResponseModel.DataBean> arrayList = new ArrayList<>();
        arrayList.clear();
        asset_object baseAssetObj = new asset_object();
        asset_object quoteAssetObj = new asset_object();

        if (null == sLoginAccount || sLoginAccount.isEmpty()) {
            return null;
        }

        try {
            baseAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseSymbol);
            if (baseAssetObj == null) {
                return null;
            }

            quoteAssetObj = BitsharesWalletWraper.getInstance().lookup_asset_symbols(quoteSymbol);
            if (quoteAssetObj == null) {
                return null;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        List<FullAccountOrder> openOrders = new ArrayList<>();
        try {
            full_account a = BitsharesWalletWraper.getInstance().get_full_account(sLoginAccount, false);
            if (null == a || null == a.limit_orders) {
                return null;
            }

            for (int j = 0; j < a.limit_orders.size(); j++) {
                LimitOrder o = a.limit_orders.get(j);
                FullAccountOrder order = new FullAccountOrder();
                order.limitOrder = o;
                order.base = baseAssetObj;
                order.quote = quoteAssetObj;
                order.price = priceToReal(o.getSellPrice(), baseAssetObj, quoteAssetObj);
                openOrders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (openOrders.size() == 0) {
            return null;
        }

        for (int i=0; i < openOrders.size(); i++) {
            FullAccountOrder open_order = openOrders.get(i);
            if (null == open_order) {
                continue;
            }

            if (open_order.base.id == null || open_order.quote.id == null) {
                return null;
            }

            String quoteAssetId = open_order.limitOrder.getSellPrice().quote.getAsset().getObjectId();
            String baseAssetId = open_order.limitOrder.getSellPrice().base.getAsset().getObjectId();
            String sourceQuoteAssetId = open_order.quote.id.toString();
            String sourceBaseAssetId = open_order.base.id.toString();

            if (orderType.equals(BUY)) {
                if (quoteAssetId.equals(sourceQuoteAssetId) && baseAssetId.equals(sourceBaseAssetId)) {
                    String sSellQuotePrice = open_order.limitOrder.getSellPrice().quote.getAmount() + "";
                    long lSellPrice = Long.valueOf(sSellQuotePrice).longValue();
                    double buyAmount = utils.get_asset_amount(lSellPrice, open_order.quote);
                    String sBuyAmount = CalculateUtils.div(buyAmount,1,open_order.quote.precision);

                    String sSellBasePrice = open_order.limitOrder.getSellPrice().base.getAmount() + "";
                    long lSellBasePrice =  Long.valueOf(sSellBasePrice).longValue();
                    double sellAmount = utils.get_asset_amount(lSellBasePrice, open_order.base);
                    String sSellAmount = CalculateUtils.div(sellAmount,1,open_order.base.precision);
                    double nPrice = sellAmount/buyAmount;
                    String sPrice =  String.format("%.5f %s/%s", nPrice,open_order.base.symbol,open_order.quote.symbol );
                    String sExpirationDate = open_order.limitOrder.getExpiration();
                    String sSourceSymbol = open_order.base.symbol;
                    String sDestSymbol = open_order.quote.symbol;
                    arrayList.add(new OrdersResponseModel.DataBean(sPrice,sBuyAmount,sSellAmount,sExpirationDate,open_order.limitOrder,""));
                }
            } else if (orderType.equals(SELL)) {
                if (baseAssetId.equals(sourceQuoteAssetId) && quoteAssetId.equalsIgnoreCase(sourceBaseAssetId) ) {
                    String sBuyQuoteAmount = open_order.limitOrder.getSellPrice().quote.getAmount() + "";
                    long   lBuyQuoteAmount = Long.valueOf(sBuyQuoteAmount).longValue();
                    double buyAmount = utils.get_asset_amount(lBuyQuoteAmount, open_order.base);
                    String sBuyAmount = CalculateUtils.div(buyAmount,1,open_order.base.precision);
                    String sSellBaseAmount  = open_order.limitOrder.getSellPrice().base.getAmount() + "";
                    long   lSellBaseAmount  = Long.valueOf(sSellBaseAmount).longValue();
                    double sellAmount = utils.get_asset_amount(lSellBaseAmount, open_order.quote);
                    String sSellAmount = CalculateUtils.div(sellAmount,1,open_order.quote.precision);
                    double nPrice =  buyAmount/sellAmount;
                    String sPrice =  String.format("%.5f %s/%s", nPrice,open_order.base.symbol,open_order.quote.symbol );
                    String sExpirationDate = open_order.limitOrder.getExpiration();
                    String sSourceSymbol = open_order.base.symbol;
                    String sDestSymbol = open_order.quote.symbol;
                    arrayList.add(new OrdersResponseModel.DataBean(sPrice,sSellAmount,sBuyAmount,sExpirationDate,open_order.limitOrder,""));
                }
            }
        }

        return arrayList;
    }

    private double assetToReal(AssetAmount a, long p) {
        String sAmount = a.getAmount()+"";
        double dAmount = Double.valueOf(sAmount).doubleValue();
        return dAmount / Math.pow(10, p);
    }

    private double priceToReal(Price p, asset_object baseAsset, asset_object quoteAsset) {
        if (p.base.getAsset().getObjectId().equals(baseAsset.id)) {
            return assetToReal(p.base, baseAsset.precision) / assetToReal(p.quote, quoteAsset.precision);
        } else {
            return assetToReal(p.quote, baseAsset.precision) / assetToReal(p.base, quoteAsset.precision);
        }
    }

    public interface OnCancelListener{
        void onCancelSuccess();
    }

    private OnCancelListener onCancelListener;
    public void registerOnCancelSuccessListener(OnCancelListener listener) {
        onCancelListener = listener;
    }

    public void unregisterOnCancelSuccessListener() {
        if (null != onCancelListener) {
            onCancelListener = null;
        }
    }

    public OnCancelListener getOnCancelListener() {
        if (null != onCancelListener) {
            return onCancelListener;
        }

        return null;
    }

    public interface OnVisibleListener {
        void onVisible();
    }

    private BuyFragment.OnVisibleListener onVisibleListener;
    public void registerOnVisibleListener(BuyFragment.OnVisibleListener listener) {
        onVisibleListener = listener;
    }

    public void unregisterOnVisibleListener() {
        onVisibleListener = null;
    }
}
