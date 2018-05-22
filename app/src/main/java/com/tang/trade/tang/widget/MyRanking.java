package com.tang.trade.tang.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tang.trade.tang.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/31.
 */

public class MyRanking extends LinearLayout{
    ImageView item0;
    ImageView item1;
    ImageView item2;
    ImageView item3;
    ImageView item4;
    boolean isFlay=false;
    int selectNum=6;//选择星星的个数
    Button.OnClickListener listener;
    List<ImageView> listItem=new ArrayList<>();
    public MyRanking(Context context) {
        super(context);
        inviDate(context);
    }

    public MyRanking(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inviDate(context);


    }


    public MyRanking(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inviDate(context);
    }

    private void inviDate(Context context) {
        LayoutInflater.from(context).inflate(R.layout.popup_evaluate_item, this, true);
        item0 = (ImageView) findViewById(R.id.menu_item0);
        item1 = (ImageView) findViewById(R.id.menu_item1);
        item2 = (ImageView) findViewById(R.id.menu_item2);
        item3 = (ImageView) findViewById(R.id.menu_item3);
        item4 = (ImageView) findViewById(R.id.menu_item4);
        listItem.add(item0);
        listItem.add(item1);
        listItem.add(item2);
        listItem.add(item3);
        listItem.add(item4);

    }
    /**设置点击事件
     * */
    public void setOnItemOnClick(){

        listener = new Button.OnClickListener(){//创建监听对象
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.menu_item0:
                        selectNum=2;
                        break;
                    case R.id.menu_item1:
                        selectNum=4;
                        break;
                    case R.id.menu_item2:
                        selectNum=6;
                        break;
                    case R.id.menu_item3:
                        selectNum=8;
                        break;
                    case R.id.menu_item4:
                        selectNum=10;
                        break;
                }
                setRanling(selectNum);

            }

        };
        for (int i=0;i<5;i++){
            listItem.get(i).setOnClickListener(listener);
        }
        setRanling(selectNum);

    }


    public void setRanling(double ranling){
        for (int i=0;i<5;i++){
            listItem.get(i).setImageResource(R.mipmap.grey_star);
        }
        if (ranling>0){
            int left = (int) (ranling/2);
            double mobile=ranling/2-left;
            for (int i=0;i<left;i++){
                listItem.get(i).setImageResource(R.mipmap.yellow_stars);
            }
            if (mobile>0){
                listItem.get(left).setImageResource(R.mipmap.half_star);
            }
        }
    }
    public int getSelectNum(){
        return selectNum;
    }


}
