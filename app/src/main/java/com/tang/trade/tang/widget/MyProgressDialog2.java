package com.tang.trade.tang.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.tang.R;


/**
 * Created by Administrator on 2017/6/5.
 */

public class MyProgressDialog2 extends Dialog {
    View view;
    Context context;
    TextView tv_desc;
    private CountDownTimer timer =null;


    public static MyProgressDialog2 getInstance(Context context) {

        return new MyProgressDialog2(context);
    }


    private MyProgressDialog2(Context context) {
        super(context);
        this.context = context;
        this.requestWindowFeature(1);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.getWindow().setDimAmount(0.7f);
        this.setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.layout_progress_dialog2,null);
        tv_desc = (TextView) view.findViewById(R.id.tv_desc_1);
        tv_desc.setText(context.getString(R.string.bds_submit_in));
        setContentView(view);
    }
    private OnButtonClickListener onItemClickListener;
    public void setOnItemButtomClickListener(OnButtonClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }
    public interface OnButtonClickListener {
        void OnclictListener();
    }


    public void setTime(int sumber){
        final String b1=context.getString(R.string.bds_TransactionSuccessfu)+","+context.getString(R.string.bds_WaitingForBlock);
        final String b2=context.getString(R.string.bds_remaining);
        final String b3=context.getString(R.string.bds_second);
        timer= new CountDownTimer(sumber, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int b= (int) (millisUntilFinished/1000);
                tv_desc.setText(b1+","+b2+b+b3);
            }

            @Override
            public void onFinish() {
                tv_desc.setText(b1+","+b2+" 0 "+b3);
                if (null!=onItemClickListener){
                    onItemClickListener.OnclictListener();
                }

            }
        };
        timer.start();
    }

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