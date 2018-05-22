package com.tang.trade.tang.ui.loginactivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.UtilOnclick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class RegisterSuccessfullyActivity extends BaseActivity  {

    private TextView tv_username,tv_private_key,tv_public_kay,tv_screen_save;
    private int index=-1;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_register_account_successfully;
    }

    @Override
    protected void onBeforeSetContentLayout() {
        super.onBeforeSetContentLayout();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }




    @Override
    public void initView() {
        tv_username = findViewById(R.id.tv_username);
        tv_private_key = findViewById(R.id.tv_privatekey);
        tv_public_kay = findViewById(R.id.tv_publickey);
        tv_screen_save = findViewById(R.id.tv_screen_save);
        tv_screen_save.setOnClickListener(this);

        tv_username.setText(getIntent().getStringExtra("name"));
        tv_private_key.setText(getIntent().getStringExtra("suggestPrivateKey"));
        tv_public_kay.setText(getIntent().getStringExtra("suggestPublicKey"));
        index=getIntent().getIntExtra("index",-1);

    }

    @Override
    public void initData() {
    }



    @Override
    public void onClick(View view) {
        if (UtilOnclick.isFastClick()) {
            switch (view.getId()) {
                case R.id.tv_screen_save:
                    screenshot();
                    break;
            }
        }
    }



    private void screenshot(){
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
                setFinishi();
            } catch (Exception e) {
                MyApp.showToast(getString(R.string.bds_screenshot_fail));
                setFinishi();
            }
        }else{
            MyApp.showToast(getString(R.string.bds_screenshot_fail));
            setFinishi();
        }
    }

    private void setFinishi(){
        if (index==1){
            startActivity(new Intent(this, LoginActivity.class).putExtra("index",index));
        }

        finish();
    }

}
