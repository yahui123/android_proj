package com.tang.trade.tang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/8.
 */

public class TaansPagerAdapter extends FragmentPagerAdapter {

    List<android.support.v4.app.Fragment> data;
    private ArrayList<String> titles;

    public TaansPagerAdapter(FragmentManager fm, List<android.support.v4.app.Fragment> data, ArrayList<String>  titles) {
        super(fm);
        this.data = data;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return isEmpty() ? 0 : data.size();
    }

    boolean isEmpty() {
        return data == null || data.size() == 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        return this.data.get(position);

    }
}
