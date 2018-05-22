package com.tang.trade.tang.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.QRCodeUtil;

/**
 * Created by Administrator on 2017/6/29.
 */
public class DeleteWalletDialog {
    private Context context;
    private AlertDialog ad;
    public DeleteWalletDialog(Context context) {
        this.context = context;
        ad = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).create();
        ad.setInverseBackgroundForced(false);
        ad.show();
        //关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
        Window window = ad.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.clear_wallet_alert);
        TextView tv_yes = (TextView) window.findViewById(R.id.tv_yes);
        TextView tv_no = (TextView) window.findViewById(R.id.tv_no);
        View left=window.findViewById(R.id.left);
        View right=window.findViewById(R.id.right);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null!=onDialogOnClick){
                    onDialogOnClick.OnSumbmitListener();
                }

            }
        });
        ad.setCanceledOnTouchOutside(true);


    }


    /*判断是否显示对话框*/
    public boolean isVisible() {
        return ad.isShowing();
    }

/*
     * 关闭对话框
    */
  public void dismiss() {
        ad.dismiss();

    }
    public OnDialogOnClick onDialogOnClick;
    public void setOnDialogOnClick(OnDialogOnClick onClick){
        this.onDialogOnClick=onClick;
    }
    public interface OnDialogOnClick {
        public void OnSumbmitListener();
    }

}
