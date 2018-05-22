package com.tang.trade.kchar.widget;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarLineChartBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harpoonman on 2016/5/9 0009.
 */
public class TouchLL extends LinearLayout {
    private PointF startPoint = new PointF();
    private int mode = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private float startDis;// 开始距离
    private PointF midPoint;// 中间点

    private int currentLeftIndex = 0;
    private float xMin = 0;

    private Context mContext;

    private List<Integer> chartList = new ArrayList<>();

    public void setChartList(List<Integer> chartList) {
        this.chartList = chartList;
    }

    public interface OnDispatchTouchEventListener {
        void OnDispatchTouchEvent(MotionEvent event);
    }

    private OnDispatchTouchEventListener listener = new OnDispatchTouchEventListener() {
        @Override
        public void OnDispatchTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    startPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
//                        LogUtils.i("KLINE", "ACTION_MOVE_DRAG" + "--" + xAll);

                        float dx = event.getX() - startPoint.x;// 得到在x轴的移动距离
                        if (dx > 10f || dx < -10f) {


//                        xAll += dx;
                            for (Integer integer : chartList) {
                                BarLineChartBase barLineChartBase = ((BarLineChartBase) findViewById(integer));

                                int count = barLineChartBase.getMaxVisibleCount();
                                float chartWidth = barLineChartBase.getViewPortHandler().getChartWidth();
                                int low = barLineChartBase.getLowestVisibleXIndex();
                                xMin = chartWidth / count;

                                int index = (int) (low - dx / xMin);
                                barLineChartBase.moveViewToX(index);
                                float sx = barLineChartBase.getScaleX();
                            }
                            startPoint.set(event.getX(), event.getY());
                        }

                    } else if (mode == ZOOM) {
                        float endDis = distance(event);
                        float dis = endDis - startDis;
                        if ((dis > 10f || dis < -10f) && endDis != 0) {

                            float scale = endDis / startDis;// 计算放大的scale

                            for (Integer integer : chartList) {
                                ((BarLineChartBase) findViewById(integer)).zoom(scale, 0, midPoint.x, midPoint.y);
                            }
                            startDis = endDis;

                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    startDis = distance(event);
                    midPoint = mid(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
            }
        }
    };

    public TouchLL(Context context) {
        this(context, null);
    }

    public TouchLL(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchLL(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void addChart(int rId){
        chartList.add(rId);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (listener != null) {
            listener.OnDispatchTouchEvent(ev);
        }
        boolean dispatch = super.dispatchTouchEvent(ev);
        return dispatch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        LogUtils.i("KLINE", "TouchLL-TouchEvent");

        return super.onTouchEvent(event);
    }


    /**
     * 计算两点之间的距离
     *
     * @param event
     * @return
     */
    public static float distance(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0;
        }
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算两点之间的中间点
     *
     * @param event
     * @return
     */
    public static PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
}
