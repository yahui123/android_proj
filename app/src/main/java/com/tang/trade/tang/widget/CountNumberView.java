package com.tang.trade.tang.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.tang.trade.tang.utils.NumberUtils;


/**
 * Created by admin on 2017/12/4.
 */

public class CountNumberView extends android.support.v7.widget.AppCompatTextView {
    //动画时长
    private int duration = 200;
    //显示数字
    private float number;
    //显示表达式
    private String regex;
    //显示表示式
    public static final String INTREGEX = "%1$01.0f";//不保留小数，整数
    public static final String FLOATREGEX = "%1$01.2f";//保留2位小数
    public static final String ONEREGEX = "%1$01.5f";//保留1位小数


    public CountNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /***
     * 显示带有动画效果的数字
     *  * @param number
     *  * @param regex
     *  
     */
    public void showNumberWithAnimation(float number, String regex, String time, final String numberReally) {
        if (TextUtils.isEmpty(regex)) {
            //默认为整数
            this.regex = ONEREGEX;
        } else {
            this.regex = regex;
        }
        if (TextUtils.isEmpty(time)) {
            duration = 0;
        } else {
            duration = 200;
        }
        //修改number属性，会调用setNumber方法
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "number", 0, number);
        objectAnimator.setDuration(duration);
        //加速器，从慢到快到再到慢
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();

        objectAnimator.addListener(new AnimatorListenerAdapter() {//便利类，只要实现需要的方法
            @Override
            public void onAnimationEnd(Animator animation) {
                setText(numberReally);
            }
        });
    }


    /***
     * 显示带有动画效果的数字
     *  * @param number
     *  * @param regex
     *  
     */
    public void showNumberWithAnimation1(float formNumber , float toNumber, String regex, String time, final String numberReally) {
        if (TextUtils.isEmpty(regex)) {
            //默认为整数
            this.regex = ONEREGEX;
        } else {
            this.regex = regex;
        }
        if (TextUtils.isEmpty(time)) {
            duration = 0;
        } else {
            duration = 1000;
        }
        //修改number属性，会调用setNumber方法
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "number", formNumber, toNumber);
        objectAnimator.setDuration(duration);
        //加速器，从慢到快到再到慢
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();

        objectAnimator.addListener(new AnimatorListenerAdapter() {//便利类，只要实现需要的方法
            @Override
            public void onAnimationEnd(Animator animation) {

                setText(numberReally);
            }
        });
    }

    /**
     * 获取当前数字
     *
     * @return
     */
    public float getNumber() {
        return number;
    }

    /**
     * 根据正则表达式，显示对应数字样式
     *
     * @param number
     */

    public void setNumber(float number) {
        this.number = number;
        setText(String.format(regex, number));
    }
}