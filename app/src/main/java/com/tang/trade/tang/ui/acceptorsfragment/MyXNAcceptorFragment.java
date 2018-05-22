package com.tang.trade.tang.ui.acceptorsfragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.AcceptantXNAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.CenterWlletModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.CashInActivity;
import com.tang.trade.tang.ui.CashOutActivity;
import com.tang.trade.tang.ui.fragment.NewAcceptorFragment;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public class MyXNAcceptorFragment extends Fragment implements View.OnClickListener, AcceptantXNAdapter.onOnButtonViewLister {
    private View view;
    private ListView listView;
    private AcceptantXNAdapter adapter;
    private ArrayList<CenterWlletModel.DataBean> list = new ArrayList<>();
    private String sortKind = "";
    private LoadingView loading;
    private NoDataView ll_nodata;
    Handler handler = new Handler();

    private int pageSearch = 1;

    MyXNAcceptorFragment fragmemt = null;
    private String str_search = "";

    private boolean isFrist = true;
    NewAcceptorFragment newAcceptorFragment;

    public MyXNAcceptorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrist = false;
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            acceptantList();
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != fragmemt && isFrist == false && isVisibleToUser == true) {
//            newAcceptorFragment.setOnZNSearchViewLister(this);
//            pageSearch=1;
            newAcceptorFragment.setSearchViewString();
            if (list.size() == 0) {
                loading.setVisibility(View.VISIBLE);
                ll_nodata.setVisibility(View.GONE);
                handler.postDelayed(runnable, 100);
            }


        } else if (null != fragmemt && isFrist == false && isVisibleToUser == false) {

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

        listView = (ListView) view.findViewById(R.id.listView);
        loading = view.findViewById(R.id.loading);
        ll_nodata = view.findViewById(R.id.ll_nodata);
        RefreshLayout refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        refreshLayout.setDisableContentWhenRefresh(false);//是否在刷新的时候禁止列表的操作
        refreshLayout.setDisableContentWhenLoading(false);//是否在加载的时候禁止列表的操作

        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);


        newAcceptorFragment = (NewAcceptorFragment) getParentFragment();

        adapter = new AcceptantXNAdapter(list, getActivity());
        adapter.setOnItemViewLister(this);
        listView.setAdapter(adapter);

        acceptantList();


    }

//    @Override
//    public void onZNSearchLister(String str) {
////        this.str_search=str;
////        pageSearch=1;
////        handler.removeCallbacks(thread1);
////        handler.post(thread1);
//
//    }

    private void acceptantList() {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        AcceptorApi.acceptantHttp(hashMap, "acceptant_digital_list", new JsonCallBack<CenterWlletModel>(CenterWlletModel.class) {
            @Override
            public void onSuccess(Response<CenterWlletModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    list.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();

                } else {

                }
                if (list.size() == 0) {
                    ll_nodata.setVisibility(View.VISIBLE);
                }
                loading.setVisibility(View.GONE);


            }

            @Override
            public void onStart(Request<CenterWlletModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<CenterWlletModel> response) {
                super.onError(response);
                loading.setVisibility(View.GONE);
                if (list.size() == 0) {
                    ll_nodata.setVisibility(View.VISIBLE);
                }


            }
        });
    }

    @Override
    public void onChongzhiLister(int position) {//充值
        Intent intent = new Intent(getActivity(), CashInActivity.class);
        intent.putExtra("dataBean", list.get(position));
        startActivity(intent);

    }

    @Override
    public void onTiXianLister(int position) {//提现
        Intent intent = new Intent(getActivity(), CashOutActivity.class);
        intent.putExtra("dataBean", list.get(position));
        startActivity(intent);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        list.clear();
    }


}
