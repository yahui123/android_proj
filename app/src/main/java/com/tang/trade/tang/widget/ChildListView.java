package com.tang.trade.tang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Administrator on 2017/10/10.
 */

public class ChildListView extends ListView {
    public ChildListView(Context context) {
        super(context);
    }
    public ChildListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ChildListView(Context context, AttributeSet attrs,
                         int defStyle) {
        super(context, attrs, defStyle);
    }


//    public boolean onTouch(View v, MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE:
//                requestDisallowInterceptTouchEvent(true);
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                requestDisallowInterceptTouchEvent(false);
//                break;
//        }
//    }


    @Override
    /**
     * 重写该方法，达到使ListView适应ScrollView的效果
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}