package com.tang.trade.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.flh.framework.QDFApplication;
import com.flh.framework.activity.BaseActivity;
import com.tang.trade.module.profile.login.LoginActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.UiUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by leo on 01/03/2018.
 */

public abstract class AbsBaseActivity extends BaseActivity {
    protected Activity mActivity;
    private Unbinder mUnbinder;
    private Toolbar mToolbar;
    private TextView mTvTitle;
    private AlertDialog dialog;
    private AlertDialog askDialog;

    private boolean isFirst;
    private long stopTimeMillis = 0;
    private long restartTimeMillis = 0;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mUnbinder = ButterKnife.bind(this);
        mActivity = this;
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mUnbinder = ButterKnife.bind(this);
        mActivity = this;
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mUnbinder = ButterKnife.bind(this);
        mActivity = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onRestart() {
        super.onRestart();

        restartTimeMillis = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*利用生命周期 实现退出5分钟后 需要重新登录*/
        if (restartTimeMillis != 0 && stopTimeMillis != 0 && restartTimeMillis > stopTimeMillis) {
            if (restartTimeMillis - stopTimeMillis > (5 * 60 * 1000)) {
                LoginActivity.start(this);
            }
        }

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopTimeMillis = System.currentTimeMillis();
    }

    public void setupDefultToolbar(int stringId) {
        setupDefultToolbar(getString(stringId));
    }

    public void setupDefultToolbar(String title) {
        setupDefultToolbar(findViewById(R.id.toolbar_defult), title);
    }

    public void setupDefultToolbar(Toolbar toolbar, int stringId) {
        setupDefultToolbar(toolbar, getString(stringId));
    }

    public void setupDefultToolbar(Toolbar toolbar, String title) {
        mToolbar = toolbar;

        if (mToolbar == null) {
            return;
        }

        if (!TextUtils.isEmpty(title)) {
            setSupportActionBar(mToolbar);
            mTvTitle = mToolbar.findViewById(R.id.tv_defult_title);
            mTvTitle.setText(title);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (getBack() != 0) {
                actionBar.setHomeAsUpIndicator(getBack());
                if (hasBack()) {
                    mToolbar.setNavigationIcon(getBack());
                }
            }

            actionBar.setDisplayHomeAsUpEnabled(hasBack());
            actionBar.setHomeButtonEnabled(hasBack());
            actionBar.setDisplayShowTitleEnabled(false);
        }

        setToolbarBackgroundColor(QDFApplication.getResColor(R.color.common_white));
        setStatusBarBackgroundColor(QDFApplication.getResColor(R.color.common_white));
    }

    public void setToolbarBackgroundColor(int bgColor) {
        if (mToolbar == null) {
            return;
        }
        mToolbar.setBackgroundColor(bgColor);

    }

    @SuppressLint("NewApi")
    public void setStatusBarBackgroundColor(int bgColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(bgColor);
            } else {
                getWindow().setStatusBarColor(QDFApplication.getResColor(R.color.rechange_switch_text));
            }

        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        /*
         * 新增状态栏适配
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
        UiUtils.FlymeSetStatusBarLightMode(this.getWindow(), true);
        UiUtils.MIUISetStatusBarLightMode(this.getWindow(), true);
    }

    public void showConfirmDialog(String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        dialog = builder.create();
        if (!isFinishing()) {
            dialog.show();
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));

    }

    public void showCancleableDialog(String title, String msg, String positiveBtnText, final DialogCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveBtnText, (dialog, which) -> {
            dialog.dismiss();
            callBack.onSuccess();
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
            callBack.onCancel();
        });
        askDialog = builder.create();
        if (!isFinishing()) {
            askDialog.show();
        }
        askDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));
        askDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));
    }

    public void showCancleableDialog(String title, String msg, String positiveBtnText, String cancelBtnText, final DialogCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveBtnText, (dialog, which) -> {
            dialog.dismiss();
            callBack.onSuccess();
        });
        builder.setNegativeButton(cancelBtnText, (dialog, which) -> {
            dialog.dismiss();
            callBack.onCancel();
        });
        askDialog = builder.create();
        if (!isFinishing()) {
            askDialog.show();
        }
        askDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));
        askDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.common_text_blue_v2));
    }

    protected boolean hasBack() {
        return true;
    }

    private int getBack() {
        return R.mipmap.common_back;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {

            mUnbinder.unbind();
        }
        if (dialog != null) {
            dialog.dismiss();
        }

        if (askDialog != null) {
            askDialog.dismiss();
        }
    }

    public interface DialogCallBack {
        void onSuccess();

        void onCancel();
    }


}
