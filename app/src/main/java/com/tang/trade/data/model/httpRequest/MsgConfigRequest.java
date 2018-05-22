package com.tang.trade.data.model.httpRequest;

/**
 * Created by daibin on 2018/5/8.
 */

public class MsgConfigRequest extends AbsHttpRequest {
    private String useType;
    private String versionId;

    public MsgConfigRequest(String useType, String versionId) {
        this.useType = useType;
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getUseType() {
        return useType;
    }

    public void setUseType(String useType) {
        this.useType = useType;
    }
}
