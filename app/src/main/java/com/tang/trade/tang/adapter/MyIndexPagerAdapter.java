package com.tang.trade.tang.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.tang.trade.tang.R;

/**
 * Created by Administrator on 2018/1/6.
 */

public class MyIndexPagerAdapter  extends PagerAdapter {
    /**
     * 装点点的ImageView数组
     */
    private ImageView[] tips;
    private ImageView[] mImageViews;

    public MyIndexPagerAdapter(ImageView[] mImageViews, ImageView[] tips) {
        this.tips = tips;
        this.mImageViews = mImageViews;
    }

    @Override
    public int getCount() {
        // 设置成最大，使用户看不到边界
        //return Integer.MAX_VALUE;
        return mImageViews.length;
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Warning：不要在这里调用removeViewr
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 对ViewPager页号求模取出View列表中要显示的项
        position %= mImageViews.length;
        if (position < 0) {
            position = mImageViews.length + position;
        }
        ImageView view = mImageViews[position];
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
        }
        container.addView(view);
        return view;
    }



}


