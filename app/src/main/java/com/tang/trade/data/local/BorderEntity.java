package com.tang.trade.data.local;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by daibin on 2018/5/8.
 * 表名：config_msg  大小写敏感
 */

@Entity(tableName = "config_msg")
public class BorderEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "msgKey")
    private String msgKey;

    @ColumnInfo(name = "code")
    private int code;

    @ColumnInfo(name = "msg")
    private String msg;


    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
