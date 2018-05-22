package com.tang.trade.tang.ui.loginactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.Device;
import com.tang.trade.tang.utils.MyTextTuils;
import com.tang.trade.tang.utils.UtilOnclick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;

import static com.tang.trade.tang.MyApp.CURRENT_NODE;

public class SettingPasswordActivity extends BaseActivity implements TangApi.MyBaseViewCallBack<ArrayList<String>>{

    @BindView(R.id.tv_ok)
    TextView tv_ok;

    @BindView(R.id.eye_password)
    ImageView eye_password;


    @BindView(R.id.eye_confirm)
    ImageView eye_confirm;


    @BindView(R.id.et_password)
    EditText et_password;


    @BindView(R.id.et_confirm)
    EditText et_confirm;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    int progress = 0;

    private int index = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 0){
                if (progress <30){
                    progress_bar.setProgress(progress);
                    progress++;
                    handler.postDelayed(thread2,30);
                }
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            String node = MyApp.get("current_node","");
            if (node.equals("")){
                TangApi.getNodes(SettingPasswordActivity.this);
            }else {
                MyApp.CURRENT_NODE = node;
                if (!Device.pingIpAddress()){
                    TangApi.getNodes(SettingPasswordActivity.this);
                }
            }

        }
    });

    Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);

        }
    });

    Runnable runnable =new Runnable() {
        @Override
        public void run() {
            global_property_object object = null;
            try {
                object = BitsharesWalletWraper.getInstance().get_global_properties();
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
            if (object == null) {
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
                return;
            }

            String str_password = et_password.getText().toString();
            String str_confirm = et_confirm.getText().toString();

            if (TextUtils.isEmpty(str_password)||TextUtils.isEmpty(str_confirm)) {
                MyApp.showToast(getString(R.string.bds_note_null_pwd));
                progressDialog.dismiss();
                return;

            }

            if (!str_password.equals(str_confirm)) {

                MyApp.showToast(getString(R.string.bds_note_pwd_not_agree));
                progressDialog.dismiss();
                return;
            }

            if (!str_password.matches("^([0-9a-zA-Z]{6,20})$")){
                MyApp.showToast(getString(R.string.bds_pwd_format));
                progressDialog.dismiss();
                return;
            }
            if (!TextUtils.isEmpty(CURRENT_NODE)) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
                Date backTime = new Date();
                String strTime = sdf.format(backTime);
                String binStr = strTime + "_" + "Default.bin";

                BitsharesWalletWraper.getInstance().encrypt_password(str_password, MyApp.CURRENT_NODE);
                BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + binStr);
                BitsharesWalletWraper.getInstance().save_wallet_file();




                ChooseWalletActivity.CURRTEN_BIN = binStr;
                Log.i("bin", ChooseWalletActivity.CURRTEN_BIN);
                ChooseWalletActivity.PASSWORD = str_password;
                MyApp.set("wallet_path", TangConstant.PATH + binStr);
                Intent intent = new Intent(SettingPasswordActivity.this, IndexRegisteredAccountActivity.class);
                startActivity(intent);
                progressDialog.dismiss();
                finish();

            } else {
                MyApp.showToast(getString(R.string.bds_node_link_fail));
                progressDialog.dismiss();
            }








        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting_password;
    }


    @Override
    protected void onBeforeSetContentLayout() {
        super.onBeforeSetContentLayout();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void initView() {


        handler.post(thread);


        tv_ok.setOnClickListener(this);
        eye_password.setOnClickListener(this);
        eye_confirm.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

                case R.id.eye_confirm:
                    if (eye_confirm.isSelected()) {
                        eye_confirm.setSelected(false);
                        et_confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                        et_confirm.setSelection(et_confirm.getText().length());
                    } else {
                        eye_confirm.setSelected(true);
                        et_confirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                        et_confirm.setSelection(et_confirm.getText().length());
                    }
                    break;
                case R.id.eye_password:
                    if (eye_password.isSelected()) {
                        eye_password.setSelected(false);
                        et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                        et_password.setSelection(et_confirm.getText().length());
                    } else {
                        eye_password.setSelected(true);
                        et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                        et_password.setSelection(et_confirm.getText().length());
                    }
                    break;
                case R.id.tv_ok:
                    if (UtilOnclick.isFastClick()) {
                        progressDialog.show();
                        handler.postDelayed(runnable,100);
                    }
                    break;
        }
    }


    @Override
    protected void onResume() {//android.permission.READ_PHONE_STATE
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)   //可读
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  //可写
                        != PackageManager.PERMISSION_GRANTED||
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)  //dadianhu
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限

//                try{
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
//                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},2);
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},3);
//                }catch(Exception e){
////                    Toast.makeText(this, "异常"+e.toString(),Toast.LENGTH_LONG).show();
//                }

            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE},
                    1);

        }
        handler.post(thread2);

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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
