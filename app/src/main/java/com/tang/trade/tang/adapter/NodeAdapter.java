package com.tang.trade.tang.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.NodeResponseModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/22.
 */

public class NodeAdapter extends BaseAdapter {

    private Context context;
    private List<NodeResponseModel.DataBean> data = new ArrayList<NodeResponseModel.DataBean>();

    public NodeAdapter(Context context, List<NodeResponseModel.DataBean> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<NodeResponseModel.DataBean> data) {
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
            convertView = View.inflate(context, R.layout.item_node_layout, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (data.get(position) != null) {
            viewHolder.tv_name.setText(data.get(position).getName());

            if (data.get(position).getSelected().equalsIgnoreCase("1")) {
                viewHolder.iv_selected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_selected.setVisibility(View.INVISIBLE);
            }
//            if (!TextUtils.isEmpty(data.get(position).getSelected())) {
//                if (data.get(position).getSelected().equalsIgnoreCase("1")) {
//                    viewHolder.iv_selected.setVisibility(View.VISIBLE);
//                } else {
//                    viewHolder.iv_selected.setVisibility(View.INVISIBLE);
//                }
//            } else {
//                if (position == 0) {
//                    viewHolder.iv_selected.setVisibility(View.VISIBLE);
//                } else {
//                    viewHolder.iv_selected.setVisibility(View.INVISIBLE);
//                }
//
//            }


            if (!TextUtils.isEmpty(data.get(position).getSpeed())) {
                viewHolder.tv_sleep.setText(data.get(position).getSpeed() + "ms");
                if (Integer.parseInt(data.get(position).getSpeed()) <= 60) {
                    viewHolder.iv_status.setImageResource(R.mipmap.iv_green);
                    viewHolder.tv_sleep.setTextColor(0xff000000);
                    viewHolder.tv_name.setTextColor(0xff000000);
                } else if (Integer.parseInt(data.get(position).getSpeed()) > 60 && Integer.parseInt(data.get(position).getSpeed()) <= 150) {
                    viewHolder.iv_status.setImageResource(R.mipmap.iv_yellow);
                    viewHolder.tv_sleep.setTextColor(0xff000000);
                    viewHolder.tv_name.setTextColor(0xff000000);
                } else if (Integer.parseInt(data.get(position).getSpeed()) > 150 && Integer.parseInt(data.get(position).getSpeed()) <= 600) {
                    viewHolder.iv_status.setImageResource(R.mipmap.iv_red);
                    viewHolder.tv_sleep.setTextColor(0xff000000);
                    viewHolder.tv_name.setTextColor(0xff000000);
                } else {
                    viewHolder.iv_status.setImageResource(R.mipmap.iv_gray);
                    viewHolder.tv_sleep.setText("--");
                    viewHolder.tv_sleep.setTextColor(0xff898989);
                    viewHolder.tv_name.setTextColor(0xff898989);
                }

            } else {
                viewHolder.tv_sleep.setText("--");
                viewHolder.iv_status.setImageResource(R.mipmap.iv_gray);
                viewHolder.tv_sleep.setTextColor(0xff898989);
                viewHolder.tv_name.setTextColor(0xff898989);
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.iv_status)
        ImageView iv_status;

        @BindView(R.id.tv_name)
        TextView tv_name;

        @BindView(R.id.iv_selected)
        ImageView iv_selected;

        @BindView(R.id.tv_sleep)
        TextView tv_sleep;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
