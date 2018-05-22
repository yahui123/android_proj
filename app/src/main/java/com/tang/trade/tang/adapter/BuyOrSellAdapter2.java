package com.tang.trade.tang.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.BuyOrSellResponseModel;
import com.tang.trade.tang.utils.NumberUtils;

import java.text.DecimalFormat;
import java.util.List;

public class BuyOrSellAdapter2 extends BaseAdapter {

    private List<BuyOrSellResponseModel.DataBean> list = null;
    private String mScale;

    private double count;

    public BuyOrSellAdapter2(List<BuyOrSellResponseModel.DataBean> list, String scale) {
        this.list = list;
        this.mScale = scale;
    }

    public void setList(List<BuyOrSellResponseModel.DataBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setCount(double count) {
        this.count = count;
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
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.fragment_buy_item2, null);

            holder = new ViewHolder();
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            holder.tv_amount = convertView.findViewById(R.id.tv_amount);
            holder.view_bg = convertView.findViewById(R.id.view_bg);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position + 1 < list.size()) {
            if (list.get(position + 1).getType().equals("Buy1")
                    || list.get(position + 1).getType().equals("买1")
                    || list.get(position + 1).getType().equals("賣")) {
//                holder.v.setVisibility(View.VISIBLE);
            } else {
//                holder.v.setVisibility(View.GONE);
            }
        } else {
//            holder.v.setVisibility(View.GONE);
        }

        holder.tv_price.setText(NumberUtils.formatNumber5(list.get(position).getPrice()));
        holder.tv_amount.setText(NumberUtils.formatNumber(list.get(position).getAmount(), new DecimalFormat(mScale)));


        WindowManager wm = (WindowManager) parent.getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        ViewGroup.LayoutParams layoutParams = holder.view_bg.getLayoutParams();
        double f;
        if (count != 0) {
            try {
//                f = Double.parseDouble(CalculateUtils.div(Double.parseDouble(list.get(position).getAmount()),count,5));
                f = Double.parseDouble(list.get(position).getAmount()) / count;
            } catch (Exception e) {
                f = 0;
            }
        } else {
            f = 0;
        }

//        int w = (int) CalculateUtils.rechange_sub(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(CalculateUtils.div(Double.parseDouble(width+""),2,5)),f)),0);
        int w = (int) (width / 2 * f);

        layoutParams.width = w;
        holder.view_bg.setLayoutParams(layoutParams);
        holder.view_bg.setVisibility(View.VISIBLE);


//        int w = (int) CalculateUtils.rechange_sub(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(CalculateUtils.div(Double.parseDouble(width+""),2,5)),f)),0);
//
//        if (w > 0){
//            holder.view_bg.setVisibility(View.VISIBLE);
//            performAnim(holder.view_bg,layoutParams.width,w);
//        }else {
//            holder.view_bg.setVisibility(View.GONE);
//        }
//        Log.i("buy",w+"  "+f+"  "+list.get(position).getAmount()+" /  "+count);
//        Log.i("sell",w+"  "+f+"  "+list.get(position).getAmount()+" /  "+count);
        if (list.get(position).getType().contains("Buy") || list.get(position).getType().contains("买") || list.get(position).getType().contains("買")) {
            holder.view_bg.setBackgroundColor(0xFFFFD6DB);

        } else {
            holder.view_bg.setBackgroundColor(0xFFA1FFD7);

        }


        holder.tv_price.setTextColor(list.get(position).getType().contains("Buy")
                || list.get(position).getType().contains("买")
                || list.get(position).getType().contains("買")
                ? Color.parseColor("#FF2929")
                : Color.parseColor("#37B569"));

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    private void performAnim(final View view, int from, int width) {

        //属性动画对象
        ValueAnimator va;
        //显示view，高度从0变到height值
        va = ValueAnimator.ofInt(from, width);

        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h = (Integer) valueAnimator.getAnimatedValue();
                //动态更新view的高度
                view.getLayoutParams().width = h;
                view.requestLayout();
            }
        });
        va.setDuration(1000);
        //开始动画
        va.start();
    }

    static class ViewHolder {
        TextView tv_price, tv_amount;
        //        View v;
        View view_bg;
    }
}
