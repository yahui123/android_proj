package com.tang.trade.tang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.acceptormodel.AccTranResponseModel;
import com.tang.trade.tang.net.acceptormodel.AcceptorResponseModel;

import java.util.List;

/**
 * Created by Administrator on 2017/12/8.
 */

public class TransFraAdapter extends BaseAdapter {
    List<AccTranResponseModel.DataBean> listDate;
    Context context;

    public TransFraAdapter(List<AccTranResponseModel.DataBean> listDate, Context context) {
        this.listDate = listDate;
        this.context = context;
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
        ViewHolder viewHolder=null;
        AccTranResponseModel.DataBean dataBean=listDate.get(i);

        if (null==view){
            view = LayoutInflater.from(context).inflate( R.layout.item_list_fra_trsns, null);
            viewHolder = new ViewHolder();
            viewHolder.tvVolume=view.findViewById(R.id.tvVolume);
            viewHolder.tvAceptentId=view.findViewById(R.id.tvAceptentId);
            viewHolder.tvStatus=view.findViewById(R.id.tvStatus);
            viewHolder.tvButton=view.findViewById(R.id.tvButton);
            viewHolder.tvTime=view.findViewById(R.id.tvTime);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();

        }
        viewHolder.tvVolume.setText(dataBean.getAmount()+" "+dataBean.getSymbol());
        viewHolder.tvAceptentId.setText(dataBean.getAcceptantBdsAccount());
        viewHolder.tvButton.setText(dataBean.getOrderType());
        viewHolder.tvTime.setText(dataBean.getCreateDate());
        viewHolder.tvStatus.setText(dataBean.getStatus());
        if (!TextUtils.isEmpty(dataBean.getAcceptantBdsAccount()))
        viewHolder.tvAceptentId.setText(dataBean.getAcceptantBdsAccount());
        String statusCode=dataBean.getStatusCode();

        if (statusCode.equals("COM")){
            viewHolder.tvStatus.setTextColor(0xff00D57B);
        }else{
            viewHolder.tvStatus.setTextColor(0xff4A90E2);

        }
        return view;

    }
    static class ViewHolder {
        private TextView tvVolume,tvAceptentId,tvStatus,tvButton;
        TextView tvTime;
    }
}
