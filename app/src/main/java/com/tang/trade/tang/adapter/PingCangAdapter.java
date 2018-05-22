package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.RechargeResponseModel;

import java.util.List;

/**
 * Created by Administrator on 2017/12/20.
 */

public class PingCangAdapter extends BaseAdapter{

    List<RechargeResponseModel> list = null;
    private Context context;

    public List<RechargeResponseModel> getList() {
        return list;
    }

    public void setList(List<RechargeResponseModel> list) {
        this.list = list;
    }

    public PingCangAdapter(List<RechargeResponseModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;

        if (view == null) {
            view = View.inflate(context, R.layout.list_pignchang_item, null);

            holder = new ViewHolder();
            holder.tc_pingcang_type = view.findViewById(R.id.tc_pingcang_type);
            holder.tc_pingcang_value = view.findViewById(R.id.tc_pingcang_value);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tc_pingcang_type.setText(list.get(i).getKey());
        holder.tc_pingcang_value.setText(list.get(i).getValue());
        return view;
    }

    static class ViewHolder {
        TextView tc_pingcang_type,tc_pingcang_value;
    }
}
