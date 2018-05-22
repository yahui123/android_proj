package com.tang.trade.module.profile.saveqrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.flh.framework.rx.RxJavaUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tang.trade.app.Const;
import com.tang.trade.base.AbsBasePresenter;
import com.tang.trade.data.model.httpRequest.SMSResult;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.http.old.BorderObserver;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.data.remote.websocket.BorderlessException;
import com.tang.trade.data.remote.websocket.BorderlessOnSubscribe;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.utils.MD5;
import com.tang.trade.utils.EnQrCode;
import com.tang.trade.utils.SPUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/17.
 */

public class SaveQrCodePresenter extends AbsBasePresenter<SaveQrCodeContract.View, SaveQrCodeContract.Model> implements SaveQrCodeContract.Presenter {
    private final String SUCCESS = "success";

    public SaveQrCodePresenter(SaveQrCodeContract.View views) {
        super(views, new SaveQrCodeModel());
    }

    @Override
    public void saveQr(Activity activity) {
        BorderlessDataManager.getInstance().executeAsync(new BorderlessOnSubscribe() {
            @Override
            public Object onPre() {

                // 获取屏幕
                View dView = activity.getWindow().getDecorView();
                dView.setDrawingCacheEnabled(true);
                dView.buildDrawingCache();
                Bitmap bmp = dView.getDrawingCache();
                if (bmp != null) {
                    try {
                        // 图片文件路径
                        String filePath = TangConstant.QR_PATH + System.currentTimeMillis() + ".jpg";


                        File destDir = new File(TangConstant.QR_PATH);
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                        }

                        File file = new File(filePath);//创建文件
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new BorderlessException(activity.getString(R.string.save_qr_failed));
                            }
                        }

                        FileOutputStream os = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                        os.close();

                        try {
                            MediaStore.Images.Media.insertImage(activity.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
                            // 最后通知图库更新
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(file);
                            mediaScanIntent.setData(contentUri);
                            activity.sendBroadcast(mediaScanIntent);

                            return SUCCESS;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            throw new BorderlessException(activity.getString(R.string.save_qr_failed));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new BorderlessException(activity.getString(R.string.save_qr_failed));
                    }
                } else {
                    throw new BorderlessException(activity.getString(R.string.save_qr_failed));
                }

            }
        }, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在保存...");
            }

            @Override
            public void onNext(Object o) {
                mView.backSuccessFish();

            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });

    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public Bitmap createCode(String url) {
        int width = 180;
        int height = 180;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix;
        try {

            QRCodeWriter writer = new QRCodeWriter();
            bitMatrix = writer.encode(url,// id
                    BarcodeFormat.QR_CODE, width, height, hints);
            return bitMatrix2Bitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void enCode(String userName) {
        EnQrCode.enQrCode(userName);
    }


    /**
     * 发送获取验证码
     *
     * @param phone
     */
    @Override
    public void sendSMS(String phone) {
        mModel.sendSMS(phone, new BorderObserver<SMSResult>() {

            @Override
            public void onError(String errorMsg) {
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading();
            }

            @Override
            public void onNext(SMSResult o) {
                mView.backSMS(o.getIdentityCode());
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void modifyPhone(String phone, String code, String identityCode) {
        String userName = SPUtils.getString(Const.USERNAME, "");
        String qrContent = SPUtils.getString(userName + Const.QR_CONTENT, "");
        String md5Key = MD5.md5(qrContent);
        mModel.modifyPhone(phone, code, identityCode, md5Key, new BorderObserver<Object>() {

            @Override
            public void onError(String errorMsg) {
                mView.dissLoading();
                DataError error = new DataError();
                error.setErrorMessage(errorMsg);
                mView.onError(error);

            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading();
            }

            @Override
            public void onNext(Object o) {
                mView.backSuccessFish();
                //  mView.backSMS(o. getIdentityCode());
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

    @Override
    public void verifyPwd(String password) {
        String userName = SPUtils.getString(Const.USERNAME, "");
        BorderlessDataManager.getInstance().verifyPwd(userName, password, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                mView.onError(error);
                mView.dissLoading();
            }

            @Override
            public void onSubscribe(Disposable d) {
                mView.showLoading("正在验证密码...");
            }

            @Override
            public void onNext(Object o) {
                mView.verifySuccess();
            }

            @Override
            public void onComplete() {
                mView.dissLoading();
            }
        });
    }

}
