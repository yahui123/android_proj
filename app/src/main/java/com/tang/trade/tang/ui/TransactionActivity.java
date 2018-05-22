package com.tang.trade.tang.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.TaansPagerAdapter;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.transationFragment.TransctionFragment;

import java.util.ArrayList;

public class TransactionActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<String> titiles = new ArrayList<String>();
    private ArrayList<String> types = new ArrayList<String>();
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private ArrayList<String> date = new ArrayList<String>();
    private TaansPagerAdapter adapter;
    private ImageView ivBack;
    private static TransactionActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transaction;
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void initView() {
        activity = this;
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setSelectedTabIndicatorHeight(3);
        ivBack = findViewById(R.id.ivBack);

        viewPager = (ViewPager) findViewById(R.id.vp_transation);
        types.add("CI");
        types.add("CO");

        titiles.add(getString(R.string.bds_DepositHistory));
        titiles.add(getString(R.string.bds_WithdrawalHistory));

        for (int i = 0; i < 2; i++) {
            TransctionFragment fragment = TransctionFragment.newInstance(types.get(i));
            fragments.add(fragment);
        }

        adapter = new TaansPagerAdapter(getSupportFragmentManager(), fragments, titiles);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);
        tabLayout.setupWithViewPager(viewPager);
        if (tabLayout.getTabCount() > 4) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setPadding(0, 30, 0, 30);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        ivBack.setOnClickListener(this);


    }

    @Override
    public void initData() {


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        onRefresh.onRefresh("");
    }

    private onRefreshData onRefresh;

    public void setRefreshData(onRefreshData refreshData) {
        this.onRefresh = refreshData;
    }

    public interface onRefreshData {
        public void onRefresh(String str);
    }

}
