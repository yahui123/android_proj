package com.tang.trade.tang.ui.loginactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.ui.SelectNodeActivity;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.UtilOnclick;
import com.tang.trade.tang.widget.DeleteWalletDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import butterknife.BindView;

import static com.tang.trade.tang.net.TangConstant.PATH;

public class ChooseWalletActivity extends BaseActivity implements TangApi.MyBaseViewCallBack<ArrayList<String>>{

    public static String PASSWORD;
    public static String CURRTEN_BIN;

    @BindView(R.id.tv_select_wallet)
    TextView tv_wallet;
    @BindView(R.id.et_password)
    EditText et_pwd;
    @BindView(R.id.btn_confirm)
    TextView btn_confirm;
    @BindView(R.id.tv_import_wallet)
    TextView tv_import;
    @BindView(R.id.tv_delete_wallet)
    TextView tv_delete;
    @BindView(R.id.line_select_wallet)
    LinearLayout ll_wallet;
    @BindView(R.id.tv_select_node)
    TextView tv_select_node;
    @BindView(R.id.eye_password)
    ImageView eye_password;

    private PopupWindow popWnd;
    private File file1;
    private String[] files;
    private String[] lastFiles;
    private int index = 0;

    Handler handler = new Handler() {};

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            if (!Device.pingIpAddress()){
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
            }else {
                String str_pwd=et_pwd.getText().toString();
                if (!TextUtils.isEmpty(str_pwd)){
                    setOnclickOK();
                }else {
                    MyApp.showToast(getString(R.string.bds_note_null_pwd));
                    progressDialog.dismiss();
                }
            }
        }
    };

    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_wallet;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String node = MyApp.get("current_node","");
        if (node.equals("")) {
            TangApi.getNodes(ChooseWalletActivity.this);
        } else {
            MyApp.CURRENT_NODE = node;
            if (!Device.pingIpAddress()) {
                TangApi.getNodes(ChooseWalletActivity.this);
            }
        }

        if (BuildConfig.UPDATE) {
            if (!BuildConfig.IS_UPDATE) {
                redirectTo();
            }
        }

        if (!MyApp.get("isload","").equalsIgnoreCase("")){
            getFileName();
            MyApp.set("isload","");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BuildConfig.UPDATE = false;
        BuildConfig.IS_UPDATE = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void redirectTo() {
        Intent intent = new Intent(this,SettingPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
    @Override
    protected void onBeforeSetContentLayout() {
        super.onBeforeSetContentLayout();

    }

    @Override
    public void initView() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)   //可读
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  //可写
                            != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)  //可写
                            != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)  //dadianhu
                            != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_PHONE_STATE}
                                ,1);
            }
        }
    }

    @Override
    public void initData() {
        getFileName();
        ll_wallet.setOnClickListener(this);

        btn_confirm.setOnClickListener(this);

        if (tv_delete != null) {
            tv_delete.setOnClickListener(this);
        }

        tv_import.setOnClickListener(this);
        tv_select_node.setOnClickListener(this);
        eye_password.setOnClickListener(this);
    }

    private boolean isHave(String[] strs, String s){
        if (strs != null && strs.length>0){
            for(int i=0;i<strs.length;i++){
                if(strs[i].indexOf(s)!=-1){
                    return true;
                }
            }
        }
        return false;
    }

    AlertDialog.Builder singleChoiceDialog=null;
    private void showSingleWalletDialog(final String[] items){
        if (singleChoiceDialog==null){
            singleChoiceDialog =
                    new AlertDialog.Builder(this);
            singleChoiceDialog.setTitle(getString(R.string.choicewallet));
            singleChoiceDialog.setNegativeButton(getString(R.string.button_cancel),null);
        }

        singleChoiceDialog.setSingleChoiceItems(items, position,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                        tv_wallet.setText(lastFiles[position]);
                        MyApp.set("wallet_path", PATH+lastFiles[position]);
                        et_pwd.setText("");
                        dialog.dismiss();
                    }
                });

        singleChoiceDialog.show();
    }

    private void getFileName(){
        file1=new File(PATH);
        files = FileUtils.getFileName1(file1);
        lastFiles = isJson();
        if (isJson() != null && lastFiles != null){
            if (lastFiles.length>0){
                if (isHave(lastFiles,"Default.bin")){
                    for (int i = 0; i < lastFiles.length ; i++){
                        if (lastFiles[i].contains("Default.bin")){
                            tv_wallet.setText(lastFiles[i]);
                            position = i;
                            MyApp.set("wallet_path", PATH+lastFiles[i]);
                            break;
                        }
                    }
                } else {
                    tv_wallet.setText(lastFiles[0]);
                    position = 0;
                    MyApp.set("wallet_path", PATH+lastFiles[0]);
                }
            } else {
                redirectTo();
            }
        } else {
            redirectTo();
        }
    }

    private String[] isJson() {
        String[] str2 = null;
        ArrayList<String> list = new ArrayList<String>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!TextUtils.isEmpty(files[i])){
                    String str = null;
                    str = FileUtils.readSDFile(PATH + files[i]).trim();
                    if (str.contains("chain_id") && str.contains("ws_server")) {
                        String str1 = str.replace("\n", "");
                        list.add(files[i]);
                    }
                }
            }

            if (list.size() > 0) {
                str2 = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    str2[i] = list.get(i);
                }
            }
        }

        return str2;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.line_select_wallet:
                if (UtilOnclick.isFastClick()){
                    if (singleChoiceDialog == null){
                        showSingleWalletDialog(lastFiles);
                    }else {
                        if (!singleChoiceDialog.create().isShowing()){
                            showSingleWalletDialog(lastFiles);
                        }
                    }
                }
                break;
            case R.id.eye_password:
                if (eye_password.isSelected()) {
                    eye_password.setSelected(false);
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().length());
                } else {
                    eye_password.setSelected(true);
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().length());
                }
                break;
            case R.id.btn_confirm:
                if (UtilOnclick.isFastClick()) {
                    // 进行点击事件后的逻辑操作'
                    progressDialog.show();
                    handler.postDelayed(runnable,100);
                }
                break;
            case R.id.tv_delete_wallet:
                showPopupClearPassword();
                break;
            case R.id.tv_import_wallet:
                startActivity(new Intent(ChooseWalletActivity.this,ImportWalletActivity.class).putExtra("class","input"));
                break;
            case R.id.tv_select_node:
                startActivity(new Intent(ChooseWalletActivity.this,SelectNodeActivity.class));
                break;
        }
    }

    DeleteWalletDialog deleteWalletDialog=null;
    private void showPopupClearPassword() {
            deleteWalletDialog=new DeleteWalletDialog(this);
            deleteWalletDialog.setOnDialogOnClick(new DeleteWalletDialog.OnDialogOnClick() {
                @Override
                public void OnSumbmitListener() {
                    deleteWalletDialog.dismiss();

                    File file=new File(PATH+lastFiles[position]);
                    if(file.exists()) {
                        BitsharesWalletWraper.getInstance().set_wallet_file_path(file.getPath());
                        BitsharesWalletWraper.getInstance().reset();
                        files = FileUtils.getFileName1(file1);
                        lastFiles = isJson();
                        position = 0;
                    }

                    if (lastFiles != null) {
                        if (lastFiles.length>0) {
                            tv_wallet.setText(lastFiles[position]);
                            MyApp.set("wallet_path", PATH+lastFiles[position]);
                        } else {
                            redirectTo();
                            MyApp.set("wallet_path","");
                        }
                    } else {
                        redirectTo();
                        MyApp.set("wallet_path","");
                    }
                }
            });
    }

    private void setOnclickOK(){
        String str_pwd=et_pwd.getText().toString();

        if (!TextUtils.isEmpty(str_pwd)){
            BitsharesWalletWraper.getInstance().clear();
            BitsharesWalletWraper.getInstance().load_wallet_file(PATH+tv_wallet.getText().toString(),str_pwd);
            int nRet = BitsharesWalletWraper.getInstance().unlock(et_pwd.getText().toString());
            if (nRet == 0){
                startActivity(new Intent(ChooseWalletActivity.this,LoginActivity.class));
                PASSWORD = et_pwd.getText().toString();
                CURRTEN_BIN = tv_wallet.getText().toString();
                Log.i("bin",CURRTEN_BIN);
                finish();
            }else{
                MyApp.showToast(getString(R.string.bds_note_ncorrecti_pwd));
                progressDialog.dismiss();
            }

        }
    }

    @Override
    public void setData(final ArrayList<String> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                if (list.size()>0) {
                    Collections.shuffle(list);

                    for (int j = 0; j < 2; j++){
                        for (int i = 0; i <list.size(); i++){
                            MyApp.CURRENT_NODE = list.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag){
                                MyApp.set("current_node",list.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }
                        if (index == 1){
                            break;
                        }
                    }

                }else {
                    Gson gson = new Gson();
                    ArrayList<String> data = gson.fromJson(TangConstant.NODE_JSON,ArrayList.class);
                    Collections.shuffle(data);
                    for (int j = 0; j < 2; j++){
                        for (int i = 0; i <data.size(); i++){
                            MyApp.CURRENT_NODE = data.get(i);
                            boolean flag = Device.pingIpAddress();
                            if (flag){
                                MyApp.set("current_node",data.get(i));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                index = 1;
                                break;
                            }
                        }

                        if (index == 1){
                            break;
                        }
                    }
                }

                if (list.size()>0){
                    for (int i = 0;i<list.size();i++){
                        MyApp.set("nodes",list.size());
                        MyApp.set("node"+i,list.get(i));
                    }
                }
            }
        }).start();
    }

    @Override
    public void start() {
        progressDialog.show();
    }

    @Override
    public void onEnd() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                ArrayList<String> data = gson.fromJson(TangConstant.NODE_JSON,ArrayList.class);

                Collections.shuffle(data);

                for (int j = 0; j < 2; j++){
                    for (int i = 0; i <data.size(); i++){
                        MyApp.CURRENT_NODE = data.get(i);
                        boolean flag = Device.pingIpAddress();
                        if (flag){
                            MyApp.set("current_node",data.get(i));
                            index = 1;
                            break;
                        }
                    }

                    if (index == 1){
                        break;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            ChooseWalletActivity.this.finish();
        }
        return true;
    }
}