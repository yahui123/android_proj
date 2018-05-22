package com.tang.trade.tang.ui.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.login.IYWConnectionListener;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.interf.BaseView;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.service.MyService;
import com.tang.trade.tang.utils.ActivityManagerUrils;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.widget.MyProgressDialog;
import com.tang.trade.tang.widget.StatusBarUtil;
import com.tang.trade.utils.SPUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by dagou on 2017/9/21.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener ,BaseView{

    protected LayoutInflater mInflater;
    private Fragment mFragment;

    private PopupWindow popWnd;
    private File file1;

    private Bundle bundle;
    private String flag = "0";

    public YWIMKit mIMKit;
    private String userid;
    private int statusbarFlay=1;//0白色 1黑色


    public void setStatusbarFlay(int statusbarFlay) {
        this.statusbarFlay = statusbarFlay;
    }

    private IYWConnectionListener mConnectionListener;
    private AlertDialog.Builder normalDialog;

    public MyProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);

        onBeforeSetContentLayout();
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        initWindow(statusbarFlay);
        //StatusBarUtil.setTranslucent(this);

        try {
            userid = SPUtils.getString(Const.USERNAME,"");
            mIMKit = YWAPI.getIMKitInstance(userid, MyApp.APP_KEY);
//            addConnectionListener();
        } catch (Exception e) {

        }
        progressDialog=MyProgressDialog.getInstance(this);

        mInflater = getLayoutInflater();
        ButterKnife.bind(this);

        init(savedInstanceState);
        initView();
        initData();
        setLanguage();




//        DragFloatingActionButton dragFloatingActionButton = new DragFloatingActionButton(this);
////        LayoutParams param = new LayoutParams();
////        param.width = 120;
////        param.height = 120;
////        dragFloatingActionButton.setLayoutParams(param);
//
//        WindowManager wm=(WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        LayoutParams params = new LayoutParams(); // 类型
//        params.type = LayoutParams.TYPE_SYSTEM_ALERT;
//        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//        // 设置flag
//        int flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
//        params.flags = flags;
//        // 不设置这个弹出框的透明遮罩显示为黑色
//        params.format = PixelFormat.TRANSLUCENT;
//        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
//        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
//        // 不设置这个flag的话，home页的划屏会有问题
//        params.width = 120;
//        params.height = 120;
//        params.gravity = Gravity.RIGHT;
//        wm.addView(dragFloatingActionButton, params);


        bundle = getIntent().getExtras();
        if (bundle != null){
            flag = bundle.getString("flag");
        }

//        Log.i("flag",flag);
       // addActivity(this);

    }




    protected void onBeforeSetContentLayout() {

    }

    protected boolean hasActionBar() {
        return getSupportActionBar() != null;
    }

    protected int getLayoutId() {
        return 0;
    }

    protected View inflateView(int resId) {
        return mInflater.inflate(resId, null);
    }

    protected int getActionBarTitle() {
        return R.string.app_name;
    }

    protected boolean hasBackButton() {
        return false;
    }

    protected void init(Bundle savedInstanceState) {
    }

    public void addFragment(int frameLayoutId, Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (mFragment != null) {
                    transaction.hide(mFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (mFragment != null) {
                    if (!fragment.isAdded()) {
                        transaction.hide(mFragment).add(frameLayoutId, fragment);
                    }
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            mFragment = fragment;
            transaction.commit();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (null!=v&&isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }

            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }


    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getRawX()> left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


    private static int result = 0;
    public void initWindow(int statusbarFlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);

            //获取状态栏高度的资源id
            if (result==0){
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    result = getResources().getDimensionPixelSize(resourceId);
                }
            }
            ViewGroup decorView = (ViewGroup) this.getWindow().getDecorView();
            View statusBarView = new View(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      result);
            statusBarView.setBackgroundColor(0xff000000);
            statusBarView.setAlpha(0.3f);
            decorView.addView(statusBarView, lp);
        }
    }



    private void setLanguage(){
        if (!MyApp.get(BuildConfig.LANGUAGE, "").equals("")){
            if (MyApp.get(BuildConfig.LANGUAGE, "").contains("繁體中文")){
                settingLanguage(Locale.TRADITIONAL_CHINESE);
                LaunchActivity.lang = "zh_HK";
            }else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("简体中文")){
                settingLanguage(Locale.SIMPLIFIED_CHINESE);
                LaunchActivity.lang =  "zh_CN";
            }else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("English")){
                settingLanguage(Locale.ENGLISH);
                LaunchActivity.lang = "en";
            }
        }
        else {

            String locale = getResources().getConfiguration().locale.getCountry();
            //繁体
            if (locale.equalsIgnoreCase("TW") || locale.equalsIgnoreCase("HK")){
                LaunchActivity.lang = "zh_HK";
                //简体
            }else if (locale.equalsIgnoreCase("CN")){
                LaunchActivity.lang = "zh_CN";
                //英文
            }else{
                LaunchActivity.lang = "en";
            }
        }

    }


    private void settingLanguage(Locale locale){
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.locale = locale; // 英文
        resources.updateConfiguration(config, dm);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceExisted()){
//            Log.e("My","进入前台");
            Intent intent = new Intent();
            intent.setAction("com.tang.trade.tang.ui.service.MyService");
            sendBroadcast(intent);
            isFirst=true;

        }
        ActivityManagerUrils.getActivityManager().popActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManagerUrils.getActivityManager().pushActivity(this);
    }


//    private void showPopupPassword() {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.wallet_password_alert, null);
//        popWnd = new PopupWindow(this);
//        popWnd.setContentView(contentView);
//        popWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
//        popWnd.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popWnd.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popWnd.setOutsideTouchable(false);
//        popWnd.showAtLocation(View.inflate(this, R.layout.activity_launch, null), Gravity.CENTER, 0, 0);
//        EditText et_pwd = (EditText) contentView.findViewById(R.id.et_pwd);
//        TextView tv_ok = (TextView) contentView.findViewById(R.id.tv_ok);
//        TextView tv_delete = (TextView) contentView.findViewById(R.id.tv_delete);
//
//
//        Window window=getWindow();
//        LayoutParams wl = window.getAttributes();
//        if (popWnd.isShowing()){
//
////        wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//            wl.alpha=0.6f;
//            window.setAttributes(wl);
//        }else{
//            wl.alpha=1.0f;
//            window.setAttributes(wl);
//        }
//
//
//        tv_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popWnd.dismiss();
//                startActivity(new Intent(BaseActivity.this,LoginActivity.class));
//                finish();
//
//            }
//        });
//
//        tv_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popWnd.dismiss();
//
//                showPopupClearPassword();
//            }
//        });
//    }
//
//    private void showPopupClearPassword() {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.clear_wallet_alert, null);
//        popWnd = new PopupWindow(this);
//        popWnd.setContentView(contentView);
//        popWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
//        popWnd.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popWnd.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popWnd.setOutsideTouchable(false);
//        popWnd.showAtLocation(View.inflate(this, R.layout.activity_launch, null), Gravity.CENTER, 0, 0);
//        TextView tv_yes = (TextView) contentView.findViewById(R.id.tv_yes);
//        TextView tv_no = (TextView) contentView.findViewById(R.id.tv_no);
//
//        Window window=getWindow();
//        LayoutParams wl = window.getAttributes();
//        if (popWnd.isShowing()){
//
////        wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//            wl.alpha=0.6f;
//            window.setAttributes(wl);
//        }else{
//            wl.alpha=1.0f;
//            window.setAttributes(wl);
//        }
//
//        tv_yes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                popWnd.dismiss();
//                if(file1.exists()){
//                    file1.delete();
//                }
//
//                Intent intent = new Intent(BaseActivity.this, ChooseWalletActivity.class);
//                startActivity(intent);
//                finish();
//
//            }
//        });
//
//        tv_no.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                popWnd.dismiss();
//                showPopupPassword();
//
//            }
//        });
//    }



    @Override
    protected void onStop() {
        super.onStop();

        if (!isAppOnForeground()&&isFirst==true){
           // Log.e("My","进入后台");
            isFirst=false;
            intent = new Intent(this, MyService.class);
            intent.putExtra("isFlay",true);
            MyService.isFirst=true;
            startService(intent);

        }else if (isFirst==true){
            KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
          //  Log.e("My", "onStop: "+flag );
            if (flag){
                isFirst=false;
                MyService.isFirst=true;
                intent = new Intent(this, MyService.class);
                intent.putExtra("isFlay",true);
                startService(intent);
            }

        }

    }
    private Intent intent;
    private boolean isFirst=true;//是否在前台


    @Override
    protected void onDestroy() {
        super.onDestroy();
       // removeActivity(this);



    }
//    public void removeActivity(Activity activity) {
//       // Log.e("MySer", "removeActivity: "+      MyApp.activityList.size());
//        MyApp.activityList.remove(activity);
//        //Log.e("MySer", "removeActivity:2 "+      MyApp.activityList.size());
//    }
//
//    // activity管理：添加activity到列表
//    public void addActivity(Activity activity) {
//        if (null==MyApp.activityList){
//            MyApp.activityList=new ArrayList<>();
//        }
//        //Log.e("MySer", "addActivity: ");
//        MyApp.activityList.add(activity);
//    }
//
//    // activity管理：结束所有activity
//    public void finishAllActivity() {
//        for (Activity activity :   MyApp.activityList) {
//            if (null != activity) {
//                activity.finish();
//            }
//        }
//    }

    public boolean isAppOnForeground() {
                // Returns a list of application processes that are running on the
                // device

                ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.
                        ACTIVITY_SERVICE);
                String packageName = getApplicationContext().getPackageName();

                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                                .getRunningAppProcesses();
                if (appProcesses == null)
                        return false;

                for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                      // The name of the process that this object is associated with.
                        if (appProcess.processName.equals(packageName)
                                       && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                return true;
                        }
                }

            return false;
        }


    public  boolean isServiceExisted() {
        String className="com.tang.trade.tang.ui.service.MyService";
        ActivityManager activityManager = (ActivityManager) this
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }





    /**
     * 监听在线状态
     */
//    private void addConnectionListener() {
//        mConnectionListener = new IYWConnectionListener() {
//            @Override
//            public void onDisconnect(int code, String info) {
//                if (code == YWLoginCode.LOGON_FAIL_KICKOFF) {
//
//                    //在其它终端登录，当前用户被踢下线
//                    normalDialog =
//                            new AlertDialog.Builder(BaseActivity.this);
//                    normalDialog.setTitle("警告");
//                    normalDialog.setMessage("该账号在别处登录,您被迫下线");
//                    normalDialog.setPositiveButton("确定",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    IYWLoginService loginService = mIMKit.getIMCore().getLoginService();
//                                    loginService.logout(new IWxCallback() {
//
//                                        @Override
//                                        public void onSuccess(Object... arg0) {
//                                            MyApp.set(BuildConfig.USERNAME, "");
//                                            MyApp.set(BuildConfig.USERNAME,"");
//                                            MyApp.set(BuildConfig.ID,"");
//                                            MyApp.set(BuildConfig.MEMOKEY,"");
//                                            startActivity(new Intent(BaseActivity.this, OptionAccountActivity.class));
//                                            finish();
//                                        }
//
//                                        @Override
//                                        public void onProgress(int arg0) {
//
//                                        }
//
//                                        @Override
//                                        public void onError(int errCode, String description) {
//                                        }
//                                    });
//                                }
//                            });
//                    normalDialog.setCancelable(false);
//                    try{
//                        normalDialog.create().show();
//                    }catch(Exception e){
//
//                    }
//
//
//
//                    //在其它终端登录，当前用户被踢下线
//                    normalDialog =
//                            new AlertDialog.Builder(BaseActivity.this);
//                    normalDialog.setTitle("警告");
//                    normalDialog.setMessage("该账号在别处登录,您被迫下线");
//                    normalDialog.setPositiveButton("确定",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    IYWLoginService loginService = mIMKit.getIMCore().getLoginService();
//                                    loginService.logout(new IWxCallback() {
//
//                                        @Override
//                                        public void onSuccess(Object... arg0) {
//                                            MyApp.set(BuildConfig.USERNAME, "");
//                                            MyApp.set(BuildConfig.USERNAME,"");
//                                            MyApp.set(BuildConfig.ID,"");
//                                            MyApp.set(BuildConfig.MEMOKEY,"");
//                                            startActivity(new Intent(BaseActivity.this, OptionAccountActivity.class));
//                                            finish();
//                                        }
//
//                                        @Override
//                                        public void onProgress(int arg0) {
//
//                                        }
//
//                                        @Override
//                                        public void onError(int errCode, String description) {
//                                        }
//                                    });
//                                }
//                            });
//                    normalDialog.setCancelable(false);
//                    if (!BaseActivity.this.isFinishing()) {
//                        normalDialog.create().show();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onReConnecting() {
//
//            }
//
//            @Override
//            public void onReConnected() {
//
//            }
//        };
//        mIMKit.getIMCore().addConnectionListener(mConnectionListener);
//    }


}
