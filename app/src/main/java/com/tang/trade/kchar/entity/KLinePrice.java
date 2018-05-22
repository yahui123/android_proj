package com.tang.trade.kchar.entity;

/**
 * Created by Harpoonman on 2016/5/7 0007.
 */
public class KLinePrice implements Comparable {
    public long time;//
    public float begin;
    public float high;
    public float low;
    public float end;
    public double volume;


    public KLinePrice(long time, float begin, float high, float low, float end, double volume) {
        this.time = time;
        this.begin = begin;
        this.high = high;
        this.low = low;
        this.end = end;
        this.volume = volume;
    }

    @Override
    public int compareTo(Object another) {
        float v = 10000 * low - 10000 * ((KLinePrice) another).low;
        if (v > 0)
            return 1;
        else if (v < 0)
            return -1;
        else return 0;
    }
}
