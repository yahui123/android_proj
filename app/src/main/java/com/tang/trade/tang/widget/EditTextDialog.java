package com.tang.trade.tang.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.QRCodeUtil;

/**
 * Created by Administrator on 2017/6/29.
 */
public class EditTextDialog {
    private Context context;
    private AlertDialog ad;
    final TextView tv_info;
    ImageView iv_cancel;
    EditText et_Password;

    TextView tv_copy;

    public EditTextDialog(Context context) {
        this.context = context;
        ad = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).create();
        ad.setInverseBackgroundForced(false);
        ad.show();
        //关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
        Window window = ad.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setContentView(R.layout.layout_dialog_editext);
        et_Password = window.findViewById(R.id.et_Password);
        tv_info = window.findViewById(R.id.tv_info);
        iv_cancel = window.findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(view -> ad.dismiss());
        tv_copy = window.findViewById(R.id.tv_copy);
        tv_copy.setOnClickListener(view -> {
            if (null != onDialogOnClick) {
                onDialogOnClick.OnSumbmitListener(et_Password.getText().toString().trim() + "");
            }

        });
        ad.setCanceledOnTouchOutside(false);
    }

    /*判断是否显示对话框*/
    public boolean isVisible() {
        return ad.isShowing();
    }

    /*关闭对话框*/
    public void dismiss() {
        ad.dismiss();
    }

    public OnDialogOnClick onDialogOnClick;

    public void setOnDialogOnClick(OnDialogOnClick onClick) {
        this.onDialogOnClick = onClick;
    }

    public interface OnDialogOnClick {
        void OnSumbmitListener(String pwd);
    }

}
