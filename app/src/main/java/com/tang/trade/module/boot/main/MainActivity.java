package com.tang.trade.module.boot.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.tang.R;

import java.util.HashMap;

/**
 * Created by daibin on 2018/5/2.
 */

public class MainActivity extends AbsMVPActivity {
    private static final String TAG_FRAG_MARKET = "tag_frag_market";
    private static final String TAG_FRAG_ACCEPTANCE = "tag_frag_acceptance";
    private static final String TAG_FRAG_TRADE = "frag_transaction";
    private static final String TAG_FRAG_MINE = "frag_mine";
//
//    @BindView(R.id.iv_market)
//    ImageView mIvMarket;
//    @BindView(R.id.llyt_market)
//    LinearLayout mLlytMarket;
//    @BindView(R.id.iv_trade)
//    ImageView mIvTrade;
//    @BindView(R.id.llyt_trade)
//    LinearLayout mLlytTrade;
//    @BindView(R.id.iv_acceptance)
//    ImageView mIvAcceptance;
//    @BindView(R.id.llyt_acceptance)
//    LinearLayout mLlytAcceptance;
//    @BindView(R.id.iv_mine)
//    ImageView mIvMine;
//    @BindView(R.id.llyt_mine)
//    LinearLayout mLlytMine;
//    @BindView(R.id.container)
//    FrameLayout container;
//    @BindView(R.id.ll_bottom)
//    LinearLayout llBottom;

    private HashMap<String, Fragment> mFragments;
    private Fragment mCurrentFragment;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            mStoreMapFragment = (StoreMapFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAG_STORE_MAP);
//            mWalletFragment = (WalletFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAG_WALLET);
//            mMineFragment = (MineFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAG_MINE);
//        } else {
//            mStoreMapFragment = StoreMapFragment.newInstance();
//            mWalletFragment = WalletFragment.newInstance();
//            mMineFragment = MineFragment.newInstance();
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_400);

        initView();

        initFragment();

    }

    private void initView() {

        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));

//        mLlytMarket.setTag(TAG_FRAG_MARKET);
//        mLlytTrade.setTag(TAG_FRAG_TRADE);
//        mLlytAcceptance.setTag(TAG_FRAG_ACCEPTANCE);
//        mLlytMine.setTag(TAG_FRAG_MINE);
    }

    private void initFragment() {
        mFragments = new HashMap<>(4);
//        mFragments.put(TAG_FRAG_MARKET, );
//        mFragments.put(TAG_FRAG_ACCEPTANCE, );
//        mFragments.put(TAG_FRAG_TRADE, );
//        mFragments.put(TAG_FRAG_MINE, );

        /*第一次进入 默认在钱包页面*/
        changeTabs(TAG_FRAG_MARKET);
    }

    private void changeTabs(String tag) {
        String selectedTab = tag;

//        mLlytMarket.setSelected(TAG_FRAG_MARKET.equals(selectedTab));
//        mLlytTrade.setSelected(TAG_FRAG_TRADE.equals(selectedTab));
//        mLlytAcceptance.setSelected(TAG_FRAG_ACCEPTANCE.equals(selectedTab));
//        mLlytMine.setSelected(TAG_FRAG_MINE.equals(selectedTab));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (null != mCurrentFragment) {
            ft.hide(mCurrentFragment);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(selectedTab);

        if (null == fragment) {
            fragment = mFragments.get(selectedTab);
        }

        mCurrentFragment = fragment;

        if (fragment.isAdded()) {
            ft.show(mCurrentFragment);
        } else {
            ft.add(R.id.container, mCurrentFragment, selectedTab);
        }

        ft.commit();
    }

    @Override
    public Object getPresenter() {
        return null;
    }

    @Override
    public void onError(DataError error) {

    }

    @Override
    public void getIntentValue() {

    }

//    @OnClick({R.id.llyt_market, R.id.llyt_trade, R.id.llyt_acceptance, R.id.llyt_mine})
//    public void onViewClicked(View view) {
//
//        changeTabs((String) view.getTag());
//    }
}
