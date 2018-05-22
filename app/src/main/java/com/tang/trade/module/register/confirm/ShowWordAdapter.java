package com.tang.trade.module.register.confirm;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by Administrator on 2018/4/16.
 */

public class ShowWordAdapter extends TagAdapter<String> {
    private Context context;

    public ShowWordAdapter(List<String> datas, Context context) {
        super(datas);
        this.context = context;
    }

    @Override
    public View getView(FlowLayout parent, int position, String s) {
        TextView view = (TextView) View.inflate(context, R.layout.item_confirm_tv, null);
        view.setText(s);
        return view;
    }
}
