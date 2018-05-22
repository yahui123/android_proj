package com.tang.trade.kchar.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by Harpoonman on 2016/5/9 0009.
 */
public class KLineCal {

    private static final DecimalFormat formatCal = new DecimalFormat("0.00000");
    private static final DecimalFormat formatUltimate = new DecimalFormat("0.00000");
    private static final int DEF_DIV_SCALE = 10;

    /**
     * 计算MA值
     *
     * @param prices 收盘价
     * @param N      n值
     * @return
     */
    public static double[] MA(double[] prices, int N) {
        int length = prices.length;
        double[] ma = new double[length];
        for (int i = 0; i < length; i++) {
            if (i < N - 1) {
                for (int j = 0; j < i + 1; j++) {
                    ma[i] += prices[j];
                }
                ma[i] /= i + 1;
            } else {
                for (int j = i - N + 1; j < i + 1; j++) {
                    ma[i] += prices[j];
                }
                ma[i] /= N;
            }
        }

        for (int i = 0; i < length; i++) {
            ma[i] = Double.parseDouble(formatCal.format(ma[i]));
        }
        return ma;
    }

    /**
     * 计算EMA值
     *
     * @param prices 收盘价
     * @param N
     * @return
     */
    public static double[] EMA(double[] prices, int N) {
        int length = prices.length;
        double[] ema = new double[length];
        ema[0] = prices[0];
        for (int i = 1; i < length; i++) {
            ema[i] = Double.parseDouble(formatCal.format(ema[i - 1] + (prices[i] - ema[i - 1]) * 2 / (N + 1)));
        }
        return ema;
    }

    /**
     * 计算DIFF值
     *
     * @param prices
     * @return
     */
    public static double[] DIFF(double[] prices) {
        int length = prices.length;
        double[] diff = new double[length];
        double[] ema12 = EMA(prices, 12);
        double[] ema26 = EMA(prices, 26);
        for (int i = 0; i < length; i++) {
            diff[i] = ema12[i] - ema26[i];
        }
        return diff;
    }

    /**
     * 计算DEA值
     *
     * @param prices
     * @return
     */
    public static double[] DEA(double[] prices) {
        int length = prices.length;
        double[] dea = new double[length];
        double[] diff = DIFF(prices);
        dea[0] = diff[0] * 0.2;
        for (int i = 1; i < length; i++) {
            dea[i] = Double.parseDouble(formatCal.format(dea[i - 1] * 0.8 + diff[i] * 0.2));
        }

        return dea;
    }

    /**
     * 计算BAR值
     *
     * @param prices
     * @return
     */
    public static double[] BAR(double[] prices) {
        int length = prices.length;
        double[] bar = new double[length];
        double[] dea = DEA(prices);
        double[] diff = DIFF(prices);
        for (int i = 0; i < length; i++) {
            bar[i] = Double.parseDouble(formatCal.format(2 * (diff[i] - dea[i])));
        }

        return bar;
    }


    /**
     * 计算RSV值，
     * KDJ(9,3.3),下面以该参数为例说明计算方法。
     * 9，3，3代表指标分析周期为9天，K值D值为3天
     *
     * @param highs 最高价集合
     * @param lows  最低价集合
     * @param ends  收盘价集合
     * @param T     分析周期
     * @param K     K值
     * @param D     D值
     * @return
     */
    private static double[] RSV(double[] highs, double[] lows, double[] ends, int T, int K, int D) {
        int length = ends.length;
        double[] rsv = new double[length];
        double[] minT = minT(lows, T);
        double[] maxT = maxT(highs, T);


        for (int i = 0; i < length; i++) {
            rsv[i] = Double.parseDouble(formatCal.format((ends[i] - minT[i]) / (maxT[i] - minT[i]) * 100));
        }

        return rsv;
    }


    /**
     * 计算 K值
     *
     * @param highs
     * @param lows
     * @param ends
     * @param T
     * @param K
     * @param D
     * @return
     */
    public static double[] RSV_K(double[] highs, double[] lows, double[] ends, int T, int K, int D) {
        double[] rsv = RSV(highs, lows, ends, T, K, D);
        double[] rsvk = new double[ends.length];
        rsvk[0] = 0;
        for (int i = 1; i < ends.length; i++) {
            rsvk[i] = Double.parseDouble(formatCal.format(((rsv[i] + 2 * rsvk[i - 1]) / K)));

        }
        return rsvk;
    }


    /**
     * 计算D值
     *
     * @param highs
     * @param lows
     * @param ends
     * @param T
     * @param K
     * @param D
     * @return
     */
    public static double[] RSV_D(double[] highs, double[] lows, double[] ends, int T, int K, int D) {
        double[] rsvk = RSV_K(highs, lows, ends, T, K, D);
        double[] rsvd = new double[ends.length];
        rsvd[0] = 0;
        for (int i = 1; i < ends.length; i++) {
            rsvd[i] = Double.parseDouble(formatCal.format(((rsvk[i] + 2 * rsvd[i - 1]) / D)));

        }
        return rsvd;
    }

    /**
     * 计算J值
     *
     * @param highs
     * @param lows
     * @param ends
     * @param T
     * @param K
     * @param D
     * @return
     */
    public static double[] RSV_J(double[] highs, double[] lows, double[] ends, int T, int K, int D) {
        double[] rsvk = RSV_K(highs, lows, ends, T, K, D);
        double[] rsvd = RSV_D(highs, lows, ends, T, K, D);
        double[] rsvj = new double[ends.length];
        for (int i = 0; i < ends.length; i++) {
            rsvj[i] = Double.parseDouble(formatCal.format((3 * rsvk[i] - 2 * rsvd[i])));

        }
        return rsvj;
    }

    /**
     * 计算T日内最低价
     *
     * @param lows
     * @param T
     * @return
     */
    private static double[] minT(double[] lows, int T) {
        int length = lows.length;
        double[] minT = new double[length];
        for (int i = 0; i < length; i++) {
            minT[i] = lows[i];
            if (i < T) {
                for (int j = 0; j < i + 1; j++) {
                    if (lows[j] < minT[i]) {
                        minT[i] = lows[j];
                    }
                }
            } else {
                for (int j = i - T + 1; j < i + 1; j++) {
                    if (lows[j] < minT[i]) {
                        minT[i] = lows[j];
                    }
                }
            }
        }

        return minT;
    }

    /**
     * 计算T日内最高价
     *
     * @param highs
     * @param T
     * @return
     */
    private static double[] maxT(double[] highs, int T) {
        int length = highs.length;
        double[] minT = new double[length];
        for (int i = 0; i < length; i++) {
            minT[i] = highs[i];
            if (i < T) {
                for (int j = 0; j < i + 1; j++) {
                    if (highs[j] > minT[i]) {
                        minT[i] = highs[j];
                    }
                }
            } else {
                for (int j = i - T + 1; j < i + 1; j++) {
                    if (highs[j] > minT[i]) {
                        minT[i] = highs[j];
                    }
                }
            }
        }

        return minT;
    }


    /**
     * double数组输出为String
     *
     * @param doubles
     * @return
     */
    public static String doubleArrayToString(double[] doubles) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < doubles.length - 1; i++) {
            builder.append(doubles[i]);
            builder.append(",");
        }
        builder.append(doubles[doubles.length - 1]);
        return builder.toString();
    }

    /**
     * 计算过程小数点后四舍五入保留5位小数，最后显示的时候也四舍五入保留5位小数
     *
     * @param original
     * @return
     */
    public static double[] showDoubleArray(double[] original) {
        double[] retu = new double[original.length];
        for (int i = 0; i < original.length; i++) {
            retu[i] = Double.parseDouble(formatUltimate.format(original[i]));
        }
        return retu;
    }

    /**
     * double的减法
     */

    public static Double sub(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return new Double(b1.subtract(b2).doubleValue());
    }

    /**
     * * 两个Double数相除 *
     *
     * @param v1 *
     * @param v2 *
     * @return Double
     */
    public static Double div(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return new Double(b1.divide(b2, DEF_DIV_SCALE, BigDecimal.ROUND_HALF_UP)
                .doubleValue());
    }
}
