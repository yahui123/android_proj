package com.tang.trade.kchar.widget;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by zhangshihao on 2018/3/14.
 */

public class MyXAxisRenderer extends XAxisRenderer {

    private final BarLineChartBase mChart;
    protected XAxis mXAxis;

    public MyXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans, BarLineChartBase chart) {
        super(viewPortHandler, xAxis, trans);
        mXAxis = xAxis;
        mChart = chart;
    }

    @Override
    protected void drawLabels(Canvas c, float pos, PointF anchor) {
        final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
        float[] position = new float[]{
                0f, 0f
        };

        for(int i = mMinX; i <= mMaxX; i += mXAxis.mAxisLabelModulus){
            position[0] = i;

            mTrans.pointValuesToPixel(position);

            if (mViewPortHandler.isInBoundsX(position[0])) {

                String label = mXAxis.getValues().get(i);

                if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                    // avoid clipping of the last
                    float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                    if ((width / 2 + position[0]) > mChart.getViewPortHandler().contentRight()) {
                        position[0] = mChart.getViewPortHandler().contentRight() - width / 2;

                    } else if ((position[0] - width / 2) < mChart.getViewPortHandler().contentLeft()) {
                        position[0] = mChart.getViewPortHandler().contentLeft() + width / 2;
                    }
                }

//                drawLabel(c, label, i, position[0], pos, anchor, labelRotationAngleDegrees);
//                String formattedLabel = mXAxis.getValueFormatter().getXValue(label, i, mViewPortHandler);

                c.drawText(label, position[0],
                        pos + Utils.convertPixelsToDp(mChart.getViewPortHandler().offsetBottom()),
                        mAxisLabelPaint);
            }
        }

//        int count = mXAxis.getXLabels().size();
//        for (int i = 0; i < count; i ++) {
//              /*获取label对应key值，也就是x轴坐标0,60,121,182,242*/
//            int ix = mXAxis.getXLabels().keyAt(i);
//            position[0] = ix;
//            /*在图表中的x轴转为像素，方便绘制text*/
//            mTrans.pointValuesToPixel(position);
//            /*x轴越界*/
//            if (mViewPortHandler.isInBoundsX(position[0])) {
//                String label = mXAxis.getXLabels().valueAt(i);
//                /*文本长度*/
//                int labelWidth = Utils.calcTextWidth(mAxisLabelPaint, label);
//                /*右出界*/
//                if ((labelWidth / 2 + position[0]) > mChart.getViewPortHandler().contentRight()) {
//                    position[0] = mChart.getViewPortHandler().contentRight() - labelWidth / 2;
//                } else if ((position[0] - labelWidth / 2) < mChart.getViewPortHandler().contentLeft()) {//左出界
//                    position[0] = mChart.getViewPortHandler().contentLeft() + labelWidth / 2;
//                }
//                c.drawText(label, position[0],
//                        pos+ Utils.convertPixelsToDp(mChart.getViewPortHandler().offsetBottom()),
//                        mAxisLabelPaint);
//            }
//        }
    }
}
