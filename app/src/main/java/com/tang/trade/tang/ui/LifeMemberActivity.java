package com.tang.trade.tang.ui;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.SPUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import butterknife.BindView;

public class LifeMemberActivity extends BaseActivity {

    @BindView(R.id.tv_copy)
    TextView tv_copy;
    @BindView(R.id.tv_load)
    TextView tv_load;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.iv_qr)
    ImageView iv_qr;



    private Bitmap bitmap;

    /**
     * 保存图片所用的名称
     */
    public static final String SHARE_PIC = "shares_borderless.png";
    /**
     * 图片保存的路劲
     **/
    public final static String ALBUM_PATH = Environment
            .getExternalStorageDirectory() + "/borderless/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_member_life;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_load:
                Bitmap bitmap = ((BitmapDrawable)iv_qr.getDrawable()).getBitmap();
                saveImageToGallery(this,bitmap);
                break;
            case R.id.tv_copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(tv_name.getText());
                MyApp.showToast(getString(R.string.copy_ed));
                break;
        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        tv_load.setOnClickListener(this);
        tv_copy.setOnClickListener(this);

    }

    @Override
    public void initData() {
        iv_qr.setImageBitmap( createCode(SPUtils.getString(Const.USERNAME,"")));
    }

    /**
     * 保存图片
     * @param context
     * @param bmp
     */
    private void saveImageToGallery(Context context, Bitmap bmp) {

        File appDir = new File(ALBUM_PATH);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = SHARE_PIC;
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);

        MyApp.showToast(getString(R.string.savepic));
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

    private Bitmap createCode(String url) {
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
}
