package com.tang.trade.tang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.chain.vesting_balance_object;

import java.util.List;

/**
 * Created by Administrator on 2018/1/25.
 */

public class PoolAdapter  extends BaseAdapter {

    List<vesting_balance_object> list=null;
    private String fee="0.00000";

    public PoolAdapter(List<vesting_balance_object> list, Context context) {
        this.list = list;
        this.context = context;
    }
    public void setList(List<vesting_balance_object> list) {
        this.list = list;
    }

    public void setFee(String fee){
        this.fee=fee;
    }

    private Context context;


    @Override
    public int getCount() {
        if (list==null){
            return 0;
        }

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
            convertView = View.inflate(parent.getContext(), R.layout.pool_item_list, null);
            holder = new ViewHolder();
            holder.tv_fanxian = (TextView) convertView.findViewById(R.id.tv_fanxian);
            holder.tv_wangcheng_bitian = (TextView) convertView.findViewById(R.id.tv_wangcheng_bitian);
            holder.tv_yaoqiu_bitian = (TextView) convertView.findViewById(R.id.tv_yaoqiu_bitian);
            holder.tv_jiedong_tianshu = (TextView) convertView.findViewById(R.id.tv_jiedong_tianshu);
            holder.tv_amount = (TextView) convertView.findViewById(R.id.tv_amount);
            holder.tv_fee = (TextView) convertView.findViewById(R.id.tv_fee);
            holder.bnt_item = (Button) convertView.findViewById(R.id.bnt_item);
            holder.tv_id=convertView.findViewById(R.id.tv_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bnt_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewLister.onBtnOnklick(position);
            }
        });
        vesting_balance_object object=list.get(position);
        if (!TextUtils.isEmpty(object.TotalBalance)){
            holder.tv_fanxian.setText(object.TotalBalance+" BDS");
        }else {
            holder.tv_fanxian.setText("0.00000 BDS");
        }

        if (!TextUtils.isEmpty(object.coin_days_earned)){
            holder.tv_wangcheng_bitian.setText(object.coin_days_earned+" "+context.getString(R.string.bds_bitian));
        }else {
            holder.tv_wangcheng_bitian.setText("0 "+context.getString(R.string.bds_bitian));
        }
        if (!TextUtils.isEmpty(object.coin_days_required)){
            holder.tv_yaoqiu_bitian.setText(object.coin_days_required+" "+context.getString(R.string.bds_bitian));
        }else {
            holder.tv_yaoqiu_bitian.setText("0 "+context.getString(R.string.bds_bitian));
        }

        if (!TextUtils.isEmpty(object.vesting_period)){
            holder.tv_jiedong_tianshu.setText(object.vesting_period+" "+context.getString(R.string.bds_days));
        }else {
            holder.tv_jiedong_tianshu.setText("0 "+context.getString(R.string.bds_days));
        }

        String str="0.0%";
        String str2="0.00000";
        if (!TextUtils.isEmpty(object.AvailablePersent)){
            str=(Double.parseDouble(object.AvailablePersent)*100)+"%";
        }
        if (!TextUtils.isEmpty(object.Available_to_claim)){
            str2=object.Available_to_claim;
        }
        holder.tv_amount.setText(str+"/"+str2+" BDS");





        holder.tv_fee.setText(fee+" BDS");
        holder.tv_id.setText(object.id);

        return convertView;
    }

    static class ViewHolder {
        TextView tv_id,tv_fanxian, tv_wangcheng_bitian, tv_yaoqiu_bitian, tv_jiedong_tianshu, tv_amount, tv_fee;
        Button bnt_item;
    }


    private onOnButtonViewLister onViewLister;

    public void setOnItemViewLister(onOnButtonViewLister onViewLister) {
        this.onViewLister = onViewLister;
    }

    public interface onOnButtonViewLister {
        public void onBtnOnklick(int position);

    }
}
