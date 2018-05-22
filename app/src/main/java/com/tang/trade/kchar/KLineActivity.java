package com.tang.trade.kchar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import com.tang.trade.kchar.widget.CoupleChartGestureListener;
import com.tang.trade.kchar.widget.MyCombinedChart;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.widget.LoadingView;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KLineActivity extends Activity implements TangApi.MyBaseViewCallBack<String> {

    public static void launch(Activity from, String symbol) {
        Intent intent = new Intent(from, KLineActivity.class);
        intent.putExtra("symbol", symbol);
        from.startActivity(intent);
    }

    int i = 0;

    private static final String[] PERIOD_TYPE = new String[]{"分时", "1分", "5分", "30分", "60分", "日线", "周线"};
    private static final String[] AVERAGE_TYPE = new String[]{"MA", "EMA", "关闭"};
    private static final String[] KM_TYPE = new String[]{"MACD", "KDJ", "关闭"};

    private int defaultPeriodType = 5;
    private int defaultAveType = 0;
    private int defaultKmType = 0;

    private String symbol;
    private String type = Type.TYPE_30_MIN;
    private int size = 300;

    private int selectedValuePos = -1;
    private boolean isFirstLoadKline = true;
    private boolean isFirstLoadVolume = true;
    private boolean isFirstLoadMACD = true;

    @BindView(R.id.sp_kline_ma_switch)
    Spinner mSpAve;
    @BindView(R.id.sp_kline_km_switch)
    Spinner mSpKM;
    @BindView(R.id.sp_kline_period_switch)
    Spinner mSpPeriod;

    @BindView(R.id.tv_kline_back)
    TextView mTvBack;

    @BindView(R.id.tv_kline_time)
    TextView mTvTime;
    @BindView(R.id.tv_kline_begin)
    TextView mTvBegin;
    @BindView(R.id.tv_kline_high)
    TextView mTvHigh;
    @BindView(R.id.tv_kline_low)
    TextView mTvLow;
    @BindView(R.id.tv_kline_end)
    TextView mTvEnd;
    @BindView(R.id.tv_kline_k_ma7)
    TextView mTvKMA7;
    @BindView(R.id.tv_kline_k_ma30)
    TextView mTvKMA30;

    @BindView(R.id.tv_kline_volume_num)
    TextView mTvVolumeNum;
    @BindView(R.id.tv_kline_volume_ma7)
    TextView mTvVolumeMA7;
    @BindView(R.id.tv_kline_volume_ma30)
    TextView mTvVolumeMA30;

    @BindView(R.id.tv_kline_macd_or_kdj)
    TextView mTvMACDorKDJ;
    @BindView(R.id.tv_kline_dif_or_k)
    TextView mTvDIForK;
    @BindView(R.id.tv_kline_dea_or_d)
    TextView mTvDEAorD;
    @BindView(R.id.tv_kline_macd_or_j)
    TextView mTvMACDorJ;

    //    @BindView(R.id.ll_kline)
//    TouchLL mLlKline;
    @BindView(R.id.chart_kline)
    CombinedChart chartKline;
    @BindView(R.id.chart_volume)
    MyCombinedChart chartVolume;
    @BindView(R.id.chart_macdkdj)
    MyCombinedChart chartMACDKDJ;
    @BindView(R.id.rl_kline_wrap_chart_macdkdj)
    RelativeLayout mRlMACD;
    @BindView(R.id.loading)
    LoadingView loading;
    @BindView(R.id.tv_kline_day)
    TextView tv_kline_day;

    private ArrayList<KLinePrice> klineprices = new ArrayList<KLinePrice>();

    private ArrayList<String> xVals;
    private ArrayList<String> xValsyMd;
    private ArrayList<String> xValsHm;

    private double[] priceEnds;
    private double[] priceHighs;
    private double[] priceLows;
    private double[] priceVolumes;

    private double[] ma7Price;
    private double[] ma30Price;
    private double[] ema7Price;
    private double[] ema30Price;

    private double[] ma7Vol;
    private double[] ma30Vol;
    private double[] ema7Vol;
    private double[] ema30Vol;

    private double[] diff;
    private double[] dea;
    private double[] bar;
    private double[] rsak;
    private double[] rsad;
    private double[] rsaj;

    private String baseSymbol = "CNY";
    private String qouteSymbol = "BDS";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                requestSuccessStuff(type, baseSymbol, qouteSymbol);
                handler.postDelayed(thread, 2000);
//                handler.removeCallbacks(thread);
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kline);
        baseSymbol = getIntent().getStringExtra("baseSymbol");
        qouteSymbol = getIntent().getStringExtra("qouteSymbol");
        ButterKnife.bind(this);
        initToolbar();
        initViews();
        initDatas();
    }

    private void initViews() {
//        mLlKline.addChart(R.id.chart_kline);
//        mLlKline.addChart(R.id.chart_volume);
//        mLlKline.addChart(R.id.chart_macdkdj);

        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initChartCommon(CombinedChart chart, boolean XEnable) {
        chart.setDrawGridBackground(false);
        chart.setDescription("");
        chart.getLegend().setEnabled(false);
        chart.setPinchZoom(false);
//        chart.setVisibleXRangeMaximum(150);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleYEnabled(false);
//        chart.setTouchEnabled(false);
//        chart.setScaleMinima(2,0);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDragDecelerationEnabled(false); // 必不可少 禁止惯性滑动
        XAxis x = chart.getXAxis();
        x.setEnabled(XEnable);
        x.setAvoidFirstLastClipping(true);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setSpaceBetweenLabels(6);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(ContextCompat.getColor(this, R.color.market_kline_text_grey));
        x.setValueFormatter(new XAxisValueFormatter() {
            @Override
            public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
                if (PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线")) {
                    return DateUtils.dateFormatyMd(Long.parseLong(xVals.get(i)));
                } else {
                    return DateUtils.dateFormatHm(Long.parseLong(xVals.get(i)));
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

    // 初始化K线图
    private void initChartKline() {
        initChartCommon(chartKline, false);
        float min = Collections.min(klineprices).low;
        float max = Collections.max(klineprices).high;
        YAxis yRight = chartKline.getAxisRight();
//        yRight.setAxisMaxValue(max);
//        yRight.setAxisMinValue(min);
        yRight.calcMinMax(min, max);
        yRight.setLabelCount(5, true);
        yRight.setStartAtZero(false);

        CombinedData combinedData = new CombinedData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm);

        List<Line> lines = new ArrayList<>();

        if (defaultPeriodType == 0) {
            lines.add(new Line("kline-end", priceEnds, R.color.common_white));
        } else {
            combinedData.setData(generateCandleData());
        }

        switch (defaultAveType) {
            case 0:
                ma7Price = KLineCal.showDoubleArray(KLineCal.MA(priceEnds, 7));
                ma30Price = KLineCal.showDoubleArray(KLineCal.MA(priceEnds, 30));
                lines.add(new Line("ma7", ma7Price, R.color.market_kline_text_orange));
                lines.add(new Line("ma30", ma30Price, R.color.market_kline_text_blue));
                combinedData.setData(generateLineData(lines, "kline"));

                break;
            case 1:
                ema7Price = KLineCal.showDoubleArray(KLineCal.EMA(priceEnds, 7));
                ema30Price = KLineCal.showDoubleArray(KLineCal.EMA(priceEnds, 30));
                lines.add(new Line("ema7", ema7Price, R.color.market_kline_text_orange));
                lines.add(new Line("ema30", ema30Price, R.color.market_kline_text_blue));
                combinedData.setData(generateLineData(lines, "kline"));
                break;
            case 2:
                break;
        }
        chartKline.setData(combinedData);
        final ViewPortHandler viewPortHandlerBar = chartKline.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(xVals.size());
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        touchmatrix.postScale(1, 1f);
        chartKline.notifyDataSetChanged();
        chartKline.invalidate();
        if (isFirstLoadKline) {
            chartKline.moveViewToX(klineprices.size());
            chartKline.animateX(2000);
            isFirstLoadKline = false;
        }

        if (!isFirstLoadKline) {
            chartKline.setVisibleXRangeMinimum(30);
        }
    }

    // 初始化交易量图
    private void initChartVolume() {
        initChartCommon(chartVolume, false);
        YAxis yRight = chartVolume.getAxisRight();
        yRight.setDrawLabels(false);

        yRight.setAxisMaxValue((float) Collections.max(klineprices, new Comparator<KLinePrice>() {
            @Override
            public int compare(KLinePrice lhs, KLinePrice rhs) {
//                float value = lhs.volume - rhs.volume;

                double value = KLineCal.sub(lhs.volume, rhs.volume);
                if (value > 0) {
                    return 1;
                } else if (value < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }).volume);
        yRight.setStartAtZero(true);
        yRight.setLabelCount(3, true);

        CombinedData combinedData = new CombinedData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm);
        combinedData.setData(generateBarData());

        List<Line> lines = new ArrayList<>();
        switch (defaultAveType) {
            case 0:
                ma7Vol = KLineCal.showDoubleArray(KLineCal.MA(priceVolumes, 7));
                ma30Vol = KLineCal.showDoubleArray(KLineCal.MA(priceVolumes, 30));
                lines.add(new Line("ma7Vol", ma7Vol, R.color.market_kline_text_orange));
                lines.add(new Line("ma30Vol", ma30Vol, R.color.market_kline_text_blue));
                combinedData.setData(generateLineData(lines, "vol"));
                break;
            case 1:
                ema7Vol = KLineCal.showDoubleArray(KLineCal.EMA(priceVolumes, 7));
                ema30Vol = KLineCal.showDoubleArray(KLineCal.EMA(priceVolumes, 30));
                lines.add(new Line("ema7Vol", ema7Vol, R.color.market_kline_text_orange));
                lines.add(new Line("ema30Vol", ema30Vol, R.color.market_kline_text_blue));
                combinedData.setData(generateLineData(lines, "vol"));
                break;
            case 2:
                break;
        }

        chartVolume.setData(combinedData);
        final ViewPortHandler viewPortHandlerBar = chartVolume.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(xVals.size());
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        touchmatrix.postScale(1, 1f);
        chartVolume.notifyDataSetChanged();
        chartVolume.invalidate();
        if (isFirstLoadVolume) {
            chartVolume.moveViewToX(klineprices.size());
            chartVolume.animateX(2000);
            isFirstLoadVolume = false;
        }

        if (!isFirstLoadVolume) {
            chartVolume.setVisibleXRangeMinimum(30);
        }
    }

    // 初始化MACDKDJ图
    private void initChartMACDKDJ() {
        initChartCommon(chartMACDKDJ, true);
        YAxis yRight = chartMACDKDJ.getAxisRight();
        yRight.setStartAtZero(false);
        yRight.setDrawLabels(false);
        yRight.setLabelCount(3, true);
        CombinedData combinedData = new CombinedData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm);

        //DIFF DEA
        List<Line> lines = new ArrayList<>();
        XAxis xVolume = chartVolume.getXAxis();
        switch (defaultKmType) {
            case 0:
                mRlMACD.setVisibility(View.VISIBLE);
                xVolume.setEnabled(false);
                diff = KLineCal.showDoubleArray(KLineCal.DIFF(priceEnds));
                dea = KLineCal.showDoubleArray(KLineCal.DEA(priceEnds));
                lines.add(new Line("diff", diff, R.color.market_kline_text_orange));
                lines.add(new Line("dea", dea, R.color.market_kline_text_blue));
                combinedData.setData(generateLineData(lines, "macdkdj"));
                bar = KLineCal.showDoubleArray(KLineCal.BAR(priceEnds));
                combinedData.setData(generatePositiveBarData(bar));
                break;
            case 1:
                mRlMACD.setVisibility(View.VISIBLE);
                xVolume.setEnabled(false);
                try {
                    rsak = KLineCal.showDoubleArray(KLineCal.RSV_K(priceHighs, priceLows, priceEnds, 9, 3, 3));
                    rsad = KLineCal.showDoubleArray(KLineCal.RSV_D(priceHighs, priceLows, priceEnds, 9, 3, 3));
                    rsaj = KLineCal.showDoubleArray(KLineCal.RSV_J(priceHighs, priceLows, priceEnds, 9, 3, 3));
                    lines.add(new Line("rsak", rsak, R.color.market_kline_text_orange));
                    lines.add(new Line("rsad", rsad, R.color.market_kline_text_blue));
                    lines.add(new Line("rsaj", rsaj, R.color.common_white));
                    combinedData.setData(generateLineData(lines, "macdkdj"));

                } catch (NumberFormatException e) {

                }
                break;
            case 2:
                mRlMACD.setVisibility(View.GONE);
                xVolume.setEnabled(true);
                break;
        }

        chartMACDKDJ.setData(combinedData);
        final ViewPortHandler viewPortHandlerBar = chartMACDKDJ.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(xVals.size());
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        touchmatrix.postScale(1, 1f);
        chartMACDKDJ.notifyDataSetChanged();
        chartMACDKDJ.invalidate();
        if (isFirstLoadMACD) {
            chartMACDKDJ.moveViewToX(klineprices.size());
            chartMACDKDJ.animateX(2000);
            isFirstLoadMACD = false;
        }
        if (!isFirstLoadMACD) {
            chartMACDKDJ.setVisibleXRangeMinimum(30);
        }
    }

    // 初始化MACDKDJ图中的柱图
    private BarData generatePositiveBarData(double[] values) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int index = 0; index < klineprices.size(); index++) {
            entries.add(new BarEntry((float) values[index], index));
            if (values[index] > 0) {
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_green));
            } else {
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_red));
            }
        }
        BarDataSet set = new BarDataSet(entries, "MACD");
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setHighLightColor(ContextCompat.getColor(this, R.color.common_white));

        set.setColors(colors);
        return new BarData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm, set);
    }

    // 初始化交易量图中的柱图
    private BarData generateBarData() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int index = 0; index < klineprices.size(); index++) {
            KLinePrice price = klineprices.get(index);
            entries.add(new BarEntry((float) price.volume, index));
        }
        BarDataSet set = new BarDataSet(entries, "VolumeBar");
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        set.setHighlightEnabled(false);
        set.setDrawValues(false);

        List<Integer> colors = new ArrayList<>();
        for (KLinePrice price : klineprices) {
            if (price.begin > price.end) {
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_red));
            } else {
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_green));
            }
        }
        set.setHighLightColor(ContextCompat.getColor(this, R.color.common_white));
        set.setColors(colors);
        return new BarData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm, set);
    }

    // 初始化K线图中的柱图
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
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_red));
            } else {
                colors.add(ContextCompat.getColor(this, R.color.market_kline_line_green));
            }
        }
        set.setColors(colors);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setShadowWidth(0.7f);
        set.setHighLightColor(ContextCompat.getColor(this, R.color.common_white));
        set.setDrawHighlightIndicators(true);
        set.setDrawVerticalHighlightIndicator(true);
        set.setDrawHorizontalHighlightIndicator(false);
        CandleData candleData = new CandleData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm, set);

        return candleData;
    }

    // 初始化均线图
    private LineData generateLineData(List<Line> lines, String chartName) {

        ArrayList<Entry> ma7Entries = new ArrayList<>();
        ArrayList<Entry> ma30Entries = new ArrayList<>();
        ArrayList<Entry> ema7Entries = new ArrayList<>();
        ArrayList<Entry> ema30Entries = new ArrayList<>();
        ArrayList<Entry> ma7VolEntries = new ArrayList<>();
        ArrayList<Entry> ma30VolEntries = new ArrayList<>();
        ArrayList<Entry> ema7VolEntries = new ArrayList<>();
        ArrayList<Entry> ema30VolEntries = new ArrayList<>();
        ArrayList<Entry> entriesDiff = new ArrayList<>();
        ArrayList<Entry> entriesDea = new ArrayList<>();
        ArrayList<Entry> entriesRsak = new ArrayList<>();
        ArrayList<Entry> entriesRsad = new ArrayList<>();
        ArrayList<Entry> entriesRsaj = new ArrayList<>();

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);

            if (line.name.equals("ma7")) {
                for (int index = 6; index < line.datas.length; index++) {
                    ma7Entries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ema7")) {
                for (int index = 6; index < line.datas.length; index++) {
                    ema7Entries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ma7Vol")) {
                for (int index = 6; index < line.datas.length; index++) {
                    ma7VolEntries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ema7Vol")) {
                for (int index = 6; index < line.datas.length; index++) {
                    ema7VolEntries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ma30")) {
                for (int index = 29; index < line.datas.length; index++) {
                    ma30Entries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ema30")) {
                for (int index = 29; index < line.datas.length; index++) {
                    ema30Entries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ma30Vol")) {
                for (int index = 29; index < line.datas.length; index++) {
                    ma30VolEntries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("ema30Vol")) {
                for (int index = 29; index < line.datas.length; index++) {
                    ema30VolEntries.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("diff")) {
                for (int index = 0; index < line.datas.length; index++) {
                    entriesDiff.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("dea")) {
                for (int index = 0; index < line.datas.length; index++) {
                    entriesDea.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("rsak")) {
                for (int index = 0; index < line.datas.length; index++) {
                    entriesRsak.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("rsad")) {
                for (int index = 0; index < line.datas.length; index++) {
                    entriesRsad.add(new Entry((float) line.datas[index], index));
                }
            } else if (line.name.equals("rsaj")) {
                for (int index = 0; index < line.datas.length; index++) {
                    entriesRsaj.add(new Entry((float) line.datas[index], index));
                }
            }
//            ArrayList<Entry> entries = new ArrayList<>();
//            for (int index = 0; index < line.datas.length; index++) {
//                entries.add(new Entry((float) line.datas[index], index));
//            }
        }

        if (klineprices.size() >= 30) {
            switch (defaultAveType) {
                case 0:
                    if (chartName.equals("kline")) {
                        dataSets.add(setMaLine(ma7Entries, "ma7", R.color.market_kline_text_orange));
                        dataSets.add(setMaLine(ma30Entries, "ma30", R.color.market_kline_text_blue));
                    } else if (chartName.equals("vol")) {
                        dataSets.add(setMaLine(ma7VolEntries, "ma7Vol", R.color.market_kline_text_orange));
                        dataSets.add(setMaLine(ma30VolEntries, "ma30Vol", R.color.market_kline_text_blue));
                    }
                    break;
                case 1:
                    if (chartName.equals("kline")) {
                        dataSets.add(setMaLine(ema7Entries, "ema7", R.color.market_kline_text_orange));
                        dataSets.add(setMaLine(ema30Entries, "ema30", R.color.market_kline_text_blue));
                    } else if (chartName.equals("vol")) {
                        dataSets.add(setMaLine(ema7VolEntries, "ema7Vol", R.color.market_kline_text_orange));
                        dataSets.add(setMaLine(ema30VolEntries, "ema30Vol", R.color.market_kline_text_blue));
                    }
                    break;
                case 2:
                    break;
            }
        } else if (klineprices.size() >= 7 && klineprices.size() < 30) {
            switch (defaultAveType) {
                case 0:
                    if (chartName.equals("kline")) {
                        dataSets.add(setMaLine(ma7Entries, "ma7", R.color.market_kline_text_orange));
                    } else if (chartName.equals("vol")) {
                        dataSets.add(setMaLine(ma7VolEntries, "ma7Vol", R.color.market_kline_text_orange));
                    }
                    break;
                case 1:
                    if (chartName.equals("kline")) {
                        dataSets.add(setMaLine(ema7Entries, "ema7", R.color.market_kline_text_orange));
                    } else if (chartName.equals("vol")) {
                        dataSets.add(setMaLine(ema7VolEntries, "ema7Vol", R.color.market_kline_text_orange));
                    }
                    break;
                case 2:
                    break;
            }
//            dataSets.add(setMaLine(ma7Entries, "ma7", R.color.kline_chart_text_orange));
        }
        switch (defaultKmType) {
            case 0:
                if (chartName.equals("macdkdj")) {
                    dataSets.add(setMaLine(entriesDiff, "diff", R.color.market_kline_text_orange));
                    dataSets.add(setMaLine(entriesDea, "dea", R.color.market_kline_text_blue));
                }
                break;
            case 1:
                if (chartName.equals("macdkdj")) {
                    dataSets.add(setMaLine(entriesRsak, "rsak", R.color.market_kline_text_orange));
                    dataSets.add(setMaLine(entriesRsad, "rsad", R.color.market_kline_text_blue));
                    dataSets.add(setMaLine(entriesRsaj, "rsaj", R.color.common_white));
                }
                break;
            case 2:

                break;
        }

        LineData data = new LineData(PERIOD_TYPE[defaultPeriodType].equals("日线") || PERIOD_TYPE[defaultPeriodType].equals("周线") ? xValsyMd : xValsHm, dataSets);
        return data;
    }

    private LineDataSet setMaLine(ArrayList<Entry> lineEntries, String name, int colorResourse) {
        LineDataSet set = new LineDataSet(lineEntries, name);
        // 必须在第一个setline.add的时候添加
        if (name.equals("ma7") || name.equals("diff") || name.equals("rsak") || name.equals("ma7Vol") || name.equals("ema7") || name.equals("ema7Vol")) {
            set.setHighlightEnabled(true);
            set.setHighLightColor(ContextCompat.getColor(this, R.color.common_white));
        } else {
            set.setHighlightEnabled(false);
        }
        set.setDrawValues(false);
        set.setDrawCubic(false);
        set.setLineWidth(1f);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColor(ContextCompat.getColor(this, colorResourse));
        set.setDrawFilled(false);
//        set.setHighLightColor(ContextCompat.getColor(this, R.color.kline_chart_text_vertical_white));
        set.setDrawHorizontalHighlightIndicator(false);//取消显示高亮水平线
        set.setDrawCircles(false);

        return set;
    }

    protected void initToolbar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void initEvents() {

        chartKline.setOnChartGestureListener(new CoupleChartGestureListener(chartKline, new BarLineChartBase[]{chartVolume, chartMACDKDJ}));
        chartVolume.setOnChartGestureListener(new CoupleChartGestureListener(chartVolume, new BarLineChartBase[]{chartKline, chartMACDKDJ}));
        chartMACDKDJ.setOnChartGestureListener(new CoupleChartGestureListener(chartMACDKDJ, new BarLineChartBase[]{chartKline, chartVolume}));

        chartKline.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int i, Highlight h) {
                handler.removeCallbacks(thread);
                selectedValuePos = e.getXIndex();
                updateIndicateData(selectedValuePos);

                chartVolume.highlightValues(new Highlight[]{h});
                chartMACDKDJ.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                chartVolume.highlightValue(null);
                chartMACDKDJ.highlightValue(null);
                handler.post(thread);
            }
        });

        chartVolume.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int i, Highlight h) {
                handler.removeCallbacks(thread);
                selectedValuePos = e.getXIndex();
                updateIndicateData(selectedValuePos);

                chartKline.highlightValues(new Highlight[]{h});
                chartMACDKDJ.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                chartKline.highlightValue(null);
                chartMACDKDJ.highlightValue(null);
                handler.post(thread);
            }
        });

        chartMACDKDJ.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int i, Highlight h) {
                handler.removeCallbacks(thread);
                selectedValuePos = e.getXIndex();
                updateIndicateData(selectedValuePos);

                chartKline.highlightValues(new Highlight[]{h});
                chartVolume.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                chartKline.highlightValue(null);
                chartVolume.highlightValue(null);
                handler.post(thread);
            }
        });

        initSpinner();
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initSpinner() {

        // 时间选择的sp
        ArrayAdapter<String> adapterPeriod = new ArrayAdapter<>(this, R.layout.sp_kline, PERIOD_TYPE);
        adapterPeriod.setDropDownViewResource(R.layout.sp_kline_drop_view_item);
        mSpPeriod.setAdapter(adapterPeriod);
        mSpPeriod.setSelection(defaultPeriodType);
        mSpPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (defaultPeriodType != position) {
                    defaultPeriodType = position;
                    type = synPeriodAndType(defaultPeriodType);
                    requestSuccessStuff(type, baseSymbol, qouteSymbol);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 均线种类的sp
        ArrayAdapter<String> adapterAve = new ArrayAdapter<>(this, R.layout.sp_kline, AVERAGE_TYPE);
        adapterAve.setDropDownViewResource(R.layout.sp_kline_drop_view_item);
        mSpAve.setAdapter(adapterAve);
        mSpAve.setSelection(defaultAveType);
        mSpAve.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                defaultAveType = position;
//                PreferencesUtils.put(Key.KLINE_EVA, defaultAveType);

                initChartKline();
                initChartVolume();

                if (selectedValuePos >= 0) {
                    updateIndicateData(selectedValuePos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 另一个sp
        ArrayAdapter<String> adapterKM = new ArrayAdapter<>(this, R.layout.sp_kline, KM_TYPE);
        adapterKM.setDropDownViewResource(R.layout.sp_kline_drop_view_item);
        mSpKM.setAdapter(adapterKM);
        mSpKM.setSelection(defaultKmType);
        mSpKM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                defaultKmType = position;
//                PreferencesUtils.put(Key.KLINE_KM, defaultKmType);
                initChartMACDKDJ();
                if (selectedValuePos >= 0) {
                    updateIndicateData(selectedValuePos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateIndicateData(int i) {
        KLinePrice price = klineprices.get(i);

        tv_kline_day.setText(DateUtils.dateFormatyMdHm(price.time).substring(0, 10));
        mTvTime.setText(DateUtils.dateFormatyMdHm(price.time).substring(11, 16));

        mTvBegin.setText(new DecimalFormat("0.00000").format(price.begin));
        mTvHigh.setText(new DecimalFormat("0.00000").format(price.high));
        mTvLow.setText(new DecimalFormat("0.00000").format(price.low));
        mTvEnd.setText(new DecimalFormat("0.00000").format(price.end));

        int vis = defaultAveType == 2 ? View.GONE : View.VISIBLE;
        mTvKMA7.setVisibility(vis);
        mTvKMA30.setVisibility(vis);
        mTvVolumeMA7.setVisibility(vis);
        mTvVolumeMA30.setVisibility(vis);

        String priceMa7 = "";
        String priceMa30 = "";
        String priceeMa7 = "";
        String priceeMa30 = "";

        switch (defaultAveType) {
            case 0:
                priceMa7 = NumberUtils.formatNumber5(String.valueOf(ma7Price[i]));
                priceMa30 = NumberUtils.formatNumber5(String.valueOf(ma30Price[i]));
                break;
            case 1:
                priceeMa7 = NumberUtils.formatNumber5(String.valueOf(ema7Price[i]));
                priceeMa30 = NumberUtils.formatNumber5(String.valueOf(ema30Price[i]));
                break;
            case 2:
                break;
        }

        mTvVolumeNum.setText("成交量(7,30):" + NumberUtils.formatNumber5(new BigDecimal(priceVolumes[i]).toPlainString()));
        switch (defaultAveType) {
            case 0:
                mTvKMA7.setText(i < 6 ? "MA7:——" : "MA7:" + priceMa7);
                mTvKMA30.setText(i < 29 ? "MA30:——" : "MA30:" + priceMa30);
                mTvVolumeMA7.setText(i < 6 ? "MA7:——" : "MA7:" + NumberUtils.formatNumber5(new BigDecimal(ma7Vol[i]).toPlainString()));
                mTvVolumeMA30.setText(i < 29 ? "MA30:——" : "MA30:" + NumberUtils.formatNumber5(new BigDecimal(ma30Vol[i]).toPlainString()));
                break;
            case 1:
                mTvKMA7.setText(i < 6 ? "EMA7:——" : "EMA7:" + priceeMa7);
                mTvKMA30.setText(i < 29 ? "EMA30:——" : "EMA30:" + priceeMa30);
                mTvVolumeMA7.setText(i < 6 ? "EMA7:——" : "EMA7:" + NumberUtils.formatNumber5(new BigDecimal(ema7Vol[i]).toPlainString()));
                mTvVolumeMA30.setText(i < 29 ? "EMA30:——" : "EMA30:" + NumberUtils.formatNumber5(new BigDecimal(ema30Vol[i]).toPlainString()));
                break;
            case 2:

                break;
        }
        switch (defaultKmType) {
            case 0:
                mTvMACDorKDJ.setText("MACD(12,26,9)");
                if (diff != null) {
                    mTvDIForK.setText("DIF:" + NumberUtils.formatNumber5(new BigDecimal(diff[i]).toPlainString()));
                }
                if (dea != null) {
                    mTvDEAorD.setText("DEA:" + NumberUtils.formatNumber5(new BigDecimal(dea[i]).toPlainString()));
                }
                if (bar != null) {
                    mTvMACDorJ.setText("MACD:" + NumberUtils.formatNumber5(new BigDecimal(bar[i]).toPlainString()));
                }
                break;
            case 1:
                mTvMACDorKDJ.setText("KDJ(9,3,3)");
                if (rsak != null) {
                    mTvDIForK.setText("K:" + NumberUtils.formatNumber5(new BigDecimal(rsak[i]).toPlainString()));
                }
                if (rsad != null) {
                    mTvDEAorD.setText("D:" + NumberUtils.formatNumber5(new BigDecimal(rsad[i]).toPlainString()));
                }
                if (rsaj != null) {
                    mTvMACDorJ.setText("J:" + NumberUtils.formatNumber5(new BigDecimal(rsaj[i]).toPlainString()));
                }
                break;
            case 2:
                break;
        }
    }

    protected void initDatas() {
        // 读取本地存储的默认选择
        defaultPeriodType = 5;
        defaultAveType = 0;
        defaultKmType = 0;
        type = synPeriodAndType(defaultPeriodType);

        handler.post(thread);
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
        priceHighs = new double[size];
        priceLows = new double[size];
        priceVolumes = new double[size];
        for (int i = 0; i < size; i++) {
            xVals.add("" + klineprices.get(i).time);
            xValsyMd.add("" + DateUtils.dateFormatyMd(Long.parseLong(xVals.get(i))));
            xValsHm.add("" + DateUtils.dateFormatyMdHm(Long.parseLong(xVals.get(i))));
            priceEnds[i] = klineprices.get(i).end;
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
        if (klineprices.size() <= 0) {
            showDialog();
        }
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
                Log.e("222222", "setData: ");


            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            if (array != null && array.size() > 0) {
                klineprices.clear();
                for (int j = 0; j < array.size(); j++) {

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
                initChartVolume();
                initChartMACDKDJ();
                updateIndicateData(klineprices.size() - 1);

                i++;
                if (i == 1) {
                    initEvents();
                }
            } else {
                showDialog();
            }
        } else {
            showDialog();
        }
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

    /**
     * nodata
     */
    android.support.v7.app.AlertDialog.Builder builder;

    private void showDialog() {
        handler.removeCallbacks(thread);
        if (builder == null) {
            builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.bds_Note);
            builder.setMessage(R.string.bds_no_k_data);
            builder.setCancelable(false);
        }

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isFinishing()) {
                    finish();
                }
            }
        });
        if (!builder.create().isShowing() && !isFinishing()) {
            builder.show();
        }
    }
}
