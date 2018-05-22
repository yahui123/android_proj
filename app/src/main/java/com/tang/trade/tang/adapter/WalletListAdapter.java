package com.tang.trade.tang.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.BlockResponModel;

import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class WalletListAdapter extends BaseAdapter {

    List<BlockResponModel.DataBean> data= null;

    public WalletListAdapter(List<BlockResponModel.DataBean> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.listview_wallet_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tvBlockId = (TextView) convertView.findViewById(R.id.blockid);
            viewHolder.tvGenerateTime = (TextView) convertView.findViewById(R.id.generatetime);
            viewHolder.tvTransactions = (TextView) convertView.findViewById(R.id.transactions);
            viewHolder.tvWitnesses = (TextView) convertView.findViewById(R.id.witnesses);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvWitnesses.setText(data.get(position).getWitness());
        viewHolder.tvTransactions.setText(data.get(position).getExchangecount());
        viewHolder.tvBlockId.setText(data.get(position).getBlocknumber());
        viewHolder.tvGenerateTime.setText(data.get(position).getTime());

        return convertView;
    }

    static class ViewHolder {
        TextView tvBlockId,tvGenerateTime,tvWitnesses,tvTransactions;
    }
}
