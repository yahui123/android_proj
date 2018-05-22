package com.tang.trade.tang.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.timepicker.NumericWheelAdapter;
import com.tang.trade.tang.widget.timepickerwidget.OnWheelChangedListener;
import com.tang.trade.tang.widget.timepickerwidget.WheelView;


/**
 * Created by user on 2017/7/3.
 */

public class EvaluateDialog extends Dialog implements OnRatingBarChangeListener{

    private TextView tv_account;
    private RatingBar myRanking1;
    private RatingBar myRanking2;
    private RatingBar myRanking3;
    private Button btn_submit;
    private ImageView iv_cancel;
    private LinearLayout line_tap;
    public EvaluateDialog(@NonNull Context context) {
        super(context, R.style.dialog_style);
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.layout_evalute_dialog, null);

        line_tap=(LinearLayout) root.findViewById(R.id.line_tap);
        myRanking1=root.findViewById(R.id.myRanking1);

        myRanking2=root.findViewById(R.id.myRanking2);
        myRanking3=root.findViewById(R.id.myRanking3);
        btn_submit=(Button)root.findViewById(R.id.btn_submit);
        iv_cancel=root.findViewById(R.id.iv_cancel);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDialogOnClick.OnSumbmitListener((int)myRanking1.getRating()*2,(int)myRanking2.getRating()*2,(int)myRanking3.getRating()*2);
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        myRanking1.setOnRatingBarChangeListener(this);
        myRanking2.setOnRatingBarChangeListener(this);
        myRanking3.setOnRatingBarChangeListener(this);
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
    /**接口回调
     * */
    public OnDialogOnClick onDialogOnClick;
    public void setOnDialogOnClick(OnDialogOnClick onClick){
        this.onDialogOnClick=onClick;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (rating==0){
            ratingBar.setRating(1);
        }
    }

    public interface OnDialogOnClick {
        public void OnSumbmitListener(int rank1,int rank2,int rank3);
    }

    public void setDalogText(int myRand1,int myRand2,int myRand3){
        myRanking1.setNumStars(myRand1);
        myRanking1.setNumStars(myRand2);
        myRanking1.setNumStars(myRand3);

    }
    public void setMyRakOnClick(){

    }


}
