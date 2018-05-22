package com.tang.trade.tang.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.acceptormodel.CenterWlletModel;
import com.tang.trade.tang.net.model.AsssetsModel;
import com.tang.trade.tang.net.model.ExchangeResponseModel;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.utils.CalculateUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import de.bitsharesmunich.graphenej.Chains;

/**
 * Created by Administrator on 2018/1/2.
 */

public class MyAssetsAdapter extends BaseAdapter {
    private Context context;
    ArrayList<AsssetsModel> listDate=new ArrayList<>();
    DecimalFormat decimalFormat;
    private String baseSymbol;

//
    public MyAssetsAdapter( ArrayList<AsssetsModel> listDate,String baseSymbol ,Context context) {
        this.listDate=listDate;
        this.context = context;
        this.baseSymbol=baseSymbol;
        decimalFormat = new DecimalFormat("#0.00000");
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
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.my_assets_item, null);
            holder = new ViewHolder();
            holder.tv_coin_type = (TextView) convertView.findViewById(R.id.tv_coinType);
            holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
            holder.iv_coin = (ImageView) convertView.findViewById(R.id.iv_coin);
            holder.tv_bds = (TextView) convertView.findViewById(R.id.tv_bds);
            holder.tv_zhehe = (TextView) convertView.findViewById(R.id.tv_zhehe);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
         AsssetsModel dataBean =listDate.get(i);
        holder.tv_coin_type.setText(dataBean.getSymbol());
        if (dataBean.getSymbol().equals(baseSymbol)){
            holder.tv_count.setText(CalculateUtils.div(Double.parseDouble(dataBean.getAmount()),1,Integer.parseInt(dataBean.getPrecision())));
            holder.tv_zhehe.setText(context.getString(R.string.bds_convert_into)+" "+baseSymbol+":"+decimalFormat.format(Double.parseDouble(dataBean.getAmount())));
        }else {
            holder.tv_count.setText(CalculateUtils.div(Double.parseDouble(dataBean.getAmount()),1,Integer.parseInt(dataBean.getPrecision())));
            String str="0";
            if (!TextUtils.isEmpty(dataBean.getAmount())&&!TextUtils.isEmpty(dataBean.getLatest())){
                str=CalculateUtils.mul(Double.parseDouble(dataBean.getAmount()),new BigDecimal(dataBean.getLatest()).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            holder.tv_zhehe.setText(context.getString(R.string.bds_convert_into)+" "+baseSymbol+":"+decimalFormat.format(Double.parseDouble(str)));
        }

        if (dataBean.getSymbol().equals("CNY")|dataBean.getSymbol().equals("USD")){
            holder.tv_bds.setVisibility(View.VISIBLE);
        }else {
            holder.tv_bds.setVisibility(View.GONE);

        }

        Glide.with(context).load(TangConstant.COINTYPE+dataBean.getSymbol()+".png").error(R.mipmap.broaderless).into(holder.iv_coin);

        return convertView;
    }

    static class ViewHolder {
        TextView tv_coin_type, tv_count,tv_bds,tv_zhehe;
        ImageView iv_coin;
      
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private onOnButtonViewLister onViewLister;

    public void setOnItemViewLister(onOnButtonViewLister onViewLister){
        this.onViewLister=onViewLister;
    }
    public interface onOnButtonViewLister {
        public void onChongzhiLister(int position);
        public void onTiXianLister(int position);
        public void onBIllLister(int position);
    }
}
