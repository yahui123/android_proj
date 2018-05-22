package com.tang.trade.tang.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.BuyOrSellResponseModel;
import com.tang.trade.tang.utils.NumberUtils;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class BuyOrSellAdapter extends BaseAdapter {

    List<BuyOrSellResponseModel.DataBean> list = null;

    public void setList(List<BuyOrSellResponseModel.DataBean> list) {
        this.list = list;
    }

    public BuyOrSellAdapter(List<BuyOrSellResponseModel.DataBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.fragment_buy_item, null);

            holder = new ViewHolder();
            holder.tv_amount = (TextView) convertView.findViewById(R.id.tv_amount);
            holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
            holder.tv_gmv = (TextView) convertView.findViewById(R.id.tv_gmv);
            holder.v =  convertView.findViewById(R.id.v);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (position+1 < list.size()){
            if (list.get(position+1).getType().equals("Buy1") || list.get(position+1).getType().equals("买1") || list.get(position+1).getType().equals("賣")){
                holder.v.setVisibility(View.VISIBLE);

            }else {
                holder.v.setVisibility(View.GONE);
            }
        }else {
            holder.v.setVisibility(View.GONE);
        }


        holder.tv_amount.setText(NumberUtils.formatNumber5(list.get(position).getAmount()));
        holder.tv_price.setText(NumberUtils.formatNumber5(list.get(position).getPrice()));
        holder.tv_gmv.setText(NumberUtils.formatNumber5(list.get(position).getGmv()));
        holder.tv_type.setText(list.get(position).getType());
        holder.tv_type.setTextColor(list.get(position).getType().contains("Buy") || list.get(position).getType().contains("买") || list.get(position).getType().contains("買")? Color.parseColor("#37B569") : Color.parseColor("#FF2929"));
        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_type, tv_price, tv_amount,tv_gmv;
        View v;
    }


}
