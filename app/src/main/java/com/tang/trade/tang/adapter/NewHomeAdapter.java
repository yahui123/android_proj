package com.tang.trade.tang.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.ExchangeResponseModel;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.CountNumberView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by Administrator on 2018/1/2.
 */

public class NewHomeAdapter extends BaseAdapter {
    private static long ANIMATOR_TIME = 300L;
    DecimalFormat decimalFormat;
    DecimalFormat decimalFormatFive;
    private Context context;
    private List<ExchangeResponseModel.DataBean> mListData = new Vector<>();
    private List<ExchangeResponseModel.DataBean> mOldListData = new Vector<>();
    private ExchangeResponseModel.DataBean mBdsDataBean;

    public NewHomeAdapter(Context context, List<ExchangeResponseModel.DataBean> listDate) {
        this.context = context;
        this.mListData = listDate;
        this.context = context;
        this.decimalFormat = new DecimalFormat("#0.00");
        this.decimalFormatFive = new DecimalFormat("#0.00000");
    }

    public void setList(ArrayList<ExchangeResponseModel.DataBean> newDate) {
        this.mOldListData.clear();
        this.mOldListData.addAll(this.mListData);
        this.mListData.clear();
        this.mListData.addAll(newDate);
        for (ExchangeResponseModel.DataBean dataBean : mListData) {
            if (dataBean.getBase().equals("BDS") && dataBean.getQuote().equals("CNY")) {
                mBdsDataBean = dataBean;
                break;
            }
        }

        for (ExchangeResponseModel.DataBean dataBean : mListData) {
            dataBean.setRateToCNY(getRateForQuote2CNY(dataBean));
        }
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int i) {
        return mListData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        ExchangeResponseModel.DataBean dataBean = mListData.get(i);

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.home_new_assets_item, null);
            holder = new ViewHolder();
            holder.mViewMask = convertView.findViewById(R.id.view_mask);
            holder.mIvCoin = convertView.findViewById(R.id.iv_coin);
            holder.mTvCoinPrefix = convertView.findViewById(R.id.tv_coin_prefix);
            holder.mTvCoinName = convertView.findViewById(R.id.tv_coin_name);
            holder.mTv24hTurnover = convertView.findViewById(R.id.tv_24h_turnover);
            holder.mTvPrice = convertView.findViewById(R.id.tv_price);
            holder.mTvExchangeRate = convertView.findViewById(R.id.tv_exchange_rate);
            holder.mTvPercentage = convertView.findViewById(R.id.tv_percentage);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (mOldListData.size() > 0 && i < mOldListData.size()) {

            double oldDate = Double.parseDouble(mOldListData.get(i).getLatest());
            double date = Double.parseDouble(dataBean.getLatest());

            if (date < oldDate) {
                holder.mViewMask.setBackgroundColor(context.getResources().getColor(R.color.market_global_lower));
                ObjectAnimator anim = ObjectAnimator.ofFloat(holder.mViewMask, "alpha", 0f, 0.4f, 0.8f, 0.4f, 0f);
                anim.setDuration(ANIMATOR_TIME);// 动画持续时间
                anim.start();

                if (Double.parseDouble(dataBean.getLatest()) < 10d) {
                    holder.mTvPrice.showNumberWithAnimation1(Float.parseFloat(oldDate + ""), Float.parseFloat(date + ""), CountNumberView.ONEREGEX, "200", NumberUtils.formatNumber5(date + ""));
                } else {
                    holder.mTvPrice.showNumberWithAnimation1(Float.parseFloat(oldDate + ""), Float.parseFloat(date + ""), CountNumberView.FLOATREGEX, "200", NumberUtils.formatNumber2(date + ""));
                }
            } else if (date > oldDate) {
                holder.mViewMask.setBackgroundColor(context.getResources().getColor(R.color.market_global_upper));
                ObjectAnimator anim = ObjectAnimator.ofFloat(holder.mViewMask, "alpha", 0f, 0.4f, 0.8f, 0.4f, 0f);
                anim.setDuration(ANIMATOR_TIME);// 动画持续时间
                anim.start();

                if (Double.parseDouble(dataBean.getLatest()) < 10d) {
                    holder.mTvPrice.showNumberWithAnimation1(Float.parseFloat(oldDate + ""), Float.parseFloat(date + ""), CountNumberView.ONEREGEX, "200", NumberUtils.formatNumber5(date + ""));
                } else {
                    holder.mTvPrice.showNumberWithAnimation1(Float.parseFloat(oldDate + ""), Float.parseFloat(date + ""), CountNumberView.FLOATREGEX, "200", NumberUtils.formatNumber2(date + ""));
                }
            } else {

                if (Double.parseDouble(dataBean.getLatest()) < 10d) {
                    holder.mTvPrice.setText(NumberUtils.formatNumber5(dataBean.getLatest()));
                } else {
                    holder.mTvPrice.setText(NumberUtils.formatNumber2(dataBean.getLatest()));
                }
            }
        } else {
            if (Double.parseDouble(dataBean.getLatest()) < 10d) {
                holder.mTvPrice.showNumberWithAnimation1(0.00000f, Float.parseFloat(NumberUtils.formatNumber2(dataBean.getLatest())), CountNumberView.ONEREGEX, "200", NumberUtils.formatNumber5(dataBean.getLatest()));
            } else {
                holder.mTvPrice.showNumberWithAnimation1(0.00f, Float.parseFloat(NumberUtils.formatNumber5(dataBean.getLatest())), CountNumberView.FLOATREGEX, "200", NumberUtils.formatNumber2(dataBean.getLatest()));
            }
        }

        //holder.tv_count.setText(decimalFormat.format(Double.parseDouble(listDate.get(i).getLatest())));
//        holder.tv_type.setText(" " + listDate.get(i).getBase());
        if (Double.parseDouble(mListData.get(i).getPer()) >= 0) {
            holder.mTvPercentage.setText("+" + NumberUtils.formatNumber2(dataBean.getPer()) + "%");
            holder.mTvPercentage.setBackground(context.getResources().getDrawable(R.drawable.bg_market_upper));
            holder.mTvPrice.setTextColor(context.getResources().getColor(R.color.market_global_upper));
        } else {
            holder.mTvPercentage.setText(NumberUtils.formatNumber2(dataBean.getPer()) + "%");
            holder.mTvPercentage.setBackground(context.getResources().getDrawable(R.drawable.bg_market_lower));
            holder.mTvPrice.setTextColor(context.getResources().getColor(R.color.market_global_lower));
        }
        if (dataBean.getQuote().equals("CNY") | dataBean.getQuote().equals("USD")) {
            holder.mTvCoinPrefix.setVisibility(View.VISIBLE);
        } else {
            holder.mTvCoinPrefix.setVisibility(View.GONE);
        }

//        int turnover = dataBean.getQuote_volume() == null ? 0 : (int) (Double.parseDouble(dataBean.getQuote_volume()) / 10000);
        String turnover = "0";

        if (TextUtils.isEmpty(mListData.get(i).getQuote_volume())) {
            turnover = "0";
        } else {

            if ((int) (Double.parseDouble(mListData.get(i).getQuote_volume())) < 10000) {
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                turnover = decimalFormat.format(Double.parseDouble(mListData.get(i).getQuote_volume()));
            } else if ((int) (Double.parseDouble(mListData.get(i).getQuote_volume())) > 100000000) {
                int s = (int) (Double.parseDouble(mListData.get(i).getQuote_volume())) / 100000000;
                DecimalFormat decimalFormat = new DecimalFormat("#0.0");
                turnover = decimalFormat.format(Double.valueOf(s));
            } else {
                int s = (int) (Double.parseDouble(mListData.get(i).getQuote_volume())) / 10000;
                DecimalFormat decimalFormat = new DecimalFormat("#0.0");
                turnover = decimalFormat.format(Double.valueOf(s));
            }
        }

        holder.mTv24hTurnover.setText(String.format(Locale.CHINA, "24H量%s万", turnover));

        holder.mTvCoinName.setText(dataBean.getQuote());

        if (dataBean.getBase().equals("BDS")) {
            holder.mTvExchangeRate.setVisibility(View.VISIBLE);

            if (Double.parseDouble(dataBean.getRateToCNY()) < 10d) {
                holder.mTvExchangeRate.setText(String.format("≈ %s CNY", NumberUtils.formatNumber5(dataBean.getRateToCNY())));
            } else {
                holder.mTvExchangeRate.setText(String.format("≈ %s CNY", NumberUtils.formatNumber2(dataBean.getRateToCNY())));
            }

        } else {

            holder.mTvExchangeRate.setVisibility(View.GONE);
        }

        Glide.with(context).load(TangConstant.COINTYPE + dataBean.getQuote() + ".png").error(R.mipmap.broaderless).into(holder.mIvCoin);

        return convertView;
    }

    private String getRateForQuote2CNY(ExchangeResponseModel.DataBean dataBean) {
        if (null == mBdsDataBean) {
            return "";
        }

        return NumberUtils.formatNumber5(String.valueOf(1 / Double.parseDouble(mBdsDataBean.getLatest()) * Double.parseDouble(dataBean.getLatest())));
    }

    static class ViewHolder {
        View mViewMask;
        ImageView mIvCoin;
        TextView mTvCoinPrefix;
        TextView mTvCoinName;
        TextView mTv24hTurnover;
        CountNumberView mTvPrice;
        TextView mTvExchangeRate;
        TextView mTvPercentage;
    }
}
