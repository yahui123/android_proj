package com.tang.trade.tang.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tang.trade.tang.R;
import com.tang.trade.tang.socket.market.MarketStat;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2018/2/2.
 */

public class MyJieRuditextSeekbar extends LinearLayout implements View.OnTouchListener{




    private TextView tv_blance;//余额
    private EditText et_blance;//输入的保证金

    private EditText et_bili;
    private TextView tv_baifen;
    private TextView tv_min;
    private TextView tv_max;
    private AppCompatSeekBar seekBar;

    private LinearLayout line_fee;
    private  int INTBILI=100;//比例1
    private int ACCURACYNUM=2;//et 的精度位

    private int max=600;
    private int min=300;
    private int intProgress=0;
    private String finalBalance="0.00000";//总的bds余额
    private String tvBalace="0.00000";//减去保证金后的余额
    private String collateral="0.00";//原有的保证金
    private String collateral2="0.00";//原有的保证金
    private Context context;
    private String strProgress="0.00";
    private String debt = "0.00";//输入的借入（）
    private String feed_price = "0.00";//喂价

    private boolean isFirst=false;//是否已经初始化

    private int TYPE =0; //0 1

    public int getType() {
        return TYPE;
    }

    public void setType(boolean isflay){
        if (isflay)
            TYPE=0;
        else
            TYPE=1;
    }

    public boolean isFirst() {
        return isFirst;
    }
    public MyJieRuditextSeekbar(Context context) {
        super(context);
    }

    public MyJieRuditextSeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        LayoutInflater.from(context).inflate(R.layout.line_jieru_myeditext_seekbar, this);
        initView();


    }



    private void initView() {
        this.tv_blance=this.findViewById(R.id.tv_banlace);
        this.et_blance=this.findViewById(R.id.et_baozheng_quate);
        this.et_bili = this.findViewById(R.id.et_fee);
        this.tv_baifen = this.findViewById(R.id.tv_baifen);
        this.tv_min = this.findViewById(R.id.tv_min);
        this.tv_max = this.findViewById(R.id.tv_max);
        this.seekBar = this.findViewById(R.id.seekbar);
        this.line_fee=this.findViewById(R.id.line_fee);


    }
    public void setSeekBar(String strMin, String strMax){
        this.min=new BigDecimal(strMin).multiply(new BigDecimal(INTBILI)).setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue();
        this.max=new BigDecimal(strMax).multiply(new BigDecimal(INTBILI)).setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue();
        this.seekBar.setMax(max-min);
        this.intProgress=min;
        strProgress=strMin;
        this.seekBar.setProgress(0);
        tv_min.setText(NumberUtils.formatNumber2(strMin));
        tv_max.setText(NumberUtils.formatNumber2(strMax));


    }
    public void initData(final String debts, final String collaterals, String feed_prices) {
        this.isFirst=true;
        TYPE=1;

        try{
            this.collateral=collaterals;
            this.collateral2=collaterals;
            this.feed_price=feed_prices;
            this.debt=debts;
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                intProgress = (int) (Double.parseDouble(NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(collateral),Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(feed_price))),2)))*100);
            }else {
                intProgress=min;
            }
            this.strProgress=CalculateUtils.div(intProgress,INTBILI,ACCURACYNUM);
            et_bili.setText(strProgress);

            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }else {
                seekBar.setProgress(intProgress-min);
            }

            et_blance.setText(NumberUtils.formatNumber2(collateral));


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        et_bili.addTextChangedListener(watcher);
        et_blance.addTextChangedListener(watcher);
        this.et_blance.setOnTouchListener(this);
        this.et_bili.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress==0){
                    strProgress=CalculateUtils.div(min,INTBILI,ACCURACYNUM);
                }else {
                    double rate=CalculateUtils.add(progress,min);
                    strProgress=CalculateUtils.div(rate,INTBILI,ACCURACYNUM);

                }
                et_bili.setText(strProgress);
                if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                    collateral2 = CalculateUtils.mul(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(strProgress))), Double.parseDouble(feed_price));
                }else {
                    collateral2="0.00";
                }

                et_blance.setText(collateral2);


                onRefresh.onRefresh(strProgress,collateral2);

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
                            line_fee.setFocusable(true);
                            line_fee.setFocusableInTouchMode(true);
                           selectTouch = 0;

                           String progress1 = et_bili.getText().toString().trim();
                           String balace1 = et_blance.getText().toString().trim();
                           if (TextUtils.isEmpty(progress1)){
                               progress1="0.00";
                           }
                            if (TextUtils.isEmpty(balace1)){
                                balace1="0.00";
                            }

                           if (Double.parseDouble(progress1) != Double.parseDouble(strProgress)) {
                               initData(progress1);
                               onRefresh.onRefresh(strProgress,collateral2);

                           } else if (Double.parseDouble(balace1) != Double.parseDouble(collateral2)) {
                                initDataCo(balace1);
                               onRefresh.onRefresh(strProgress,collateral2);

                           }


                       }}


                });

    }


/**重置
 * */

    public void initUpdateData(final String debts, final String collaterals, String feed_prices) {
        TYPE=1;

        this.collateral="0.00000";//原有的保证金
        this.collateral2 ="0.00000";//原有的保证金
        this.strProgress=CalculateUtils.div(min,INTBILI,ACCURACYNUM);
        this.intProgress=min;
        this.debt = "0.00000";//借入
        this.feed_price = "0.00";//喂价
        try{
            this.collateral=collaterals;
            this.collateral2=collaterals;
            this.feed_price=feed_prices;
            this.debt=debts;
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                intProgress = (int) (Double.parseDouble(NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(collateral),Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(feed_price))),2)))*100);
            }else {
                intProgress=min;
            }
            this.strProgress=CalculateUtils.div(intProgress,INTBILI,ACCURACYNUM);
            et_bili.setText(strProgress);

            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }else {
                seekBar.setProgress(intProgress-min);
            }

            et_blance.setText(collateral);


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


    }
    /**
     * 更新进度
     * */

    public void initData(final String strProgresss){
        try{
            this.intProgress= (int) (Double.parseDouble(strProgresss)*INTBILI);

            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }else {
                seekBar.setProgress(intProgress-min);
            }
            this.strProgress=CalculateUtils.div(intProgress,INTBILI,ACCURACYNUM);
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                collateral2 = CalculateUtils.mul(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(strProgress))), Double.parseDouble(feed_price));
            }else {
                collateral2="0.00";
            }

            et_bili.setText(strProgress);
            et_blance.setText(collateral2);

            tvBalace = "" + CalculateUtils.add(Double.parseDouble(finalBalance), CalculateUtils.sub(Double.parseDouble(collateral), Double.parseDouble(collateral2)));
            tv_blance.setText(context.getString(R.string.bds_keyong_quate) + NumberUtils.formatNumber5(tvBalace) + " BDS");


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * 介入不变更新保证金
     * */

    public void initDataCo(final String collateral2s){


        try{
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                intProgress = (int) (Double.parseDouble(NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(collateral2s),Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(feed_price))),2)))*100);

            }else {
                intProgress=min;
            }
            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }else {
                seekBar.setProgress(intProgress-min);
            }
            this.strProgress=CalculateUtils.div(intProgress,INTBILI,ACCURACYNUM);
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                collateral2 = CalculateUtils.mul(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(strProgress))), Double.parseDouble(feed_price));
            }else {
                collateral2 = "0.00";
            }

            et_bili.setText(strProgress);


            et_blance.setText(collateral2);
            tvBalace = "" + CalculateUtils.add(Double.parseDouble(finalBalance), CalculateUtils.sub(Double.parseDouble(collateral), Double.parseDouble(collateral2)));
            tv_blance.setText(context.getString(R.string.bds_keyong_quate) + NumberUtils.formatNumber5(tvBalace) + " BDS");

            onRefresh.onRefresh(strProgress,collateral2);


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * 介入修改
     * */

    public void initDataDebt(final String debts){

        try{
            this.debt=debts;
            intProgress=(int)Double.parseDouble(CalculateUtils.mul(Double.parseDouble(strProgress),100));
            if (intProgress<min){
                intProgress=min;
                seekBar.setProgress(0);
            }else if (intProgress>max){
                intProgress=max;
                seekBar.setProgress(max);
            }

            this.strProgress=CalculateUtils.div(intProgress,INTBILI,ACCURACYNUM);
            if (!TextUtils.isEmpty(debt) && !debt.equals("0") && Double.parseDouble(debt) != 0 && Double.parseDouble(feed_price) != 0) {
                collateral2 = CalculateUtils.mul(Double.parseDouble(CalculateUtils.mul(Double.parseDouble(debt),Double.parseDouble(strProgress))), Double.parseDouble(feed_price));
            }else {
                collateral2="0.00";
            }
            et_blance.setText(collateral2);
            tvBalace = "" + CalculateUtils.add(Double.parseDouble(finalBalance), CalculateUtils.sub(Double.parseDouble(collateral), Double.parseDouble(collateral2)));
            tv_blance.setText(context.getString(R.string.bds_keyong_quate) + NumberUtils.formatNumber5(tvBalace) + " BDS");

            onRefresh.onRefresh(strProgress,collateral2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


    }



    public void updateBDSBlance(String finalBalance){
        this.finalBalance=finalBalance;
        this.tvBalace=finalBalance;
        tv_blance.setText(context.getString(R.string.bds_keyong_quate) + NumberUtils.formatNumber5(finalBalance) + " BDS");
    }




    public MyJieRuditextSeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            String temp = s.toString().trim();
            //
            if (s.toString().startsWith(".")) {
                et_bili.setText("");
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
        if (v.getId()==R.id.et_fee){
            if (!v.hasFocus()){
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }else {
                return false;
            }


        }else if (v.getId()==R.id.et_baozheng_quate){
            if (!v.hasFocus()){
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }else {
                return false;
            }
        }


       return false;


    }

    public interface onRefreshData {
        public void onRefresh(String strProgress,String collateral2);
    }
}
