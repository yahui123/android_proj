package com.tang.trade.tang.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.SPUtils;

/**
 * Created by 18301 on 2017/12/18.
 */

public class ConversationActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private YWIMKit mIMKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        initView();
    }






    public void initView() {
        frameLayout = (FrameLayout) findViewById(R.id.realtabcontent);

        try {
            String userid = SPUtils.getString(Const.USERNAME,"");
            mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);
//            addConnectionListener();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Fragment fragment1 = mIMKit.getConversationFragment();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction =manager.beginTransaction();
        // 把碎片添加到碎片中
        transaction.add(R.id.realtabcontent,fragment1).show(fragment1);
        transaction.commit();
    }

}
