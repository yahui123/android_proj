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

public class AcceptorAdapter extends BaseAdapter {

    List<String> list = null;

    public AcceptorAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    private Context context;

    public void setList(List<String> list) {
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
            convertView = View.inflate(parent.getContext(), R.layout.fragment_acceptor_item, null);

            holder = new ViewHolder();
            holder.tc_acc_type = convertView.findViewById(R.id.tc_acc_type);
            holder.view_line =  convertView.findViewById(R.id.view_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if ( position == 2 || list.get(position).contains("我的交易")){
            holder.view_line.setVisibility(View.VISIBLE);
        }else {
            holder.view_line.setVisibility(View.GONE);
        }

        holder.tc_acc_type.setText(list.get(position));

        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tc_acc_type;
        View view_line;
    }


}
