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
import com.tang.trade.tang.net.acceptormodel.ChongZhiOrTiXianModel;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.List;

/**
 * Created by Administrator on 2017/12/12.
 */

public class AccesstanceReAdapter extends BaseAdapter {
    List<AccTranResponseModel.DataBean> listDate;
    Context context;
    final int yellow7ED321=0xff7ED321;
    final int red_doo=0xffD0021B;

    public AccesstanceReAdapter(List<AccTranResponseModel.DataBean> listDate, Context context) {
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

        if (null==view){
            view = LayoutInflater.from(context).inflate( R.layout.item_list_acc_re_trsns, null);
            viewHolder = new ViewHolder();
            viewHolder.tvVolume=view.findViewById(R.id.tvVolume);
            viewHolder.tvAceptentId=view.findViewById(R.id.tvAceptentId);
            viewHolder.tvStatus=view.findViewById(R.id.tvStatus);
            viewHolder.tvTime=view.findViewById(R.id.tvTime);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        AccTranResponseModel.DataBean dataBean=listDate.get(i);
        viewHolder.tvVolume.setText(dataBean.getAmount()+" "+dataBean.getSymbol());
        viewHolder.tvAceptentId.setText(dataBean.getMemberBdsAccount()+"");
        viewHolder.tvStatus.setText(dataBean.getStatus());
        viewHolder.tvTime.setText(dataBean.getCreateDate());
        String statusCode=dataBean.getStatusCode();

        if (statusCode.equals("COM")){
            viewHolder.tvStatus.setTextColor(0xff00D57B);
        }else{
            viewHolder.tvStatus.setTextColor(0xff4A90E2);

        }

        return view;
    }
    static class ViewHolder {
        TextView tvVolume,tvAceptentId,tvStatus,tvTime;
    }
}
