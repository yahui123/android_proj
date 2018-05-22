package com.tang.trade.module.splash;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.data.local.BorderDao;
import com.tang.trade.data.local.BorderDatabase;
import com.tang.trade.data.local.BorderEntity;
import com.tang.trade.data.local.DatabaseManager;
import com.tang.trade.data.model.entity.ConfigMessage;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.module.profile.login.LoginActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.MyIndexPagerAdapter;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.model.UpdateResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.MainActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.TLog;
import com.tang.trade.utils.SPUtils;
import com.tang.trade.utils.ScreenUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.disposables.Disposable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by dagou on 2017/9/11.
 */

public class OldLaunchActivity extends AppCompatActivity {

    private static final int REDIRECT_DELAY = 0;
    private static final int WAITING_DATA = 3000;

    private static final int UI_ANIMATION_DELAY = 100;
    private static final int APK_INSTALL_CODE = 300;
    public static String lang;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    LinearLayout group;
    private FrameLayout faYidao;
    private ViewPager viewPager;
    private GifImageView fullscreen_content;
    private TextView btn_confirm;//
    /**
     * 装点点的ImageView数组
     */
    private ImageView[] tips;
    /**
     * 装ImageView数组
     */
    private ImageView[] mImageViews;
    private int index = 0;
    private View mContentView;
    private PopupWindow popWnd;
    private ProgressDialog progressDialog;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private LinearLayout ll_progress;
    private PopupWindow popUpProgress;
    private UpdateResponseModel updateResponse;
    private Thread mRedirectToHandler = new Thread(() -> redirectTo());
    private Thread mRedirectToHandler1 = new Thread(() -> {
        if (index == 0) {
            redirectTo();
        }

    });
    private BorderDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

        setContentView(R.layout.activity_launch);

        mContentView = findViewById(R.id.fullscreen_content);
        frameLayout = (FrameLayout) findViewById(R.id.bg);
        faYidao = findViewById(R.id.fa_yidao);
        viewPager = findViewById(R.id.viewPager);
        group = (LinearLayout) findViewById(R.id.viewGroup);
        fullscreen_content = findViewById(R.id.fullscreen_content);
        btn_confirm = findViewById(R.id.btn_confirm);


        if (ScreenUtils.getScreenAspect(this) > 1.9) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 280);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            btn_confirm.setLayoutParams(layoutParams);
        }

        //获取配置提示信息
        getMessageConfig();

        setLanguage();
        boolean isFlase = MyApp.get("isFlaseLoginBorderless", true);
        if (isFlase) {
            MyApp.set("isFlaseLoginBorderless", false);
            fullscreen_content.setVisibility(View.GONE);
            faYidao.setVisibility(View.VISIBLE);

            setYIdaoIndex();//导航页
        } else {
            checkUpdate();
            mHideHandler.postDelayed(mRedirectToHandler, 4000);
        }

        Intent intent = getIntent();
        if (intent != null) {
            if (MainActivity.mainActivity != null) {
                MainActivity.mainActivity.finish();
            }
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (intent.ACTION_VIEW.equals(action)) {
                    String url = intent.getDataString().replace("file:/", "/");
                    url = Uri.decode(url);
                    Log.i("wallet_bin", url);
                    if (url.contains(".txt") || url.contains(".bin") || url.contains(".doc") || url.contains(".pdf")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
                        Date backTime = new Date();
                        String strTime = sdf.format(backTime);
                        String strFileName = "wallet_" + strTime + ".bin";
                        Log.i("wallet_bin", strFileName);
                        FileUtils.copyFile(new File(url), Environment.getExternalStorageDirectory() + "/bds/borderless/bin/", strFileName);
                    } else {
                        MyApp.showToast(getString(R.string.bds_invalid_format));
                    }
                }
            }
        }

    }


    /**
     * 从服务器获取提示信息
     */
    private void getMessageConfig() {

        BorderDao borderDao = DatabaseManager.getInstance().borderDao();

        String versionId = SPUtils.getString(Const.SP.ROOM_VERSION_ID, "0");
        String useType = "APP";
        HttpDataManager.getInstance().getMessageConfig(useType, versionId, new BorderObserver<ConfigMessage>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ConfigMessage configMessage) {

                int versionIdServer = configMessage.getVersionId();

                if (versionIdServer != Integer.valueOf(versionId)) {

                    SPUtils.put(Const.SP.ROOM_VERSION_ID, String.valueOf(versionIdServer));

                    List<BorderEntity> list = new ArrayList<>();

                    if (null != configMessage.getMsgs()) {

                        for (ConfigMessage.Msgs msgs : configMessage.getMsgs()) {
                            BorderEntity borderEntity = new BorderEntity();
                            borderEntity.setCode(msgs.getCode());
                            borderEntity.setMsg(msgs.getMsg());
                            borderEntity.setMsgKey(msgs.getMsgKey());
                            list.add(borderEntity);
                        }

                        borderDao.removeAll(list);

                        borderDao.insertAll(list);
                    }
                }

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(String errorMsg) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        PackageInstallReceiver.registerReceiver(this);
        if (BuildConfig.UPDATE) {
            if (!BuildConfig.IS_UPDATE) {
                redirectTo();
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)   //可读
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  //可写
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)  //电话
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)  //dadianhu
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  //可写
                        != PackageManager.PERMISSION_GRANTED) {

            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA},
                    1);

        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    checkUpdate();
//                    mHideHandler.postDelayed(mRedirectToHandler1, 4000);
//                    MyApp.set("isPermision",true);
//                } else {
//                    MyApp.showToast(getString(R.string.bds_permision_err));
//                    finish();
//                }
//                return;
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BuildConfig.UPDATE = false;
        BuildConfig.IS_UPDATE = false;
//        PackageInstallReceiver.unregisterReceiver(this);
    }

    private void setYIdaoIndex() {
        int[] imgIdArray = new int[3];
        if (OldLaunchActivity.lang.equals("en")) {
            imgIdArray[0] = R.mipmap.iv_guide_left_us;
            imgIdArray[1] = R.mipmap.iv_guide_mobile_us;
            imgIdArray[2] = R.mipmap.iv_guide_right_us;

        } else {
            imgIdArray[0] = R.mipmap.guide_1;
            imgIdArray[1] = R.mipmap.guide_2;
            imgIdArray[2] = R.mipmap.guide_3;
        }
        //将点点加入到ViewGroup中
        tips = new ImageView[imgIdArray.length];
        for (int i = 0; i < tips.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
            tips[i] = imageView;
            if (i == 0) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(dip2px(0), dip2px(0)));
            layoutParams.leftMargin = dip2px(10);
            layoutParams.rightMargin = dip2px(10);
            group.addView(imageView, layoutParams);
        }

        //将图片装载到数组中
        mImageViews = new ImageView[imgIdArray.length];
        for (int i = 0; i < mImageViews.length; i++) {
            ImageView imageView = new ImageView(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(getWindowManager().getDefaultDisplay().getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            mImageViews[i] = imageView;
            imageView.setImageResource(imgIdArray[i]);
        }
        //设置Adapter
        viewPager.setAdapter(new MyIndexPagerAdapter(mImageViews, tips));
        //设置监听，主要是设置点点的背景
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectTo();
            }
        });
        btn_confirm.setVisibility(View.GONE);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setImageBackground(position % mImageViews.length);
                if (position == 2) {
                    btn_confirm.setVisibility(View.VISIBLE);

                } else {
                    if (btn_confirm.getVisibility() == View.VISIBLE)
                        btn_confirm.setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //设置ViewPager的默认项, 设置为长度的100倍，这样子开始就能往左滑动
        //viewPager.setCurrentItem((mImageViews.length) * 100);
        viewPager.setCurrentItem(0);
    }

    private void checkUpdate() {
        TangApi.getUpdateInfo(new TangApi.MyBaseViewCallBack<UpdateResponseModel>() {
            @Override
            public void start() {
            }

            @Override
            public void onEnd() {
            }

            @Override
            public void setData(UpdateResponseModel updateResponseModel) {
                updateResponse = updateResponseModel;
            }

        });
    }

    private void redirectTo() {
        if (updateResponse != null) {
            int i = Device.compareVersion(updateResponse.getAndversion(), String.valueOf(Device.getVersionCode()));
            if (i == 1) {
                Random random = new Random();
                int nextInt = random.nextInt(updateResponse.getAndurl().size());
                try {
                    fullscreen_content.setImageResource(R.mipmap.qidongye);
                    showPopup(updateResponse, nextInt);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                frameLayout.setAlpha(0.6f);
                frameLayout.setVisibility(View.VISIBLE);
                //index = 1;
            } else {
                Intent intent = new Intent(this, LoginActivity.class);


                startActivity(intent);
                overridePendingTransition(R.anim.new_fade_in, R.anim.new_fade_out);
                finish();
            }

        } else {
            Intent intent = new Intent(this, LoginActivity.class);

            startActivity(intent);
            overridePendingTransition(R.anim.new_fade_in, R.anim.new_fade_out);
            finish();
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void hide() {
        // Hide UI firs
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void showProgressDialog() {
        progressDialog.show();
    }

    private void ProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialog, which) -> {

        });
    }

    private void showPopupProgress() {
        View contentView = LayoutInflater.from(OldLaunchActivity.this).inflate(R.layout.popup_progress, null);
        progressBar = (ProgressBar) contentView.findViewById(R.id.progressbar);
        progressBar.setMax(100);
        tvProgress = (TextView) contentView.findViewById(R.id.text_progressbar);
        ll_progress = (LinearLayout) contentView.findViewById(R.id.ll_progress);

        popUpProgress = new PopupWindow(OldLaunchActivity.this);
        popUpProgress.setContentView(contentView);
        popUpProgress.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
        popUpProgress.setWidth(dip2px(300));
        popUpProgress.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popUpProgress.setOutsideTouchable(false);
        popUpProgress.showAtLocation(View.inflate(this, R.layout.activity_launch, null), Gravity.CENTER, 0, 0);
    }

    private void showPopup(final UpdateResponseModel updateResponseModel, final int i) {
        View contentView = LayoutInflater.from(OldLaunchActivity.this).inflate(R.layout.download_alert, null);
        popWnd = new PopupWindow(OldLaunchActivity.this);
        popWnd.setContentView(contentView);
        popWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_rect));
        popWnd.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popWnd.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popWnd.setOutsideTouchable(false);
        popWnd.showAtLocation(View.inflate(OldLaunchActivity.this, R.layout.activity_launch, null), Gravity.CENTER, 0, 0);
        TextView tvContent = (TextView) contentView.findViewById(R.id.content);

        if (!MyApp.get(BuildConfig.LANGUAGE, "").equals("")) {
            if (MyApp.get(BuildConfig.LANGUAGE, "").contains("繁體中文")) {
                tvContent.setText(updateResponseModel.getContents_tw());
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("简体中文")) {
                tvContent.setText(updateResponseModel.getContents_cn());
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("English")) {
                tvContent.setText(updateResponseModel.getContents());
            }
        } else {
            String locale = getResources().getConfiguration().locale.getCountry();
            if (locale.equalsIgnoreCase("TW") || locale.equalsIgnoreCase("HK")) {
                tvContent.setText(updateResponseModel.getContents_tw());
                //简体
            } else if (locale.equalsIgnoreCase("CN")) {
                tvContent.setText(updateResponseModel.getContents_cn());
                //英文
            } else {
                tvContent.setText(updateResponseModel.getContents());
            }
        }

        contentView.findViewById(R.id.confirm).setOnClickListener(v -> {
            popWnd.dismiss();

            TangApi.downloadApk(
                    updateResponseModel.getAndurl().get(i)
                    , new TangApi.BaseViewCallBackWithProgress<File>() {
                        @Override
                        public void setData(File file) {
                            Device.openFile(OldLaunchActivity.this, file);
                            // finish();
                        }

                        @Override
                        public void setProgress(Progress progress) {
//                                progressDialog.setMax((int) progress.totalSize);
//                                progressDialog.setProgress((int) progress.currentSize);
                            TLog.log("progress.currentSize/progress.totalSize)" + (float) progress.currentSize / (float) progress.totalSize);
                            TLog.log("(int) ((progress.currentSize/progress.totalSize)*100)===" + (((float) progress.currentSize / (float) progress.totalSize) * 100));
                            TLog.log("((int) ((progress.currentSize/progress.totalSize)*100))+\"%\"===" + ((float) ((progress.currentSize / progress.totalSize) * 100)) + "%");
                            progressBar.setProgress((int) ((progress.currentSize / (float) progress.totalSize) * 100));
                            tvProgress.setText((int) ((((float) progress.currentSize / (float) progress.totalSize) * 100)) + "%");
                        }

                        @Override
                        public void onStart(Request<File, ? extends Request> request) {

//                                ProgressDialog();
//                                showProgressDialog();
                            showPopupProgress();
                        }

                        @Override
                        public void onFinish() {
//                                progressDialog.dismiss();
                            frameLayout.setVisibility(View.INVISIBLE);
                            popUpProgress.dismiss();
                        }
                    });
        });

        contentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameLayout.setVisibility(View.INVISIBLE);
                popWnd.dismiss();

                if (Boolean.parseBoolean(updateResponseModel.getStatus())) {
                    finish();
                    System.exit(0);
                } else {
                    updateResponse = null;
                    mHideHandler.postDelayed(mRedirectToHandler, REDIRECT_DELAY);
                    popWnd.dismiss();
                }
            }
        });
    }

    private void setLanguage() {
        if (!MyApp.get(BuildConfig.LANGUAGE, "").equals("")) {
            if (MyApp.get(BuildConfig.LANGUAGE, "").contains("繁體中文")) {
                settingLanguage(Locale.TRADITIONAL_CHINESE);
                lang = "zh_HK";
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("简体中文")) {
                settingLanguage(Locale.SIMPLIFIED_CHINESE);
                lang = "zh_CN";
            } else if (MyApp.get(BuildConfig.LANGUAGE, "").contains("English")) {
                settingLanguage(Locale.ENGLISH);
                lang = "en";
            }
        } else {
//            settingLanguage(Locale.ENGLISH);

            String locale = getResources().getConfiguration().locale.getCountry();
            //繁体
            if (locale.equalsIgnoreCase("TW") || locale.equalsIgnoreCase("HK")) {
                lang = "zh_HK";
                //简体
            } else if (locale.equalsIgnoreCase("CN")) {
                lang = "zh_CN";
                //英文
            } else {
                lang = "en";
            }
        }
    }

    private void settingLanguage(Locale locale) {
        Resources resources = getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = locale; // 英文
        resources.updateConfiguration(config, dm);
    }


    public int dip2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 设置选中的tip的背景
     *
     * @param selectItems
     */
    private void setImageBackground(int selectItems) {
        for (int i = 0; i < tips.length; i++) {
            if (i == selectItems) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }
        }
    }

}
