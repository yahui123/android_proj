package com.tang.trade.data.model.entity;

/**
 * Created by Administrator on 2018/4/2.
 */

public class Empty {

    private String id;
    private String LegalMoney;
    private String price;
    private String Count;
    private String Turnover;
    private String Type;

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLegalMoney() {
        return LegalMoney;
    }

    public void setLegalMoney(String legalMoney) {
        LegalMoney = legalMoney;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    public String getTurnover() {
        return Turnover;
    }

    public void setTurnover(String turnover) {
        Turnover = turnover;
    }
}
