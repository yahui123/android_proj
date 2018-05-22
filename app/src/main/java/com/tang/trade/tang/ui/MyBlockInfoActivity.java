package com.tang.trade.tang.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.MyBlockInfoAdapter;
import com.tang.trade.tang.net.model.MyBlockInfoResponseModel;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MyBlockInfoActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.lv_block)
    ListView lv_block;
    @BindView(R.id.et_search)
    EditText et_search;

    private List<MyBlockInfoResponseModel.DataBean> data = new ArrayList<MyBlockInfoResponseModel.DataBean>();
    private MyBlockInfoAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back){
            finish();
        }
    }

    @Override
    public void initView() {
        ivBack.setOnClickListener(this);
        adapter = new MyBlockInfoAdapter(this,data);
        lv_block.setAdapter(adapter);
    }

    @Override
    public void initData() {
        setData();

        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_block_info;
    }

    private void setData(){
       for (int i = 0 ; i<2 ; i++){
           data.add(new MyBlockInfoResponseModel.DataBean("name"+i,"我的信息","1017"));
       }
    }


}
