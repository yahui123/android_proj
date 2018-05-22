package com.tang.trade.tang.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;

/**
 * Created by Administrator on 2018/1/27.
 */

public class BottomBarView extends RelativeLayout {

    private int msgCount;
    private TextView bar_num;
    private ImageView bar_iv;

    public BottomBarView(Context context) {
        this(context, null);
    }

    public BottomBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.badge_layout, this, true);
        bar_num = (TextView) rl.findViewById(R.id.bar_num);
        bar_iv = (ImageView) rl.findViewById(R.id.bar_iv);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.badge_stule);
        if (attributes != null) {
            //处理titleBar背景色
            int titleBarBackGround = attributes.getResourceId(R.styleable.badge_stule_drawable_badge, Color.GREEN);
            bar_iv.setImageResource(titleBarBackGround);
        }
    }

    public void setMessageCount(int count) {
        msgCount = count;
        if (count == 0) {
            bar_num.setVisibility(View.GONE);
        } else {
            bar_num.setVisibility(View.VISIBLE);
            if (count < 100) {
                bar_num.setText(count + "");
            } else {
                bar_num.setText("99+");
            }
        }
        invalidate();
    }

    public void addMsg() {
        setMessageCount(msgCount + 1);
    }
}
