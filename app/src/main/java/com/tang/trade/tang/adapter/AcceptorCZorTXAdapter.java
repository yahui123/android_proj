package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.RechargeResponseModel;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class AcceptorCZorTXAdapter extends BaseAdapter {

    private String statusCode="NA";

    List<RechargeResponseModel> list = null;

    public AcceptorCZorTXAdapter(List<RechargeResponseModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    private Context context;

    public void setList(List<RechargeResponseModel> list) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.list_acceptor_cz_tx_xq_item, null);
            holder = new ViewHolder();
            holder.tc_acc_type = convertView.findViewById(R.id.tc_acc_type);
            holder.tc_acc_value = convertView.findViewById(R.id.tc_acc_value);
            holder.view_line =  convertView.findViewById(R.id.view_line);
            holder.iv_more = convertView.findViewById(R.id.iv_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tc_acc_value.setVisibility(View.VISIBLE);
        if (list.get(position).getKey().equals("订单状态")){
        if (statusCode.equals("COM")){
            holder.tc_acc_value.setTextColor(0xff00D57B);
        }else{
            holder.tc_acc_value.setTextColor(0xff4A90E2);

        }
        }else {
            holder.tc_acc_value.setTextColor(context.getResources().getColor(R.color.common_color_blank));
        }

        if (list.get(position).getKey().contains("收款二维码")){
            holder.iv_more.setVisibility(View.VISIBLE);
            holder.iv_more.setImageResource(R.mipmap.mine_accorder_qrcode_icon);
            holder.view_line.setVisibility(View.VISIBLE);
            holder.iv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null!=onClickListener){
                        onClickListener.onChildClick(position);
                    }
                }
            });
        }else {
            holder.iv_more.setVisibility(View.GONE);
            holder.view_line.setVisibility(View.GONE);
        }

        if (list.get(position).getKey().equals(context.getString(R.string.bds_actual_transfer))){
            holder.tc_acc_type.setTextColor(0xfffc5353);
            holder.tc_acc_value.setTextColor(0xfffc5353);

        }else {
            holder.tc_acc_type.setTextColor(0xff4d4d4d);
            holder.tc_acc_value.setTextColor(0xff4d4d4d);
        }

        holder.tc_acc_type.setText(list.get(position).getKey());
        holder.tc_acc_value.setText(list.get(position).getValue());

        return convertView;

    }



    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tc_acc_type,tc_acc_value;
        View view_line;
        ImageView iv_more;
    }


    MyOnClickListener onClickListener;

    public void setOnClickListener(MyOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface MyOnClickListener{
        void onChildClick(int posistion);
    }
}
