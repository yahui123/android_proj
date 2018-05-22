package com.tang.trade.tang.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;

/**
 * Created by Administrator on 2018/2/2.
 */

public class MyEditextSeekbar extends LinearLayout implements View.OnTouchListener{

    private EditText et_fee;
    private TextView tv_baifen;
    private TextView tv_min;
    private TextView tv_max;
    private AppCompatSeekBar seekBar;
    private LinearLayout line_fee;
    private  int INTBILI1=10000;//比例1
    private  int INTBILI2=100;//
    private int ACCURACYNUM=2;//et 的精度位
    private int ACCURACYNUM2=4; //精度位
    
    private int max=0;
    private int min=0;
    private int intProgress=0;
    private Context context;

    private boolean isFirst=false;//是否已经初始化

    public boolean isFirst() {
        return isFirst;
    }

    private String strRate;
    public MyEditextSeekbar(Context context) {
        super(context);
    }

    public MyEditextSeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        LayoutInflater.from(context).inflate(R.layout.line_myeditext_seekbar, this);
        initView();


    }

    private void initView() {
        et_fee = this.findViewById(R.id.et_fee);
        tv_baifen = this.findViewById(R.id.tv_baifen);
        tv_min = this.findViewById(R.id.tv_min);
        tv_max = this.findViewById(R.id.tv_max);
        seekBar = this.findViewById(R.id.seekbar);
        line_fee=this.findViewById(R.id.line_fee)
;
    }
    public void initData(final String strMax, String strMin, String strProgress) {

        try{
            this.strRate=strProgress;
            this.max=(int)Double.parseDouble((CalculateUtils.mul(Double.parseDouble(strMax),INTBILI1)));
            this.min=(int)Double.parseDouble((CalculateUtils.mul(Double.parseDouble(strMin),INTBILI1)));
            this.intProgress=(int)Double.parseDouble(CalculateUtils.mul(Double.parseDouble(strProgress),INTBILI1));

            seekBar.setMax(max-min);
            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }else {
                seekBar.setProgress(intProgress-min);
            }
            strRate=CalculateUtils.div(intProgress,INTBILI1,ACCURACYNUM2);
            String str =CalculateUtils.div(intProgress,INTBILI2,ACCURACYNUM);
            et_fee.setText(str);

            tv_min.setText(CalculateUtils.div(min,INTBILI2,ACCURACYNUM)+"%");
            tv_max.setText(CalculateUtils.div(max,INTBILI2,ACCURACYNUM)+"%");
            tv_baifen.setVisibility(VISIBLE);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        et_fee.addTextChangedListener(watcher);
        et_fee.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress==0){
                    et_fee.setText(CalculateUtils.div(min,INTBILI2,ACCURACYNUM));
                    strRate=CalculateUtils.div(min,INTBILI1,ACCURACYNUM2);
                }else {
                    double rate=CalculateUtils.add(progress,min);
                    strRate=CalculateUtils.div(rate,INTBILI1,ACCURACYNUM2);
                    String str =CalculateUtils.div(rate,INTBILI2,ACCURACYNUM);
                    et_fee.setText(str);
                }

                onRefresh.onRefresh(strRate);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        line_fee.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        line_fee.getWindowVisibleDisplayFrame(r);
                        int screenHeight = line_fee.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);
                        if (heightDifference > 200) {

                            //软键盘显示
                        } else {
                            //软键盘隐藏

                         updateData();

                       }
                    }

                });

    }


    public void updateData(){

        if (!TextUtils.isEmpty(et_fee.getText())){
            this.strRate=et_fee.getText().toString();
        }else {
            this.strRate="0";
        }
        this.intProgress=(int)Double.parseDouble((CalculateUtils.mul(Double.parseDouble(strRate),INTBILI2)));
        if (intProgress<min){
            intProgress=min;
            seekBar.setProgress(0);
            onRefresh.onRefresh(strRate);

        }else if (intProgress>max){
            intProgress=max;
            seekBar.setProgress(intProgress-min);
            onRefresh.onRefresh(strRate);

        }else {
            seekBar.setProgress(intProgress-min);
        }
        strRate=CalculateUtils.div((intProgress),INTBILI1,ACCURACYNUM2);
        String str =CalculateUtils.div((intProgress),INTBILI2,ACCURACYNUM);
        et_fee.setText(str);

    }


    public MyEditextSeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            String temp = s.toString().trim();
            //
            if (s.toString().startsWith(".")) {
                et_fee.setText("");
            }

            int posDot = temp.indexOf(".");
            if (posDot>0&&temp.length() - posDot - 1 > ACCURACYNUM) {
                s.delete(posDot + ACCURACYNUM + 1, posDot + ACCURACYNUM + 2);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };


    private onRefreshData onRefresh;
    public void setRefreshData(onRefreshData refreshData){
        this.onRefresh=refreshData;
    }
    private  static  int selectTouch=0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
    }

    public interface onRefreshData {
        public void onRefresh(String rate);
    }
}
