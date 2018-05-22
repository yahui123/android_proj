package com.tang.trade.tang.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.HomeListAdapter;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by dagou on 2017/9/12.
 */

public class TransferRecordctivity extends BaseActivity {


    @BindView(R.id.listview)
    ListView listview;

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.loading)
    LoadingView loading;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;

    private String username;
    private HomeListAdapter adapter;
    private ArrayList<HistoryResponseModel.DataBean> data = new ArrayList<HistoryResponseModel.DataBean>();
    private HashMap<String,List<HistoryResponseModel.DataBean>> map = new HashMap<>();
    private DownTask downTask;


    public static void start(Context context) {
        Intent starter = new Intent(context, TransferRecordctivity.class);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = getIntent().getStringExtra("username");
        if (TextUtils.isEmpty(username)) {
            username = SPUtils.getString(Const.USERNAME,"");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_transfer;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_back){
            finish();
        }
    }

    @Override
    public void initView() {
        loading.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(this);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TransferRecordctivity.this,TransferDetailsActivity.class);
                intent.putExtra("detials",data.get(i));
                startActivity(intent);
            }
        });

    }




    private void initTransferData() {

        if (Device.pingIpAddress()) {
            String id = "1.11.0";

            for (int i = 0; i < 10; i++) {
                try {
                     map = BitsharesWalletWraper.getInstance().cli_transfer_record(username, id);
                    for (Map.Entry<String, List<HistoryResponseModel.DataBean>> vo : map.entrySet()) {
                        id = vo.getKey();
                        data.addAll((ArrayList<HistoryResponseModel.DataBean>) vo.getValue());
                    }
                    if (!id.equals("op_id")) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (data.size() > 0) {
                                        adapter.setList(data);
                                        adapter.setUsername(username);
                                        adapter.notifyDataSetChanged();

                                    }
                                    loading.setVisibility(View.GONE);
                                }
                            });



                        String count = id.substring(id.lastIndexOf(".") + 1, id.length());

                        String i1 = Integer.parseInt(count) - 1 + "";

                        id = id.substring(0, id.lastIndexOf(".")) + "." + i1;
                    }
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }

                if (isDestroyed()){
                    return;
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data.size()>0){
                        if (ll_nodata.getVisibility()==View.VISIBLE)
                            ll_nodata.setVisibility(View.GONE);
                    }else {
                        ll_nodata.setVisibility(View.VISIBLE);

                    }
                }
            });

        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.showToast(getString(R.string.network));
                    loading.setVisibility(View.GONE);
                    if (data==null||data.size() ==0) {
                        ll_nodata.setVisibility(View.VISIBLE);
                    }

                }
            });
        }


    }

    @Override
    public void initData() {
        adapter = new HomeListAdapter(data,this);
        listview.setAdapter(adapter);

        downTask = new DownTask();
        downTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();


//        downTask = new DownTask();
//        downTask.execute();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downTask != null && downTask.getStatus() == AsyncTask.Status.RUNNING) {
            downTask.cancel(true);
            downTask = null;
        }
    }

    public class DownTask extends AsyncTask<String, Void, ArrayList<HistoryResponseModel.DataBean>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<HistoryResponseModel.DataBean> doInBackground(String... params) {
            initTransferData();
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryResponseModel.DataBean> dataBeen) {
            super.onPostExecute(dataBeen);
        }
    }
}
