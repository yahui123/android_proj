package com.tang.trade.tang.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.utils.SPUtils;

import org.bitcoinj.core.ECKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;

import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;

public class MyInfoActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.iv_qr)
    ImageView iv_qr;


    @BindView(R.id.tv_privatekey)
    TextView tv_privatekey;

    @BindView(R.id.tv_publickey)
    TextView tv_publickey;

    @BindView(R.id.et_password)
    TextView et_password;

    @BindView(R.id.tv_submit)
    TextView tv_submit;


    @BindView(R.id.tv_privatekey1)
    TextView tv_privatekey1;

    @BindView(R.id.tv_publickey1)
    TextView tv_publickey1;

    @BindView(R.id.et_password1)
    TextView et_password1;

    @BindView(R.id.tv_submit1)
    TextView tv_submit1;

    @BindView(R.id.ll_bg)
    LinearLayout ll_bg;
    @BindView(R.id.ll_bg1)
    LinearLayout ll_bg1;

    private String qrImportStr;

    private final String WALLET_BRAINKEY = "HELLO LICK BOTH word WOOD BLUE SONN GREEN LAURA ONE ZIKE FIRST BIG GART ";
    private String suggestPublicKey;
    private String suggestPrivateKey;
    private private_key accountPrivateKey;
    private boolean needGenNewKey = false;
    private ClipboardManager cm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_myinfo;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_submit:
                if (tv_submit.getText().equals(getString(R.string.bds_screenshot_save))){

                }else {
                    if (!TextUtils.isEmpty(et_password.getText().toString())){
                        if (et_password.getText().toString().equalsIgnoreCase(PASSWORD)){
                            tv_privatekey.setText(getString(R.string.bds_private_key)+"：\n"+suggestPrivateKey);
                            tv_publickey.setText(getString(R.string.bds_public_key)+"： \n"+suggestPublicKey);
                            tv_submit.setText(getString(R.string.bds_screenshot_save));

                            tv_privatekey1.setText(getString(R.string.bds_private_key)+"：\n"+suggestPrivateKey);
                            tv_publickey1.setText(getString(R.string.bds_public_key)+"： \n"+suggestPublicKey);
                            et_password1.setText(et_password.getText().toString());

                            qrImportStr = "borderless{\"name\":\""+SPUtils.getString(Const.USERNAME,"")+"\",\"key\":\""+suggestPrivateKey+"\"}";

                            iv_qr.setImageBitmap(createCode(qrImportStr));
                            ll_bg.setVisibility(View.GONE);
                            ll_bg1.setVisibility(View.VISIBLE);

                        }else {
                            MyApp.showToast(getString(R.string.bds_incorrect_wallet_password));
                        }
                    }else {
                        MyApp.showToast(getString(R.string.bds_enter_wallet_password));
                    }

                }

                break;

            case R.id.tv_submit1:

                screenshot();
                break;

        }
    }



    @Override
    public void initView() {
        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        tv_submit.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        tv_submit1.setOnClickListener(this);


        tv_privatekey1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // 将文本内容放到系统剪贴板里。
                cm.setText(suggestPrivateKey);
                Toast.makeText(MyInfoActivity.this, getString(R.string.bds_copied_to_clipboard), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        tv_publickey1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // 将文本内容放到系统剪贴板里。
                cm.setText(suggestPublicKey);
                Toast.makeText(MyInfoActivity.this, getString(R.string.bds_copied_to_clipboard), Toast.LENGTH_LONG).show();
                return false;
            }
        });


    }

    @Override
    public void initData() {
        suggest_brain_key(PASSWORD);

        if (suggestPrivateKey == null || suggestPrivateKey.equals("")){
            suggest_brain_key(PASSWORD);
        }

        tv_privatekey.setText(getString(R.string.bds_private_key)+"：\n"+setTheSecret(suggestPrivateKey));
        tv_publickey.setText(getString(R.string.bds_public_key)+"：\n"+setTheSecret(suggestPublicKey));
    }

    private void suggest_brain_key(String password) {

        if (needGenNewKey) { //generate a new key with password
            BrainKey mBrainKey = new BrainKey(WALLET_BRAINKEY + password, BrainKey.DEFAULT_SEQUENCE_NUMBER);
            try {
                Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
                ECKey ecKey = mBrainKey.getPrivateKey();
                accountPrivateKey = new private_key(ecKey.getPrivKeyBytes());
                suggestPublicKey = address.toString();
                suggestPrivateKey = mBrainKey.getWalletImportFormat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //get account key with account

            try {
                account_object account = BitsharesWalletWraper.getInstance().get_account_object(SPUtils.getString(Const.USERNAME,""));
                if (account != null) {
                    types.public_key_type pubKeyType = account.owner.get_keys().get(0);
                    if (pubKeyType != null) {
                        suggestPublicKey = pubKeyType.toString();
                        types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(pubKeyType);
                        if (privateKeyType != null) {
                            suggestPrivateKey = privateKeyType.toString();
                        } else{
                            BitsharesWalletWraper.getInstance().clear();
                            BitsharesWalletWraper.getInstance().load_wallet_file(TangConstant.PATH+CURRTEN_BIN,PASSWORD);
                            BitsharesWalletWraper.getInstance().unlock(PASSWORD);
                            privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(pubKeyType);
                            if (privateKeyType != null) {
                                suggestPrivateKey = privateKeyType.toString();
                            }
                        }

                    }
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

        }

    }


    private String setTheSecret(String string){
        String str = "";
        if (string == null){
            return str;
        }

        byte [] b = string.getBytes();
        for (int i = 0;i<b.length;i++){
            b[i] = '*';
        }
        str = new String(b);
        return str;
    }



    private void screenshot()
    {
        // 获取屏幕
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();
        if (bmp != null)
        {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + "/bds/" + System.currentTimeMillis()+"_my_key.png";

                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                try {
                    MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // 最后通知图库更新
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                MyApp.showToast(getString(R.string.bds_screenshot_save_successful));
            } catch (Exception e) {
                MyApp.showToast(getString(R.string.bds_screenshot_fail));
            }
        }
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
