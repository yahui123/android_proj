package com.tang.trade.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;

/**
 * 总共分为三部分  左 中  右
 * 可以自由向 某一部分自由添加
 * 左部分 是自左向右依次添加
 * 中部分 是自左向右依次添加
 * 右部分 是自右向左一次添加
 * <p>
 * 未设置字体大小和颜色
 * Created by Administrator on 2018/4/12.
 */

public class TitleBar extends LinearLayout {
    private LinearLayout lin_left, lin_center, lin_right;
    private View baseView;

    public TitleBar(Context context) {
        super(context);
        init(context, null);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化属性
     */
    private void initAttr(@Nullable AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.att);
        String title = t.getString(0);
//        boolean isBack = t.getBoolean(  R.styleable.back, false);
//        float titleSize = t.getDimension(2, 20);
//        initOption(title, isBack, titleSize);
        t.recycle();
    }

    private void initOption(String title, boolean isBack, float titleSize) {
        if (!TextUtils.isEmpty(title)) {
            setCenterTitle(title);
            ((TextView) lin_center.getChildAt(0)).setTextSize(titleSize);
        }
        if (isBack) {
            setLeftImageBack(R.mipmap.common_back);
        }
    }

    private BackListener initListener;

    public void setBackListener(BackListener listener) {
        initListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        setBackgroundColor(Color.WHITE);
        setOrientation(HORIZONTAL);
        baseView = View.inflate(context, R.layout.include_title_400, null);
        initView();
        addView(baseView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        initAttr(attrs);

    }

    private void initView() {
        lin_left = baseView.findViewById(R.id.title_left);
        lin_center = baseView.findViewById(R.id.title_center);
        lin_right = baseView.findViewById(R.id.title_right);
    }

    /**
     * 设置back返回
     *
     * @param source
     */
    @SuppressLint("ResourceAsColor")
    public void setLeftImageBack(int source) {
        if (source == 0) {
            return;
        }
        ImageView img = new ImageView(getContext());
        img.setImageResource(source);
        img.setPadding(20, 20, 20, 20);
        lin_left.addView(img);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initListener != null) {
                    initListener.back();
                }
            }
        });

    }

    /**
     * 设置标题
     *
     * @param title
     */
    @SuppressLint("ResourceAsColor")
    public void setCenterTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            throw new NullPointerException("请填写标题");
        }
        TextView tv_title = new TextView(getContext());
        tv_title.setText(title);
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv_title.setTextColor(R.color.common_text_blank);
        lin_center.addView(tv_title);
    }

    /**
     * 设置右面图标
     *
     * @param source
     * @param listener
     */
    public void setRightImage(int source, final ClickListener listener) {
        if (source == 0) {
            return;
        }
        ImageView img = new ImageView(getContext());
        img.setImageResource(source);
        img.setPadding(20, 20, 20, 20);
        lin_right.addView(img);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.click();
                }
            }
        });
    }

    /**
     * 设置右面文字
     *
     * @param title
     */
    public void setRightText(CharSequence title, final ClickListener clickListener) {
        if (TextUtils.isEmpty(title)) {
            throw new NullPointerException("请填写标题");
        }
        TextView tv_right = new TextView(getContext());
        tv_right.setText(title);
        //设置个默认字体
//        tv_title.setTextSize();
        lin_right.addView(tv_right);

        tv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.click();
                }
            }
        });

    }

    /**
     * 获取右面视图
     *
     * @param position
     * @param <T>
     * @return
     */
    public <T extends View> T getRighView(int position) {
        if (lin_right.getChildCount() < position || position <= 0) {
            throw new NullPointerException("View 不存在");
        } else {
            return (T) lin_right.getChildAt(position - 1);
        }
    }

    public interface BackListener {
        void back();
    }

    public interface ClickListener {
        void click();
    }

}
