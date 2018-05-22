package com.tang.trade.tang.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.TaansPagerAdapter;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.ui.acceptorsfragment.AccRechargeFragment;
import com.tang.trade.tang.ui.acceptorsfragment.AccWithdrawalsFragment;
import com.tang.trade.tang.ui.base.BaseActivity;

import java.util.ArrayList;

import butterknife.BindView;


public class AcceptanceActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.tv_recharge)
    TextView tv_recharge;

    @BindView(R.id.tv_with)
    TextView tv_with;

    @BindView(R.id.iv_back)
    ImageView iv_back;


    @BindView(R.id.rg_acceptor)
    RadioGroup rg_acceptor;

    @BindView(R.id.vp_acceptor)
    ViewPager vp_acceptor;

    private TaansPagerAdapter adapter;

    private AccRechargeFragment mFragment;
    private AccWithdrawalsFragment mFragment1;
    private String bdsAccount="";

    private ArrayList<String> titiles = new ArrayList<String>();
    private ArrayList<String> types = new ArrayList<String>();
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    public AcceptotXiangqingModel.DataBean dataBean=null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_acc_main;
    }





    public void initView() {

        bdsAccount=getIntent().getStringExtra("bdsAccount");
        dataBean= (AcceptotXiangqingModel.DataBean) getIntent().getSerializableExtra("dateBean");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mFragment = new AccRechargeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type",0);
        bundle.putString("bdsAccount",bdsAccount);
        mFragment.setArguments(bundle);
        mFragment1 = new AccWithdrawalsFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt("type",1);
        bundle1.putString("bdsAccount",bdsAccount);
        mFragment1.setArguments(bundle1);
        iv_back.setOnClickListener(this);

        types.add("CI");
        types.add("CO");

        titiles.add(getString(R.string.bds_deposit));
        titiles.add(getString(R.string.bds_withdraw));
        fragments.add(mFragment);
        fragments.add(mFragment1);

        adapter=new TaansPagerAdapter(getSupportFragmentManager(),fragments,titiles);
        vp_acceptor.setAdapter(adapter);
        vp_acceptor.setOffscreenPageLimit(0);

        rg_acceptor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.rg0){
                    vp_acceptor.setCurrentItem(0);
                    rg_acceptor.setSelected(false);
                }else {
                    vp_acceptor.setCurrentItem(1);
                    rg_acceptor.setSelected(true);
                }
            }
        });
        vp_acceptor.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                View view=rg_acceptor.getChildAt(position);
                rg_acceptor.check(view.getId());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    @Override
    public void initData() {

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.tv_recharge:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                tv_recharge.setSelected(true);
                tv_with.setSelected(false);
                transaction.show(mFragment).hide(mFragment1).commit();
                break;
            case R.id.tv_with:
                FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                tv_recharge.setSelected(false);
                tv_with.setSelected(true);
                transaction1.show(mFragment1).hide(mFragment).commit();
                break;
            case R.id.iv_back:
                finish();
                break;

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)  //可写
                            != PackageManager.PERMISSION_GRANTED) {

                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        1);

            }
        }
    }




}
