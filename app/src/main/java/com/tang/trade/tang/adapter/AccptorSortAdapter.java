package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.acceptormodel.AccoptorSortModel;

import java.util.List;

/**
 * Created by Administrator on 2018/1/10.
 */

public class AccptorSortAdapter extends BaseAdapter {
    List<AccoptorSortModel.DataBean> list;
    Context context;

    public AccptorSortAdapter(List<AccoptorSortModel.DataBean> list,Context context) {
        this.list = list;
        this.context=context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHoder hoder=null;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.sort_item, null);
            hoder=new ViewHoder();
            hoder.tv_sort=convertView.findViewById(R.id.tv_sort);
            hoder.iv_icon=convertView.findViewById(R.id.iv_icon);
            hoder.line_view=convertView.findViewById(R.id.line_view);
            convertView.setTag(hoder);
        }else {
            hoder= (ViewHoder) convertView.getTag();

        }
        hoder.line_view.setVisibility(View.VISIBLE);
        hoder.tv_sort.setText(list.get(position).getName());
        Glide.with(context).load(list.get(position).getPic()).error(R.mipmap.iv_changzhi_fee_icon).into(hoder.iv_icon);
//        if (position==0){
//            hoder.iv_icon.setImageResource(R.mipmap.iv_changzhi_fee_icon);
//        }else if (position==1) {
//            hoder.iv_icon.setImageResource(R.mipmap.iv_tixian_icon);
//        }else if (position==2){
//            hoder.iv_icon.setImageResource(R.mipmap.iv_haoping_pw_icon);
//        }else
//
        if (position==list.size()-1){
            hoder.line_view.setVisibility(View.GONE);
        }


        return convertView;
    }
    static class ViewHoder{
        TextView tv_sort;
        ImageView iv_icon;
        View line_view;
    }
}
