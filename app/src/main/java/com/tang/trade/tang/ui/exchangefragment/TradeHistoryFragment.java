package com.tang.trade.tang.ui.exchangefragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.HistoryAdapter;
import com.tang.trade.tang.net.model.OrdersResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.block_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operation_history_object;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.market.MarketTrade;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.tang.widget.TextViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.loginAccountId;


public class TradeHistoryFragment extends Fragment implements View.OnClickListener{
    public static final String TAG ="TradeHistoryFragment";
    public static final String BUY = "buy";
    public static final String SELL = "sell";

    private View view;
    private Unbinder unbinder;

    private HistoryAdapter adapter;

    private DownTask downTask;

    @BindView(R.id.lv_history)
    ListView lv_history;

    @BindView(R.id.quote)
    TextView title_quote;

    @BindView(R.id.base)
    TextView title_base;

    @BindView(R.id.time)
    TextView time;

    @BindView(R.id.loading)
    LoadingView loadingView;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;

    @BindView(R.id.ll_title)
    LinearLayout ll_title;

    private String type;
    private String typeBase,typeSelect;
    //一个月按30天算
    private static final long DEFAULT_BUCKET_SECS = TimeUnit.MINUTES.toSeconds(30*24*60);

    private boolean isViewCreated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_history,container,false);
        }

        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();

        isViewCreated = true;

        adapter = new HistoryAdapter(new ArrayList<OrdersResponseModel.DataBean>());
        lv_history.setAdapter(adapter);

        if (isVisible) {
            downTask = new DownTask();
            downTask.execute();
//            downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
            //((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private void initData(){
        type = getArguments().getString("type","0");
        if (typeBase == null && typeSelect == null) {
            if (LaunchActivity.lang.equalsIgnoreCase("en")) {
                typeBase = getArguments().getString(ReChangeFragment2.BASE,"USD");
            } else {
                typeBase = getArguments().getString(ReChangeFragment2.BASE,"CNY");
            }
            typeSelect = getArguments().getString(ReChangeFragment2.SELECT,"BDS");
        }

        TextViewUtils.setTextView(title_quote, getContext(), typeSelect );
        TextViewUtils.setTextView(title_base, getContext(), typeBase);
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

            if (downTask != null && downTask.getStatus() == AsyncTask.Status.RUNNING) {
                downTask.cancel(true);
                downTask = null;
            }
        }
    }

    private ArrayList<OrdersResponseModel.DataBean> getHistoryData(){
        ArrayList<OrdersResponseModel.DataBean> arrayList = new ArrayList<>();
        long bucketSecs = DEFAULT_BUCKET_SECS;
        String baseSymbol = typeBase;
        String quoteSymbol = typeSelect;
        asset_object baseAssetObj = new asset_object();
        asset_object quoteAssetObj = new asset_object();

        try {//获取符号资产
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

        if (type.equalsIgnoreCase(getActivity().getString(R.string.bds_history_2))) {
            try {//交易历史
                List<operation_history_object> accountOp = BitsharesWalletWraper.getInstance().get_account_history(loginAccountId,100,true);
                if (accountOp == null) {
                    return null;
                }

                for (int i=0; i<accountOp.size(); i++) {
                    operation_history_object op = accountOp.get(i);
                    if (op == null) {
                        continue;
                    }

                    if (op.op.nOperationType != 4) {
                        continue;
                    }

                    operations.fill_order_operation order = (operations.fill_order_operation)op.op.operationContent;
                    long nAmount = order.pays.amount; //买入数量
                    object_id<asset_object> assetId = order.pays.asset_id;
                    long nAmount2 = order.receives.amount; //最低收入
                    object_id<asset_object> assetId2 = order.receives.asset_id;
                    String strBaseAssetId = baseAssetObj.id.toString();
                    String strQuoteAssetId = quoteAssetObj.id.toString();
                    String strAssetId1 = assetId.toString();
                    String strAssetId2 = assetId2.toString();

                    block_object block_num = BitsharesWalletWraper.getInstance().get_block(op.block_num,0);
                    if (block_num == null) {
                        return null;
                    }

                    String blockNumber = block_num.timeStame.replace("T","\n").replace(" ","\n");
                    String strAmount, strValue, strPrice;
                    int pre = quoteAssetObj.precision;
                    if (strBaseAssetId.equals(strAssetId1) && strQuoteAssetId.equals(strAssetId2)) {
                        strAmount = baseAssetObj.get_legible_asset_object(nAmount).count;
                        strValue = quoteAssetObj.get_legible_asset_object(nAmount2).count;
                        strPrice = CalculateUtils.div(Double.parseDouble(strAmount),Double.parseDouble(strValue), pre);
                        arrayList.add(new OrdersResponseModel.DataBean(strPrice,strValue,strAmount,blockNumber,null, BUY));
                    } else if (strBaseAssetId.equals(strAssetId2) && strQuoteAssetId.equals(strAssetId1)) {
                        strAmount = quoteAssetObj.get_legible_asset_object(nAmount).count;
                        strValue = baseAssetObj.get_legible_asset_object(nAmount2).count;
                        strPrice = CalculateUtils.div(Double.parseDouble(strValue),Double.parseDouble(strAmount), pre);
                        arrayList.add(new OrdersResponseModel.DataBean(strPrice,strAmount,strValue,blockNumber,null, SELL));
                    }
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        } else {
            final int maxLimitCount = 100; //一个月100条
            Date startTradeDate = new Date(System.currentTimeMillis() - bucketSecs * 1000);
            Date endTradeDate = new Date(System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS);
            try {
                List<MarketTrade> trades = BitsharesWalletWraper.getInstance().get_trade_history(baseSymbol,quoteSymbol,endTradeDate,startTradeDate,maxLimitCount);
                if (trades == null) {
                    return null;
                }

                for (int i=0; i<trades.size(); i++) {
                    MarketTrade tradeMarket = trades.get(i);
                    if (tradeMarket == null) {
                        return null;
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
                    String strDateTime = formatter.format(tradeMarket.date);
                    String strPrice = tradeMarket.price + "";
                    String strAmount = tradeMarket.amount + "";
                    String strValue = tradeMarket.value + "";
                    arrayList.add(new OrdersResponseModel.DataBean(strPrice,strAmount,strValue,strDateTime,null,""));
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }

        return arrayList;
    }

    public class DownTask extends AsyncTask<String, Void, ArrayList<OrdersResponseModel.DataBean>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingView.setVisibility(View.VISIBLE);
            ll_nodata.setVisibility(View.INVISIBLE);
            ll_title.setVisibility(View.INVISIBLE);
            lv_history.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ArrayList<OrdersResponseModel.DataBean> doInBackground(String... params) {
            return getHistoryData();
        }

        @Override
        protected void onPostExecute(ArrayList<OrdersResponseModel.DataBean> dataBeen) {
            super.onPostExecute(dataBeen);

            loadingView.setVisibility(View.INVISIBLE);

            if (dataBeen == null || dataBeen.size() == 0) {
                ll_title.setVisibility(View.INVISIBLE);
                lv_history.setVisibility(View.INVISIBLE);
                ll_nodata.setVisibility(View.VISIBLE);
                return;
            }

            if (dataBeen.size() > 0) {
                ll_nodata.setVisibility(View.INVISIBLE);
                ll_title.setVisibility(View.VISIBLE);
                lv_history.setVisibility(View.VISIBLE);

                adapter.setList(dataBeen);
                adapter.notifyDataSetChanged();
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
    public void onClick(View v) {}

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
