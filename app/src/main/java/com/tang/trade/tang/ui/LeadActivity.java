package com.tang.trade.tang.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import com.tang.trade.tang.AppOperator;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.UltraPagerAdapter;
import com.tmall.ultraviewpager.UltraViewPager;

/**
 * Created by dagou on 2017/9/12.
 */

public class LeadActivity extends AppCompatActivity {

    private int[] src = {R.mipmap.timg,R.mipmap.timg2,R.mipmap.timg3};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead);

        getWindow().getDecorView().setSystemUiVisibility(
                UltraViewPager.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        AppOperator.runOnThread(new Runnable() {
            @Override
            public void run() {
                downloadLeadImage();
            }
        });

        initWidget();

    }

    private void initWidget() {
        UltraViewPager ultraViewPager = (UltraViewPager)findViewById(R.id.ultra_viewpager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        //UltraPagerAdapter 绑定子view到UltraViewPager
        PagerAdapter adapter = new UltraPagerAdapter(false,src,this);
        ultraViewPager.setAdapter(adapter);

        //内置indicator初始化
        ultraViewPager.initIndicator();
        //设置indicator样式
        ultraViewPager.getIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setFocusColor(Color.GREEN)
                .setNormalColor(Color.WHITE)
                .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        //设置indicator对齐方式
        ultraViewPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        //构造indicator,绑定到UltraViewPager
        ultraViewPager.getIndicator().build();

        //设定页面循环播放
//        ultraViewPager.setInfiniteLoop(true);
        //设定页面自动切换  间隔2秒
//        ultraViewPager.setAutoScroll(2000);

        ultraViewPager.disableAutoScroll();

    }

    private void downloadLeadImage() {


    }
}
