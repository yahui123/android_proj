package com.tang.trade.tang.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.MortgageAdapter;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.CallOrder;
import com.tang.trade.tang.socket.chain.FullAccountOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.SPUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.LimitOrder;

public class MyMortgageActivity extends BaseActivity {


    @BindView(R.id.lv_mortgage)
    ListView lv_mortgage;

    @BindView(R.id.iv_back)
    ImageView iv_back;
    private MortgageAdapter adapter;
    private List<CallOrder> dateList;
    private String sLoginAccount="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_mortgage;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
            break;
            default:
                break;
        }


    }

    @Override
    public void initView() {

        iv_back.setOnClickListener(this);
        dateList=new ArrayList<>();
        adapter=new MortgageAdapter(dateList,this);
        lv_mortgage.setAdapter(adapter);
        sLoginAccount = SPUtils.getString(Const.USERNAME,"");

    }

    @Override
    public void initData() {

        try {
            List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
            if (null!=objAssets&&objAssets.size()!=0){
                adapter.setAssetIdList(objAssets);
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        full_account a = null;
        try {
            a = BitsharesWalletWraper.getInstance().get_full_account(sLoginAccount, false);
            if (a != null  && a.call_orders != null) {
                dateList.addAll(a.call_orders);
                adapter.notifyDataSetChanged();
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
