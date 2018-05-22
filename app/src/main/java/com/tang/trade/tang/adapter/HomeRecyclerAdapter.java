package com.tang.trade.tang.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.HistoryResponseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dagou on 2017/9/22.
 */

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {


    List<HistoryResponseModel.DataBean> arrayList = null;


    public HomeRecyclerAdapter(List arrayList) {
        this.arrayList = arrayList;
    }

    public void notifies(List arrayList) {
        arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;

        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_home_recyclerview_item, parent, false);
        viewHolder = new ViewHolder(item);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private TextView tv_action;
        private TextView tv_amount;
        private ImageView iv_kind;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_action = (TextView) itemView.findViewById(R.id.tv_action);
            tv_amount = (TextView) itemView.findViewById(R.id.tv_amount);
            iv_kind = (ImageView) itemView.findViewById(R.id.iv_kind);

        }
    }

}
