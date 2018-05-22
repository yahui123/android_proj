package com.tang.trade.tang.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.AccountResponseModel;
import com.tang.trade.tang.ui.SelectAccountActivity;

import java.util.ArrayList;

/**
 * Created by dagou on 2017/9/22.
 */

public class AccountAdapter extends BaseAdapter {
    private int selectPosition=-1;

    ArrayList<AccountResponseModel.DataBean> list = null;
    private Context context;

    public AccountAdapter(ArrayList<AccountResponseModel.DataBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(ArrayList<AccountResponseModel.DataBean> list) {
        this.list = list;
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.layout_account_item, null);

            holder = new ViewHolder();

            holder.tv_pay_cn = (TextView) convertView.findViewById(R.id.tv_pay_cn);
            holder.tv_pay_en = (TextView) convertView.findViewById(R.id.tv_pay_en);
            holder.tv_pay_name = (TextView) convertView.findViewById(R.id.tv_pay_name);
            holder.tv_pay_number = (TextView) convertView.findViewById(R.id.tv_pay_number);
            holder.ll_bg = convertView.findViewById(R.id.ll_bg);
            holder.iv_account_select=(ImageView) convertView.findViewById(R.id.iv_account_select);
            holder.ll_account_select=(LinearLayout)convertView.findViewById(R.id.ll_account_select);
            holder.ll_bianji=(LinearLayout)convertView.findViewById(R.id.ll_bianji);
            holder.ll_delete=(LinearLayout)convertView.findViewById(R.id.ll_delete);
            holder.ll_moren=convertView.findViewById(R.id.ll_moren);
            holder.iv_moren=convertView.findViewById(R.id.btn_moren);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_pay_name.setText(context.getString(R.string.bds_full_name)+": "+list.get(position).getName());
        holder.tv_pay_number.setText(context.getString(R.string.Account)+" "+list.get(position).getPayAccountID());

        final  String typeWay = list.get(position).getTypeCode();
        if (typeWay.equalsIgnoreCase("AP")){
            holder.tv_pay_cn.setText(context.getString(R.string.bds_Alipay));
            holder.tv_pay_en.setText("ALIPAY");
            holder.ll_bg.setBackgroundResource(R.mipmap.mine_selectaccount_apliay_bg);
        }else if (typeWay.equalsIgnoreCase("WC")){
            holder.tv_pay_cn.setText(context.getString(R.string.bds_WeChat));
            holder.tv_pay_en.setText("WeChat");
            holder.ll_bg.setBackgroundResource(R.mipmap.mine_selectaccount_wc_bg);
        }else{
            holder.tv_pay_en.setText("");
            holder.ll_bg.setBackgroundResource(R.mipmap.mine_selectaccount_bank_bg);

            holder.tv_pay_cn.setText(list.get(position).getBank());
        }

        holder.ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewLister.onDeleteLister(position);
            }
        });


        holder.ll_account_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPosition=position;
                notifyDataSetChanged();
            }
        });

        if (SelectAccountActivity.TYPE.equals(SelectAccountActivity.TYPE_TIXIAN)){//提现

            holder.iv_moren.setVisibility(View.GONE);
            holder.ll_account_select.setVisibility(View.VISIBLE);
            holder.ll_delete.setVisibility(View.VISIBLE);
            if(selectPosition == position){
                holder.iv_account_select.setImageResource(R.mipmap.mine_selectaccount_selected_icon);
            }
            else{
                holder.iv_account_select.setImageResource(R.mipmap.mine_selectaccount_noselected_icon);
            }


        }else if (SelectAccountActivity.TYPE.equals(SelectAccountActivity.TYPE_Chongzhi)){
            holder.iv_moren.setVisibility(View.GONE);
            holder.ll_account_select.setVisibility(View.VISIBLE);
            holder.ll_delete.setVisibility(View.GONE);
            if(selectPosition == position){
                holder.iv_account_select.setImageResource(R.mipmap.mine_selectaccount_selected_icon);
            }
            else{
                holder.iv_account_select.setImageResource(R.mipmap.mine_selectaccount_noselected_icon);
            }


        } else if (SelectAccountActivity.TYPE.equals(SelectAccountActivity.TYPE_GUANLI)){//flase
            holder.iv_moren.setVisibility(View.VISIBLE);
            holder.ll_account_select.setVisibility(View.GONE);

            if (list.get(position).getIsAcceptantPay().equals("1")){
                holder.iv_moren.setChecked(true);
            }else {
                holder.iv_moren.setChecked(false);
            }
            holder.iv_moren.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (list.get(position).getIsAcceptantPay().equals("1")){

                        }else {
                            onViewLister.onMorenLister(position,b);
                        }

                    }else {
                        if (!list.get(position).getIsAcceptantPay().equals("1")){

                        }else {
                            onViewLister.onMorenLister(position,b);
                        }

                    }
                }
            });

        }



        return convertView;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public int getSelectPosttion(){
        return selectPosition;
    }

    static class ViewHolder {
        TextView tv_pay_cn, tv_pay_en,tv_pay_name,tv_pay_number;
        LinearLayout ll_bg,ll_account_select,ll_bianji,ll_delete,ll_moren;
        ImageView iv_account_select;
        ToggleButton iv_moren;
    }


    private onOnButtonViewLister onViewLister;

    public void setOnItemViewLister(onOnButtonViewLister onViewLister){
        this.onViewLister=onViewLister;
    }
    public interface onOnButtonViewLister {
        public void onBiLiLister(int position);
        public void onDeleteLister(int position);
        public void onMorenLister(int position,boolean isflay);
    }
    public void setSelectPosition(int position){
        this.selectPosition=position;
    }



}
