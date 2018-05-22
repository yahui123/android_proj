package com.tang.trade.tang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.acceptormodel.NumberOrderModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/12/29.
 */

public class NumberOrderAdapter extends BaseAdapter {
    private List<NumberOrderModel.DataBean> listDate=null;
    private Context context;
    private String type;
    private String TYPE_CI="CI";
    private String TYPE_CO="CO";

    public NumberOrderAdapter(List<NumberOrderModel.DataBean> listDate, Context context,String type) {
        this.listDate = listDate;
        this.context = context;
        this.type=type;
    }

    @Override
    public int getCount() {
        return listDate.size();
    }

    @Override
    public Object getItem(int i) {
        return listDate.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if (view == null) {
            holder=new ViewHolder();
            view = View.inflate(context, R.layout.number_order_item, null);
            holder.tv_symbol=view.findViewById(R.id.tv_symbol);
            holder.tv_number=view.findViewById(R.id.tv_number);
            holder.tv_fee=view.findViewById(R.id.tv_fee);
            holder.tv_status=view.findViewById(R.id.tv_status);
            holder.tv_address=view.findViewById(R.id.tv_address);
            holder.tv_address2=view.findViewById(R.id.tv_address2);
            holder.tv_time=view.findViewById(R.id.tv_time);
            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }
        NumberOrderModel.DataBean dataBean=listDate.get(i);
        holder.tv_symbol.setText(dataBean.getSymbol());
        holder.tv_number.setText(dataBean.getAmount());
        holder.tv_fee.setText(dataBean.getFee());
        if (dataBean.getStatus().equals("COM")){
            holder.tv_status.setText("完成");
        }else {
            holder.tv_status.setText("未完成");
        }
        holder.tv_time.setText(chengeTime(dataBean.getCreateDate()));
        if (!TextUtils.isEmpty(dataBean.getAddress())){
            holder.tv_address.setText(dataBean.getAddress());
            holder.tv_address.setVisibility(View.VISIBLE);
            holder.tv_address2.setVisibility(View.VISIBLE);
        }else {

            holder.tv_address.setVisibility(View.GONE);
            holder.tv_address2.setVisibility(View.GONE);
        }


        return view;
    }
    static class ViewHolder {
        TextView tv_symbol, tv_number, tv_address,tv_address2,tv_time,tv_fee,tv_status;

    }
    public  String chengeTime(String time) {
        if (time == null || time.isEmpty() || time.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        Date date1 = null;
        String s = null;
        try {
            date1 = sdf.parse(time);
            s = sdf1.format(date1);
        } catch (ParseException e) {

        }
        return s;
    }
}
