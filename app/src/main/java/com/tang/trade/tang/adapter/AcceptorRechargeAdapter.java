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

public class AcceptorRechargeAdapter extends BaseAdapter {

    List<RechargeResponseModel> list = null;
    private int feePhone=0;
    private TextView feeText;

    public AcceptorRechargeAdapter(List<RechargeResponseModel> list, Context context) {
        this.list = list;
        this.context = context;
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
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.fragment_acceptor_item, null);

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
        if ( position == 0 || list.get(position).getKey().equals(context.getString(R.string.bds_acceptor)) || list.get(position).getKey().contains(context.getString(R.string.bds_maximum_deposit)) || list.get(position).getKey().contains(context.getString(R.string.bds_contact_number)) ){
            holder.view_line.setVisibility(View.VISIBLE);
        }else {
            holder.view_line.setVisibility(View.GONE);
        }

        if (list.get(position).getKey().contains(context.getString(R.string.bds_Deposit))|list.get(position).getKey().contains(context.getString(R.string.bds_Withdraw_method))){//充值方式，提现方式
            holder.iv_more.setVisibility(View.VISIBLE);
            holder.iv_more.setImageResource(R.mipmap.common_enter_left);
        }else  if (list.get(position).getKey().contains(context.getString(R.string.bds_transaction_reference))){// 交易码
            holder.iv_more.setVisibility(View.VISIBLE);
            holder.iv_more.setImageResource(R.mipmap.accept_accept_transact_code);

        }else {
            holder.iv_more.setVisibility(View.GONE);
        }
        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener!=null){
                    onClickListener.onChildClick(position);
                }

            }
        });

        holder.tc_acc_type.setText(list.get(position).getKey());
        holder.tc_acc_value.setText(list.get(position).getValue());

        if (list.get(position).getKey().equals(context.getString(R.string.bds_GatewayServiceFee))){//网关服务费
            feePhone=position;
            feeText=holder.tc_acc_value;
        }

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

    public int  getFeePostion(){
        return feePhone;
    }





}
