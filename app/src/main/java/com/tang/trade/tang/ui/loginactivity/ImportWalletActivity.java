package com.tang.trade.tang.ui.loginactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.UtilOnclick;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;

public class ImportWalletActivity extends BaseActivity {
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.tv_path)
    TextView tv_path;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.tv_submit)
    TextView tv_submit;
    @BindView(R.id.iv_back)
    ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_wallet;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_submit:

                if (UtilOnclick.isFastClick()) {

                    if (!tv_address.getText().toString().trim().equalsIgnoreCase(getString(R.string.address))){
                        if (tv_address.getText().toString().contains(".bin") || tv_address.getText().toString().contains(".txt") ||
                                tv_address.getText().toString().contains(".pdf") || tv_address.getText().toString().contains(".doc")) {
                            File file = new File(tv_address.getText().toString().trim());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
                            Date backTime = new Date();
                            String strTime = sdf.format(backTime);
                            String strFileName = strTime + "_Default.bin";
                            String s = FileUtils.copyFile(file, TangConstant.PATH, strFileName);
                            if (s.equalsIgnoreCase("1")) {
                                MyApp.showToast(getString(R.string.bds_import_fail));
                            } else {
                                MyApp.showToast(getString(R.string.bds_import_success));
                                if (getIntent().getStringExtra("class").equalsIgnoreCase("input")) {
                                    MyApp.set("isload", "yes");
                                } else {
                                    startActivity(new Intent(this, ImportWalletActivity.class));
                                }
                                finish();
                            }
                        }else {
                            MyApp.showToast(getString(R.string.bds_file_err));
                        }
                    }else{
                        MyApp.showToast(getString(R.string.bds_full_adress));
                    }
                }



                break;
            case R.id.tv_path:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//
//
//                StorageManager mStorageManager = this.getSystemService(StorageManager.class);
//                StorageVolume storageVolume = mStorageManager.getPrimaryStorageVolume();
//                storageVolume.createAccessIntent(Environment.DIRECTORY_PICTURES);

                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        tv_submit.setOnClickListener(this);
        tv_path.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }


    String path;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                tv_address.setText(path);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                tv_address.setText(path);
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                tv_address.setText(path);

            }
        }
    }



    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }


    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
