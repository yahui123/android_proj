package com.tang.trade.tang.socket.market;


import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.limit_order_object;

public class OpenOrder {
    public limit_order_object limitOrder;
    public asset_object base;
    public asset_object quote;
    public double price;
}
