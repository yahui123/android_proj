package com.tang.trade.tang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Html;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @ explain:
 * @ author：xujun on 2016/7/30 18:48
 * @ email：gdutxiaoxu@163.com
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    List<Fragment> data;
    private ArrayList<String> titles;

    public HomePagerAdapter(FragmentManager fm, List<Fragment> data, ArrayList<String> titles) {
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
        if (titles.get(position).equals("CNY") || titles.get(position).equals("USD")) {
            return Html.fromHtml("<small>BDS</small>" + titles.get(position));
        }
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
