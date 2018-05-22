package com.tang.trade.tang.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.model.OrdersResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.limit_order_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.exchangefragment.OrderFragment;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.MyProgressDialog;

import java.util.List;

import de.bitsharesmunich.graphenej.LimitOrder;

/**
 * Created by dagou on 2017/9/22.
 */

public class OrdersAdapter extends BaseAdapter {

    private List<OrdersResponseModel.DataBean> list = null;
    private Context context;
    private String type = "buy";

    public void setType(String type) {
        this.type = type;
    }

    public void setList(List<OrdersResponseModel.DataBean> list) {
        this.list = list;
    }

    public OrdersAdapter(List<OrdersResponseModel.DataBean> list) {
        this.list = list;
    }
    public OrdersAdapter(List<OrdersResponseModel.DataBean> list, Context context) {
        this.list = list;
        this.context=context;
    }

    private OrderFragment mOrderFragment;
    public void setOrderFragment(OrderFragment orderFragment) {
        mOrderFragment = orderFragment;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.fragment_order_item, null);

            holder = new ViewHolder();
            holder.tv_btc = convertView.findViewById(R.id.tv_btc);
            holder.tv_bds = convertView.findViewById(R.id.tv_bds);
            holder.tv_price = convertView.findViewById(R.id.tv_price);
            holder.tv_time = convertView.findViewById(R.id.tv_time);
            holder.tv_cancel = convertView.findViewById(R.id.tv_cancel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_btc.setText(list.get(position).getBtc());
        holder.tv_price.setText(list.get(position).getPrice());
        holder.tv_bds.setText(list.get(position).getBds());
        holder.tv_time.setText(list.get(position).getTime());

        if (type.equalsIgnoreCase("sell")){

            holder.tv_btc.setTextColor(Color.parseColor("#37B569"));
            holder.tv_price.setTextColor(Color.parseColor("#37B569"));
            holder.tv_bds.setTextColor(Color.parseColor("#37B569"));
            holder.tv_time.setTextColor(Color.parseColor("#37B569"));
        } else {

            holder.tv_btc.setTextColor(Color.parseColor("#FF2929"));
            holder.tv_price.setTextColor(Color.parseColor("#FF2929"));
            holder.tv_bds.setTextColor(Color.parseColor("#FF2929"));
            holder.tv_time.setTextColor(Color.parseColor("#FF2929"));

        }

        holder.tv_cancel.setOnClickListener(new MyOnclick(holder.tv_cancel,position));

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    static class ViewHolder {
        TextView tv_price, tv_btc, tv_bds,tv_time,tv_cancel;
    }

    class MyOnclick implements View.OnClickListener{

        private TextView textView;
        private int position;

        public MyOnclick(TextView textView, int position) {
            this.textView = textView;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
          new DownTaskDelete().execute(position);
        }
    }

    private MyProgressDialog progressDialog;
    public class DownTaskDelete extends AsyncTask<Integer, Void,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=MyProgressDialog.getInstance(context);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return setDeleteDate(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();

            if (aBoolean){
                notifyDataSetChanged();
                MyApp.showToast(context.getString(R.string.bds_cancel_complete));
                mOrderFragment.getOnCancelListener().onCancelSuccess();
            }else {
                MyApp.showToast(context.getString(R.string.bds_cancel_fail));
            }
        }
    }
    private Boolean setDeleteDate(int position){
        LimitOrder limitOrder = list.get(position).getLimitOrder();
        String orderId = limitOrder.getObjectId();

        if (orderId != null) {
            object_id<limit_order_object> limit_order_id = object_id.create_from_string(orderId);
            signed_transaction signed_transaction = null;

            try {
                signed_transaction = BitsharesWalletWraper.getInstance().cancel_order(limit_order_id);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

            if (signed_transaction != null){
                list.remove(position);
                return true;
            }
        }

        return false;
    }

}
