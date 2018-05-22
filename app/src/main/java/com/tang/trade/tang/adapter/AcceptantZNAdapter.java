package com.tang.trade.tang.adapter;

import android.content.Context;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;

import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.BlurringView;
import com.tang.trade.tang.widget.MyRanking;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/8.
 */

public class AcceptantZNAdapter extends BaseAdapter {
    ArrayList<AcceptotXiangqingModel.DataBean> listDate;
    Context context;


    public AcceptantZNAdapter(ArrayList<AcceptotXiangqingModel.DataBean> listDate, Context context) {
        this.listDate = listDate;
        this.context = context;
    }

    public void updateListDate(ArrayList<AcceptotXiangqingModel.DataBean> listDate) {
        this.listDate = listDate;
    }

    public void appendListDate(ArrayList<AcceptotXiangqingModel.DataBean> listDate) {
        this.listDate.addAll(listDate);
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_acceptant, null);
            viewHolder = new ViewHolder();
            viewHolder.iv_huihua = view.findViewById(R.id.iv_huihua);
            viewHolder.tv_acceptantNama = view.findViewById(R.id.tv_acceptantNama);

            viewHolder.tvTotalVolume = view.findViewById(R.id.tvTotalVolume);
            viewHolder.tvRechargeAmount = view.findViewById(R.id.tvRechargeAmount);
            viewHolder.tvRechargeRata = view.findViewById(R.id.tvRechargeRata);

            viewHolder.tvTranspens = view.findViewById(R.id.tvTranspens);

            viewHolder.tvWithdrawalAmount = view.findViewById(R.id.tvWithdrawalAmount);
            viewHolder.tvWithdrawalRate = view.findViewById(R.id.tvWithdrawalRate);
            viewHolder.line_zaixian = view.findViewById(R.id.line_item);
            viewHolder.myRanking = view.findViewById(R.id.myRanking);
            viewHolder.line_myRanking = view.findViewById(R.id.line_myRanking);
            viewHolder.tv_introduce = view.findViewById(R.id.tv_introduce);
            viewHolder.id_name_time = view.findViewById(R.id.id_name_time);
            viewHolder.line_buZaiXian = view.findViewById(R.id.line_buZaiXian);
            viewHolder.line_bottom = view.findViewById(R.id.line_bottom);
            viewHolder.botLine = view.findViewById(R.id.bot_line);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (listDate.size() - 1 == i) {
            viewHolder.botLine.setVisibility(View.VISIBLE);
        } else {
            viewHolder.botLine.setVisibility(View.GONE);
        }

        AcceptotXiangqingModel.DataBean dataBean = listDate.get(i);

        if (dataBean.getIsOnline().equals("0")) {//不在线
            viewHolder.line_buZaiXian.setVisibility(View.VISIBLE);
            viewHolder.line_zaixian.setVisibility(View.GONE);
            viewHolder.id_name_time.setText(dataBean.getAcceptantBdsAccount() + context.getString(R.string.bds_yingye_time) + "： " + dataBean.getOnlineStartTime() + "~" + dataBean.getOnlineEndTime());

        } else {
            viewHolder.line_buZaiXian.setVisibility(View.GONE);
            viewHolder.line_zaixian.setVisibility(View.VISIBLE);

            viewHolder.tv_acceptantNama.setText(dataBean.getAcceptantBdsAccount());
            viewHolder.tvTranspens.setText(dataBean.getDealCount());
            if (!TextUtils.isEmpty(dataBean.getDealAmount())) {
                String volme = dataBean.getDealAmount();
                viewHolder.tvTotalVolume.setText(CalculateUtils.round(volme) + "");
            } else {
                viewHolder.tvTotalVolume.setText("0.00");
            }


            double cashInLowerLimit = 0;//充值下限
            double cashOutLowerLimit = 0;//提现下限
            double cashInLowerUp = 0;//充值上限
            double cashOutLowerUp = 0;//提现上限
            double acceptLimit = 0;//承兑额度
            double accountBalance = 0;//账户余额accountBalance
//            double cashInUncompletedAmount=0;//未完成充值订单金额
//            double cashOutUncompletedAmount=0;//未完成提现订单金额
            if (!TextUtils.isEmpty(dataBean.getCashInLowerLimit())) {
                cashInLowerLimit = Double.parseDouble(dataBean.getCashInLowerLimit());
            }

            if (!TextUtils.isEmpty(dataBean.getCashInUpperLimit())) {
                cashInLowerUp = Double.parseDouble(dataBean.getCashInUpperLimit());
            }
            if (!TextUtils.isEmpty(dataBean.getCashOutLowerLimit())) {
                cashOutLowerLimit = Double.parseDouble(dataBean.getCashOutLowerLimit());
            }

            if (!TextUtils.isEmpty(dataBean.getCashOutUpperLimit())) {
                cashOutLowerUp = Double.parseDouble(dataBean.getCashOutUpperLimit());
            }
            if (!TextUtils.isEmpty(dataBean.getAcceptLimit())) {
                acceptLimit = Double.parseDouble(dataBean.getAcceptLimit());
            }
            if (!TextUtils.isEmpty(dataBean.getAccountBalance())) {
                accountBalance = Double.parseDouble(dataBean.getAccountBalance());
            }
//            if (!TextUtils.isEmpty(dataBean.getCashInUncompletedAmount())){
//                cashInUncompletedAmount= Double.parseDouble(dataBean.getCashInUncompletedAmount());
//            }
//            if (!TextUtils.isEmpty(dataBean.getCashOutUncompletedAmount())){
//                cashOutUncompletedAmount= Double.parseDouble(dataBean.getCashOutUncompletedAmount());
//            }
            if ((!TextUtils.isEmpty(dataBean.getIntroduce()))) {
                viewHolder.tv_introduce.setText(dataBean.getIntroduce());
            } else {
                viewHolder.tv_introduce.setText(context.getString(R.string.bds_nothiing_acceptor));//There is nothing about the acceptor.
            }
//            cashInLowerUp=CalculateUtils.rechange_sub(accountBalance,cashInUncompletedAmount);
//            cashInLowerUp=CalculateUtils.rechange_sub(cashInLowerUp,cashOutUncompletedAmount);
            if (cashInLowerUp > acceptLimit) {
                cashInLowerUp = acceptLimit;
            }
//            cashOutLowerUp=CalculateUtils.rechange_sub(acceptLimit,cashOutUncompletedAmount);

//            if (cashInLowerUp >accountBalance ) {
//                cashInLowerUp = accountBalance;
//            }
            if (cashInLowerLimit > cashInLowerUp) {//充值下限，大于等于上限
                viewHolder.tvRechargeAmount.setText("~");
            } else {
                viewHolder.tvRechargeAmount.setText(CalculateUtils.round(cashInLowerLimit + "") + "~" + CalculateUtils.round(cashInLowerUp + "") + "" + dataBean.getSymbol());
            }
            if (cashOutLowerLimit > cashOutLowerUp) {//提现下限，大于等于上限
                viewHolder.tvWithdrawalAmount.setText("~");
            } else {
                viewHolder.tvWithdrawalAmount.setText(CalculateUtils.round(cashOutLowerLimit + "") + "~" + CalculateUtils.round(cashOutLowerUp + "") + "" + dataBean.getSymbol());
            }

            String rechargeRata = "0.00%";
            if (!TextUtils.isEmpty(dataBean.getCashInServiceRate())) {
                rechargeRata = NumberUtils.formatNumber2(CalculateUtils.mul(Double.parseDouble(dataBean.getCashInServiceRate()), 100) + "") + "%";

            }
            viewHolder.tvRechargeRata.setText(rechargeRata);

            String withdrawalRate = "0.00%";
            if (!TextUtils.isEmpty(dataBean.getCashOutServiceRate())) {
                withdrawalRate = NumberUtils.formatNumber2(CalculateUtils.mul(Double.parseDouble(dataBean.getCashOutServiceRate()), 100) + "") + "%";
            }
            viewHolder.tvWithdrawalRate.setText(withdrawalRate);

            double ranling = 0;
            if (!TextUtils.isEmpty(dataBean.getTotalEvaluation())) {
                ranling = Double.parseDouble(dataBean.getTotalEvaluation());
                viewHolder.myRanking.setRanling(ranling);
            }


            viewHolder.iv_huihua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewLister.onDuihuaLister(i);

                }
            });
            viewHolder.line_bottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewLister.onItemonLister(i);


                }
            });
            final View finalView = viewHolder.myRanking;
            viewHolder.line_myRanking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewLister.onItemRating(i, finalView);
                }
            });

        }


        return view;
    }

    static class ViewHolder {
        private ImageView iv_huihua;
        private TextView tv_acceptantNama, tvTotalVolume, tvRechargeAmount, tvRechargeRata, tvTranspens, tvWithdrawalAmount, tvWithdrawalRate;
        private LinearLayout line_zaixian, line_myRanking;
        private MyRanking myRanking;
        private TextView tv_introduce;
        private TextView id_name_time;
        private LinearLayout line_buZaiXian;
        private LinearLayout line_bottom;
        private View botLine;
    }

    private onOnButtonViewLister onViewLister;

    public void setOnItemViewLister(onOnButtonViewLister onViewLister) {
        this.onViewLister = onViewLister;
    }

    public interface onOnButtonViewLister {
        public void onDuihuaLister(int position);

        public void onItemonLister(int position);

        public void onItemRating(int position, View view);
    }

}
