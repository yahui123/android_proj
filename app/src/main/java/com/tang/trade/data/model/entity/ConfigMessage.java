package com.tang.trade.data.model.entity;

import java.util.List;

/**
 * Created by daibin on 2018/5/8.
 */

public class ConfigMessage {

    private int versionId;
    private List<Msgs> msgs;

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public int getVersionId() {
        return versionId;
    }

    public void setMsgs(List<Msgs> msgs) {
        this.msgs = msgs;
    }

    public List<Msgs> getMsgs() {
        return msgs;
    }

    public static class Msgs {

        private int code;
        private String msg;
        private String msgKey;

        public void setCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsgKey(String msgKey) {
            this.msgKey = msgKey;
        }

        public String getMsgKey() {
            return msgKey;
        }
    }
}
