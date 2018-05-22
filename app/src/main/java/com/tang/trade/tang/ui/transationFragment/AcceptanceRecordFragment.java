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
import com.tang.trade.tang.adapter.AccesstanceReAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AccTranResponseModel;
import com.tang.trade.tang.net.acceptormodel.ChongZhiOrTiXianModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.AccOrderActivity;
import com.tang.trade.tang.ui.AcceptanceManagerActivity;
import com.tang.trade.tang.ui.AcceptanceRecordActivity;
import com.tang.trade.tang.ui.AccountOrderActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AcceptanceRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcceptanceRecordFragment extends Fragment implements AcceptanceRecordActivity.onRefreshData {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String TYPE1="type1";//充值记录
    private String TYPE2="type2";//提现记录
    private static final String ARG_PARAM1 = "param1";

    private String isGetCashIn ="0";//0不获取，1获取 、、充值
    private String isGetCashOut ="0";//0不获取，1获取 、、提现

    private String TYPE;
    private ListView listView;
    private AccesstanceReAdapter adapter;
    private List<AccTranResponseModel.DataBean> date =new ArrayList<>();
    private AcceptanceRecordFragment fragment;
    private AcceptanceRecordActivity activity;
    private boolean isFrist=true;
    private LoadingView loadingView;
    private NoDataView ll_nodata;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity()!=null){
                if (msg.what==1){//suceeess
                    if (date.size()>0){
                        loadingView.setVisibility(View.GONE);
                        ll_nodata.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }else {
                        ll_nodata.setVisibility(View.VISIBLE);
                    }

                }else if (msg.what==2){//fial
                    loadingView.setVisibility(View.GONE);
                    if (date.size()==0){
                        ll_nodata.setVisibility(View.VISIBLE);
                    }else {
                        ll_nodata.setVisibility(View.GONE);
                    }


                }else {//Error
                    loadingView.setVisibility(View.GONE);
                    MyApp.showToast(getContext().getString(R.string.network));
                    if (date.size()==0){
                        ll_nodata.setVisibility(View.VISIBLE);
                    }else {
                        ll_nodata.setVisibility(View.GONE);
                    }

                }

            }

        }
    };

    
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            acceptantDate();
        }
    };


    public AcceptanceRecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != fragment&&isFrist==false&& isVisibleToUser == true) {
            if (getActivity()!=null){
                activity.setRefreshData(this);
                if (date.size()==0){
                    loadingView.setVisibility(View.VISIBLE);
                    ll_nodata.setVisibility(View.GONE);
                    handler.postDelayed(runnable,500);
                }
            }
        }else if (null != fragment &&isFrist==false&&isVisibleToUser == false) {
            handler.removeCallbacks(runnable);
        }

    }


    public static AcceptanceRecordFragment newInstance(String param1) {
        AcceptanceRecordFragment fragment = new AcceptanceRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrist=false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TYPE = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_acceptance_record, container, false);
        initView(view);
        fragment=this;
        return view;
    }
    private void initView(View v) {

        if (TYPE.equals(TYPE1)){
            isGetCashIn="1";
        }else {
            isGetCashOut="1";

        }


        listView=(ListView) v.findViewById(R.id.lvTransFm);
        loadingView=(LoadingView) v.findViewById(R.id.loading);
        ll_nodata=v.findViewById(R.id.ll_nodata);
        adapter=new AccesstanceReAdapter(date,getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AcceptanceManagerActivity.Listtype=3;
                Intent intent=new Intent(getActivity(), AccOrderActivity.class);
                intent.putExtra("dataBean",  date.get(i));
                startActivity(intent);
            }
        });
        activity= (AcceptanceRecordActivity) getActivity();
        if (TYPE.equals(TYPE1)){
          activity.setRefreshData(this);
        }


        acceptantDate();



    }
    /**
     * 充值 0r 提现 记录//acceptant_get_cashin_list
     * */
    private void acceptantDate(){
        final String username= SPUtils.getString(Const.USERNAME,"");
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        hashMap.put("isAcceptant","1");
       // hashMap.put("orderStatusCode","COM");
        hashMap.put("isGetCashIn",isGetCashIn);
        hashMap.put("isGetCashOut",isGetCashOut);

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)){
            MyApp.showToast(getContext().getString(R.string.encryption_failed));
            ll_nodata.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
            return;
        }
        hashMap.put("encmsg",signMessage);
        hashMap.put("bdsAccount",SPUtils.getString(Const.USERNAME,""));
        AcceptorApi.acceptantHttp(hashMap,"get_order_info",new JsonCallBack<AccTranResponseModel>(AccTranResponseModel.class) {
            @Override
            public void onSuccess(Response<AccTranResponseModel> response) {

                if (getActivity()!=null){
                    if (response.body().getStatus().equals("success")){
                        date.clear();
                        date.addAll(response.body().getData());

                    }else {
                        if (!response.body().getMsg().equals("nodata")) {
                            MyApp.showToast(response.body().getMsg());
                        }
                    }
                    if (date.size()>0){
                        ll_nodata.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }else {
                        ll_nodata.setVisibility(View.VISIBLE);
                    }
                    loadingView.setVisibility(View.GONE);

                }

            }

            @Override
            public void onStart(Request<AccTranResponseModel, ? extends Request> request) {
                super.onStart(request);

            }

            @Override
            public void onError(Response<AccTranResponseModel> response) {
                super.onError(response);
                if (getActivity()!=null){
                    loadingView.setVisibility(View.GONE);
                    MyApp.showToast(getContext().getString(R.string.network));
                    if (date.size()==0){
                        ll_nodata.setVisibility(View.VISIBLE);
                    }else {
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
