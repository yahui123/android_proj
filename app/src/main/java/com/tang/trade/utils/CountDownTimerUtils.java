package com.tang.trade.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.tang.trade.tang.R;

/**
 * 发送验证码 倒计时
 * Created by Administrator on 2018/4/17.
 */

public class CountDownTimerUtils extends CountDownTimer {
    private TextView mTextView;
    private static final long SECOND = 1000;
    public static final long COUNT = 60000;
    private Context context;
    private String phone = "";

    /**
     * @param textView       The TextView
     * @param millisInFuture The number of millis in the future from the call
     *                       to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                       is called.
     */
    public CountDownTimerUtils(TextView textView, long millisInFuture) {
        super(millisInFuture, SECOND);
        this.mTextView = textView;
    }

    public CountDownTimerUtils(TextView textView, Context context) {
        super(COUNT, SECOND);
        this.mTextView = textView;
        this.context = context;
    }

    public CountDownTimerUtils(TextView textView) {
        super(COUNT, SECOND);
        this.mTextView = textView;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mTextView.setClickable(false); //设置不可点击
        mTextView.setText(millisUntilFinished / 1000 + "秒后可重新发送");  //设置倒计时时间
        if (context != null)
            mTextView.setTextColor(ContextCompat.getColor(context, R.color.common_text_gray));
    }

    @Override
    public void onFinish() {
        mTextView.setText("重新获取验证码");
        mTextView.setClickable(true);//重新获得点击
        if (context != null) {
            if (phone.length() != 11) {
                mTextView.setTextColor(ContextCompat.getColor(context, R.color.common_text_gray));
            } else {
                mTextView.setTextColor(ContextCompat.getColor(context, R.color.common_text_blue));
            }
        }
    }
}
