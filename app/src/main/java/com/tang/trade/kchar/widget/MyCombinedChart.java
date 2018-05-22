package com.tang.trade.kchar.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.renderer.YAxisRenderer;

/**
 * Created by zhangshihao on 2018/3/14.
 */

public class MyCombinedChart extends CombinedChart {

    public MyCombinedChart(Context context) {
        super(context);
    }

    public MyCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCombinedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mXAxis = new XAxis();
        mXAxisRenderer = new MyXAxisRenderer(mViewPortHandler, mXAxis, mLeftAxisTransformer, this);
    }

}
