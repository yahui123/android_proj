package com.tang.trade.tang.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.HomePagerAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.GongGaoModel;
import com.tang.trade.tang.net.acceptormodel.KlineModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.CoinList;
import com.tang.trade.tang.net.model.ExchangeResponseModel;
import com.tang.trade.tang.net.model.ReallyCoinList;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.ui.GongGaoActivity;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.BottomBarView;
import com.tang.trade.tang.widget.TextViewUtils;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tang.trade.tang.net.TangConstant.K_CHAR;

public class  HomePageFragment extends Fragment {
    private Unbinder unbinder;
    private View view;
    private String username;
    private String baseSymbol = "CNY";
    private String baseSymbol1;//page1的基础货币
    private String baseSymbol2 = "BDS";//page2的基础货币
    public static int page = 1;
    private int waitingTime = 5000;
    private boolean isFristPage2 = true;

    private YWIMKit mIMKit;
    private IYWConversationUnreadChangeListener mConversationUnreadChangeListener;
    private IYWConversationService mConversationService;

    public static  List<CoinList.DataBean> COINLIST_PRECISION = new ArrayList<>(); //交易对价格及数量精度信息

//    @BindView(R.id.home_rg)
//    RadioGroup radioGroup;
//    @BindView(R.id.rg0)
//    RadioButton rb0;
//    @BindView(R.id.rg1)
//    RadioButton rb1;
    @BindView(R.id.vp_home)
    ViewPager viewPager;
//    @BindView(R.id.line_tap)
//    LinearLayout line_tap;
    @BindView(R.id.ivGongGao)
    BottomBarView ivGongGao;
    @BindView(R.id.ivKefu)
    BottomBarView ivKefu;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private String gaonGaoUrl = "";

    private HomePagerAdapter mPagerAdapter;
    private ArrayList<String> titles = new ArrayList<String>();
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    HomePageFragment fragmemt;

    public static ArrayList<ReallyCoinList> datas = new ArrayList<>();
    public static CoinList coinList1;//交易对
    private List<String> symbolList = new ArrayList<>();
    public ArrayList<ExchangeResponseModel.DataBean> listDate = new ArrayList<>();

    private final int success = 1;

    public HomePageFragment() {
        // Required empty public constructor
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == success && onViewRefreshLister != null) {
                if (listDate.size() > 0) {
                    if (baseSymbol.equals(listDate.get(0).getBase()))
                        onViewRefreshLister.onRefreshLister(baseSymbol);
                } else {
                    onViewRefreshLister.onRefreshLister("");
                }
            }
        }
    };


    private TimerTask timerTask;
    private Timer timer;

    private void startTimer() {
        stopTimer();

        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                synchronized (this) {
                    try {
                        if (page == 1) {
                            baseSymbol = baseSymbol1;
                        } else {
                            baseSymbol = baseSymbol2;
                            if (isFristPage2 == true) {
                                waitingTime = 500;
                                isFristPage2 = false;
                            } else {
                                waitingTime = 3000;
                            }
                        }

                        if (datas.size() != 0) {
                            for (int i = 0; i < datas.size(); i++) {
                                if (datas.get(i).getBaseSymbol().equals(baseSymbol)) {
                                    symbolList = datas.get(i).getQouteSymbols();
                                    break;
                                }
                            }
                        }
                        updateData(baseSymbol, symbolList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        timer.schedule(timerTask, 200, waitingTime);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        username = SPUtils.getString(Const.USERNAME,"");
        view = inflater.inflate(R.layout.fragment_home_page, container, false);
        unbinder = ButterKnife.bind(this, view);
        fragmemt = this;
        if (LaunchActivity.lang.equals("en")) {
            baseSymbol1 = "USD";
        } else {
            baseSymbol1 = "CNY";
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private void initData() {
        ivKefu.setMessageCount(0);
        ivGongGao.setMessageCount(0);
        String userid = SPUtils.getString(Const.USERNAME,"");
        if (!userid.equals("")) {
            mIMKit = YWAPI.getIMKitInstance(userid, "24738110");
            mConversationService = mIMKit.getConversationService();
            initConversationServiceAndListener();
        }
        ivKefu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIMKit != null) {
                    EServiceContact contact = new EServiceContact(BuildConfig.BDSSERVICE, 0);
                    IMListener.startChatting(getContext(),contact);
//                    Intent intent = mIMKit.getChattingActivityIntent(contact);
//                    startActivity(intent);
                }
            }
        });

        ivGongGao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivGongGao.setMessageCount(0);
                startActivity(new Intent(getActivity(), GongGaoActivity.class));
            }
        });


        titles.add(baseSymbol1);
        titles.add(baseSymbol2);

        for (int i = 0; i < titles.size(); i++) {
            NewHomeFragment fragment = new NewHomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", titles.get(i));
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }

        mPagerAdapter = new HomePagerAdapter(getChildFragmentManager(), fragments, titles);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(0);

//        rb1.setText(baseSymbol2);
//        TextViewUtils.setTextView(rb0, getActivity(), baseSymbol1);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.rg0) {
//                    viewPager.setCurrentItem(0);
//                    radioGroup.setSelected(false);
//                } else {
//                    viewPager.setCurrentItem(1);
//                    radioGroup.setSelected(true);
//                }
//            }
//        });

        mTabLayout.setupWithViewPager(viewPager);

//        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            @Override
//            public void onPageSelected(int position) {
////                View view = radioGroup.getChildAt(position);
////                radioGroup.check(view.getId());
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

        getCoinLists();
        getURl();

    }

    private void initConversationServiceAndListener() {

//        mConversationUnreadChangeListener = new IYWConversationUnreadChangeListener() {
//            @Override
//            public void onUnreadChange() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        EServiceContact contact = new EServiceContact(BuildConfig.BDSSERVICE, 0);//
//                        int c=mIMKit.getConversationService().getConversationCreater().createConversationIfNotExist(contact).addUnreadChangeListener().getUnreadCount();
//                        BadgeView badge = new BadgeView(getActivity());
//                        badge.setTargetView(ivKefu);
//                        badge.setBadgeCount(c);
//                    }
//                });
//            }
//        };

        final EServiceContact contact = new EServiceContact(BuildConfig.BDSSERVICE, 0);//
        mIMKit.getConversationService().getConversationCreater().createConversationIfNotExist(contact).addUnreadChangeListener(new IYWConversationUnreadChangeListener() {
            @Override
            public void onUnreadChange() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int c = mIMKit.getConversationService().getConversationCreater().createConversationIfNotExist(contact).getUnreadCount();
                        ivKefu.setMessageCount(c);
                    }
                });

            }
        });
        mConversationService.addTotalUnreadChangeListener(mConversationUnreadChangeListener);
    }

    private boolean isFrist = true;

    @Override
    public void onResume() {
        super.onResume();
        isFrist = false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (!hidden && null != fragmemt && !isFrist) {
//            onViewRefreshLister.onRefreshLister(true);
            startTimer();
        } else if (hidden && null != fragmemt && !isFrist) {
//            onViewRefreshLister.onRefreshLister(false);
            stopTimer();

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mConversationUnreadChangeListener != null) {
            mConversationService.removeTotalUnreadChangeListener(mConversationUnreadChangeListener);
        }
        handler.removeCallbacksAndMessages(null);

    }

    private onViewRefreshLister onViewRefreshLister;

    public void setOnRefreshLister(onViewRefreshLister onViewLister) {
        this.onViewRefreshLister = onViewLister;
    }


    public interface onViewRefreshLister {
        public void onRefreshLister(String baseSymbol);
    }

    private void getCoinLists() {
        final List<CoinList.DataBean> dataBeans = new ArrayList<>();
        final HashMap<String, String> map = new HashMap();
        AcceptorApi.acceptantHttp(map, "marrie_deputy_coins_list", new JsonCallBack<CoinList>(CoinList.class) {
            @Override
            public void onSuccess(Response<CoinList> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    CoinList coinList = response.body();
                    dataBeans.addAll(coinList.getData());
                    COINLIST_PRECISION.clear();
                    COINLIST_PRECISION.addAll(coinList.getData());
                    coinList1 = getBaseSymbol(response.body());
                    if (dataBeans != null && dataBeans != null && dataBeans.size() > 0) {
                        ReallyCoinList reallyCoinList = null;
                        ArrayList<String> qouteSymbols = null;

                        datas.clear();
                        for (int i = 0; i < coinList1.getData().size(); i++) {
                            reallyCoinList = new ReallyCoinList();
                            qouteSymbols = new ArrayList<>();

                            reallyCoinList.setBaseSymbol(coinList1.getData().get(i).getCoins());
                            for (int j = 0; j < dataBeans.size(); j++) {
                                if (dataBeans.get(j).getCoins().equalsIgnoreCase(coinList1.getData().get(i).getCoins())) {
                                    qouteSymbols.add(dataBeans.get(j).getDeputycoin());
                                }
                            }
                            reallyCoinList.setQouteSymbols(qouteSymbols);
                            datas.add(reallyCoinList);
                        }
                    }
                }
                if (datas.size() > 0) {
                    startTimer();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onStart(Request<CoinList, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<CoinList> response) {
                super.onError(response);
            }
        });


    }

    private CoinList getBaseSymbol(CoinList lists) {
        for (int i = 0; i < lists.getData().size() - 1; i++) {
            for (int j = lists.getData().size() - 1; j > i; j--) {
                if (lists.getData().get(j).getCoins().equals(lists.getData().get(i).getCoins())) {
                    lists.getData().remove(j);
                }
            }
        }
        return lists;
    }

    private CoinList getDefaultSymbol(CoinList lists) {
        for (int i = 0; i < lists.getData().size() - 1; i++) {
            if (lists.getData().get(i).getDe().equalsIgnoreCase("0")) {
                lists.getData().remove(i);
            }
        }
        return lists;
    }


    private void updateData(String baseSymbol, List<String> listType) {
        ArrayList<ExchangeResponseModel.DataBean> listDate2 = new ArrayList<>();
        String selectSymbol;
        int symbolsSize = listType.size();
        ExchangeResponseModel.DataBean dataBean = null;
        if (symbolsSize > 0) {
            for (int i = 0; i < symbolsSize; i++) {
                selectSymbol = listType.get(i);
                if (!selectSymbol.equals(baseSymbol)) {
                    MarketTicker marketTicker = null;
                    try {
                        marketTicker = BitsharesWalletWraper.getInstance().get_ticker(baseSymbol, selectSymbol);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                    if (marketTicker != null) {
                        dataBean = new ExchangeResponseModel.DataBean();
                        dataBean.setQuote(marketTicker.quote);
                        dataBean.setBase(marketTicker.base);
                        dataBean.setHighest_bid(marketTicker.highest_bid + "");
                        dataBean.setLatest(marketTicker.latest + "");
                        dataBean.setTurnover(marketTicker.base_volume + "");
                        dataBean.setPer(marketTicker.percent_change + "");
                        dataBean.setLowest_ask(marketTicker.lowest_ask + "");
                        dataBean.setQuote_volume(marketTicker.quote_volume + "");
                        listDate2.add(dataBean);
                    } else {
                        if (listDate.size() != 0) {
                            dataBean = listDate.get(i);
                            listDate2.add(dataBean);
                        } else {
                            dataBean = new ExchangeResponseModel.DataBean();
                            dataBean.setQuote(selectSymbol);
                            dataBean.setBase(baseSymbol);
                            dataBean.setLatest("0.00000");
                            dataBean.setTurnover("0.00000");
                            dataBean.setPer("0.00000");
                            dataBean.setQuote_volume("0.00000");
                            listDate2.add(dataBean);
                        }

                    }

                }
            }

        }
        if (listDate2 != null && listDate2.size() > 0) {
            listDate.clear();
            listDate.addAll(listDate2);
        }
        handler.sendEmptyMessage(success);
    }

    private void getURl() {

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("encmsg", "");
        AcceptorApi.acceptantHttp(hashMap, "borderless_notice", new JsonCallBack<GongGaoModel>(GongGaoModel.class) {
            @Override
            public void onSuccess(Response<GongGaoModel> response) {
                if (response.body().getStatus().equals("success")) {
                    gaonGaoUrl = response.body().getData().get(0).getVaule();
                    String index = response.body().getData().get(0).getStatus();
                    if (!MyApp.get("gaonGaoIndex", "-1").equals(index)) {
                        ivGongGao.setMessageCount(1);
                        MyApp.set("gaonGaoIndex", index);
                    }
                    MyApp.set("gaonGaoUrl", response.body().getData().get(0).getVaule());
                }

                getKlineURl();

            }

            @Override
            public void onStart(Request<GongGaoModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<GongGaoModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));

            }
        });

    }

    private void getKlineURl() {

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("encmsg", "");
        AcceptorApi.acceptantHttp(hashMap, "kline_url", new JsonCallBack<KlineModel>(KlineModel.class) {
            @Override
            public void onSuccess(Response<KlineModel> response) {
                if (response.body().getStatus().equals("success")) {
                    String kline_url = response.body().getData().get(0).getValue();
                    if (!TextUtils.isEmpty(kline_url)) {
                        K_CHAR = kline_url;
                        MyApp.set("kline_url", kline_url);
                    } else {
                        K_CHAR = MyApp.get("kline_url", K_CHAR);
                    }
                }

            }

            @Override
            public void onStart(Request<KlineModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<KlineModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
                K_CHAR = MyApp.get("kline_url", K_CHAR);

            }
        });

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
