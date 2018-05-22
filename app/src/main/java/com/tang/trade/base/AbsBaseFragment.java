package com.tang.trade.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by leo on 02/03/2018.
 */

public abstract class AbsBaseFragment extends Fragment {
    protected Activity mContext;
    Unbinder mUnbinder;
    private View mRootView;
    private boolean mFirstInit = true;    // 是否第一次初始化
    private AlertDialog askDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initArgs(getArguments());
    }

    /**
     * 初始化参数
     */
    protected void initArgs(Bundle arguments) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = getLayoutInflater().inflate(getLayoutResId(), container, false);
            initWidget(mRootView);
        } else {
            // 当视图存在时，需要将视图从其父控件中移除，
            // 以免二次加载，这是因为返回时会自动去加载到负布局
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (null != parent) {
                parent.removeView(mRootView);
            }
        }

        return mRootView;
    }

//    public void showAskDialog(String msg, final AbsBaseActivity.DialogCallBack callBack) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setMessage(msg);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                callBack.onSuccess();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                callBack.onCancel();
//            }
//        });
//        askDialog = builder.create();
//        if (!isDetached()){
//            askDialog.show();
//        }
//        askDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.txt_gray3));
//        if (SPUtils.getBoolean(Constants.SP.ISCAR, true)) {
//            askDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(),R.color.color_car));
//        }else {
//            askDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(),R.color.color_acs));
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mFirstInit) {
            mFirstInit = false;
            initFirst();
        }

        setupWidget(view, savedInstanceState);
    }

    /**
     * 只在第一次进来时才初始化的数据
     */
    protected void initFirst() {

    }

    /**
     * 初始化控件
     */
    protected void initWidget(View rootView) {
        mUnbinder = ButterKnife.bind(this, rootView);
    }

    /**
     * 设置页面控件
     */
    protected abstract void setupWidget(View view, Bundle savedInstanceState);


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    /**
     * 获得当前布局的Id，由子类实现
     *
     * @return 当前Activity的布局
     */
    protected abstract int getLayoutResId();

}
