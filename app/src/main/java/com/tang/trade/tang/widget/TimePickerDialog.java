package com.tang.trade.tang.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.timepicker.NumericWheelAdapter;
import com.tang.trade.tang.widget.timepickerwidget.OnWheelChangedListener;
import com.tang.trade.tang.widget.timepickerwidget.WheelView;

import java.util.Locale;


/**
 * Created by user on 2017/7/3.
 */

public class TimePickerDialog extends Dialog {

    private int startHous=0;
    private int startMins=0;
    private int endHous=0;
    private int endMin=0;
    private String type="";//start end
    private String typeStart="start";
    private String typeEnd="end";
    private Context context;
    private String format="%02d";
    private LayoutInflater inflater = null;
    private WheelView hour;
    private WheelView mins;
    private TextView tv_startTime,tv_endTime;
    public TimePickerDialog(@NonNull final Context context) {
        super(context, R.style.dialog_style);
        this.context=context;
        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.time_picker_layout, null);
        type=typeStart;

        tv_startTime=root.findViewById(R.id.tv_starTime);

        tv_startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="start";
                hour.setCurrentItem(startHous);
                mins.setCurrentItem(startMins);
                tv_startTime.setBackgroundColor(context.getResources().getColor(R.color.mine_accmanage_text_gray));
                tv_endTime.setBackgroundColor(context.getResources().getColor(R.color.common_white));


            }
        });
        tv_endTime=root.findViewById(R.id.tv_endTime);

        tv_endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="end";
                hour.setCurrentItem(endHous);
                mins.setCurrentItem(endMin);
                tv_startTime.setBackgroundColor(context.getResources().getColor(R.color.common_white));
                tv_endTime.setBackgroundColor(context.getResources().getColor(R.color.mine_accmanage_text_gray));


            }
        });


        hour = (WheelView) root.findViewById(R.id.hour);
        initHour();
        mins = (WheelView) root.findViewById(R.id.mins);
        initMins();
        // 设置当前时间
        hour.setCurrentItem(startHous);
        mins.setCurrentItem(startMins);
        tv_startTime.setText(context.getString(R.string.bds_start)+" "+getFormat(startHous)+":"+getFormat(startMins));
        tv_endTime.setText(context.getString(R.string.bds_end)+" "+getFormat(endHous)+":"+getFormat(endMin));


        //设置item个数、
        hour.setVisibleItems(7);
        mins.setVisibleItems(7);

        // 设置监听
        Button ok = (Button) root.findViewById(R.id.set);
        Button cancel = (Button) root.findViewById(R.id.cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onDialogOnClick.OnSumbmitListener(getFormat(startHous)+":"+getFormat(startMins),getFormat(endHous)+":"+getFormat(endMin));
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        LinearLayout cancelLayout = (LinearLayout) root.findViewById(R.id.view_none);
        cancelLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
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
    /**接口回调
     * */
    public OnDialogOnClick onDialogOnClick;
    public void setOnDialogOnClick(OnDialogOnClick onClick){
        this.onDialogOnClick=onClick;
    }
    public interface OnDialogOnClick {
        public void OnSumbmitListener(String startTime,String endTime);
    }

    private void initHour() {

        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(context,0, 23, "%02d");
       // numericWheelAdapter.setLabel(" 时");
        numericWheelAdapter.setLabel("");
        numericWheelAdapter.setTextSize(13);  //设置字体大小
        hour.setViewAdapter(numericWheelAdapter);
        hour.setCyclic(true);
        hour.setBackgroundColor(context.getResources().getColor(R.color.common_text_blank));
        hour.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (type.equals("start")){
                startHous=newValue;
                tv_startTime.setText(context.getString(R.string.bds_start)+" "+getFormat(startHous)+":"+getFormat(startMins));
            }else {
                endHous=newValue;
                tv_endTime.setText(context.getString(R.string.bds_end)+" "+getFormat(endHous)+":"+getFormat(endMin));
            }
            }
        });
    }

    /**
     * 初始化分
     */
    private void initMins() {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(context,0, 59, "%02d");
        numericWheelAdapter.setLabel("");
		numericWheelAdapter.setTextSize(13);//  设置字体大小
        mins.setViewAdapter(numericWheelAdapter);
        mins.setCyclic(true);
        mins.setBackgroundColor(context.getResources().getColor(R.color.common_text_blank));
        mins.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (type.equals("start")){
                    startMins=newValue;
                    tv_startTime.setText("开始 "+getFormat(startHous)+":"+getFormat(startMins));
                }else {
                    endMin=newValue;
                    tv_endTime.setText("结束 "+getFormat(endHous)+":"+getFormat(endMin));
                }
            }
        });
    }
    private String getFormat(int value){
        return format != null ? String.format(format, value) : Integer.toString(value);
    }
    public void setTime(String starTime,String endTime){
        startHous=0;
        startMins=0;
        endHous=0;
        endMin=0;
        hour.setCurrentItem(startHous);
        mins.setCurrentItem(startMins);
        tv_startTime.setText("开始 "+getFormat(startHous)+":"+getFormat(startMins));
        tv_endTime.setText("结束 "+getFormat(endHous)+":"+getFormat(endMin));
        type="start";

    }


}
