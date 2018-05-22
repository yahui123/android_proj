package com.tang.trade.module.recover;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2018/4/10.
 *
 */

public class MyFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> mList;
    private String[] titles = {"二维码恢复","速记词恢复","文件恢复","私钥恢复"};

    public MyFragmentAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.mList = list;
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
