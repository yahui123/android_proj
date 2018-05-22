package com.tang.trade.tang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.chain.CallOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.List;

/**
 * Created by Administrator on 2017/11/24.
 */

public class MortgageAdapter extends BaseAdapter {
    private List<CallOrder> listDate;
    private Context mCntext;
    List<asset_object> objAssets;

    public MortgageAdapter(List<CallOrder> listDate, Context mCntext) {
        this.listDate = listDate;
        this.mCntext = mCntext;

    }
    public void setAssetIdList(List<asset_object> objAssets) {
        this.objAssets=objAssets;
    }

    @Override
    public int getCount() {
        return listDate.size();
    }

    @Override
    public Object getItem(int i) {
        return listDate.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        CallOrder dataBean=listDate.get(i);

        if (view == null) {
            view = View.inflate(mCntext, R.layout.layout_mortgage_item, null);
            holder = new ViewHolder();

            holder.tv_mortgage_sumber = (TextView) view.findViewById(R.id.tv_mortgage_sumber);
            holder.tv_mortgage_type = (TextView) view.findViewById(R.id.tv_mortgage_type);
            holder.tv_mortgage_str = (TextView) view.findViewById(R.id.tv_mortgage_str);

            holder.tv_quota_sumber = (TextView) view.findViewById(R.id.tv_quota_sumber);
            holder.tv_quota_type = (TextView) view.findViewById(R.id.tv_quota_type);
            holder.tv_quote_str = (TextView) view.findViewById(R.id.tv_quote_str);
            holder.ll_bg = view.findViewById(R.id.ll_bg);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (!TextUtils.isEmpty(dataBean.getCollateral())){
            holder.tv_mortgage_sumber.setText(dataBean.getCollateral()+"");
        }
        if (!TextUtils.isEmpty(dataBean.getDebt())){
            holder.tv_quota_sumber.setText(dataBean.getDebt()+"");
        }
        if (null!=objAssets&&objAssets.size()!=0){
            String str_type=getAssetType(dataBean.quote_asset_id);
            if (!TextUtils.isEmpty(str_type)){
                holder.tv_quota_type.setText(str_type);
            }
        }else {

        }


        return view;
    }

    private String getAssetType(String _id){
        for (int i=0;i<objAssets.size();i++){
            asset_object asset_object=objAssets.get(i);
            if (_id.equals(asset_object.id)){
                return asset_object.symbol;
            }

        }

        return "";

    }


    //mortgage
    static class ViewHolder {
        TextView tv_quota_sumber, tv_quota_type,tv_quote_str;
        TextView tv_mortgage_sumber, tv_mortgage_type,tv_mortgage_str;

        LinearLayout ll_bg;
    }

}
