package com.tang.trade.tang.net.acceptormodel;

import java.util.List;

public class DownloadAddressModel extends ResponseModelBase {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            url = url;
        }
    }
}
