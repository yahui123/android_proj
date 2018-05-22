package com.tang.trade.tang.ui.transationFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.TransFraAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AccTranResponseModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.AccOrderActivity;
import com.tang.trade.tang.ui.AccountOrderActivity;
import com.tang.trade.tang.ui.TransactionActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

public class TransctionFragment extends Fragment implements TransactionActivity.onRefreshData {
    private static final String TYPE_CI = "CI";
    private static final String TYPE_CO = "CO";
    private static final String ARG_PARAM1 = "type";

    // TODO: Rename and change types of parameters
    private String type;
    private ListView listView;
    private TransFraAdapter adapter;
    private List<AccTranResponseModel.DataBean> date = new ArrayList<>();
    private LoadingView loadingView;
    private NoDataView ll_nodata;
    private TransctionFragment fragment;
    private TransactionActivity activity;
    private boolean isFrist = true;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity() != null) {
                if (msg.what == 1) {//suceeess
                    if (date.size() > 0) {
                        loadingView.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        ll_nodata.setVisibility(View.GONE);
                    } else {
                        ll_nodata.setVisibility(View.VISIBLE);
                    }

                } else if (msg.what == 2) {//fial
                    loadingView.setVisibility(View.GONE);
                    ll_nodata.setVisibility(View.VISIBLE);

                } else {//Error
                    loadingView.setVisibility(View.GONE);
                    MyApp.showToast(getString(R.string.network));
                    if (date.size() == 0) {
                        ll_nodata.setVisibility(View.VISIBLE);
                    } else {
                        ll_nodata.setVisibility(View.GONE);
                    }
                }

            }

        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            acceptantDate(type);
        }
    };

    public TransctionFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TransctionFragment newInstance(String param1) {
        TransctionFragment fragment = new TransctionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transction, container, false);
        initView(view);
        fragment = this;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrist = false;


    }

    private void initView(View v) {

        if (type.equals(TYPE_CI)) {
            isGetCashIn = "1";
        } else {
            isGetCashOut = "1";

        }
        listView = (ListView) v.findViewById(R.id.lvTransFm);
        loadingView = v.findViewById(R.id.loading);
        ll_nodata = v.findViewById(R.id.ll_nodata);
        adapter = new TransFraAdapter(date, getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), AccountOrderActivity.class);
                intent.putExtra("dataBean", date.get(i));
                startActivity(intent);
            }
        });
//        acceptantDate(type);
        handler.post(runnable);
        activity = (TransactionActivity) getActivity();
        if (type.equals(TYPE_CI)) {
            activity.setRefreshData(this);
        }


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != fragment && isFrist == false && isVisibleToUser == true) {
            if (getActivity() != null) {
                if (date.size() == 0) {
                    loadingView.setVisibility(View.VISIBLE);
                    ll_nodata.setVisibility(View.GONE);
                    handler.postDelayed(runnable, 500);
                }

                activity.setRefreshData(this);//绑定监听

            }

        }

        if (null != fragment && isFrist == false && isVisibleToUser == false) {
            handler.removeCallbacks(runnable);
        }

    }

    /*
     * 获取订单里列表
     * */
    private String isGetCashIn = "0";//0不获取，1获取 、、充值
    private String isGetCashOut = "0";//0不获取，1获取 、、提现

    private void acceptantDate(String str) {
        final String username = SPUtils.getString(Const.USERNAME, "");
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
        hashMap.put("isAcceptant", "0");
        hashMap.put("orderStatusCode", "COM");
        hashMap.put("isGetCashIn", isGetCashIn);
        hashMap.put("isGetCashOut", isGetCashOut);
        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            ll_nodata.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
            return;
        }
        hashMap.put("encmsg", signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));

        AcceptorApi.acceptantHttp(hashMap, "get_order_info", new JsonCallBack<AccTranResponseModel>(AccTranResponseModel.class) {
            @Override
            public void onSuccess(Response<AccTranResponseModel> response) {
                if (getActivity() != null) {
                    if (response.body().getStatus().equals("success")) {
                        date.clear();
                        date.addAll(response.body().getData());
                        //   handler.sendEmptyMessage(1);

                    } else {
                        //    handler.sendEmptyMessage(2);
                        if (!response.body().getMsg().equals("nodata")) {
                            MyApp.showToast(response.body().getMsg());
                        }
                    }
                    loadingView.setVisibility(View.GONE);
                    if (date.size() > 0) {

                        adapter.notifyDataSetChanged();
                        ll_nodata.setVisibility(View.GONE);
                    } else {
                        ll_nodata.setVisibility(View.VISIBLE);
                    }

                }


            }

            @Override
            public void onStart(Request<AccTranResponseModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<AccTranResponseModel> response) {
                super.onError(response);
                if (getActivity() != null) {
                    loadingView.setVisibility(View.GONE);
                    if (date.size() == 0) {
                        ll_nodata.setVisibility(View.VISIBLE);
                    } else {
                        ll_nodata.setVisibility(View.GONE);
                    }
                }


            }
        });
    }

    @Override
    public void onRefresh(String str) {
        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
    }
}
