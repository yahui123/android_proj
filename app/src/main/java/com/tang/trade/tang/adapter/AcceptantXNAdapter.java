package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.acceptormodel.CenterWlletModel;
import com.tang.trade.tang.net.model.AcceptorListModel;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/8.
 */

public class AcceptantXNAdapter extends BaseAdapter {
    ArrayList<CenterWlletModel.DataBean> listDate;
    Context context;

    public AcceptantXNAdapter(ArrayList<CenterWlletModel.DataBean> listDate, Context context) {
        this.listDate = listDate;
        this.context = context;
    }

    public void setListDate(ArrayList<CenterWlletModel.DataBean> listDate) {
        this.listDate = listDate;
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
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if (null == view) {
            view = View.inflate(context, R.layout.item_list_xn_acceptant, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_acceptantNama = view.findViewById(R.id.tv_acceptantNama);
            viewHolder.btn_chongzhi = view.findViewById(R.id.btn_chongzhi);
            viewHolder.btn_tixian = view.findViewById(R.id.btn_tixian);
            viewHolder.iv_currency = view.findViewById(R.id.iv_currency);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        CenterWlletModel.DataBean dataBean = listDate.get(i);
        viewHolder.tv_acceptantNama.setText(dataBean.getType());
        viewHolder.btn_chongzhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewLister.onChongzhiLister(i);

            }
        });
        viewHolder.btn_tixian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewLister.onTiXianLister(i);

            }
        });

        Glide.with(context).load(TangConstant.COINTYPE + dataBean.getType() + ".png").error(R.mipmap.broaderless).into(viewHolder.iv_currency);


        return view;
    }

    static class ViewHolder {
        private TextView tv_acceptantNama;
        private LinearLayout btn_chongzhi, btn_tixian;
        private ImageView iv_currency;
    }

    private onOnButtonViewLister onViewLister;

    public void setOnItemViewLister(onOnButtonViewLister onViewLister) {
        this.onViewLister = onViewLister;
    }

    public interface onOnButtonViewLister {
        public void onChongzhiLister(int position);

        public void onTiXianLister(int position);

    }


}
