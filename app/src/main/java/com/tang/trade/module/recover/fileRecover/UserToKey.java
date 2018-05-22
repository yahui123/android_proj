package com.tang.trade.module.recover.fileRecover;

/**
 * Created by db on 2018/4/16.
 * 用户名-私钥
 */

public class UserToKey {
    private String userName;
    private String privateKey;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
