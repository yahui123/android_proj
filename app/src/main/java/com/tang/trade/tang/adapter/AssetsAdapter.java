package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.HistoryResponseModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class AssetsAdapter extends BaseAdapter {

    List<HistoryResponseModel.DataBean> list = null;

    public AssetsAdapter(List<HistoryResponseModel.DataBean> list,Context context) {
        this.list = list;
        this.context = context;
    }

    private Context context;

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
            convertView = View.inflate(parent.getContext(), R.layout.home_assets_item, null);

            holder = new ViewHolder();
            holder.tv_coin_type = (TextView) convertView.findViewById(R.id.tv_coinType);
            holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
            holder.iv_coin = (ImageView) convertView.findViewById(R.id.iv_coin);
            holder.tv_bds = (TextView) convertView.findViewById(R.id.tv_bds);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00000");

        holder.tv_coin_type.setText(list.get(position).getSymbol());
        if (list.get(position).getSymbol().equalsIgnoreCase("USD") || list.get(position).getSymbol().equalsIgnoreCase("CNY")){

            holder.tv_bds.setVisibility(View.VISIBLE);
        }else{
            holder.tv_bds.setVisibility(View.GONE);
        }

        holder.tv_count.setText(decimalFormat.format(Double.parseDouble(list.get(position).getAmount())));

        Glide.with(context).load(TangConstant.COINTYPE+list.get(position).getSymbol()+".png").error(R.mipmap.broaderless).into(holder.iv_coin);
//
//        if (list.get(position).getSymbol().equalsIgnoreCase("BDS")){
//            holder.iv_coin.setImageResource(R.mipmap.broaderless);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("ETH")){
//            holder.iv_coin.setImageResource(R.mipmap.etb);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("LTC")){
//            holder.iv_coin.setImageResource(R.mipmap.ltb);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("CNY")){
//            holder.iv_coin.setImageResource(R.mipmap.cny);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("USD")){
//            holder.iv_coin.setImageResource(R.mipmap.dollar);
//        }else if (list.get(position).getSymbol().equalsIgnoreCase("BTC")){
//            holder.iv_coin.setImageResource(R.mipmap.bitcoin);
//        }else {
//            holder.iv_coin.setImageResource(R.mipmap.broaderless);
//        }
        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_coin_type, tv_count,tv_bds;
        ImageView iv_coin;
    }


}
