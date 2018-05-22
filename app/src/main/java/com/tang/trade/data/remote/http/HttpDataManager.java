package com.tang.trade.data.remote.http;

import com.flh.framework.rx.RxJavaUtil;
import com.tang.trade.data.model.entity.ChangePhoneBean;
import com.tang.trade.data.model.entity.ConfigMessage;
import com.tang.trade.data.model.entity.HelpCenter;
import com.tang.trade.data.model.entity.PhoneBean;
import com.tang.trade.data.model.entity.PhoneResult;
import com.tang.trade.data.model.entity.QrCodeBean;
import com.tang.trade.data.model.entity.RandomEncryptResult;
import com.tang.trade.data.model.entity.VerifyBean;
import com.tang.trade.data.model.httpRequest.MsgConfigRequest;
import com.tang.trade.data.model.httpRequest.PhoneRequest;
import com.tang.trade.data.model.httpRequest.RandomEncryptRequest;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.http.old.BorderService;
import com.tang.trade.data.remote.http.old.HttpHandleFuc;
import com.tang.trade.module.register.create.RegisterBean;
import com.tang.trade.tang.net.JsonCreator;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;

/**
 * Created by leo on 01/03/2018.
 */

public class HttpDataManager {
    private static volatile HttpDataManager instance;
    private final BorderService sBorderService;
    private final NodeService sNodeService;
    private final IMService sIMService;
    private final BorderService sNewBorderService;

    private HttpDataManager() {

        sBorderService = HttpApiClient.getBorderService();
        sNodeService = HttpApiClient.getNodeService();
        sIMService = HttpApiClient.getIMService();
        sNewBorderService = HttpApiClient.getNewBorderService();

    }

    public static HttpDataManager getInstance() {
        if (instance == null) {
            synchronized (HttpDataManager.class) {
                if (instance == null) {
                    instance = new HttpDataManager();
                }
            }
        }
        return instance;
    }

    public void getNodes(Observer<List<String>> observer) {
        sNodeService.getNodes()
                .compose(RxJavaUtil.<List<String>>threadTransform())
                .subscribe(observer);
    }

    /**
     * 注册IM
     */
    public void regIM(String userName, Observer<Object> observer) {
//        RegIMRequest request = new RegIMRequest(userName);
//        bdsaccount
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("bdsaccount", userName);

        sIMService.regIM("member_im_account_add", JsonCreator.getJsonString(hashMap))
                .map(new HttpHandleFuc<Object>())
                .compose(RxJavaUtil.<Object>threadTransform())
                .subscribe(observer);
    }

    /**
     * 注册
     */
    public void register(String accountname, String publickey, String referreraccount, Observer<Object> observer) {
        RegisterBean bean = new RegisterBean(accountname, publickey, referreraccount);

        sNewBorderService.register(bean)
                .map(new HttpFunction<Object>())
                .compose(RxJavaUtil.<Object>threadTransform())
                .subscribe(observer);
//                            sBorderService.register("register_account", JsonCreator.getJsonString(hashMap))
//                    .map(new HttpHandleFuc<Object>())
//                    .compose(RxJavaUtil.<Object>threadTransform())
//                    .subscribe(observer);

    }

    /**
     * 发送短信
     */
    public void sendSMS(String phone, Observer<SMSResult> observer) {
        PhoneBean bean = new PhoneBean(phone);
        sNewBorderService.sendSMS(bean)
                .map(new HttpFunction<SMSResult>())
                .compose(RxJavaUtil.<SMSResult>threadTransform())
                .subscribe(observer);

    }

    /**
     * 验证短信
     */
    public void verifySMS(String phone, String code, String identityCode, Observer<Object> observer) {
        VerifyBean bean = new VerifyBean(phone, code, identityCode);
        sNewBorderService.verifySMS(bean)
                .map(new HttpFunction<Object>())
                .compose(RxJavaUtil.<Object>threadTransform())
                .subscribe(observer);

    }

    /**
     * 保存二维码
     */
    public void saveQrCode(String phone, String code, String identityCode, String md5Key, String encryptKey, Observer<Object> observer) {
        QrCodeBean bean = new QrCodeBean(phone, code, identityCode, md5Key, encryptKey);
        sNewBorderService.saveQrCode(bean)
                .map(new HttpFunction<Object>())
                .compose(RxJavaUtil.<Object>threadTransform())
                .subscribe(observer);

    }

    /**
     * 修改手机号
     */
    public void modifyPhone(String phone, String code, String identityCode, String md5Key, Observer<Object> observer) {
        ChangePhoneBean bean = new ChangePhoneBean(phone, code, identityCode, md5Key);
        sNewBorderService.modifyPhone(bean)
                .map(new HttpFunction<Object>())
                .compose(RxJavaUtil.<Object>threadTransform())
                .subscribe(observer);

    }


    /**
     * 获取MD5主键对应手机号
     *
     * @param md5QrAes 二维码内容再次MD5
     * @param observer
     */
    public void correspondingPhone(String md5QrAes, Observer<PhoneResult> observer) {
        PhoneRequest request = new PhoneRequest(md5QrAes);

        sNewBorderService.correspondingPhone(request)
                .map(new HttpFunction<PhoneResult>())
                .compose(RxJavaUtil.<PhoneResult>threadTransform())
                .subscribe(observer);
    }

    /**
     * 获取16位随机加密串
     *
     * @param md5Key
     * @param phone
     * @param code
     * @param identityCode
     */
    public void getRandomEncrypt(String md5Key, String phone, String code, String identityCode, Observer<RandomEncryptResult> observer) {
        RandomEncryptRequest request = new RandomEncryptRequest(md5Key, phone, code, identityCode);

        sNewBorderService.getRandomEncrypt(request)
                .map(new HttpFunction<RandomEncryptResult>())
                .compose(RxJavaUtil.<RandomEncryptResult>threadTransform())
                .subscribe(observer);
    }

    /**
     * 获取配置信息
     *
     * @param useType
     * @param versionId
     * @param observer
     */
    public void getMessageConfig(String useType, String versionId, Observer<ConfigMessage> observer) {
        MsgConfigRequest request = new MsgConfigRequest(useType,versionId);

        sNewBorderService.getMessageConfig(request)
                .map(new HttpFunction<ConfigMessage>())
                .compose(RxJavaUtil.<ConfigMessage>threadTransform())
                .subscribe(observer);
    }

    public void getHelpCenter(Observer<HelpCenter> observer) {
        sNewBorderService.getHelpCenter()
                .map(new HttpFunction<HelpCenter>())
                .compose(RxJavaUtil.threadTransform())
                .subscribe(observer);
    }
}
