package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.MyBlockInfoResponseModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/22.
 */

public class MyBlockInfoAdapter extends BaseAdapter {

    private Context context;
    private List<MyBlockInfoResponseModel.DataBean> data = new ArrayList<MyBlockInfoResponseModel.DataBean>();

    public MyBlockInfoAdapter(Context context, List<MyBlockInfoResponseModel.DataBean> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<MyBlockInfoResponseModel.DataBean> data) {
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
            convertView = View.inflate(context, R.layout.block_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_name.setText(data.get(position).getName());
        viewHolder.tv_info.setText(data.get(position).getInfo());
        viewHolder.tv_time.setText(data.get(position).getTime());

        return convertView;
    }

    static class ViewHolder {

        @BindView(R.id.tv_name)
        TextView tv_name;

        @BindView(R.id.tv_info)
        TextView tv_info;


        @BindView(R.id.tv_time)
        TextView tv_time;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
