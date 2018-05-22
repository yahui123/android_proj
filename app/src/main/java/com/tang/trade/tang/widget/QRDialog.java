package com.tang.trade.tang.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.QRCodeUtil;

/**
 * Created by Administrator on 2017/6/29.
 */
public class QRDialog {
    private Context context;
    private AlertDialog ad;
    TextView tv_name;
    ImageView iv_qr;
    final TextView tv_info;
    ImageView iv_cancel;

    TextView tv_copy;

    public QRDialog(Context context) {
        this.context = context;
        ad = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).create();
        ad.setInverseBackgroundForced(false);
        ad.show();
        //关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
        Window window = ad.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.layout_dialog_qr);
        tv_name = (TextView) window.findViewById(R.id.tv_type);
        iv_qr = (ImageView) window.findViewById(R.id.iv_qr);
        tv_info = (TextView) window.findViewById(R.id.tv_info);
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
        iv_cancel=window.findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        tv_copy=window.findViewById(R.id.tv_copy);
        tv_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null!=onDialogOnClick){
                    onDialogOnClick.OnSumbmitListener();
                }

            }
        });
        ad.setCanceledOnTouchOutside(true);


    }
    public void setQrImageBitmap(String payCode,Bitmap logo){
        iv_qr.setImageBitmap(QRCodeUtil.getLogoQRBitMap(payCode,logo));
    }
    public void setMessage(String name,String accountID){
        tv_name.setText(name);
        tv_info.setText(accountID);


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
