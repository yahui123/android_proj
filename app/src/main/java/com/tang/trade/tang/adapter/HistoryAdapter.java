package com.tang.trade.tang.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.OrdersResponseModel;
import com.tang.trade.tang.utils.NumberUtils;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class HistoryAdapter extends BaseAdapter {

    List<OrdersResponseModel.DataBean> list = null;

    public HistoryAdapter(List<OrdersResponseModel.DataBean> list) {
        this.list = list;
    }

    public void setList(List<OrdersResponseModel.DataBean> list) {
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
        return 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.fragment_order_item, null);

            holder = new ViewHolder();
            holder.tv_btc = convertView.findViewById(R.id.tv_btc);
            holder.tv_bds = convertView.findViewById(R.id.tv_bds);
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            holder.tv_time = convertView.findViewById(R.id.tv_time);
            holder.tv_cancel = convertView.findViewById(R.id.tv_cancel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.size() > 0 && position < list.size()) {

            holder.tv_cancel.setVisibility(View.GONE);
            holder.tv_btc.setText(list.get(position).getBtc());
            holder.tv_price.setText(NumberUtils.formatNumber5(list.get(position).getPrice()));
            holder.tv_bds.setText(NumberUtils.formatNumber5(list.get(position).getBds()));
            holder.tv_time.setText(list.get(position).getTime());

            holder.tv_price.setGravity(Gravity.LEFT);
            holder.tv_time.setGravity(Gravity.RIGHT);
            /**
             * 记录有颜色
             */
            if (list.get(position).getType().equalsIgnoreCase("sell")) {

                holder.tv_btc.setTextColor(R.color.rechange_history_sell);
                holder.tv_price.setTextColor(R.color.rechange_history_sell);
                holder.tv_bds.setTextColor(R.color.rechange_history_sell);
                holder.tv_time.setTextColor(R.color.rechange_history_sell);
            } else if (list.get(position).getType().equalsIgnoreCase("buy")) {

                holder.tv_btc.setTextColor(R.color.rechange_history_buy);
                holder.tv_price.setTextColor(R.color.rechange_history_buy);
                holder.tv_bds.setTextColor(R.color.rechange_history_buy);
                holder.tv_time.setTextColor(R.color.rechange_history_buy);
            } else {
                holder.tv_btc.setTextColor(R.color.common_color_blank);
                holder.tv_price.setTextColor(R.color.common_color_blank);
                holder.tv_bds.setTextColor(R.color.common_color_blank);
                holder.tv_time.setTextColor(R.color.common_color_blank);
            }
        }
        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_price, tv_btc, tv_bds, tv_time, tv_cancel;
    }
}
