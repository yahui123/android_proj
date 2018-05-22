package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.AccDetailsModel;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/22.
 */

public class TransferDetailsAdapter extends BaseAdapter {

    private Context context;
    private List<AccDetailsModel> data = new ArrayList<AccDetailsModel>();

    public TransferDetailsAdapter(Context context, List<AccDetailsModel> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<AccDetailsModel> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        if (null!=data)
            return data.size();
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView==null)
        {
            convertView = View.inflate(context, R.layout.transfer_details_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (data.get(position).getKey().equals("交易ID") || data.get(position).getKey().equals("备注 ")){
            viewHolder.tv_value.setVisibility(View.GONE);
            viewHolder.tv_value1.setVisibility(View.VISIBLE);
            viewHolder.tv_value1.setText(data.get(position).getValue());
        }else{
            viewHolder.tv_value1.setVisibility(View.GONE);
            viewHolder.tv_value.setVisibility(View.VISIBLE);

            if (data.get(position).getKey().equals("金额")){
                if(data.get(2).getValue().equalsIgnoreCase("BTC") || data.get(2).getValue().equalsIgnoreCase("ETH") || data.get(2).getValue().equalsIgnoreCase("LTC")){
                    viewHolder.tv_value.setText(data.get(position).getValue());
                }else {
                    viewHolder.tv_value.setText(NumberUtils.formatNumber5(data.get(position).getValue()));
                }
            }else {
                viewHolder.tv_value.setText(data.get(position).getValue());
            }
        }

        if (data.get(position).getKey().equals("币种") || data.get(position).getKey().equals("来自于") || data.get(position).getKey().equals("去向")){
            viewHolder.tv_value.setTextColor(0xff00A2F2);
        }else {
            viewHolder.tv_value.setTextColor(0xff4d4d4d);
        }


        viewHolder.tv_key.setText(data.get(position).getKey());

        return convertView;
    }

    class ViewHolder {

        @BindView(R.id.tv_key)
        TextView tv_key;

        @BindView(R.id.tv_value1)
        TextView tv_value1;

        @BindView(R.id.tv_value)
        TextView tv_value;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
