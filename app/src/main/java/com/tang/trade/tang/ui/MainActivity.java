package com.tang.trade.tang.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.ViewServer;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBaseActivity;
import com.tang.trade.module.profile.generateqrcode.GenerateQrCodeActivity;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HideTradeResponseModel;
import com.tang.trade.tang.net.model.IsAccResponseModel;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.fragment.HomePageFragment;
import com.tang.trade.tang.ui.fragment.Me2Fragment;
import com.tang.trade.tang.ui.fragment.NewAcceptorFragment;
import com.tang.trade.tang.ui.fragment.ReChangeFragment2;
import com.tang.trade.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseActivity {
    public static final int home = 0;
    public static final int trade = 1;
    public static final int accept = 2;
    public static final int me = 3;
    public int selectFragment = -1;

    public static MainActivity mainActivity;

    private TextView tv_home;
    private TextView tv_me;
    private RelativeLayout rl_me;
    private ImageView iv_me;
    private TextView tv_exchange;
    private TextView tv_acceptance;
    private TextView tv_message_count;

    private HomePageFragment homeFragment;
    private Me2Fragment meFragment;
    private ReChangeFragment2 reChangeFragment;
    private NewAcceptorFragment acceptanceFragment;

    public HomePageFragment getHomeFragment() {
        return homeFragment;
    }

    public ReChangeFragment2 getReChangeFragment() {
        return reChangeFragment;
    }

    public static ArrayList<HideTradeResponseModel.DataBean> list = new ArrayList<>();

    public static boolean isAcc = false;

    private IYWConversationUnreadChangeListener mConversationUnreadChangeListener;

    private IYWConversationService mConversationService;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AlertDialog askDialog;


    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_home:
                if (selectFragment != home) {
                    switchSelectState(home);
                    addFragment(R.id.container, homeFragment);
                    reChangeFragment.setChildHidden();
                }

                break;
            case R.id.rl_me:
            case R.id.tv_me:
            case R.id.iv_me:
                if (selectFragment != me) {
                    switchSelectState(me);
                    addFragment(R.id.container, meFragment);
                    reChangeFragment.setChildHidden();
                }
                break;
            case R.id.tv_exchange:
                if (selectFragment != trade) {
                    switchSelectState(trade);
                    addFragment(R.id.container, reChangeFragment);
                    reChangeFragment.setChildVisible();
                }
                break;
            case R.id.tv_acceptance:
                if (selectFragment != accept) {
                    switchSelectState(accept);
                    addFragment(R.id.container, acceptanceFragment);
                    reChangeFragment.setChildHidden();
                }
                break;
        }
    }

    public void switchSelectState(int type) {
        this.selectFragment = type;
        if (type == home) {
            tv_home.setSelected(true);
            tv_me.setSelected(false);
            iv_me.setSelected(false);
            tv_exchange.setSelected(false);
            tv_acceptance.setSelected(false);
        } else if (type == trade) {
            tv_me.setSelected(false);
            iv_me.setSelected(false);
            tv_home.setSelected(false);
            tv_exchange.setSelected(true);
            tv_acceptance.setSelected(false);
        } else if (type == accept) {
            tv_me.setSelected(false);
            iv_me.setSelected(false);
            tv_home.setSelected(false);
            tv_exchange.setSelected(false);
            tv_acceptance.setSelected(true);
        } else if (type == me) {
            tv_me.setSelected(true);
            iv_me.setSelected(true);
            tv_home.setSelected(false);
            tv_exchange.setSelected(false);
            tv_acceptance.setSelected(false);
        }
    }

    @Override
    public void initView() {
        mainActivity = this;
        tv_message_count = findViewById(R.id.tv_message_count);
        rl_me = findViewById(R.id.rl_me);
        rl_me.setOnClickListener(this);
        iv_me = findViewById(R.id.iv_me);
        iv_me.setOnClickListener(this);
        tv_home = findViewById(R.id.tv_home);
        tv_home.setOnClickListener(this);
        tv_me = findViewById(R.id.tv_me);
        tv_me.setOnClickListener(this);
        tv_exchange = findViewById(R.id.tv_exchange);
        tv_exchange.setOnClickListener(this);
        tv_acceptance = findViewById(R.id.tv_acceptance);
        tv_acceptance.setOnClickListener(this);

        homeFragment = new HomePageFragment();
        meFragment = new Me2Fragment();
        reChangeFragment = new ReChangeFragment2();
        acceptanceFragment = new NewAcceptorFragment();

        addFragment(R.id.container, homeFragment);
        tv_home.setSelected(true);


        TangApi.isAcc(SPUtils.getString(Const.USERNAME, ""), new JsonCallBack<IsAccResponseModel>(IsAccResponseModel.class) {
            @Override
            public void onSuccess(Response<IsAccResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    isAcc = true;
                } else {
                    isAcc = false;
                }
            }

            @Override
            public void onError(Response<IsAccResponseModel> response) {
                super.onError(response);
                isAcc = false;
            }

            @Override
            public void onStart(Request<IsAccResponseModel, ? extends Request> request) {
                super.onStart(request);
            }
        });

        TangApi.getHideTrade(new JsonCallBack<HideTradeResponseModel>(HideTradeResponseModel.class) {
            @Override
            public void onSuccess(Response<HideTradeResponseModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    list.addAll(response.body().getData());
                }
            }
        });
        setCoPublicKey();

        mConversationService = mIMKit.getConversationService();
        initConversationServiceAndListener();
        initQrCode();

    }

    @Override
    public void initData() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            if (listener != null) {
                listener.onTouch(ev);
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<>(10);

    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }

    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener);
    }

    public interface MyOnTouchListener {
        void onTouch(MotionEvent ev);
    }

    public void setCoPublicKey() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("acceptantBdsAccount", SPUtils.getString(Const.USERNAME, ""));
        hashMap.put("selfacc", "1");
        AcceptorApi.acceptantHttp(hashMap, "get_acceptant_info", new JsonCallBack<AcceptotXiangqingModel>(AcceptotXiangqingModel.class) {
            @Override
            public void onSuccess(Response<AcceptotXiangqingModel> response) {
                if (response.body().getStatus().equals("success")) {

                    if (!TextUtils.isEmpty(response.body().getData().get(0).getBsdcopubkey())) {
                        MyApp.BDS_CO_PUBLICKEY = response.body().getData().get(0).getBsdcopubkey();
                    }
                }
            }

            @Override
            public void onStart(Request<AcceptotXiangqingModel, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<AcceptotXiangqingModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectFragment = -1;

        ViewServer.get(this).removeWindow(this);

        if (mConversationUnreadChangeListener != null) {
            mConversationService.removeTotalUnreadChangeListener(mConversationUnreadChangeListener);
        }
        if (askDialog != null) {
            askDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewServer.get(this).addWindow(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ViewServer.get(this).setFocusedWindow(this);
    }

    private void initConversationServiceAndListener() {

        mConversationUnreadChangeListener = new IYWConversationUnreadChangeListener() {
            @Override
            public void onUnreadChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        //获取当前登录用户的所有未读数
                        int unReadCount = mConversationService.getAllUnreadCount();
                        //设置桌面角标的未读数
//                        mIMKit.setShortcutBadger(unReadCount);
                        if (unReadCount > 0) {
                            if (unReadCount > 99) {
                                tv_message_count.setText("99+");
                            } else {
                                tv_message_count.setText(unReadCount + "");
                            }
                            tv_message_count.setVisibility(View.VISIBLE);
                        } else {
                            tv_message_count.setVisibility(View.GONE);
                        }
                    }
                });
            }
        };
        mConversationService.addTotalUnreadChangeListener(mConversationUnreadChangeListener);
    }

    public void showCancleableDialog(String title, String msg, String positiveBtnText, String cancelBtnText, final AbsBaseActivity.DialogCallBack callBack) {
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

    private void initQrCode() {
        String userName = SPUtils.getString(Const.USERNAME, "");
        String qr_content = SPUtils.getString(userName + Const.QR_CONTENT, "");
        if (TextUtils.isEmpty(qr_content)) {


            showCancleableDialog("提示", "您的账户还未备份", "立即备份", "下次再说", new AbsBaseActivity.DialogCallBack() {
                @Override
                public void onSuccess() {
                    GenerateQrCodeActivity.start(MainActivity.this);
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }
}
