package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.RecordResponseModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class RecordAdapter extends BaseAdapter {

    List<RecordResponseModel.DataBean> list = null;
    private Context context;

    private String index;

    public void setIndex(String index) {
        this.index = index;
    }

    public RecordAdapter(List<RecordResponseModel.DataBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<RecordResponseModel.DataBean> list) {
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
            convertView = View.inflate(parent.getContext(), R.layout.fragment_record_item, null);

            holder = new ViewHolder();
            holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            holder.tv_user = (TextView) convertView.findViewById(R.id.tv_user);
            holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.000");
;
        holder.tv_user.setText(list.get(position).getAcceptanceID());
        holder.tv_count.setText(decimalFormat.format(Double.parseDouble(list.get(position).getCount())));
        if (list.get(position).getStatus().equalsIgnoreCase("0")){
            holder.tv_status.setText(context.getString(R.string.bds_unactivation));
            holder.tv_status.setTextColor(0xff828282);
        }else if (list.get(position).getStatus().equalsIgnoreCase("1")){
            if (index.equals("0")){
                holder.tv_status.setText(context.getString(R.string.bds_PendingPayment));
            }else if (index.equals("1")){
                holder.tv_status.setText(context.getString(R.string.bds_WaitingTransfer));
            }

            holder.tv_status.setTextColor(0xffff0000);
        }else if (list.get(position).getStatus().equalsIgnoreCase("2")){
            if (index.equals("0")){
                holder.tv_status.setText(context.getString(R.string.bds_ConfirmedPayment));
            }else if (index.equals("1")){
                holder.tv_status.setText(context.getString(R.string.bds_WaitingTransfer));
            }
            holder.tv_status.setTextColor(0xff419BF9);
        }else if (list.get(position).getStatus().equalsIgnoreCase("3")){
            holder.tv_status.setText(context.getString(R.string.bds_Complete));
            holder.tv_status.setTextColor(0xff3EE525);
        }else if (list.get(position).getStatus().equalsIgnoreCase("4")){
            holder.tv_status.setText(context.getString(R.string.bds_Membership));
            holder.tv_status.setTextColor(0xff828282);
        }

        String date = list.get(position).getCreateDate();
        String [] date1 = date.split(" ");
        holder.tv_time.setText(date1[0]+"\n"+date1[1]);
        holder.tv_type.setText(list.get(position).getType());


        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_type, tv_count, tv_user,tv_time,tv_status;
    }


}
