package com.tang.trade.module.recover;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.tang.trade.app.Const;
import com.tang.trade.base.AbsMVPActivity;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.recover.fileRecover.FileRecoverFragment;
import com.tang.trade.module.recover.keyRecover.KeyRecoverFragment;
import com.tang.trade.module.recover.qrRecover.QrRecoverFragment;
import com.tang.trade.module.recover.wordRecover.WordRecoverFragment;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class RecoverActivity extends AbsMVPActivity<RecoverContract.Presenter> implements RecoverContract.View {

    @BindView(R.id.tab)
    TabLayout mTab;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    private MyFragmentAdapter mAdapter;
    private QrRecoverFragment mQrRecoverFragment;
    private WordRecoverFragment mWordRecoverFragment;
    private FileRecoverFragment mFileRecoverFragment;
    private KeyRecoverFragment mKeyRecoverFragment;

    public static void start(Context context) {
        Intent starter = new Intent(context, RecoverActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // if (savedInstanceState != null){ //防止内存不足时，切换回来后会出现重影的问题
        //
        // }else {
        // mQrRecoverFragment = QrRecoverFragment.newInstance();
        // mWordRecoverFragment = WordRecoverFragment.newInstance();
        // mFileRecoverFragment = FileRecoverFragment.newInstance();
        // mKeyRecoverFragment = KeyRecoverFragment.newInstance();
        // }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_400);

        // 防止销毁重建的时候 节点为空
        // filterNodes();
        MyApp.CURRENT_NODE = MyApp.get("current_node", "");

        requestPermissions();

        setupViews();

        List<Fragment> list = new ArrayList<>();
        list.add(QrRecoverFragment.newInstance());
        list.add(WordRecoverFragment.newInstance());
        list.add(FileRecoverFragment.newInstance());
        list.add(KeyRecoverFragment.newInstance());

        mAdapter = new MyFragmentAdapter(getSupportFragmentManager(), list);
        mViewpager.setAdapter(mAdapter);
        mTab.setupWithViewPager(mViewpager);

        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                hideIputKeyboard(RecoverActivity.this);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public  void hideIputKeyboard(final Context context) {
        final Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            InputMethodManager mInputKeyBoard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null) {
                mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        });
    }

    @Override
    public void getIntentValue() {

    }

    /**
     * 筛选节点
     */
    private void filterNodes() {
        //判断current_node
        if (TextUtils.isEmpty(MyApp.get("current_node", ""))) {
            String nodes = SPUtils.getString(Const.NODES, "");
            if (!TextUtils.isEmpty(nodes)) {
                mPresenter.pingIPAddress(nodes);
            }

        } else {
            MyApp.CURRENT_NODE = MyApp.get("current_node", "");
        }
    }

    @Override
    public void pingIPResult(String s) {

    }

    private void setupViews() {
        setupDefultToolbar(getString(R.string.recover_account_400));
        setToolbarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
        setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.common_white));
    }

    @Override
    public RecoverContract.Presenter getPresenter() {
        return new RecoverPresenter(this);
    }

    @Override
    public void onError(DataError error) {

    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}