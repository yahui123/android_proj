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
import com.tang.trade.tang.net.model.AccountResponseModel;

public class AcceptanceBottomDialog extends Dialog {

    private Button btn_submit;
    private LinearLayout line_tap;
    private ImageView iv_cancel;
    private Context mContext;

    private ImageView iv_pay_icon;
    private TextView tv_pay_name;
    private TextView tv_pay_account;
    private TextView tv_pay_mothod;

    public AcceptanceBottomDialog(@NonNull Context context) {
        super(context, R.style.dialog_style);
        mContext=context;
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.layout_acceptant_bottom_dialog, null);

        line_tap = root.findViewById(R.id.line_tap);
        iv_pay_icon =root.findViewById(R.id.iv_pay_icon);
        tv_pay_name =root.findViewById(R.id.tv_pay_name);
        tv_pay_account =root.findViewById(R.id.tv_pay_account);
        tv_pay_mothod =root.findViewById(R.id.tv_pay_mothod);



        btn_submit = root.findViewById(R.id.btn_submit);
        iv_cancel=root.findViewById(R.id.iv_cancel);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDialogOnClick.OnSubmitListener();
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
        setCanceledOnTouchOutside(false);
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


    public void setDialogText(AccountResponseModel.DataBean zhiFuDate) {
        if (zhiFuDate.getTypeCode().equals("AP")) {
            tv_pay_mothod.setVisibility(View.GONE);
            iv_pay_icon.setImageResource(R.mipmap.acaept_alipay_icon);
            tv_pay_name.setText(mContext.getString(R.string.bds_full_name) + " " + zhiFuDate.getName() + "");
            tv_pay_account.setText(mContext.getString(R.string.bds_apliay_account) + " " + zhiFuDate.getPayAccountID());

        } else if (zhiFuDate.getTypeCode().equals("WC")) {
            tv_pay_mothod.setVisibility(View.GONE);
            iv_pay_icon.setImageResource(R.mipmap.accept_wx_icon);
            tv_pay_name.setText(mContext.getString(R.string.bds_weChat_name) + " " + zhiFuDate.getName() + "");
            tv_pay_account.setText(mContext.getString(R.string.bds_weChat_account) + " " + zhiFuDate.getPayAccountID());
        } else {
            iv_pay_icon.setImageResource(R.mipmap.accept_bank_icon);
            tv_pay_mothod.setText(zhiFuDate.getBank());
            tv_pay_name.setText(mContext.getString(R.string.bds_full_name) + "" + zhiFuDate.getName());
            tv_pay_account.setText(mContext.getString(R.string.banknum) + " " + zhiFuDate.getPayAccountID());
        }

    }

}
