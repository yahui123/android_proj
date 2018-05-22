package com.tang.trade.tang.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;

public class BottomDialog extends Dialog {
    private TextView tv_account;
    private TextView tv_baozhengjian;
    private TextView tv_zhaiwu;
    private TextView tv_fee;
    private Button btn_submit;
    private LinearLayout line_tap;
    private ImageView iv_cancel;
    private LinearLayout ll_fee;
    private TextView type1;
    private TextView type2;
    private TextView type3;

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.dialog_style);
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.layout_bottom_dialog, null);

        line_tap = root.findViewById(R.id.line_tap);

        tv_account = root.findViewById(R.id.tv_account);
        tv_baozhengjian = root.findViewById(R.id.tv_baozhengjian);
        tv_zhaiwu = root.findViewById(R.id.tv_zhaiwu);
        iv_cancel = root.findViewById(R.id.iv_cancel);
        tv_fee = root.findViewById(R.id.tv_fee);

        type1 = root.findViewById(R.id.tv_type1);
        type2 = root.findViewById(R.id.tv_type2);
        type3 = root.findViewById(R.id.tv_type3);
        ll_fee = root.findViewById(R.id.ll_fee);

        btn_submit = root.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDialogOnClick.OnSubmitListener();
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        line_tap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContentView(root);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setWindowAnimations(R.style.dialog_animatoion_style); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = -20; // 新位置Y坐标
        lp.width = (int) context.getResources().getDisplayMetrics().widthPixels; // 宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
//      lp.alpha = 9f; // 透明度
        root.measure(0, 0);
        //lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    /**
     * 接口回调
     **/
    public OnDialogClick onDialogOnClick;

    public void setOnDialogClick(OnDialogClick onClick){
        this.onDialogOnClick = onClick;
    }

    public interface OnDialogClick {
        void OnSubmitListener();
    }

    public void setDialogText(String account, String baozhengjin, String zhaiwu, String fee){
        tv_account.setText(account);
        tv_baozhengjian.setText(baozhengjin);
        tv_zhaiwu.setText(zhaiwu);
        tv_fee.setText(fee);
    }

    public void setExchargeDalogText(String price,String amount,String turnover){
        tv_account.setText(price);
        tv_baozhengjian.setText(amount);
        tv_zhaiwu.setText(turnover);
        ll_fee.setVisibility(View.GONE);

        type1.setText(R.string.bds_price);
        type2.setText(R.string.bds_quantity);
        type3.setText(R.string.bds_turnover);
    }
}
