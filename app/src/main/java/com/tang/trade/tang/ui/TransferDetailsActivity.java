package com.tang.trade.tang.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.TransferDetailsAdapter;
import com.tang.trade.tang.net.model.AccDetailsModel;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

public class TransferDetailsActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.lv_details)
    ListView lv_details;

    private List<AccDetailsModel> data = new ArrayList<>();
    private TransferDetailsAdapter adapter;

    private HistoryResponseModel.DataBean dataBean;


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
        adapter = new TransferDetailsAdapter(this,data);
        lv_details.setAdapter(adapter);

    }

    @Override
    public void initData() {
        setData();
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_details;
    }

    private void setData(){

        Intent intent = getIntent();
        dataBean = (HistoryResponseModel.DataBean) intent.getSerializableExtra("detials");

        HashMap<String,String> map = null;
        if (dataBean != null){
            try {
                map = BitsharesWalletWraper.getInstance().cli_get_block(dataBean.getBlockNum(),Integer.parseInt(dataBean.getIndex()));
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

        }

        data.add(new AccDetailsModel("区块",dataBean.getBlockNum()));
        if (map != null){
            data.add(new AccDetailsModel("时间",map.get("date")));
        }else {
            data.add(new AccDetailsModel("时间",""));
        }
        data.add(new AccDetailsModel("币种",dataBean.getSymbol()));
        data.add(new AccDetailsModel("金额",dataBean.getAmount()));
        data.add(new AccDetailsModel("来自于",dataBean.getFrom()));
        data.add(new AccDetailsModel("去向",dataBean.getTo()));
        data.add(new AccDetailsModel("手续费",dataBean.getFee()));
        data.add(new AccDetailsModel("交易ID所在区块索引",dataBean.getIndex()));
        data.add(new AccDetailsModel("操作历史ID",dataBean.getId()));
        if (map != null){
            data.add(new AccDetailsModel("交易ID",map.get("transaction_ids")));
        }else{
            data.add(new AccDetailsModel("交易ID",""));
        }

        data.add(new AccDetailsModel("备注 ",dataBean.getMemo()));
    }
}
