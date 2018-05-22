package com.tang.trade.tang.ui.fragment;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.AccptorSortAdapter;
import com.tang.trade.tang.adapter.BaseViewPagerAdapter;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.acceptormodel.AccoptorSortModel;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.ui.LaunchActivity;
import com.tang.trade.tang.ui.TransactionActivity;
import com.tang.trade.tang.ui.acceptorsfragment.MyXNAcceptorFragment;
import com.tang.trade.tang.ui.acceptorsfragment.MyZNAcceptorFragment;
import com.tang.trade.tang.utils.PopupWindowUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewAcceptorFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    Handler handler = new Handler();
    private TabLayout tablayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private ArrayList<String> listType = new ArrayList<String>();
    private BaseViewPagerAdapter mPagerAdapter;
    private EditText searchView;
    private ImageView tvPupList;
    private LinearLayout line_search;
    private LinearLayout lineGeway;
    private TextView tvGetWay;
    private MyZNAcceptorFragment myZNAcceptorFragment;
    private MyXNAcceptorFragment myXNAcceptorFragment;
    private ArrayList<AccoptorSortModel.DataBean> listSort = new ArrayList<>();
    private View view;
    private NewAcceptorFragment myFragmemt;
    private String sortKind = "";//排序
    private String baseSymbol;//
    private boolean isFrist = true;
    private PopupWindow mPopupWindow = null;
    private onZNSearchViewLister onZNSearchViewLister;
    private TextWatcher textBianHua = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!TextUtils.isEmpty(s.toString())) {
                onZNSearchViewLister.onSearchLister(s.toString());
            } else {
                onZNSearchViewLister.onSearchLister("");
            }

        }
    };


    public NewAcceptorFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        isFrist = false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (!hidden && null != myFragmemt && !isFrist) {
            if (onZNSearchViewLister != null) {
                onZNSearchViewLister.onJinruLister();
            }
        } else if (hidden && null != myFragmemt && !isFrist) {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0);
            }
            setSearchViewString();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_acceptor, container, false);
        myFragmemt = this;
        intiView(view);

        return view;
    }

    private void intiView(View view) {

        if (LaunchActivity.lang.equals("en")) {
            baseSymbol = "USD";
        } else {
            baseSymbol = "CNY";
        }
        searchView = (EditText) view.findViewById(R.id.searchView);
        line_search = (LinearLayout) view.findViewById(R.id.line_search);
        line_search.setOnClickListener(this);
        tvPupList = (ImageView) view.findViewById(R.id.tvPupList);
        lineGeway = (LinearLayout) view.findViewById(R.id.line_gateway_qiehuan);
        tablayout = view.findViewById(R.id.tablayout);
        tablayout.setSelectedTabIndicatorHeight(3);
        tvPupList.setOnClickListener(this);
        lineGeway.setOnClickListener(this);

        viewPager = (ViewPager) view.findViewById(R.id.vp_exchange);
        myZNAcceptorFragment = new MyZNAcceptorFragment();
        myXNAcceptorFragment = new MyXNAcceptorFragment();

        fragments.add(myZNAcceptorFragment);
        fragments.add(myXNAcceptorFragment);
        listType.add(baseSymbol + " " + getString(R.string.bds_acceptance));
        listType.add(getString(R.string.bds_cryptocurrency_acceptors));

        mPagerAdapter = new BaseViewPagerAdapter(getChildFragmentManager(), fragments, listType);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(0);

        viewPager.setAdapter(mPagerAdapter);

        tablayout.setupWithViewPager(viewPager);
        if (tablayout.getTabCount() > 4) {
            tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tablayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        LinearLayout linearLayout = (LinearLayout) tablayout.getChildAt(0);
        linearLayout.setPadding(0, 30, 0, 30);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        searchView.setOnClickListener(this);


        searchView.addTextChangedListener(textBianHua);

        acceptantSort(0, null);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.tvPupList) {
            if (listSort.size() != 0) {
                showPopupWindow(view);
            } else {
                acceptantSort(1, view);
            }

        } else if (view.getId() == R.id.ivBack) {

        } else if (view.getId() == R.id.line_search || view.getId() == R.id.searchView) {
            onZNSearchViewLister.onBacKTap();
        } else if (view.getId() == R.id.line_gateway_qiehuan) {
            startActivity(new Intent(getActivity(), TransactionActivity.class));

        }

    }

    private void showPopupWindow(View anchorView) {
        if (mPopupWindow != null)
            mPopupWindow.dismiss();
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_content_layout, null);
        View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click " + ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
            }
        };

        ListView listView = contentView.findViewById(R.id.list_item);
        AccptorSortAdapter array = new AccptorSortAdapter(listSort, getActivity());
        listView.setAdapter(array);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listSort.get(position).getSort().equals(sortKind)) {
                    sortKind = listSort.get(position).getSort();
                    onZNSearchViewLister.onKindSort(sortKind);
                }
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        //  mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //  mPopupWindow.setBackgroundDrawable(getResources().getDrawable( R.mipmap.blank_bg_pwd));
        // 如果设置PopupWindow的背景
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setFocusable(true);// 点击空白处时，隐藏掉pop窗口

        mPopupWindow.setAnimationStyle(R.style.accouptor_popwindow_anim_style);

//            mPopupWindow.showAsDropDown(anchorView, -20, 10);
        int windowPos[] = PopupWindowUtil.calculatePopWindowPos(anchorView, contentView);
        int xOff = 20;
        int yOff = 10;// 可以自己调整偏移
        windowPos[0] -= xOff;
        windowPos[1] += yOff;
        mPopupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);

    }

    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getActivity().getWindow().setAttributes(lp);
    }

    public void setOnZNSearchViewLister(onZNSearchViewLister onSearchViewLister) {
        this.onZNSearchViewLister = onSearchViewLister;
    }

    public void setSearchViewString() {
        //
        searchView.removeTextChangedListener(textBianHua);
        searchView.setText("");

        int i = viewPager.getCurrentItem();
        if (i == 0) {
            tvPupList.setVisibility(View.VISIBLE);
        } else {
            tvPupList.setVisibility(View.GONE);
        }
        searchView.addTextChangedListener(textBianHua);

    }

    private void acceptantSort(final int flay, final View view) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        AcceptorApi.acceptantHttp(hashMap, "acc_list_sort", new JsonCallBack<AccoptorSortModel>(AccoptorSortModel.class) {
            @Override
            public void onSuccess(Response<AccoptorSortModel> response) {
                if (response.body().getStatus().equalsIgnoreCase("success")) {
                    listSort.clear();
                    listSort.addAll(response.body().getData());
                    if (flay == 1) {
                        showPopupWindow(view);
                    }
                }
            }

            @Override
            public void onStart(Request<AccoptorSortModel, ? extends Request> request) {
                super.onStart(request);


            }

            @Override
            public void onError(Response<AccoptorSortModel> response) {
                super.onError(response);
                MyApp.showToast(getString(R.string.network));

            }
        });
    }

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

    public interface onZNSearchViewLister {
        public void onSearchLister(String strSearch);

        public void onKindSort(String sort);

        public void onJinruLister();//进入刷新

        public void onBacKTap();
    }
}
