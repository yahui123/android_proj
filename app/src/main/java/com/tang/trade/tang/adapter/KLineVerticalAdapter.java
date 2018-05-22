package com.tang.trade.tang.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.AllHistoryResult;

import java.util.List;

/**
 * Created by Administrator on 2018/1/16.
 */

public class KLineVerticalAdapter extends BaseAdapter {

    List<AllHistoryResult> list = null;
    private Context mContext;

    public KLineVerticalAdapter(Context context, List<AllHistoryResult> list) {
        mContext = context;
        this.list = list;
    }

    public void setList(List<AllHistoryResult> list) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LatestDealAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.kline_vertical_item, null);

            holder = new LatestDealAdapter.ViewHolder();
            holder.tv_time = convertView.findViewById(R.id.tv_time);
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            holder.tv_amount_cny = convertView.findViewById(R.id.tv_amount_cny);
            holder.tv_amount_bds= convertView.findViewById(R.id.tv_amount_bds);

            convertView.setTag(holder);
        } else {
            holder = (LatestDealAdapter.ViewHolder) convertView.getTag();
        }

        holder.tv_time.setText(list.get(position).getTime());
        holder.tv_price.setText(list.get(position).getPrice());
        holder.tv_amount_cny.setText(list.get(position).getBaseSymbolAmount());
        holder.tv_amount_bds.setText(list.get(position).getQouteSymbolAmount());

        if (list.get(position).getType().equalsIgnoreCase("sell")){
            holder.tv_price.setTextColor(mContext.getResources().getColor(R.color.rechange_history_sell));
        }else  if (list.get(position).getType().equalsIgnoreCase("buy")){
            holder.tv_price.setTextColor(mContext.getResources().getColor(R.color.rechange_history_buy));
        }

        holder.tv_time.setTextColor(Color.parseColor("#a1a1a1"));
        holder.tv_amount_cny.setTextColor(Color.parseColor("#a1a1a1"));
        holder.tv_amount_bds.setTextColor(Color.parseColor("#a1a1a1"));

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_time, tv_price, tv_amount_bds, tv_amount_cny;
    }
}
