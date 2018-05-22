package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.AccDetailsModel;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class AccDetailsAdapter extends BaseAdapter {

    List<AccDetailsModel> list = null;
    Context context;

    private String index;

    public void setIndex(String index) {
        this.index = index;
    }

    public AccDetailsAdapter(List<AccDetailsModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<AccDetailsModel> list) {
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
            convertView = View.inflate(parent.getContext(), R.layout.acc_details_item, null);

            holder = new ViewHolder();
            holder.tv_key = (TextView) convertView.findViewById(R.id.tv_key);
            holder.tv_value = (TextView) convertView.findViewById(R.id.tv_value);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (list.get(position).getKey().equalsIgnoreCase(context.getString(R.string.bds_OrderStatus))){
            if (list.get(position).getValue().equalsIgnoreCase("0")){
                holder.tv_value.setText(context.getString(R.string.bds_unactivation));
                holder.tv_value.setTextColor(0xff828282);
            }else if (list.get(position).getValue().equalsIgnoreCase("1")){
                if (index.equals("3")){
                    holder.tv_value.setText(context.getString(R.string.bds_PendingPaymentFrom));
                }else if (index.equals("4")){
                    holder.tv_value.setText(context.getString(R.string.bds_waitmember));
                }

                holder.tv_value.setTextColor(0xffff0000);
            }else if (list.get(position).getValue().equalsIgnoreCase("2")){
                if (index.equals("3")){
                    holder.tv_value.setText(context.getString(R.string.bds_ConfirmedPayment));
                }else if (index.equals("4")){
                    holder.tv_value.setText(context.getString(R.string.bds_confirmtransfer));
                }
                holder.tv_value.setTextColor(0xff419BF9);
            }else if (list.get(position).getValue().equalsIgnoreCase("3")){
                holder.tv_value.setText(context.getString(R.string.bds_Complete));
                holder.tv_value.setTextColor(0xff3EE525);
            }else if (list.get(position).getValue().equalsIgnoreCase("4")){
                holder.tv_value.setText(context.getString(R.string.bds_Membership));
                holder.tv_value.setTextColor(0xff828282);
            }

        }else {
            holder.tv_value.setText(list.get(position).getValue());

        }

        holder.tv_key.setText(list.get(position).getKey());



        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_key,tv_value;
    }


}
