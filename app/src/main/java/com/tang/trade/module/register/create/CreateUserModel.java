package com.tang.trade.module.register.create;

import com.flh.framework.util.FileUtil;
import com.tang.trade.base.AbsBaseModel;
import com.tang.trade.data.model.entity.Key;
import com.tang.trade.data.remote.http.HttpDataManager;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.tang.net.TangConstant;

/**
 * Created by Administrator on 2018/4/10.
 */

public class CreateUserModel extends AbsBaseModel implements CreateUserContract.Model {
    private String privateKey;
    /**
     * 生成公钥和私钥
     */
    @Override
    public void GeneratePublicPrivateKey(String word,AsyncObserver<Key> observer) {
        BorderlessDataManager.getInstance().generatePrivateKeyFromWord(word.toUpperCase(), observer);
    }

    /**
     * 注册接口
     * @param user
     * @param publicKey
     * @param referName
     * @param privateKey
     * @param observer
     */
    @Override
    public void register(final String user,String publicKey, String referName,String privateKey, BorderObserver observer) {
        this.privateKey=privateKey;
        HttpDataManager.getInstance().register(user, publicKey, referName,observer);

    }

    @Override
    public void createWalletFile(final String user, final String secondPassword,AsyncObserver<Object> observer) {
        FileUtil.deleteFile(TangConstant.PATH + user + ".bin");
        BorderlessDataManager.getInstance().createWalletFile(user, privateKey, secondPassword,observer);

    }

    @Override
    public void keyImport(String userName,AsyncObserver<Object> observer) {
        BorderlessDataManager.getInstance().keyImport(userName, privateKey,observer);
    }


}
