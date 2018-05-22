package com.tang.trade.tang.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.ReceptionRecordResponseModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/22.
 */

public class ReceptionAdapter extends BaseAdapter {

    private Context context;
    private List<ReceptionRecordResponseModel.NewObject> data = new ArrayList<ReceptionRecordResponseModel.NewObject>();

    public ReceptionAdapter(Context context, List<ReceptionRecordResponseModel.NewObject> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<ReceptionRecordResponseModel.NewObject> data) {
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
            convertView = View.inflate(context, R.layout.view_reception_lv_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(!TextUtils.isEmpty(data.get(position).getFrom()))
        {
            viewHolder.tv1.setText(data.get(position).getFrom());
        }

        if(!TextUtils.isEmpty(data.get(position).getAmount()))
        {
//            double amount = Double.parseDouble(data.get(position).getAmount());
//            BigDecimal big1 = new BigDecimal(amount);
//            BigDecimal big2 = new BigDecimal(100000);
//            BigDecimal shang = big1.divide(big2, 5, BigDecimal.ROUND_HALF_UP);//保留5位小数，返回bigDecimal
            viewHolder.tv2.setText(data.get(position).getAmount()+"");
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv1)
        TextView tv1;
        @BindView(R.id.tv2)
        TextView tv2;
        @BindView(R.id.iv1)
        ImageView iv1;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
