package com.tang.trade.tang.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.NumberUtils;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class HomeListAdapter extends BaseAdapter {

    List<HistoryResponseModel.DataBean> list = null;
    Context context;
    private String username;

    public HomeListAdapter(List<HistoryResponseModel.DataBean> list,Context context) {
        this.list = list;
        this.context = context;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setList(List<HistoryResponseModel.DataBean> list) {
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
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.view_home_recyclerview_item, null);

            holder = new ViewHolder();
            holder.tv_amount = (TextView) convertView.findViewById(R.id.tv_amount);
            holder.tv_from = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_to = (TextView) convertView.findViewById(R.id.tv_action);
            holder.iv_kind = (ImageView) convertView.findViewById(R.id.iv_kind);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String from = list.get(position).getFrom();
        String to = list.get(position).getTo();
        holder.tv_from.setText(TextUtils.equals(username, from) ? to : from);
        holder.tv_to.setText(TextUtils.equals(username, from) ? R.string.rollout : R.string.rollin);


        Glide.with(context).load(TangConstant.COINTYPE+list.get(position).getSymbol()+".png").error(R.mipmap.broaderless).into(holder.iv_kind);
//        if (list.get(position).getSymbol().equalsIgnoreCase("BDS")){
//            holder.iv_kind.setImageResource(R.mipmap.broaderless);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("ETH")){
//            holder.iv_kind.setImageResource(R.mipmap.etb);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("LTC")){
//            holder.iv_kind.setImageResource(R.mipmap.ltb);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("CNY")){
//            holder.iv_kind.setImageResource(R.mipmap.cny);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("USD")){
//            holder.iv_kind.setImageResource(R.mipmap.dollar);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("BTC")){
//            holder.iv_kind.setImageResource(R.mipmap.bitcoin);
//        }else{
//            holder.iv_kind.setImageResource(R.mipmap.broaderless);
//        }

        if(list.get(position).getSymbol().equalsIgnoreCase("BTC") || list.get(position).getSymbol().equalsIgnoreCase("ETH") || list.get(position).getSymbol().equalsIgnoreCase("LTC")){
            holder.tv_amount.setText(list.get(position).getAmount());
        }else {
            holder.tv_amount.setText(NumberUtils.formatNumber5(list.get(position).getAmount()));
        }
        holder.tv_amount.setTextColor(TextUtils.equals(username, from) ? Color.parseColor("#00d0a1") : Color.parseColor("#fc5353"));
        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_from, tv_to, tv_amount;
        ImageView iv_kind;
    }


}
