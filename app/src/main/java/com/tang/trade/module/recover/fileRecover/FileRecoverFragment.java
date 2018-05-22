package com.tang.trade.module.recover.fileRecover;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flh.framework.toast.ToastAlert;
import com.google.gson.Gson;
import com.tang.trade.base.AbsMVPFragment;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.module.recover.setPassword.SetPwdActivity;
import com.tang.trade.tang.R;
import com.tang.trade.utils.AlphaUtil;
import com.tang.trade.utils.BdsTextWatcher;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by admin on 2018/4/10.
 * 文件恢复
 */

public class FileRecoverFragment extends AbsMVPFragment<FileRecoverContract.Presenter> implements FileRecoverContract.View {

    private static final int REQUEST_CODE = 1024;

    @BindView(R.id.tv_import_file)
    TextView mTvFileName;
    @BindView(R.id.tv_select_file)
    TextView mTvSelectFile;
    @BindView(R.id.et_pwd)
    EditText mEtPwd;
    @BindView(R.id.iv_eye)
    ImageView mIvEye;
    @BindView(R.id.tv_btn)
    TextView mTvBtn;

    private boolean isShow = true;
    private String mPath;

    public static FileRecoverFragment newInstance(/*String param1, String param2*/) {
        FileRecoverFragment fragment = new FileRecoverFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_file_recover_400;
    }

    @Override
    protected void setupWidget(View view, Bundle savedInstanceState) {
        AlphaUtil.setDisable(mTvBtn);

        BdsTextWatcher watcher = new BdsTextWatcher() {

            @Override
            protected void afterChanged(Editable s) {
                String fileName = mTvFileName.getText().toString().trim();
                String pwd = mEtPwd.getText().toString().trim();

                if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(pwd)) {
                    AlphaUtil.setDisable(mTvBtn);
                } else {
                    AlphaUtil.setEnable(mTvBtn);
                }
            }
        };
        mEtPwd.addTextChangedListener(watcher);
        mTvFileName.addTextChangedListener(watcher);
    }

    @Override
    public FileRecoverContract.Presenter getPresenter() {
        return new FileRecoverPresenter(this);
    }

    @Override
    public void onError(DataError error) {
        ToastAlert.showToast(getContext(), error.getErrorMessage());
    }

    @Override
    public void loadSuccess() {
        String path = mTvFileName.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        /*2. 读取钱包文件中的用户信息*/
        /*3. 根据用户名获取对应私钥*/
        mPresenter.getUserInfoFromWallet(path, pwd);
    }

    @Override
    public void userInfoSuccess(List<UserToKey> list) {
        /*4. 得到了用户名以及私钥*/
        SetPwdActivity.start(getContext(), new Gson().toJson(list), SetPwdActivity.FILE_RECOVER);

    }

    @OnClick({R.id.tv_select_file, R.id.iv_eye, R.id.tv_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_select_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//                StorageManager mStorageManager = this.getSystemService(StorageManager.class);
//                StorageVolume storageVolume = mStorageManager.getPrimaryStorageVolume();
//                storageVolume.createAccessIntent(Environment.DIRECTORY_PICTURES);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.iv_eye:
                if (isShow) {
                    mEtPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIvEye.setImageResource(R.mipmap.common_eye_true);
                    isShow = false;
                } else {
                    mEtPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIvEye.setImageResource(R.mipmap.common_eye_false);
                    isShow = true;
                }
                mEtPwd.setSelection(mEtPwd.getText().toString().trim().length());
                break;
            case R.id.tv_btn:
                String path = mTvFileName.getText().toString().trim();
                String pwd = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(pwd)) {
                    ToastAlert.showToast(getContext(), getString(R.string.path_not_be_null));
                    return;
                }

                /*1. 验证钱包文件（其实就是load,unlock的过程）*/
                mPresenter.loadWallet(path, pwd);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {//是否选择，没选择就不会继续
            return;
        }

        if (requestCode == REQUEST_CODE) {

            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                mPath = uri.getPath();
                mTvFileName.setText(mPath);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                mPath = getPath(getContext(), uri);
                mTvFileName.setText(mPath);
            } else {//4.4以下下系统调用方法
                mPath = getRealPathFromURI(uri);
                mTvFileName.setText(mPath);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
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

                final long id = Long.parseLong(DocumentsContract.getDocumentId(uri));
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id);

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
