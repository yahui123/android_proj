package com.tang.trade.tang.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.bumptech.glide.Glide;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AcceptorOpenModel;
import com.tang.trade.tang.net.acceptormodel.AcceptotXiangqingModel;
import com.tang.trade.tang.net.acceptormodel.GongGaoModel;
import com.tang.trade.tang.net.acceptormodel.IsResponseModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.AcceptanceManagerActivity;
import com.tang.trade.tang.ui.AcceptantOpenActivity;
import com.tang.trade.tang.ui.BorrowingActivity;
import com.tang.trade.tang.ui.ConversationActivity;
import com.tang.trade.tang.ui.GongGaoActivity;
import com.tang.trade.tang.ui.HelpCenterActivity;
import com.tang.trade.tang.ui.MeActivity;
import com.tang.trade.tang.ui.MeAssetsActivity;
import com.tang.trade.tang.ui.NewReceptionActivity;
import com.tang.trade.tang.ui.PhoneAdministrationActivity;
import com.tang.trade.tang.ui.PingCangActivity;
import com.tang.trade.tang.ui.SendOutActivity;
import com.tang.trade.tang.ui.TransferRecordctivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.GlideCircleTransform;
import com.tang.trade.tang.widget.BottomBarView;
import com.tang.trade.utils.IMListener;
import com.tang.trade.utils.SPUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.loginAccountId;
import static com.tang.trade.tang.utils.BuildConfig.strPubWifKey;

/**
 * A simple {@link Fragment} subclass.
 */
public class Me2Fragment extends Fragment implements View.OnClickListener {
    Unbinder unbinder;
    String username;

    @BindView(R.id.ll_witness)
    LinearLayout ll_witness;

    @BindView(R.id.ll_jieru)
    LinearLayout ll_jieru;

    @BindView(R.id.ll_pingcang)
    LinearLayout ll_pingcang;

    @BindView(R.id.ll_acceptance_tran)
    LinearLayout ll_acceptance_tran;

    @BindView(R.id.ll_anquan_center)
    LinearLayout ll_anquan_center;

    @BindView(R.id.tv_shezhi)
    LinearLayout tv_shezhi;

    @BindView(R.id.ll_xitong_gonggao)
    LinearLayout ll_xitong_gonggao;

    @BindView(R.id.ll_kefu_zhongxin)
    LinearLayout ll_kefu_zhongxin;

    @BindView(R.id.ll_transfer)
    LinearLayout ll_transfer;

    @BindView(R.id.btn_reception)
    Button btn_reception;
    @BindView(R.id.btn_sendout)
    Button btn_sendout;

    @BindView(R.id.iv_dengpao)
    ImageView iv_dengpao;
//    @BindView(R.id.tv_count)
//    TextView tv_count;

    @BindView(R.id.tvAccount)
    TextView tvAccount;

    @BindView(R.id.tv_zongzichan)//总资产
            TextView tv_zongzichan;

    @BindView(R.id.ll_my_assets)
    LinearLayout llMyAssets;

    @BindView(R.id.tv_acceptance_tran)
    TextView tv_acceptance_tran;


    @BindView(R.id.ll_bangzhu_zhongxin)
    LinearLayout ll_bangzhu_zhongxin;

    @BindView(R.id.iv_manage)
    BottomBarView iv_manage;

    @BindView(R.id.tv_id)
    TextView tv_id;


    @BindView(R.id.iv_icon)
    ImageView iv_icon;


    private boolean isAccportant = false;//判断是否是承兑商

    private IYWConversationUnreadChangeListener mConversationUnreadChangeListener;
    //
    public YWIMKit mIMKit;
    private IYWConversationService mConversationService;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public Me2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        username = SPUtils.getString(Const.USERNAME, "");
        View view = inflater.inflate(R.layout.fragment_me2, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        setData();
    }

    private void initView() {
        // Glide.with(this).load(R.drawable.mine_home_head_bg).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(gifView);
        btn_reception.setOnClickListener(this);
        btn_sendout.setOnClickListener(this);
        iv_manage.setMessageCount(0);

        llMyAssets.setOnClickListener(this);
        ll_witness.setOnClickListener(this);
        ll_jieru.setOnClickListener(this);
        ll_pingcang.setOnClickListener(this);
        ll_acceptance_tran.setOnClickListener(this);
        ll_anquan_center.setOnClickListener(this);
        tv_shezhi.setOnClickListener(this);
        ll_xitong_gonggao.setOnClickListener(this);
        ll_kefu_zhongxin.setOnClickListener(this);
        ll_transfer.setOnClickListener(this);
        iv_dengpao.setOnClickListener(this);
        iv_manage.setOnClickListener(this);
        ll_bangzhu_zhongxin.setOnClickListener(this);

        tvAccount.setText(SPUtils.getString(Const.USERNAME, ""));

        if (!TextUtils.isEmpty(username)) {
            mIMKit = YWAPI.getIMKitInstance(username, MyApp.APP_KEY);
            mConversationService = mIMKit.getConversationService();
            initConversationServiceAndListener();
        }
        if (loginAccountId != null)
            tv_id.setText("ID:" + loginAccountId.toString() + "");
    }

    private void setData() {
        getAccportant();
        getAccuntIcon();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (hidden == false) {
            getAccportant();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_my_assets:
                startActivity(new Intent(getActivity(), MeAssetsActivity.class));
                break;
            case R.id.ll_jieru:
                startActivity(new Intent(getActivity(), BorrowingActivity.class));
                break;
            case R.id.ll_pingcang:
                startActivity(new Intent(getActivity(), PingCangActivity.class));
                break;
            case R.id.ll_transfer:
                startActivity(new Intent(getActivity(), TransferRecordctivity.class));
                break;
            case R.id.btn_reception:
                startActivity(new Intent(getContext(), NewReceptionActivity.class));
                break;
            case R.id.btn_sendout:
                startActivity(new Intent(getContext(), SendOutActivity.class));
                break;
            //设置
            case R.id.iv_dengpao:
                break;

            case R.id.iv_manage:
                startActivity(new Intent(getActivity(), ConversationActivity.class));
                break;

            case R.id.ll_witness:
                Intent w = new Intent(getActivity(), MeActivity.class);
                w.putExtra("type", MeActivity.TYPE_JIANZHENGREN);
                startActivity(w);
                break;
            case R.id.ll_acceptance_tran:

                if (MyApp.get(SPUtils.getString(Const.USERNAME, "") + "isAccportant", false) == false) {
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
                                if (!TextUtils.isEmpty(response.body().getData().get(0).getSymbol())) {//管理
                                    startActivity(new Intent(getActivity(), AcceptanceManagerActivity.class));

                                } else if (!TextUtils.isEmpty(response.body().getData().get(0).getBdsAccountCo()) && TextUtils.isEmpty(response.body().getData().get(0).getSymbol())) {
                                    startActivity(new Intent(getActivity(), AcceptantOpenActivity.class).putExtra("payAccount", response.body().getData().get(0).getBdsAccountCo()));
                                } else {
                                    HashMap<String, String> hashMap = new HashMap<String, String>();
                                    hashMap.put("memberBdsAccount", SPUtils.getString(Const.USERNAME, ""));
                                    AcceptorApi.acceptantHttp(hashMap, "member_get_verify_state", new JsonCallBack<IsResponseModel>(IsResponseModel.class) {
                                        @Override
                                        public void onSuccess(Response<IsResponseModel> response) {
                                            if (response.body().getStatus().equals("success")) {
                                                IsResponseModel.DataBean dataBean = response.body().getData().get(0);
                                                if (dataBean != null) {
                                                    if (dataBean.getIsVerified().equals("1")) {//开通
                                                        acceptantOpen();
                                                    } else {//实名认证
                                                        startActivity(new Intent(getActivity(), PhoneAdministrationActivity.class).putExtra("type", "0"));
                                                    }
                                                } else {
                                                    MyApp.showToast(getString(R.string.network));
                                                }

                                            } else {
                                                MyApp.showToast(getString(R.string.network));
                                            }
                                        }

                                        @Override
                                        public void onStart(Request<IsResponseModel, ? extends Request> request) {
                                            super.onStart(request);


                                        }

                                        @Override
                                        public void onError(Response<IsResponseModel> response) {
                                            super.onError(response);
                                            MyApp.showToast(getString(R.string.network));
                                        }
                                    });

                                }
                            } else {
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                hashMap.put("memberBdsAccount", SPUtils.getString(Const.USERNAME, ""));
                                AcceptorApi.acceptantHttp(hashMap, "member_get_verify_state", new JsonCallBack<IsResponseModel>(IsResponseModel.class) {
                                    @Override
                                    public void onSuccess(Response<IsResponseModel> response) {
                                        if (response.body().getStatus().equals("success")) {
                                            IsResponseModel.DataBean dataBean = response.body().getData().get(0);
                                            if (dataBean != null) {
                                                if (dataBean.getIsVerified().equals("1")) {//开通
                                                    acceptantOpen();
                                                } else {//实名认证
                                                    startActivity(new Intent(getActivity(), PhoneAdministrationActivity.class).putExtra("type", "0"));
                                                }
                                            } else {
                                                MyApp.showToast(getString(R.string.network));
                                            }

                                        } else {
                                            MyApp.showToast(getString(R.string.network));
                                        }
                                    }

                                    @Override
                                    public void onStart(Request<IsResponseModel, ? extends Request> request) {
                                        super.onStart(request);


                                    }

                                    @Override
                                    public void onError(Response<IsResponseModel> response) {
                                        super.onError(response);
                                        MyApp.showToast(getString(R.string.network));
                                    }
                                });

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

                } else {
                    startActivity(new Intent(getActivity(), AcceptanceManagerActivity.class));
                }

                break;
            case R.id.ll_anquan_center:
                Intent an = new Intent(getActivity(), MeActivity.class);
                an.putExtra("type", MeActivity.TYPE_ANQUAN);
                startActivity(an);
                break;
            case R.id.tv_shezhi:
                Intent sh = new Intent(getActivity(), MeActivity.class);
                sh.putExtra("type", MeActivity.TYPE_SHEZHI);
                startActivity(sh);
                break;
            case R.id.ll_xitong_gonggao:
                startActivity(new Intent(getActivity(), GongGaoActivity.class));
                break;
            case R.id.ll_kefu_zhongxin:
                EServiceContact contact = new EServiceContact(BuildConfig.BDSSERVICE, 0);
                IMListener.startChatting(getContext(), contact);
//                Intent intent = mIMKit.getChattingActivityIntent(contact);
//                startActivity(intent);
                break;
            case R.id.ll_bangzhu_zhongxin:
                startActivity(new Intent(getActivity(), HelpCenterActivity.class));
                break;
            default:
                break;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 开通承兑商  "bdsAccount": "BDS1001"
     */
    private void acceptantOpen() {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));

        String signMessage = BitsharesWalletWraper.getInstance().getSignMessage(strPubWifKey);
        if (TextUtils.isEmpty(signMessage)) {
            MyApp.showToast(getString(R.string.encryption_failed));
            return;
        }
        hashMap.put("encmsg", signMessage);
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
        AcceptorApi.acceptantHttp(hashMap, "member_acceptor_creat", new JsonCallBack<AcceptorOpenModel>(AcceptorOpenModel.class) {
            @Override
            public void onSuccess(Response<AcceptorOpenModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    startActivity(new Intent(getActivity(), AcceptantOpenActivity.class).putExtra("payAccount", response.body().getData().get(0).getBdsaccountCo()));
                } else {
                    MyApp.showToast(response.body().getMsg());
                }
            }

            @Override
            public void onStart(Request<AcceptorOpenModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AcceptorOpenModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
            }
        });
    }


    private void initConversationServiceAndListener() {

        mConversationUnreadChangeListener = () -> mHandler.post(() -> {

            //获取当前登录用户的所有未读数
            int unReadCount = mConversationService.getAllUnreadCount();
            //设置桌面角标的未读数
//                        mIMKit.setShortcutBadger(unReadCount);
            iv_manage.setMessageCount(unReadCount);
        });
        mConversationService.addTotalUnreadChangeListener(mConversationUnreadChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConversationUnreadChangeListener != null) {
            mConversationService.removeTotalUnreadChangeListener(mConversationUnreadChangeListener);
        }
    }

    private void getAccportant() {
        isAccportant = MyApp.get(SPUtils.getString(Const.USERNAME, "") + "isAccportant", false);
        if (isAccportant) {
            tv_acceptance_tran.setText(getString(R.string.bds_acceptance_tran));
        } else {
            tv_acceptance_tran.setText(getString(R.string.bds_open_acceptant));
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
                        if (!TextUtils.isEmpty(response.body().getData().get(0).getSymbol())) {//管理
                            MyApp.set(SPUtils.getString(Const.USERNAME, "") + "isAccportant", true);
                            tv_acceptance_tran.setText(getString(R.string.bds_acceptance_tran));
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
                    MyApp.showToast(getContext().getString(R.string.network));
                }


            });
        }
    }


    private void getAccuntIcon() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsAccount", SPUtils.getString(Const.USERNAME, ""));
        hashMap.put("selfacc", "1");
        AcceptorApi.acceptantHttp(hashMap, "member_get_pic", new JsonCallBack<GongGaoModel>(GongGaoModel.class) {
            @Override
            public void onSuccess(Response<GongGaoModel> response) {
                if (response.body().getStatus().equals("success")) {
                    if (getActivity() != null) {
                        Glide.with(getActivity()).load(response.body().getData().get(0).getVaule()).placeholder(R.mipmap.iv_borderless_icon_write).error(R.mipmap.iv_borderless_icon_write).transform(new GlideCircleTransform(getActivity())).into(iv_icon);
                    }
                }
            }

            @Override
            public void onStart(Request<GongGaoModel, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<GongGaoModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));
            }


        });

    }

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SAVE_IS_HIDDEN, true);
    }
}
