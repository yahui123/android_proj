package com.tang.trade.tang.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.tang.R;


/**
 * Created by Administrator on 2017/6/5.
 */

public class MyProgressDialog extends Dialog {
    View view;
    Context context;
    TextView tv_desc;


    public static MyProgressDialog getInstance(Context context) {

        return new MyProgressDialog(context);
    }


    private MyProgressDialog(Context context) {
        super(context);
        this.context = context;
        this.requestWindowFeature(1);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.getWindow().setDimAmount(0.7f);
        this.setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.layout_progress_dialog,null);
        tv_desc = (TextView) view.findViewById(R.id.tv_desc_1);
        tv_desc.setText(context.getString(R.string.loading1));
        setContentView(view);
    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.
//        tv_desc = (TextView) findViewById(R.id.tv_desc_1);
//    }

    public void show() {
        if (!((Activity) this.context).isFinishing()) {
            super.show();
        }

    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
//        super.setTitle(title);
        tv_desc.setText(title);
    }
}