package com.tang.trade.kchar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.tang.trade.kchar.constant.Type;
import com.tang.trade.kchar.entity.KLinePrice;
import com.tang.trade.kchar.util.DateUtils;
import com.tang.trade.kchar.util.KLineCal;
import com.tang.trade.kchar.widget.MyCombinedChart;
import com.tang.trade.tang.R;
import com.tang.trade.tang.adapter.KLineVerticalAdapter;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.model.AllHistory;
import com.tang.trade.tang.net.model.AllHistoryResult;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.TimeUtils;
import com.tang.trade.tang.widget.LoadingView;
import com.tang.trade.tang.widget.MyScrollViewListView;
import com.tang.trade.tang.widget.TextViewUtils;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class VerticalKLineActivity extends BaseActivity implements TangApi.MyBaseViewCallBack<String> {

    @BindView(R.id.tv_growth)
    TextView tvGrowth;
    @BindView(R.id.tv_growth_rate)
    TextView tvGrowthRate;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_high)
    TextView tvHigh;
    @BindView(R.id.tv_price_high)
    TextView tvPriceHigh;
    @BindView(R.id.tv_low)
    TextView tvLow;
    @BindView(R.id.tv_price_low)
    TextView tvPriceLow;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.tv_price_start)
    TextView tvPriceStart;
    @BindView(R.id.tv_volume)
    TextView tvVolume;
    @BindView(R.id.tv_price_volume)
    TextView tvPriceVolume;

    public static void launch(Activity from, String symbol) {
        Intent intent = new Intent(from, VerticalKLineActivity.class);
        intent.putExtra("symbol", symbol);
        from.startActivity(intent);
    }

    int i = 0;

    private static final String[] PERIOD_TYPE = new String[]{"分时", "1分", "5分", "30分", "60分", "日线", "周线"};
    private static final String[] PERIOD_Radio_TYPE = new String[]{"分时", "日线", "周线"};
    private static final String[] PERIOD_Pw_TYPE = new String[]{"1分", "5分", "30分", "60分"};
    private static final String[] AVERAGE_TYPE = new String[]{"MA", "EMA", "关闭"};
    private static final String[] KM_TYPE = new String[]{"MACD", "KDJ", "关闭"};

    private int defaultPeriodType = 5;
    private int defaultAveType = 0;
    private int defaultKmType = 2;

    private String symbol;
    private String type = Type.TYPE_1_MIN;
    private int size = 300;

    private int selectedValuePos = -1;
    private boolean isFirstLoadKline = true;
    private boolean isFirstLoadVolume = true;
    private boolean isFirstLoadMACD = true;


    private static final String SELL = "sell";
    private static final String BUY = "buy";

    private Map<String, String> mapData = new HashMap<>();


    @BindView(R.id.tv_kline_back)
    ImageView mTvBack;

    @BindView(R.id.tv_kline_volume_num)
    TextView mTvVolumeNum;
    @BindView(R.id.tv_kline_volume_ma7)
    TextView mTvVolumeMA7;
    @BindView(R.id.tv_kline_volume_ma30)
    TextView mTvVolumeMA30;

    @BindView(R.id.chart_kline)
    MyCombinedChart chartKline;
    @BindView(R.id.loading)
    LoadingView loading;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.radioTime)
    RadioGroup radioTime;

    @BindView(R.id.rbList)
    TextView rbList;

    @BindView(R.id.tv_new_price)
    TextView tvNewPrice;

//    @BindView(R.id.iv_zhangdie)
//    ImageView ivZhangDie;
//
//    @BindView(R.id.tv_new_price2)
//    TextView tvNewPrice2;
//
//    @BindView(R.id.tvZhangDie24)
//    TextView tvZhangDie24;
//
//    @BindView(R.id.tvTurnover24)
//    TextView tvTurnover24;
//
//    @BindView(R.id.tvMaxPrice24)
//    TextView tvMaxPrice24;
//
//    @BindView(R.id.tvMinPrice24)
//    TextView tvMinPrice24;

    @BindView(R.id.line_title)
    RelativeLayout lineTitle;

    @BindView(R.id.line_hoder)
    LinearLayout lineHoder;

    @BindView(R.id.line_view)
    View line_view;

    @BindView(R.id.viewLime)
    View viewLime;

    @BindView(R.id.tv_basesymbol)
    TextView tv_basesymbol;

    @BindView(R.id.tv_qouteSymbol)
    TextView tv_qouteSymbol;

    @BindView(R.id.btn_buy)
    Button btn_buy;

    @BindView(R.id.btn_sell)
    Button btn_sell;

    @BindView(R.id.iv_quanping)
    ImageView iv_quanping;

//    @BindView(R.id.tv_item1)
//    TextView tv_item1;
//    @BindView(R.id.tv_item2)
//    TextView tv_item2;
//    @BindView(R.id.tv_item3)
//    TextView tv_item3;
//    @BindView(R.id.tv_item4)
//    TextView tv_item4;

    private ArrayList<KLinePrice> klineprices = new ArrayList<KLinePrice>();

    private ArrayList<String> xVals;
    private ArrayList<String> xValsyMd;
    private ArrayList<String> xValsHm;


    private double[] priceEnds;
    private double[] priceBegin;
    private double[] priceHighs;
    private double[] priceLows;
    private double[] priceVolumes;

    private double[] ma7Price;
    private double[] ma30Price;

    private String baseSymbol = "CNY";
    private String qouteSymbol = "BDS";
    private boolean isPup = false;
    private boolean isFrist = true;
    Intent intent = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {//"latest":"","base_volume":"","percent_change":"","heighpice":"","lowerprice":"","items"
                requestSuccessStuff(type, baseSymbol, qouteSymbol);
                handler.postDelayed(thread, 2000);
//                handler.removeCallbacks(thread);
            }
        }
    };

    Thread thread = new Thread(() -> handler.sendEmptyMessage(0));

    List<AllHistoryResult> dateBuyOrSell = new ArrayList<>();
    KLineVerticalAdapter adapter;

    @BindView(R.id.kListView)
    MyScrollViewListView kListView;

    Thread thread1 = new Thread(() -> {
        try {
            getAllHistory(baseSymbol, qouteSymbol, 20);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.activity_vertical_kline);
//        baseSymbol = getIntent().getStringExtra("baseSymbol");
//        qouteSymbol = getIntent().getStringExtra("qouteSymbol");
//
//      //  ButterKnife.bind(this);
//        //initToolbar();
//        initViews();
//        initDatas();

        String color = getIntent().getStringExtra("color");

        lineTitle.setBackgroundColor(Color.parseColor(color));
        lineHoder.setBackgroundColor(Color.parseColor(color));

    }

    public static final String TRADETYPE = "tradeType";
    public static final int IBUY = 0;
    public static final int ISELL = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vertical_kline;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sell:
                intent.putExtra(TRADETYPE, ISELL);
                finish();
                break;
            case R.id.btn_buy:
                intent.putExtra(TRADETYPE, IBUY);
                finish();
                break;
            case R.id.iv_quanping:
                Intent intent = new Intent(this, KLineActivity.class);
                intent.putExtra("baseSymbol", baseSymbol).putExtra("qouteSymbol", qouteSymbol);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void initView() {
        intent = getIntent();

        baseSymbol = intent.getStringExtra("baseSymbol");
        qouteSymbol = intent.getStringExtra("qouteSymbol");

        initViews();
        initDatas();
    }

    @Override
    public void initData() {
        setResult(0, intent);
    }

    private void initViews() {
//        mLlKline.addChart(R.id.chart_kline);

//        mLlKline.addChart(R.id.chart_volume);
//        mLlKline.addChart(R.id.chart_macdkdj);

        adapter = new KLineVerticalAdapter(this, dateBuyOrSell);
        kListView.setAdapter(adapter);
        kListView.setFocusable(false);

        radioTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int position = 0;
                switch (checkedId) {
                    case R.id.rbMin1:
                        position = 0;
                        break;
                    case R.id.rbDay1:
                        position = 5;
                        break;
                    case R.id.rbweek:
                        position = 6;
                        break;
                }
                isFirstLoadKline = true;
                if (defaultPeriodType != position && isPup == false) {
                    defaultPeriodType = position;
                    type = synPeriodAndType(defaultPeriodType);
                    chartKline.setVisibility(View.INVISIBLE);
//                    handler.removeCallbacks(thread);
                    requestSuccessStuff(type, baseSymbol, qouteSymbol);
                    rbList.setSelected(false);
                    rbList.setText(getString(R.string.bds_select_min));
                }

            }
        });
        rbList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);

            }
        });

        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_sell.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        iv_quanping.setOnClickListener(this);
    }

    private void initChartCommon(MyCombinedChart chart, boolean XEnable) {
        chart.setDrawGridBackground(false); // 是否绘制背景颜色
        chart.setLogEnabled(false);
        chart.setDescription(""); // 图标默认右下角说明
        chart.getLegend().setEnabled(false); // 设置图例
        chart.setPinchZoom(false); // 设置X轴Y轴是否同时缩放

        chart.setVisibleXRangeMaximum(150);//一屏最多显示多少条
        chart.setScaleEnabled(false); // 是否可以缩放 x和y轴
        chart.setScaleXEnabled(true); //是否可以缩放 仅x轴
        chart.setDragEnabled(true); //是否可以拖拽
        chart.setScaleYEnabled(false); //是否可以缩放 仅Y轴
        chart.setDragDecelerationEnabled(false); // 必不可少 禁止惯性滑动
//        chart.setHighlightPerDragEnabled(true);// 高亮拖拽

//        chart.setTouchEnabled(false);

//        if (klineprices.size() >= 180) {
//            chart.setScaleMinima(4, 0);
//        } else if (klineprices.size() >= 145) {
//            chart.setScaleMinima(3, 0);
//        } else if (klineprices.size() >= 90) {
//            chart.setScaleMinima(2, 0);
//        }

        chart.getAxisLeft().setEnabled(false); // 是否显示左边的坐标轴
        chart.getAxisRight().setEnabled(true);  // 是否显示右边的坐标轴
        chart.setAutoScaleMinMaxEnabled(true); //
        chart.setExtraBottomOffset(15); // 设置数据与轴的间距
        XAxis x = chart.getXAxis();
        x.setEnabled(XEnable); // 是否显示X坐标轴及对应的刻度竖线
        x.resetLabelsToSkip();
        x.setAvoidFirstLastClipping(true);
        x.setDrawGridLines(false); // 是否显示X坐标轴上的刻度竖线
        x.setDrawAxisLine(false); // 是否绘制坐标轴的线，即含有坐标的那条线
        x.setSpaceBetweenLabels(5); // 坐标轴字间距
//        x.setLabelRotationAngle(-45); // X轴的字斜着显示
        x.setPosition(XAxis.XAxisPosition.BOTTOM); // 坐标轴位置
        x.setTextColor(ContextCompat.getColor(this, R.color.market_kline_text_grey)); // 字体颜色
//        x.setLabelsToSkip(klineprices.size() - 2);

        x.setValueFormatter(new XAxisValueFormatter() {
            @Override
            public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
                if (defaultPeriodType == 0 || (!rbList.getText().toString().equals(getString(R.string.bds_select_min)))) {
                    return DateUtils.dateFormatHm(Long.parseLong(xVals.get(i)));
                } else {
                    return DateUtils.dateFormatyMd(Long.parseLong(xVals.get(i)));
                }
            }
        });

        YAxis yRight = chart.getAxisRight();
        yRight.setDrawAxisLine(false);
        yRight.setDrawGridLines(false);
        yRight.setTextColor(ContextCompat.getColor(this, R.color.market_kline_text_grey));
        yRight.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yRight.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, YAxis yAxis) {
                return new DecimalFormat("###,##0.00").format(v);
            }
        });
    }

    private void initChartKline() {
        initChartCommon(chartKline, true);

        float min = Collections.min(klineprices).low;
        float max = Collections.max(klineprices).high;

        YAxis yRight = chartKline.getAxisRight();
//        yRight.setAxisMaxValue(max);
//        yRight.setAxisMinValue(min);
        yRight.calcMinMax(min, max);
        yRight.setLabelCount(5, true); // Y轴坐标个数和是否平均显示
        yRight.setStartAtZero(false); //设置Y轴坐标是否从0开始

//        CombinedData combinedData = new CombinedData(xVals);
        CombinedData combinedData = new CombinedData(defaultPeriodType == 0 || (!rbList.getText().toString().equals(getString(R.string.bds_select_min))) ? xValsHm : xValsyMd);


        List<Line> lines = new ArrayList<>();

        if (defaultPeriodType == 0) {
            lines.add(new Line("kline-end", priceEnds, R.color.market_kline_highlight));
        } else {
            combinedData.setData(generateCandleData());
        }

        ma7Price = KLineCal.showDoubleArray(KLineCal.MA(priceEnds, 7));
        ma30Price = KLineCal.showDoubleArray(KLineCal.MA(priceEnds, 30));


        lines.add(new Line("ma7", ma7Price, R.color.market_kline_text_orange));

        lines.add(new Line("ma30", ma30Price, R.color.market_kline_text_blue));

        combinedData.setData(generateLineData(lines));

        chartKline.setData(combinedData);

//        ViewPortHandler viewPortHandlerBar = chartKline.getViewPortHandler();
//        viewPortHandlerBar.setMaximumScaleX(xVals.size());
//        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
//        touchmatrix.postScale(1, 1f);
//        chartKline.notifyDataSetChanged();
        chartKline.invalidate();
        if (isFirstLoadKline) {
            chartKline.setVisibleXRangeMaximum(50);
            chartKline.moveViewToX(klineprices.size() - 1);
            chartKline.animateX(2000);
            isFirstLoadKline = false;
        }

        if (!isFirstLoadKline) {
            chartKline.setVisibleXRangeMaximum(200);
            chartKline.setVisibleXRangeMinimum(30);//一屏最少显示多少条

//            if (defaultPeriodType == 0 || defaultPeriodType == 6) {
//                if (klineprices.size() < 30) {
//                    chartKline.setVisibleXRangeMinimum(35);
//                }
//            }
        }
    }

    private CandleData generateCandleData() {
        ArrayList<CandleEntry> entries = new ArrayList<>();

        for (int index = 0; index < klineprices.size(); index++) {
            KLinePrice price = klineprices.get(index);
            entries.add(new CandleEntry(index, price.high, price.low, price.begin, price.end));
        }

        CandleDataSet set = new CandleDataSet(entries, "KlineMain");
//        set.setDecreasingColor(ContextCompat.getColor(this, R.color.kline_chart_line_red));
//        set.setIncreasingColor(ContextCompat.getColor(this, R.color.kline_chart_line_green));

        set.setIncreasingPaintStyle(Paint.Style.FILL);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        List<Integer> colors = new ArrayList<>();
        for (KLinePrice price : klineprices) {
            if (price.begin > price.end) {
                colors.add(ContextCompat.getColor(this, R.color.market_global_lower));
            } else {
                colors.add(ContextCompat.getColor(this, R.color.market_global_upper));
            }
        }
        set.setColors(colors);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setShadowWidth(0.7f);
        set.setHighLightColor(ContextCompat.getColor(this, R.color.market_kline_highlight));
        set.setDrawHighlightIndicators(true);
        set.setDrawVerticalHighlightIndicator(true);
        set.setDrawHorizontalHighlightIndicator(false);
//        CandleData candleData = new CandleData(xVals, set);
        CandleData candleData = new CandleData(defaultPeriodType == 0 || (!rbList.getText().toString().equals(getString(R.string.bds_select_min))) ? xValsHm : xValsyMd, set);

        return candleData;
    }

    private LineData generateLineData(List<Line> lines) {

        ArrayList<Entry> line7Entries = new ArrayList<>();
        ArrayList<Entry> line30Entries = new ArrayList<>();

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);

            if (line.name.equals("ma7")) {
                for (int index = 6; index < line.datas.length; index++) {
                    line7Entries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ma30")) {
                for (int index = 29; index < line.datas.length; index++) {
                    line30Entries.add(new Entry((float) line.datas[index], index));
                }
            }
        }

        if (ma7Price.length >= 7) {
            dataSets.add(setMaLine(line7Entries, "ma7", R.color.market_kline_text_orange));
        }

        if (ma30Price.length >= 30) {
            dataSets.add(setMaLine(line30Entries, "ma30", R.color.market_kline_text_blue));
        }

//        LineData data = new LineData(xVals, dataSets);
        LineData data = new LineData(defaultPeriodType == 0 || (!rbList.getText().toString().equals(getString(R.string.bds_select_min))) ? xValsHm : xValsyMd, dataSets);

        return data;
    }

//    private LineData generateLineData(List<Line> lines) {
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//
//        for (int i = 0; i < lines.size(); i++) {
//            Line line = lines.get(i);
//            ArrayList<Entry> entries = new ArrayList<>();
//            for (int index = 0; index < line.datas.length; index++) {
//                entries.add(new Entry((float) line.datas[index], index));
//            }
//            LineDataSet set = new LineDataSet(entries, line.name);
//            set.setDrawValues(false);
//            set.setDrawCubic(false);
//            set.setLineWidth(1f);
//            set.setAxisDependency(MyYAxis.AxisDependency.RIGHT);
//            set.setColor(ContextCompat.getColor(this, line.colorResourse));
//            set.setDrawFilled(false);
//            set.setHighLightColor(ContextCompat.getColor(this, R.color.kline_chart_text_vertical_white));
//            set.setDrawHorizontalHighlightIndicator(false);//取消显示高亮水平线
//            set.setDrawCircles(false);
//            dataSets.add(set);
//        }
//
//        LineData data = new LineData(xVals, dataSets);
//
//        return data;
//    }

    private LineDataSet setMaLine(ArrayList<Entry> lineEntries, String name, int colorResourse) {
        LineDataSet set = new LineDataSet(lineEntries, name);
        if (name.equals("ma7")) {
            set.setHighlightEnabled(true);
            set.setDrawHorizontalHighlightIndicator(false);//取消显示高亮水平线
            set.setHighLightColor(ContextCompat.getColor(this, R.color.market_kline_highlight));
        } else {
            set.setHighlightEnabled(false);
        }
        set.setDrawValues(false);
        set.setDrawCubic(false);
        set.setLineWidth(1f);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColor(ContextCompat.getColor(this, colorResourse));
        set.setDrawFilled(false);
        set.setHighlightEnabled(false);
        set.setDrawCircles(false);

        return set;
    }

    protected void initEvents() {
        OnChartValueSelectedListener onChartValueSelectedListener = new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                Highlight highlight = new Highlight(e.getXIndex(), h.getDataSetIndex(), h.getStackIndex());
                chartKline.highlightValues(new Highlight[]{highlight});
                handler.removeCallbacks(thread);
                selectedValuePos = e.getXIndex();
                updateIndicateData(selectedValuePos);
                updateTitleData(selectedValuePos, true);

            }

            @Override
            public void onNothingSelected() {
                chartKline.highlightValue(null);
                handler.post(thread);
            }
        };

        chartKline.setOnChartValueSelectedListener(onChartValueSelectedListener);

        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateTitleData(int i, boolean isTimeShow) {
        tvNewPrice.setText(NumberUtils.formatNumber5(String.valueOf(priceEnds[i])));
//        tvNewPrice.setText("" + priceEnds[i]);
        double change;
        double changeRate;
        if (i == 0) {
            change = KLineCal.sub(priceEnds[i], priceBegin[i]);
            changeRate = KLineCal.div(change, priceBegin[i]);
        } else {
            change = KLineCal.sub(priceEnds[i], priceEnds[i - 1]);
            changeRate = KLineCal.div(change, priceEnds[i - 1]);
        }
        if (!isTimeShow) {
            String percent_change = mapData.get("percent_change");
            if (TextUtils.isEmpty(percent_change))
                percent_change = "0.00";
            changeRate = Double.parseDouble(percent_change);
        }


        tvPriceHigh.setText(NumberUtils.formatNumber5(String.valueOf(priceHighs[i])));
        tvPriceLow.setText(NumberUtils.formatNumber5(String.valueOf(priceLows[i])));
        tvPriceStart.setText(NumberUtils.formatNumber5(String.valueOf(priceBegin[i])));
        tvPriceVolume.setText(NumberUtils.formatNumber5(String.valueOf(priceVolumes[i])));

        if (isTimeShow) {
            tvTime.setVisibility(View.VISIBLE);
            tvTime.setText(defaultPeriodType == 0 || (!rbList.getText().toString().equals(getString(R.string.bds_select_min))) ? xValsHm.get(i) : xValsyMd.get(i));
        } else {
            tvTime.setVisibility(View.INVISIBLE);
        }

        if (change > 0) {
            /* 进入页面时，由上一个页面传入色值。不再在此页面判断 */
//            lineTitle.setBackgroundColor(getResources().getColor(R.color.global_upper));
//            lineHoder.setBackgroundColor(getResources().getColor(R.color.global_upper));
            line_view.setBackgroundColor(getResources().getColor(R.color.market_global_upper));
            viewLime.setBackgroundColor(getResources().getColor(R.color.market_global_upper));
            tvGrowth.setText("+" + NumberUtils.formatNumber2(String.valueOf(change)));
            //tvGrowthRate.setText("+" + String.format("%.2f", ) + "%");

            if (!isTimeShow) {
                if (changeRate >= 0) {

                    tvGrowthRate.setText("+" + String.format("%.2f", changeRate) + "%");
                } else {

                    tvGrowthRate.setText(String.format("%.2f", changeRate) + "%");
                }
            } else {
                if (changeRate >= 0) {

                    tvGrowthRate.setText("+" + NumberFormat.getPercentInstance().format(changeRate));
                } else {

                    tvGrowthRate.setText(NumberFormat.getPercentInstance().format(changeRate));
                }
            }


            isFrist = false;
        } else {
//            lineTitle.setBackgroundColor(getResources().getColor(R.color.global_lower));
//            lineHoder.setBackgroundColor(getResources().getColor(R.color.global_lower));
            line_view.setBackgroundColor(getResources().getColor(R.color.market_global_lower));
            viewLime.setBackgroundColor(getResources().getColor(R.color.market_global_lower));
            tvGrowth.setText(NumberUtils.formatNumber2(String.valueOf(change)));
            //tvGrowthRate.setText(String.format("%.2f", mapData.get("percent_change")) + "%");

            if (!isTimeShow) {
                tvGrowthRate.setText(String.format("%.2f", changeRate) + "%");
            } else {
                tvGrowthRate.setText(NumberFormat.getPercentInstance().format(changeRate));
            }

            isFrist = false;
        }
    }

    private void updateIndicateData(int i) {

//        KLinePrice price = klineprices.get(i);

        int vis = defaultAveType == 2 ? View.GONE : View.VISIBLE;
        mTvVolumeMA7.setVisibility(vis);
        mTvVolumeMA30.setVisibility(vis);

//        mTvVolumeNum.setText("成交量(7,30):" + priceVolumes[i]);
        String time = "";
        if (defaultPeriodType == 0) {
            time = getString(R.string.bds_mins);
        } else if (defaultPeriodType == 5) {
            time = getString(R.string.bds_DayLine);
        } else if (defaultPeriodType == 6) {
            time = getString(R.string.bds_weekLine);
        } else {
            time = rbList.getText().toString();
        }
        String priceEnd = "";
        priceEnd = NumberUtils.formatNumber5(String.valueOf(priceEnds[i]));
        mTvVolumeNum.setText(time + "(7,30):" + priceEnd);

        mTvVolumeMA7.setText(i < 6 ? "MA7:——" : "MA7:" + ma7Price[i]);
        mTvVolumeMA30.setText(i < 29 ? "MA30:——" : "MA30:" + ma30Price[i]);
    }

    protected void initDatas() {
        // 读取本地存储的默认选择
        defaultPeriodType = 5;
        defaultAveType = 0;
        defaultKmType = 2;
        type = synPeriodAndType(defaultPeriodType);
        TextViewUtils.setTextView(tv_qouteSymbol, this, qouteSymbol);
        TextViewUtils.setTextView(tv_basesymbol, this, baseSymbol);
        if (0 < defaultAveType && defaultAveType < 5) {
            rbList.setText(PERIOD_TYPE[defaultPeriodType]);
            rbList.setSelected(true);
        }

        TextViewUtils.setTextView1(tv_title, this, baseSymbol, qouteSymbol);

        handler.post(thread);
//        requestSuccessStuff(type, baseSymbol, qouteSymbol);
        thread1.start();
    }

    private String synPeriodAndType(int periodCode) {
        String type;
        switch (periodCode) {
            case 0:
            case 1:
                type = Type.TYPE_1_MIN;
                break;
            case 2:
                type = Type.TYPE_5_MIN;
                break;
            case 3:
                type = Type.TYPE_30_MIN;
                break;
            case 4:
                type = Type.TYPE_ONE_HOUR;
                break;
            case 5:
                type = Type.TYPE_ONE_DAY;
                break;
            case 6:
                type = Type.TYPE_ONE_WEEK;
                break;
            default:
                type = Type.TYPE_1_MIN;
                break;
        }
        return type;
    }

    /**
     * 提取其中的每一部分价格作为double数组，方便计算K线时使用
     */
    private void parseKLinePriceParts() {
        int size = klineprices.size();
        xVals = new ArrayList<>();
        xValsyMd = new ArrayList<>();
        xValsHm = new ArrayList<>();
        priceEnds = new double[size];
        priceBegin = new double[size];
        priceHighs = new double[size];
        priceLows = new double[size];
        priceVolumes = new double[size];
        for (int i = 0; i < size; i++) {
            xVals.add("" + klineprices.get(i).time);
            xValsyMd.add("" + DateUtils.dateFormatyMd(Long.parseLong(xVals.get(i))));
            xValsHm.add("" + DateUtils.dateFormatyMdHm(Long.parseLong(xVals.get(i))));
            priceEnds[i] = klineprices.get(i).end;
            priceBegin[i] = klineprices.get(i).begin;
            priceHighs[i] = klineprices.get(i).high;
            priceLows[i] = klineprices.get(i).low;
            priceVolumes[i] = klineprices.get(i).volume;
        }
        priceVolumes = KLineCal.showDoubleArray(priceVolumes);
    }

    private void requestSuccessStuff(String type, String baseSymbol, String qouetSymbol) {
        TangApi.getKCharData(type, baseSymbol.toLowerCase(), qouetSymbol.toLowerCase(), this);
    }

    @Override
    public void start() {
        if (i == 0) {
            loading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEnd() {
        loading.setVisibility(View.GONE);
        chartKline.setVisibility(View.VISIBLE);
        i++;

//        if (mapData.size() != 0) {
//            tvNewPrice.setText(mapData.get("latest"));
//            tvNewPrice.setText(NumberUtils.formatNumber(mapData.get("latest")));
////            tvTurnover24.setText(NumberUtils.formatNumber(mapData.get("base_volume")) + "");
////            tvMaxPrice24.setText(NumberUtils.formatNumber(mapData.get("heighpice")) + "");
////            tvMinPrice24.setText(NumberUtils.formatNumber(mapData.get("lowerprice")) + "");
//
//            double change = Double.parseDouble(mapData.get("percent_change"));
//            if (change < 0) {
////                if (ivZhangDie.isSelected() || isFrist == true) {
////                    ivZhangDie.setSelected(false);
//                lineTitle.setBackgroundColor(GREAN_COLOR);
//                lineHoder.setBackgroundColor(GREAN_COLOR);
//                tv_k.setBackgroundColor(0xff34EEA0);
//                line_view.setBackgroundColor(GREAN_COLOR);
//                viewLime.setBackgroundColor(0xff1AE18D);
////                    tvZhangDie24.setText("+" + String.format("%.2f", change) + "%");
////                    tv_item1.setTextColor(0xffE7F8D5);
////                    tv_item2.setTextColor(0xffE7F8D5);
////                    tv_item3.setTextColor(0xffE7F8D5);
////                    tv_item4.setTextColor(0xffE7F8D5);
//                isFrist = false;
////                }
//            } else {
////                if (!ivZhangDie.isSelected() || isFrist == true) {
////                    ivZhangDie.setSelected(true);
//                lineTitle.setBackgroundColor(RED_COLOR);
//                lineHoder.setBackgroundColor(RED_COLOR);
//                line_view.setBackgroundColor(RED_COLOR);
//                tv_k.setBackgroundColor(0xffFF6667);
//                viewLime.setBackgroundColor(RED_LIne_COLOR);
////                    tvZhangDie24.setText(String.format("%.2f", change) + "%");
////                    tv_item1.setTextColor(0xffFFBEBE);
////                    tv_item2.setTextColor(0xffFFBEBE);
////                    tv_item3.setTextColor(0xffFFBEBE);
////                    tv_item4.setTextColor(0xffFFBEBE);
//                isFrist = false;
////                }
//            }
//        }
    }

    @Override
    public void setData(String datas) {
        if (!TextUtils.isEmpty(datas) && !datas.equals("error_code") && !datas.equals("[]")) {
            JSON.parse(datas);
            JSONArray array = null;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(datas);
                String items = jsonObject.getString("items");
                array = JSON.parseArray(items);
                Log.e("111111111", "setData: ");

                mapData.put("latest", NumberUtils.optString(jsonObject.optString("latest"), "0"));
                mapData.put("base_volume", NumberUtils.optString(jsonObject.optString("base_volume"), "0"));
                mapData.put("percent_change", NumberUtils.optString(jsonObject.optString("percent_change"), "0"));
                mapData.put("heighpice", NumberUtils.optString(jsonObject.optString("heighpice"), "0"));
                mapData.put("lowerprice", NumberUtils.optString(jsonObject.optString("lowerprice"), "0"));
            } catch (JSONException e) {
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            if (array != null && array.size() > 0) {
                klineprices.clear();
                int size = 0;
                for (int j = size; j < array.size(); j++) {
                    JSONArray jsonArray = array.getJSONArray(j);
                    long time = jsonArray.getLong(0);
                    float begin = jsonArray.getFloat(1);
                    float high = jsonArray.getFloat(2);
                    float low = jsonArray.getFloat(3);
                    float end = jsonArray.getFloat(4);
                    double volume = jsonArray.getDouble(5);
                    klineprices.add(new KLinePrice(time, begin, high, low, end, volume));
                }


                Collections.sort(klineprices, new Comparator<KLinePrice>() {
                    @Override
                    public int compare(KLinePrice lhs, KLinePrice rhs) {

                        return (lhs.time - rhs.time >= 0) ? 1 : -1;
                    }
                });

                parseKLinePriceParts();
                initChartKline();
                if (isFirstLoadKline) {
                    chartKline.moveViewToX(klineprices.size() - 1);
                    isFirstLoadKline = false;
                }
                updateIndicateData(klineprices.size() - 1);
                updateTitleData(klineprices.size() - 1, false);

                i++;
                if (i == 1) {
                    initEvents();
                }
            } else {
                if (isFirstLoadKline) {
                    chartKline.clear();
                    updateNullData();
                }
                klineprices.clear();
            }
        } else {
            if (isFirstLoadKline) {
                chartKline.clear();
                updateNullData();
            }
            klineprices.clear();
        }
        if (i == 0) {
            i++;
        }
    }

    private void updateNullData() {

        mTvVolumeMA7.setText("MA7:——");
        mTvVolumeMA30.setText("MA30:——");

        String time;
        if (defaultPeriodType == 0) {
            time = getString(R.string.bds_mins);
        } else if (defaultPeriodType == 5) {
            time = getString(R.string.bds_DayLine);
        } else if (defaultPeriodType == 6) {
            time = getString(R.string.bds_weekLine);
        } else {
            time = rbList.getText().toString();
        }
        mTvVolumeNum.setText(time + "(7,30):——");
        tvGrowth.setText("——");
        tvGrowthRate.setText("——");
        tvNewPrice.setText("0.00000");
        tvPriceHigh.setText("——");
        tvPriceVolume.setText("——");
        tvPriceStart.setText("——");
        tvPriceLow.setText("——");

    }

    private class Line {
        String name;
        double[] datas;
        int colorResourse;

        public Line(String name, double[] datas, int colorResourse) {
            this.name = name;
            this.datas = datas;
            this.colorResourse = colorResourse;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(thread);
    }

    private void getAllHistory(String baseSymbol, String quoteSymbol, int nLimit) throws NetworkStatusException {
        List<AllHistoryResult> datas = new ArrayList<>();
        dateBuyOrSell.clear();
        AllHistory allHistory = null;
        asset_object baseAsset = null;
        asset_object quoteAsset = null;
        try {
            baseAsset = BitsharesWalletWraper.getInstance().lookup_asset_symbols(baseSymbol);
            if (baseAsset == null || baseAsset.id == null || TextUtils.isEmpty(baseAsset.id.toString())) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyApp.showToast(getString(R.string.network));
//                    }
//                });

                return;
            }

            quoteAsset = BitsharesWalletWraper.getInstance().lookup_asset_symbols(quoteSymbol);
            if (quoteAsset == null || quoteAsset.id == null || TextUtils.isEmpty(quoteAsset.id.toString())) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyApp.showToast(getString(R.string.network));
//                    }
//                });
                return;
            }
            allHistory = BitsharesWalletWraper.getInstance().get_all_history(baseAsset.id.toString(), quoteAsset.id.toString(), nLimit);//("1.3.3","1.3.0",300);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }

        if (allHistory != null && allHistory.getResult() != null && allHistory.getResult().size() > 0) {
            for (int i = 0; i < allHistory.getResult().size(); i++) {
                if (i % 2 != 0) {
                    String date = TimeUtils.utc2Local(allHistory.getResult().get(i).getTime().replace("T", " "));
                    String time = date.replace(" ", "\n");
                    String strAmount;
                    String strValue;
                    String strPrice;
                    //buy
                    if (allHistory.getResult().get(i).getOp().getPays().getAsset_id().equals(baseAsset.id.toString())) {
                        strAmount = baseAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getPays().getAmount()).count;
                        strValue = quoteAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getReceives().getAmount()).count;
                        strPrice = CalculateUtils.div(Double.parseDouble(strAmount), Double.parseDouble(strValue), 5);
                        datas.add(new AllHistoryResult(time, strPrice, BUY, strAmount, strValue));
                    }
                    //sell
                    else {
                        strAmount = quoteAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getPays().getAmount()).count;
                        strValue = baseAsset.get_legible_asset_object(allHistory.getResult().get(i).getOp().getReceives().getAmount()).count;
                        strPrice = CalculateUtils.div(Double.parseDouble(strValue), Double.parseDouble(strAmount), 5);
                        datas.add(new AllHistoryResult(time, strPrice, SELL, strValue, strAmount));
                    }
                }
            }
        }

        dateBuyOrSell.addAll(datas);

        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.setList(dateBuyOrSell);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private PopupWindow mPopupWindow;

    private void showPopupWindow(View anchorView) {
        if (mPopupWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.popup_kline_vertical_layout, null);
            View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Click " + ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                    if (mPopupWindow != null) {
                        mPopupWindow.dismiss();
                    }
                }
            };

            ListView listView = contentView.findViewById(R.id.list_item);
            ArrayAdapter array = new ArrayAdapter(this, R.layout.popup_kline_vertical_item, PERIOD_Pw_TYPE);
            listView.setAdapter(array);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int po = position + 1;
                    if (defaultPeriodType != po) {
                        isPup = true;
                        radioTime.clearCheck();
                        defaultPeriodType = po;
                        chartKline.setVisibility(View.INVISIBLE);
                        handler.removeCallbacks(thread);
                        type = synPeriodAndType(defaultPeriodType);
                        loading.setVisibility(View.VISIBLE);
                        isFirstLoadKline = true;
                        requestSuccessStuff(type, baseSymbol, qouteSymbol);
                        rbList.setSelected(true);
                        rbList.setText(PERIOD_TYPE[po]);
                        isPup = false;

                    }
                    mPopupWindow.dismiss();
                }
            });

            mPopupWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            //  mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            //  mPopupWindow.setBackgroundDrawable(getResources().getDrawable( R.mipmap.blank_bg_pwd));
            // 如果设置PopupWindow的背景
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            mPopupWindow.setFocusable(true);// 点击空白处时，隐藏掉pop窗口

            mPopupWindow.setAnimationStyle(R.style.accouptor_popwindow_anim_style);

            mPopupWindow.showAsDropDown(anchorView, 0, 10);
        } else {
            mPopupWindow.setFocusable(true);
            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAsDropDown(anchorView, 0, 10);
            }
        }
    }
}
