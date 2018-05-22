package com.tang.trade.module.register.confirm;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.base.BaseAdapter;
import com.tang.trade.base.BaseViewHolder;
import com.tang.trade.tang.R;

import butterknife.BindView;


/**
 * Created by Administrator on 2018/4/10.
 */

public class ConfirmGVAdapter extends BaseAdapter<Word, ConfirmGVAdapter.GvHolder> {

    public ConfirmGVAdapter(Context context) {
        super(context);

    }

    @Override
    protected int getViewLayoutId() {
        return R.layout.item_confirm_checkbox_400;
    }

    @Override
    protected GvHolder createViewHolder(View view) {
        return new GvHolder(view);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void showData(final GvHolder holder, final Word item, int position) {
        holder.check.setText(item.getWord());
        if (item.isCheck()) {
            holder.check.setBackground(getContext().getDrawable(R.drawable.login_register_checkboxed_color));
            holder.check.setTextColor(ContextCompat.getColor(mContext, R.color.common_white));
        } else {
            holder.check.setBackground(getContext().getDrawable(R.drawable.login_register_checkbox_color));
            holder.check.setTextColor(ContextCompat.getColor(mContext, R.color.common_text_gray));
        }
//        holder.check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (item.isCheck() ) {
//                    return;
//                } else {
//                    holder.check.setBackground(getContext().getDrawable(R.drawable.login_register_checkboxed_color));
//                    if (listener != null) {
//                        listener.select(item.getWord());
//                    }
//                    item.setCheck(true);
//                }
//            }
//        });

    }

    private SelectWordListener listener;

    public void setSelectListener(SelectWordListener listener) {
        this.listener = listener;
    }

    public interface SelectWordListener {
        //选中单词
        void select(String str);

    }

    public static final class GvHolder extends BaseViewHolder {
        @BindView(R.id.item_gv_checkbox)
        public TextView check;

        public GvHolder(View view) {
            super(view);
            // check = view.findViewById(R.id.item_gv_checkbox);

        }
    }

}
