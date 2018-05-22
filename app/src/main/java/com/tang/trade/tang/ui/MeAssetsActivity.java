package com.tang.trade.tang.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.MyAssetsAdapter;
import com.tang.trade.tang.net.model.AsssetsModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.asset;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.fragment.HomePageFragment;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.NoDataView;
import com.tang.trade.utils.SPUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Asset;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.loginAccountId;

public class MeAssetsActivity extends BaseActivity  {
    View hoder;
    String baseSymbol="CNY";//基础货币

    @BindView(R.id.ivBack)
    ImageView ivBack;

    @BindView(R.id.lv_myassets)
    ListView lv_myassets;

    @BindView(R.id.loading)
    LoadingView loading;

    @BindView(R.id.ll_nodata)
    NoDataView ll_nodata;

    TextView tvAsset;
    TextView iv_baseSymbol;
    MyAssetsAdapter assetsAdapter;

    String zongAmount="0";
    public ArrayList<AsssetsModel> listAssetsDate= new ArrayList<>();
    private List<String> symbolList=new ArrayList<>();

    String username;
    DecimalFormat decimalFormat = new DecimalFormat("#0.00000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_me_assets;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:
                finish();
                break;
        }

    }

    @Override
    public void initView() {
        if ("en".equals(LaunchActivity.lang)){
            baseSymbol = "USD";
        }else {
            baseSymbol = "CNY";
        }
        if (HomePageFragment.datas.size()!=0){
            for (int i=0;i<HomePageFragment.datas.size();i++){
                if (HomePageFragment.datas.get(i).getBaseSymbol().equals(baseSymbol)){
                    symbolList.clear();
                    symbolList.addAll(HomePageFragment.datas.get(i).getQouteSymbols());
                    break;
                }
            }
        }
        username = SPUtils.getString(Const.USERNAME,"");
        ivBack.setOnClickListener(this);


        hoder=View.inflate(this,R.layout.header_my_asster,null);
        tvAsset=hoder.findViewById(R.id.tvAsset);
        iv_baseSymbol=hoder.findViewById(R.id.iv_baseSymbol);

        assetsAdapter = new MyAssetsAdapter(listAssetsDate,baseSymbol,this);
        lv_myassets.setAdapter(assetsAdapter);
        lv_myassets.addHeaderView(hoder);



    }

    @Override
    public void initData() {

        iv_baseSymbol.setText("("+baseSymbol+")");
        if (Device.pingIpAddress()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAccountAssets();
                }
            }).start();
        }else {
            ll_nodata.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        }



    }
    private void getAccountAssets() {
        try {
            if (username != null) {
                String sAssetSymbol;
                String sAssetAmount;
                boolean findAssetInAccount = false;
                Map<String, Map<String,String>> symbolAssetList = new HashMap();
                List<asset> accountAsset = null;
                if (loginAccountId != null) {
                    accountAsset = BitsharesWalletWraper.getInstance().list_account_balance(loginAccountId, true);
                    if (accountAsset == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyApp.showToast(getString(R.string.network));
                            }
                        });
                        return;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                        }
                    });
                    return;

                }

                //查询资产列表
                List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
                if (objAssets == null || objAssets.size()<=0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                        }
                    });
                    return;
                }
                List<Asset> gAssetList = BitsharesWalletWraper.getInstance().list_assets("", 100);
                if (gAssetList == null  || gAssetList.size()<=0 ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.showToast(getString(R.string.network));
                        }
                    });

                    return;
                }
                //遍历资产
                if (gAssetList.size() > 0) {
                    for (int i = 0; i < gAssetList.size(); i++) {
                        Map<String,String> map=new HashMap<>();
                        Asset gAssetobj = gAssetList.get(i);
                        sAssetSymbol = gAssetobj.getSymbol();
                        //查询基础货币BDS,和所有智能资产以及非智能资产CNY
//                       if (objAssets.get(i).is_base_asset_object() || gAssetobj.getBitassetId() != null || sAssetSymbol.equals("CNY")|| sAssetSymbol.equals("BTC")) {
                        findAssetInAccount = false;
                        if (accountAsset.size() > 0) {
                            for (int j = 0; j < accountAsset.size(); j++) {
                                asset account_asset = accountAsset.get(j);
                                String gAsset_id = gAssetobj.getObjectId();
                                String sAccount_sid = account_asset.asset_id.toString();
                                if (sAccount_sid.equals(gAsset_id)) {
                                    findAssetInAccount = true;
                                    //计算账户余额
                                    asset_object.asset_object_legible myasset = objAssets.get(i).get_legible_asset_object(account_asset.amount);
                                    sAssetAmount = myasset.count;
                                    map.put(sAssetSymbol, sAssetAmount);
                                }
                            }
                        }
                        if (!findAssetInAccount) {
                            map.put(sAssetSymbol, "0.00000");
                        }
                        map.put("precision",gAssetobj.getPrecision()+"");
                        symbolAssetList.put(sAssetSymbol,map);



                    }
                }
                String[] arr = {"BDS", "CNY", "USD", "BTC", "LTC", "ETH"};

                //排序处理
                if (symbolAssetList.size() > 0) {
                    for (int i = 0; i < arr.length; i++) {
                        if (symbolAssetList.get(arr[i]) != null) {
                            AsssetsModel asssetsModel = new AsssetsModel();
                            asssetsModel.setSymbol(arr[i]);
                            asssetsModel.setAmount(symbolAssetList.get(arr[i]).get(arr[i]));
                            asssetsModel.setPrecision(symbolAssetList.get(arr[i]).get("precision"));
                            symbolAssetList.remove(arr[i]);
                            listAssetsDate.add(asssetsModel);

                        }
                    }
                    for (Map.Entry<String, Map<String,String> > entry : symbolAssetList.entrySet()) {
                        AsssetsModel asssetsModel = new AsssetsModel();
                        asssetsModel.setSymbol(entry.getKey());
                        asssetsModel.setAmount( entry.getValue().get(entry.getKey()));
                        asssetsModel.setPrecision( entry.getValue().get("precision"));
                        listAssetsDate.add(asssetsModel);

                    }
                    updateData(baseSymbol);
                }
            }


        } catch(NetworkStatusException e){
            e.printStackTrace();
        }

    }

    private void updateData(String baseSymbol){

        String selectSymbol ;
        int symbolsSize = listAssetsDate.size();
        if (symbolsSize > 0) {
            for (int i=0; i < symbolsSize; i++ ) {
                AsssetsModel dataBean=listAssetsDate.get(i);
                selectSymbol = dataBean.getSymbol();
                if (dataBean.getSymbol().equals(baseSymbol)){
                    zongAmount=Double.parseDouble(zongAmount)+Double.parseDouble(dataBean.getAmount())+"";
                    dataBean.setLatest("1");
                    dataBean.setPer("0");
                    continue;
                }
                boolean isFlay=false;
                for(int j=0;j<symbolList.size();j++){
                    if (selectSymbol.equals(symbolList.get(j))){
                        isFlay=true;
                        break;
                    }
                }
                if (isFlay==true){
                        MarketTicker marketTicker = null;
                        try {
                            marketTicker = BitsharesWalletWraper.getInstance().get_ticker(baseSymbol, selectSymbol);
                        } catch (NetworkStatusException e) {
                            e.printStackTrace();
                        }
                        if (marketTicker != null) {

                            dataBean.setLatest(marketTicker.latest + "");
                            dataBean.setPer(marketTicker.percent_change + "");
                            String amount = NumberUtils.formatNumber5(CalculateUtils.mul(new BigDecimal(marketTicker.latest).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue(), Double.parseDouble(dataBean.getAmount())));
                            zongAmount=CalculateUtils.add(Double.parseDouble(zongAmount),Double.parseDouble(amount))+"";

                        }

                }else {
                    dataBean.setLatest("0");
                    dataBean.setPer("0");
                }

            }

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               assetsAdapter.notifyDataSetChanged();
                tvAsset.setText(decimalFormat.format(Double.parseDouble(zongAmount)));
                loading.setVisibility(View.GONE);
                if (listAssetsDate.size()==0){
                    ll_nodata.setVisibility(View.VISIBLE);
                }
            }
        });




    }
}
