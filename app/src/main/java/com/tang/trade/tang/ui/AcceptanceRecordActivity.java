package com.tang.trade.tang.ui;

import android.content.Intent;
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
import com.tang.trade.tang.ui.transationFragment.AcceptanceRecordFragment;
import com.tang.trade.tang.widget.BadgeView;

import java.util.ArrayList;

public class AcceptanceRecordActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<String> titiles = new ArrayList<String>();
    private ArrayList<String> types = new ArrayList<String>();
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private ArrayList<String> date = new ArrayList<String>();
    private TaansPagerAdapter adapter;
    private ImageView ivBack;
    private boolean isFrist=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acceptance_record;
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setSelectedTabIndicatorHeight(3);

        viewPager = (ViewPager) findViewById(R.id.vp_transation);
        ivBack=(ImageView)findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        types.add("type1");
        types.add("type2");

        titiles.add(getString(R.string.bds_DepositHistory));
        titiles.add(getString(R.string.bds_WithdrawalHistory));

        for (int i=0;i<2;i++){
            AcceptanceRecordFragment fragment=AcceptanceRecordFragment.newInstance(types.get(i));
            fragments.add(fragment);
        }

        adapter=new TaansPagerAdapter(getSupportFragmentManager(),fragments,titiles);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);

        tabLayout.setupWithViewPager(viewPager);
        if(tabLayout.getTabCount()>4)
        {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setPadding(0,30,0,30);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

    }

    @Override
    public void initData() {
        Intent i=getIntent();

        int cashInCount =i.getIntExtra("cashInCount",0);
        int cashOutCount =i.getIntExtra("cashOutCount",0);
        BadgeView badge1=new BadgeView(this);
        badge1.setTargetView(((LinearLayout) tabLayout.getChildAt(0)).getChildAt(0));
        badge1.setBadgeCount(cashInCount);

        BadgeView badge2=new BadgeView(this);
        badge2.setTargetView(((LinearLayout) tabLayout.getChildAt(0)).getChildAt(1));
        badge2.setBadgeCount(cashOutCount);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFrist){
            isFrist=false;
        }else {
            onRefresh.onRefresh("");

        }

    }

    private onRefreshData onRefresh;
    public void setRefreshData(onRefreshData refreshData){
        this.onRefresh=refreshData;
    }
    public interface onRefreshData {
        public void onRefresh(String str);
    }
}
