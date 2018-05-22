package com.tang.trade.tang.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.UtilOnclick;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;

import static com.tang.trade.tang.net.TangConstant.PATH;

public class BackUpActivity extends BaseActivity {

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
    private File file;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void backup()
    {
        //check password
        String strPassword = et_password.getText().toString();
        if (strPassword.isEmpty()) {
             MyApp.showToast(getString(R.string.bds_note_null_pwd));
        } else {

            if (!strPassword.equals(ChooseWalletActivity.PASSWORD)){
                MyApp.showToast(getString(R.string.bds_invalid_password));
                return;
            }


           if (0 == BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD)) {
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
               Date backTime = new Date();
               String strTime = sdf.format(backTime);
               String strFileName = "wallet_"+strTime+".bin";
               String strWalletFilePath= TangConstant.PATH_BACKUP + strFileName;
               int result = FileUtils.backUpCli(PATH+ChooseWalletActivity.CURRTEN_BIN,strWalletFilePath);
               if (result == 0){
                   MyApp.showToast(getString(R.string.bds_backup_success));
                   showMyDialog();
               }else {
                   MyApp.showToast(getString(R.string.bds_backup_fail));
               }

           } else {
               MyApp.showToast(getString(R.string.bds_invalid_password));
           }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_back_up;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_submit:
                if (UtilOnclick.isFastClick()) {
                    backup();
                }
                break;
            case R.id.tv_path:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
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
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                 str = path.substring(0,path.lastIndexOf("/"))+"/";
                tv_address.setText(getString(R.string.address)+str);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                 str = path.substring(0,path.lastIndexOf("/"))+"/";
                tv_address.setText(getString(R.string.address)+str);
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                 str = path.substring(0,path.lastIndexOf("/"))+"/";
                tv_address.setText(getString(R.string.address)+str);

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


    //分享单张图片
    public void shareSingleBin() {
        String imagePath = TangConstant.PATH + ChooseWalletActivity.CURRTEN_BIN;
        //由文件得到uri
//        Uri imageUri = Uri.fromFile(new File(imagePath));
//        Log.d("share", "uri:" + imageUri);
//
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.setType("*/*");
//        startActivity(Intent.createChooser(shareIntent, getString(R.string.bds_share)));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(this, "tang",  new File(imagePath));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            uri = Uri.fromFile(  new File(imagePath));
            intent.setDataAndType(uri, "*/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        intent.setType("*/*");

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.bds_share)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 分享
     *   AlertDialog.Builder builder
     */
    private AlertDialog alertDialog=null;
    private void showMyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (alertDialog == null) {
            alertDialog = builder.create();
        }
        alertDialog.setCanceledOnTouchOutside(false);
        builder.setCancelable(false);

        builder.setTitle(R.string.bds_safety_alert);
        builder.setMessage(R.string.bds_safety_alert_message);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shareSingleBin();
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

            }
        });
        builder.show();

    }


}
