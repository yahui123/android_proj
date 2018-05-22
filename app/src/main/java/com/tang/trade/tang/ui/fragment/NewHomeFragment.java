package com.tang.trade.tang.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tang.trade.app.Const;
import com.tang.trade.kchar.VerticalKLineActivity;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.NewHomeAdapter;
import com.tang.trade.tang.net.model.CoinList;
import com.tang.trade.tang.net.model.ExchangeResponseModel;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 */
public class NewHomeFragment extends Fragment implements HomePageFragment.onViewRefreshLister {
    private static final String TAG = "NewHome";
    private static final String ARG_PARAM1 = "type";
    public static int[] sScale;
    public List<ExchangeResponseModel.DataBean> listDate = new Vector<>();
    public ArrayList<ExchangeResponseModel.DataBean> newDate = new ArrayList<>();
    @BindView(R.id.loading)
    LoadingView loadingView;
    @BindView(R.id.lv_newHome)
    ListView lv_newHome;
    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;
    private NewHomeFragment fragment;
    private Unbinder unbinder;
    private View view;
    private String username;
    private NewHomeAdapter assetsAdapter;
    private List<String> symbolList;
    private String baseSymbol;
    private HomePageFragment fatherFragment;
    private boolean isFirst = true;

    public NewHomeFragment() {
        // Required empty public constructor
    }

    public static NewHomeFragment newInstance(String param1) {
        NewHomeFragment fragment = new NewHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            baseSymbol = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (null != fragment && !isFirst && isVisibleToUser) {
            fatherFragment.setOnRefreshLister(this);
            if (baseSymbol.equals("CNY") || baseSymbol.equals("USD")) {
                fatherFragment.page = 1;
            } else {
                fatherFragment.page = 2;
            }

        }
    }

    @Override
    public void onRefreshLister(String base) {
        if (baseSymbol.equals(base)) {
            assetsAdapter.setList(fatherFragment.listDate);
            assetsAdapter.notifyDataSetChanged();
            loadingView.setVisibility(View.GONE);
            if (sScale == null)
                sScale = getScale(listDate.get(0).getBase(), listDate.get(0).getQuote());

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        username = SPUtils.getString(Const.USERNAME, "");
        view = inflater.inflate(R.layout.fragment_new_home, null);
        unbinder = ButterKnife.bind(this, view);
        fragment = this;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private void initData() {

        fatherFragment = (HomePageFragment) getParentFragment();

        if (baseSymbol.equals("CNY") || baseSymbol.equals("USD")) {
            fatherFragment.page = 1;
            fatherFragment.setOnRefreshLister(this);
        }

        symbolList = new ArrayList<>();

        assetsAdapter = new NewHomeAdapter(getActivity(), listDate);
        lv_newHome.setAdapter(assetsAdapter);

        lv_newHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listDate.size() > i) {
                    sScale = getScale(listDate.get(i).getBase(), listDate.get(i).getQuote());
                    Intent intent = new Intent(getActivity(), VerticalKLineActivity.class);
                    if (Double.parseDouble(listDate.get(i).getPer()) >= 0) {
//                        intent.putExtra("color", getResources().getColor(R.color.market_global_upper));
                        intent.putExtra("color","#FFE20A16");
                    } else {
//                        intent.putExtra("color", getResources().getColor(R.color.market_global_lower));
                        intent.putExtra("color","#FF22D491");
                    }
                    intent.putExtra("baseSymbol", listDate.get(i).getBase()).putExtra("qouteSymbol", listDate.get(i).getQuote());
                    startActivityForResult(intent, 0);
                }

            }
        });
    }

    private int[] getScale(String baseSymbol, String quoteSymbol) {
        int[] precision = {2, 2};

        List<CoinList.DataBean> coinList = HomePageFragment.COINLIST_PRECISION;
        for (int i = 0; i < coinList.size(); i++) {
            CoinList.DataBean dataBean = coinList.get(i);

            if (baseSymbol.equals(dataBean.getCoins()) && quoteSymbol.equals(dataBean.getDeputycoin())) {
                precision[0] = Integer.valueOf(dataBean.getPriceScale());
                precision[1] = Integer.valueOf(dataBean.getCountScale());
            }
        }

        return precision;
    }

//    public static int [] mPrecision;
//    private int [] getPrecision(String baseSymbol, String quoteSymbol) {
//        int [] precision = {2,2};
////        if (quoteSymbol.equals("BTC") || quoteSymbol.equals("LTC") || quoteSymbol.equals("ETH")) {
////            precision[0] = 1;
////            precision[1] = 4;
////        } else {
////            precision[0] = 2;
////            precision[0] = 2;
////        }
//
//        for (int i = 0; i < HomePageFragment.datas.size(); i++) {
//            ReallyCoinList dataBean = HomePageFragment.datas.get(i);
//            if (dataBean.getBaseSymbol().equals("CNY") || dataBean.getBaseSymbol().equals("BDS")) {
//                ArrayList<String> quotes = dataBean.getQouteSymbols();
//
//                if (baseSymbol.equals())
//            }
//
////            if (baseSymbol.equals(dataBean.getCoins()) && quoteSymbol.equals(dataBean.getDeputycoin())) {
////                precision[0] = Integer.valueOf(dataBean.getPrice());
////                precision[1] = Integer.valueOf(dataBean.getCount());
////            }
//        }
//
//        return precision;
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                int type = data.getIntExtra(VerticalKLineActivity.TRADETYPE, -1);
                if (type != -1) {
                    ReChangeFragment2 reChangeFragment = MainActivity.mainActivity.getReChangeFragment();
                    MainActivity.mainActivity.addFragment(R.id.container, reChangeFragment);
                    MainActivity.mainActivity.switchSelectState(MainActivity.trade);

                    reChangeFragment.setAcceptType(type, baseSymbol, data.getStringExtra("qouteSymbol"));

                    reChangeFragment.initChildVisible(type);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
