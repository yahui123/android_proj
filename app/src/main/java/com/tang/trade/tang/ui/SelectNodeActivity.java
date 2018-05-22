package com.tang.trade.tang.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.NodeAdapter;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.NodeResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.widget.LoadingView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;

public class SelectNodeActivity extends BaseActivity implements TangApi.MyBaseViewCallBack<ArrayList<String>> {
    private int counter;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.iv_refresh)
    ImageView iv_refresh;
    @BindView(R.id.lv_node)
    ListView lv_node;

    @BindView(R.id.loading)
    LoadingView loading;

    private List<NodeResponseModel.DataBean> datas = new Vector<>();
    private NodeAdapter adapter;
    private String node ;

    private Handler handler = new Handler();
    private ExecutorService service = Executors.newFixedThreadPool(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        node = MyApp.CURRENT_NODE;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.iv_refresh) {
            if (datas.size()>0) {
                for (int i = 0; i < datas.size(); i++) {
                    if(datas.get(i) != null) {
                        getNetSleep1(datas.get(i));
                    }
                }
            }
        }
    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        iv_refresh.setOnClickListener(this);
        adapter = new NodeAdapter(this, datas);
        lv_node.setAdapter(adapter);

        lv_node.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!TextUtils.isEmpty(datas.get(position).getSpeed())) {
                    if (Integer.parseInt(datas.get(position).getSpeed())<=600) {
                        for (int i = 0; i < datas.size(); i++) {
                            if (i == position) {
                                datas.get(i).setSelected("1");
                            } else {
                                datas.get(i).setSelected("0");
                            }
                        }

                        MyApp.CURRENT_NODE = datas.get(position).getName();
                        MyApp.set("current_node",datas.get(position).getName());
                        node =  datas.get(position).getName();
                        MyApp.set("node",datas.get(position).getName());
                        adapter.setData(datas);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void initData() {
        setData();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_node;
    }

    private void setData() {
        TangApi.getNodes(this);
    }

    @Override
    public void setData(ArrayList<String> data) {
        if (data.size()>0) {
            for (int i = 0; i < data.size(); i++) {
                getNetSleep(data.get(i),data.size());
            }
        } else {
            if (TextUtils.isEmpty(MyApp.CURRENT_NODE)) {
                data.add( MyApp.CURRENT_NODE);
                for (int i = 0 ; i < data.size() ; i++) {
                    getNetSleep(data.get(i),data.size());
                }
            }
        }
    }

    private void SucessUpData(){
        if (datas.size() > 0) {
            Collections.sort(datas, new Comparator<NodeResponseModel.DataBean>() {
                @Override
                public int compare(NodeResponseModel.DataBean o1, NodeResponseModel.DataBean o2) {
                    Integer id1 = 0;
                    Integer id2 = 0;
                    if (o1 != null) {
                        if (!TextUtils.isEmpty(o1.getSpeed())) {
                            id1 = Integer.parseInt(o1.getSpeed());
                        }
                    }

                    if (o2 != null) {
                        if (!TextUtils.isEmpty(o2.getSpeed())) {
                            id2 = Integer.parseInt(o2.getSpeed());
                        }
                    }

                    return id1.compareTo(id2);
                }
            });
        }

        if (datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++){
                if (datas.get(i) != null) {
                    if (datas.get(i).getSpeed().equalsIgnoreCase("0") || TextUtils.isEmpty(datas.get(i).getSpeed()) || Integer.parseInt(datas.get(i).getSpeed()) > 600) {
                        datas.get(i).setSpeed("700");
                    }
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.setData(datas);
                    adapter.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                }
            });
        }
    }

    private  int countStr(String str1, String str2) {
        int counter = 0;
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else if (str1.indexOf(str2) != -1) {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) + str2.length()), str2);
            return counter;
        }
        return 0;
    }

    public void getNetSleep(final String url,final int size){
        service.submit(new Runnable() {
            @Override
            public void run() {
                String s;
                counter = countStr(url, ":");
                if (counter >= 1) {
                    s = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(":"));
                } else {
                    s = url.substring(url.lastIndexOf("/") + 1, url.length());
                }

                String sleep = "";
                Process p;
                try {
                    p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + s);
                    BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String str;

                    while ((str = buf.readLine()) != null) {
                        Log.i("wang", str);
                        if (str.contains("avg")) {
                            int i = str.indexOf("/", 20);
                            int j = str.indexOf(".", i);
                            sleep = str.substring(i + 1, j);
                            //sleep = sleep+"ms";
                        }
                    }

                    NodeResponseModel.DataBean dataBean;
                    dataBean = new NodeResponseModel.DataBean(url);
                    MyApp.CURRENT_NODE = url;
                    if (TextUtils.isEmpty(sleep) || !Device.pingIpAddress()) {
                        sleep = "700";
                    }
                    MyApp.CURRENT_NODE = node;
                    dataBean.setSpeed(sleep);
                    if (url.equals(node)){
                        dataBean.setSelected("1");
                    }else {
                        dataBean.setSelected("0");
                    }
                    datas.add(dataBean);
                    if (datas.size()==size){
                        SucessUpData();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getNetSleep1(final NodeResponseModel.DataBean dataBean){
        final String url = dataBean.getName();
        service.submit(new Runnable() {
            @Override
            public void run() {
                String s;
                counter = countStr(url, ":");
                if (counter >= 1) {
                    s = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(":"));
                } else {
                    s = url.substring(url.lastIndexOf("/") + 1, url.length());
                }
                String sleep = "";
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + s);
                    BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String str;

                    while ((str = buf.readLine()) != null) {
                        if (str.contains("avg")) {
                            int i = str.indexOf("/", 20);
                            int j = str.indexOf(".", i);
                            sleep = str.substring(i + 1, j);
                            //sleep = sleep+"ms";
                        }
                    }

                    MyApp.CURRENT_NODE = url;
                    if (TextUtils.isEmpty(sleep) || !Device.pingIpAddress()) {
                        sleep = "700";
                    }
                    MyApp.CURRENT_NODE = node;

                    dataBean.setSpeed(sleep);

                    if (url.equals(node)){
                        dataBean.setSelected("1");
                    }else {
                        dataBean.setSelected("0");
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                           SucessUpData();
                        }

                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void start() {
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEnd() {
        ArrayList<String> data = new ArrayList<>();
        if (MyApp.get("nodes",0) != 0){
            for (int i = 0; i<MyApp.get("nodes",0); i++){
                data.add(MyApp.get("node"+i,""));
            }
            for (int i = 0 ; i < data.size(); i++) {
                getNetSleep(data.get(i),data.size());
            }
        } else {
            Gson gson = new Gson();
            data = gson.fromJson(TangConstant.NODE_JSON,ArrayList.class);
            for (int i = 0 ; i < data.size() ; i++) {
                getNetSleep(data.get(i),data.size());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }
}
