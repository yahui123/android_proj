package com.tang.trade.tang.ui.acceptorsfragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.AcceptantZNAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.acceptormodel.AccetorPingjiaModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.AcceptanceActivity;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.fragment.NewAcceptorFragment;
import com.tang.trade.tang.utils.PopupWindowUtil;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.MyRanking;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * CNY 承兑
 */
public class MyZNAcceptorFragment extends Fragment implements View.OnClickListener, NewAcceptorFragment.onZNSearchViewLister, AcceptantZNAdapter.onOnButtonViewLister {

    public static final long REQUEST_DELAY_TIME = 5 * 60 * 1000;
    private static final String TAG = "MyZNAcceptorFragment";
    private static final int REFRESH_SUCCESS = 2;// Intel success
    private static final int LOADMORE_SUCCESS = 3;// Intel success
    private static final int SEARCH_IN = 4;
    private static final String REQUEST_REFRESH_TYPE = "request_refresh_type";
    private static final String REQUEST_LOADMORE_TYPE = "request_loadmore_type";
    MyZNAcceptorFragment fragmemt = null;
    NewAcceptorFragment fuFragment = null;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_SUCCESS:

                    break;
                case LOADMORE_SUCCESS:
                    break;
                case SEARCH_IN:
                    break;
            }
//            if (msg.what == listSuccess) {
//                loading.setVisibility(View.GONE);
//                if (list.size() > 0) {
//                    adapter.setListDate(list);
//                    adapter.notifyDataSetChanged();
//                    Gson gson = new Gson();
//
//                    SPUtils.put(Const.SP.SP_ACCEPTENCE_REQUEST_TIME, System.currentTimeMillis());
//                    SPUtils.put(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, gson.toJson(list));
//                } else if (msg.what == SEARCH_IN) {
//                    mHasLoading = true;
//                    handler.post(runnable);
//                } else {
//                    ll_nodata.setVisibility(View.VISIBLE);
//                }
//
//
//            }
        }
    };
    private boolean mHasLoading;
    private View view;
    private ListView listView;
    private NoDataView ll_nodata;
    private AcceptantZNAdapter adapter;
    private ArrayList<AcceptotXiangqingModel.DataBean> list = new ArrayList<AcceptotXiangqingModel.DataBean>();
    private TextView tv_search;
    private String sortKind = "";
    private LoadingView loading;
    private String baseSymbol;
    //    private int pageSearch = 1;//当前请求page
    private int mPage = 1; //记录当前page
    private String str_search = "";
    private ImageView iv_fanhui_top;
    private boolean isFrist = true;
    private PopupWindow mPopupWindow = null;
    private RefreshLayout mRefreshLayout;
    private String mRequestType;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            acceptantList(baseSymbol, mPage, str_search, mHasLoading);
        }
    };

    public MyZNAcceptorFragment() {
        // Required empty public constructor
    }

    private void requestForRefresh(boolean hasLoading) {
        mPage = 1;
        mHasLoading = hasLoading;
        mRequestType = REQUEST_REFRESH_TYPE;
        handler.post(runnable);
    }

    private void requestForLoadMore() {
        mPage = mPage + 1;
        mHasLoading = false;
        mRequestType = REQUEST_LOADMORE_TYPE;
        handler.post(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrist = false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != fragmemt && !isFrist && isVisibleToUser) {
            fuFragment.setOnZNSearchViewLister(this);

            fuFragment.setSearchViewString();
        } else if (null != fragmemt && !isFrist && !isVisibleToUser) {
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_acceptor_myzn, container, false);
        initView(view);
        fragmemt = this;
        return view;
    }

    @Override
    public void onClick(View view) {

    }

    public void initView(View view) {

        if (LaunchActivity.lang.equals("en")) {
            baseSymbol = "USD";
        } else {
            baseSymbol = "CNY";
        }
        tv_search = (TextView) view.findViewById(R.id.tv_search);

        listView = (ListView) view.findViewById(R.id.listView);
        ll_nodata = view.findViewById(R.id.ll_nodata);
        loading = view.findViewById(R.id.loading);

        iv_fanhui_top = view.findViewById(R.id.iv_fanhui_top);

        mRefreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadmore(true);
        mRefreshLayout.setEnableLoadmoreWhenContentNotFull(false);
        mRefreshLayout.setDisableContentWhenRefresh(false);//是否在刷新的时候禁止列表的操作
        mRefreshLayout.setDisableContentWhenLoading(false);//是否在加载的时候禁止列表的操作

        mRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                requestForLoadMore();
                refreshlayout.finishLoadmore();
            }
        });

        mRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                requestForRefresh(false);
                refreshlayout.finishRefresh();
            }
        });

        adapter = new AcceptantZNAdapter(list, getActivity());
        adapter.setOnItemViewLister(this);

        listView.setAdapter(adapter);
        fuFragment = (NewAcceptorFragment) getParentFragment();
        fuFragment.setOnZNSearchViewLister(this);

        initData();


    }

    public void initData() {
        if (TextUtils.isEmpty(SPUtils.getString(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, ""))) {
            requestForRefresh(true);
        } else {
            list = new Gson().fromJson(
                    SPUtils.getString(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, ""),
                    new TypeToken<List<AcceptotXiangqingModel.DataBean>>() {
                    }.getType());
            adapter.updateListDate(list);
            adapter.notifyDataSetChanged();
            mRefreshLayout.autoRefresh();
        }
    }

    private void updateAcceptorCache() {

    }

    private void appendAcceptorCache() {

    }

    private synchronized void acceptantList(String symbol, final int pages, final String searchSring, boolean hasLoading) {
        if (hasLoading && loading.getVisibility() == View.GONE) {
            loading.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("sortType", sortKind);
        hashMap.put("symbol", symbol);
        hashMap.put("page", "" + pages);
        hashMap.put("acceptantName", searchSring);
        hashMap.put("status", "A");

        AcceptorApi.acceptantHttp(hashMap, "get_acceptant_info", new JsonCallBack<AcceptotXiangqingModel>(AcceptotXiangqingModel.class) {
            @Override
            public void onSuccess(Response<AcceptotXiangqingModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    if (response.body().getData().size() > 0) {
                        switch (mRequestType) {
                            case REQUEST_REFRESH_TYPE:
                                list.clear();
                                list.addAll(response.body().getData());
                                adapter.updateListDate(list);
                                adapter.notifyDataSetChanged();

                                Gson gson = new Gson();
                                SPUtils.put(Const.SP.SP_ACCEPTENCE_REQUEST_TIME, System.currentTimeMillis());
                                SPUtils.put(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, gson.toJson(list));

                                break;
                            case REQUEST_LOADMORE_TYPE:
                                list.addAll(response.body().getData());
                                adapter.updateListDate(list);
                                adapter.notifyDataSetChanged();
                                break;
                        }

                    }

                    loading.setVisibility(View.GONE);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            if (list.size() == 0) {
                                ll_nodata.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                }
            }

            @Override
            public void onStart(Request<AcceptotXiangqingModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AcceptotXiangqingModel> response) {
                super.onError(response);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        if (list.size() == 0) {
                            ll_nodata.setVisibility(View.VISIBLE);
                        }

                    }
                });
            }
        });
    }

    private void acceptantDetailEvaluate(String acceptantName, final View view) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount", acceptantName);//get_acceptant_detail_evaluation
        AcceptorApi.acceptantHttp(hashMap, "get_acceptant_detail_evaluation", new JsonCallBack<AccetorPingjiaModel>(AccetorPingjiaModel.class) {
            @Override
            public void onSuccess(Response<AccetorPingjiaModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    showPopupWindow(view, response.body().getData().get(0));

                } else {
                    MyApp.showToast(getString(R.string.network));
                }


            }

            @Override
            public void onStart(Request<AccetorPingjiaModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AccetorPingjiaModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));


            }
        });
    }

    @Override
    public void onDuihuaLister(int position) {//跳转到对话列表

        IMListener.startChatting(getContext(), list.get(position).getAcceptantBdsAccount());
//        YWIMKit mIMKit = YWAPI.getIMKitInstance(SPUtils.getString(Const.USERNAME, ""), MyApp.APP_KEY);
//        Intent intent = mIMKit.getChattingActivityIntent(list.get(position).getAcceptantBdsAccount(), MyApp.APP_KEY);
//        startActivity(intent);
    }

    @Override
    public void onItemonLister(int position) {//跳转到充值提现
        if (!list.get(position).getAcceptantBdsAccount().equals(SPUtils.getString(Const.USERNAME, ""))) {
            Intent intent = new Intent(getActivity(), AcceptanceActivity.class);
            intent.putExtra("bdsAccount", list.get(position).getAcceptantBdsAccount());//
            intent.putExtra("dateBean", list.get(position));
            startActivity(intent);
        } else {
            MyApp.showToast(getString(R.string.bds_acceptor_not_me_ci_or_co));
        }


    }

    @Override
    public void onItemRating(int position, View view) {

        acceptantDetailEvaluate(list.get(position).getAcceptantBdsAccount(), view);
    }

    private void showPopupWindow(View anchorView, AccetorPingjiaModel.DataBean dataBean) {//popup_content_layout//totalEvaluate

        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_evaluate_layout, null);
        MyRanking myRanking1 = (MyRanking) contentView.findViewById(R.id.myRanking1);
        MyRanking myRanking2 = (MyRanking) contentView.findViewById(R.id.myRanking2);
        MyRanking myRanking3 = (MyRanking) contentView.findViewById(R.id.myRanking3);
        double attitudeEvaluate = 0;
        double speedEvaluate = 0;
        double honestEvaluate = 0;
        if (!TextUtils.isEmpty(dataBean.getAttitudeEvaluation())) {
            attitudeEvaluate = Double.parseDouble(dataBean.getAttitudeEvaluation());
            myRanking1.setRanling(attitudeEvaluate);
        }

        if (!TextUtils.isEmpty(dataBean.getSpeedEvaluation())) {
            speedEvaluate = Double.parseDouble(dataBean.getSpeedEvaluation());
            myRanking2.setRanling(speedEvaluate);
        }

        if (!TextUtils.isEmpty(dataBean.getHonestEvaluation())) {
            honestEvaluate = Double.parseDouble(dataBean.getHonestEvaluation());
            myRanking3.setRanling(honestEvaluate);
        }


        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setFocusable(true);// 点击空白处时，隐藏掉pop窗口

        mPopupWindow.setAnimationStyle(R.style.accouptor_popwindow_anim_style);

        // mPopupWindow.showAsDropDown(anchorView, -10, 10);

        // 设置好参数之后再show
        int windowPos[] = PopupWindowUtil.calculatePopWindowPos(anchorView, contentView);
        int xOff = 23;
        int yOff = 10;// 可以自己调整偏移
        windowPos[0] -= xOff;
        windowPos[1] += yOff;
        mPopupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        list.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchLister(String strSearch) {
        this.str_search = strSearch;
        mPage = 1;
        this.sortKind = "";

        if (list.size() > 0) {
            list.clear();
            adapter.notifyDataSetChanged();
        }
        if (ll_nodata.getVisibility() == View.VISIBLE) {
            ll_nodata.setVisibility(View.GONE);
        }

        if (loading.getVisibility() == View.GONE) {
            loading.setVisibility(View.VISIBLE);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 500);
//
//        acceptantList(baseSymbol,pageSearch,str_search);

    }

    @Override
    public void onKindSort(String sort) {
        this.sortKind = sort;
        mPage = 1;
        this.str_search = "";
        fuFragment.setSearchViewString();
//        listView.LoadComplete();
        list.clear();
        if (ll_nodata.getVisibility() == View.VISIBLE) {
            ll_nodata.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        loading.setVisibility(View.VISIBLE);
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 20);

    }

    @Override
    public void onJinruLister() {
        this.sortKind = "";
        this.str_search = "";
        mPage = 1;
//        listView.LoadComplete();
        if (ll_nodata.getVisibility() == View.VISIBLE) {
            ll_nodata.setVisibility(View.GONE);
        }

        list.clear();

        long lastRequestTime = SPUtils.getLong(Const.SP.SP_ACCEPTENCE_REQUEST_TIME, -1);

        if (System.currentTimeMillis() > (lastRequestTime + REQUEST_DELAY_TIME)) {
            if (TextUtils.isEmpty(SPUtils.getString(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, ""))) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 20);
            } else {
                list = new Gson().fromJson(
                        SPUtils.getString(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, ""),
                        new TypeToken<List<AcceptotXiangqingModel.DataBean>>() {
                        }.getType());
                adapter.updateListDate(list);
                adapter.notifyDataSetChanged();
                mRefreshLayout.autoRefresh();
            }

        } else {

            list = new Gson().fromJson(
                    SPUtils.getString(Const.SP.SP_ACCEPTENCE_REQUEST_LIST_JSON, ""),
                    new TypeToken<List<AcceptotXiangqingModel.DataBean>>() {
                    }.getType());
            adapter.updateListDate(list);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBacKTap() {
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(0);

    }


}
