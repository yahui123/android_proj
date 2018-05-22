package com.tang.trade.module.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 */

public class SplashActivity extends AbsMVPActivity<SplashContract.Presenter> implements SplashContract.View {

    private int index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_400);

        String node = MyApp.get("current_node", "");
        if (TextUtils.isEmpty(node)) {
            //没有存储要重新获取节点
            mPresenter.getNodes();
        } else {
            MyApp.CURRENT_NODE = node;
            //ping不通也要重新请求获取
            mPresenter.pingIPAddress();
        }
    }

    @Override
    public void getIntentValue() {

    }

    @Override
    public void nodeSuccess(final List<String> list) {
        //获取到节点
        Log.i(TAG, "nodeSuccess: " + list.toString());

        //保存节点列表
        SPUtils.put(Const.NODES, new Gson().toJson(list));

        showLoading("正在筛选节点...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list.size() > 0) {
                    for (int j = 0; j < 2; j++) {
                        for (int i = 0; i < list.size(); i++) {
                            MyApp.CURRENT_NODE = list.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag) {
                                MyApp.set("current_node", list.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }
                        if (index == 1) {
                            break;
                        }
                    }

                } else {
                    Gson gson = new Gson();
                    ArrayList<String> data = gson.fromJson(TangConstant.NODE_JSON, ArrayList.class);
                    for (int j = 0; j < 2; j++) {
                        for (int i = 0; i < data.size(); i++) {
                            MyApp.CURRENT_NODE = data.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag) {
                                MyApp.set("current_node", list.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }
                        if (index == 1) {
                            break;
                        }
                    }
                }

                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        MyApp.set("nodes", list.size());
                        MyApp.set("node" + i, list.get(i));
                    }
                }
            }
        }).start();


    }

    @Override
    public void pingIPResult(String s) {
        if (s.equals("true")) {
            //ping通  则不需要操作
        } else if (s.equals("false")) {
            //本地存储的节点ping不通，需要重新获取
            mPresenter.getNodes();
        }
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter(this);
    }

    @Override
    public void onError(DataError error) {

    }
}
