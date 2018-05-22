package com.tang.trade.tang.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.ExchangeResponseModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class ExchangeAdapter extends BaseAdapter {

    List<ExchangeResponseModel.DataBean> list = null;
    Context context;

    public ExchangeAdapter(List<ExchangeResponseModel.DataBean> list,  Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<ExchangeResponseModel.DataBean> list) {
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
            convertView = View.inflate(parent.getContext(), R.layout.exchange_layout_item, null);

            holder = new ViewHolder();
            holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            holder.tv_quote_volume = (TextView) convertView.findViewById(R.id.tv_quote_volume);
            holder.tv_per = (TextView) convertView.findViewById(R.id.tv_per);
            holder.max = (TextView) convertView.findViewById(R.id.max);
            holder.min = (TextView) convertView.findViewById(R.id.min);
            holder.vol = (TextView) convertView.findViewById(R.id.vol);
            holder.turn = (TextView) convertView.findViewById(R.id.turn);
            holder.iv_coin = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tv_bds = (TextView) convertView.findViewById(R.id.tv_bds);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00000");
        holder.tv_type.setText(list.get(position).getQuote());

        if (list.get(position).getQuote().equalsIgnoreCase("CNY") || list.get(position).getQuote().equalsIgnoreCase("USD")){
            holder.tv_bds.setVisibility(View.VISIBLE);

        }else {
            holder.tv_bds.setVisibility(View.GONE);
        }

        holder.tv_quote_volume.setText(decimalFormat.format(Double.parseDouble(list.get(position).getLatest())));
        try{
            if (Double.parseDouble(list.get(position).getPer())>=0){
                holder.tv_per.setText("+"+decimalFormat.format(Double.parseDouble(list.get(position).getPer()))+"%");
                holder.tv_per.setTextColor(Color.parseColor("#37B569"));
            }else{
                holder.tv_per.setText(decimalFormat.format(Double.parseDouble(list.get(position).getPer()))+"%");
                holder.tv_per.setTextColor(Color.parseColor("#FF2929"));
            }

        }catch (Exception e){

                holder.tv_per.setText("+"+0.00+"%");
                holder.tv_per.setTextColor(Color.parseColor("#37B569"));
        }


        holder.max.setText(decimalFormat.format(Double.parseDouble(list.get(position).getHighest_bid())));
        holder.min.setText(decimalFormat.format(Double.parseDouble(list.get(position).getLowest_ask())));
        holder.vol.setText(decimalFormat.format(Double.parseDouble(list.get(position).getQuote_volume())));
        holder.turn.setText(decimalFormat.format(Double.parseDouble(list.get(position).getTurnover())));

        Glide.with(context).load(TangConstant.COINTYPE+list.get(position).getQuote()+".png").error(R.mipmap.broaderless).into(holder.iv_coin);
//        if (list.get(position).getQuote().equalsIgnoreCase("BDS")){
//            holder.iv_coin.setImageResource(R.mipmap.broaderless);
//        }else if (list.get(position).getQuote().equalsIgnoreCase("ETH")){
//            holder.iv_coin.setImageResource(R.mipmap.etb);
//        }else if (list.get(position).getQuote().equalsIgnoreCase("LTC")){
//            holder.iv_coin.setImageResource(R.mipmap.ltb);
//        }else if (list.get(position).getQuote().equalsIgnoreCase("CNY")){
//            holder.iv_coin.setImageResource(R.mipmap.cny);
//        }else if (list.get(position).getQuote().equalsIgnoreCase("USD")){
//            holder.iv_coin.setImageResource(R.mipmap.dollar);
//        }else if (list.get(position).getQuote().equalsIgnoreCase("BTC")){
//            holder.iv_coin.setImageResource(R.mipmap.bitcoin);
//        }else{
//            holder.iv_coin.setImageResource(R.mipmap.broaderless);
//        }
        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_type, tv_quote_volume, tv_per,max,min,vol,turn,tv_bds;
        ImageView iv_coin;
    }


}
