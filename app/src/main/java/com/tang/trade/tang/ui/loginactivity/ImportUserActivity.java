package com.tang.trade.tang.ui.loginactivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.AccountModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.CaptureActivity;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.FileUtils;
import com.tang.trade.tang.utils.QRCodeUtil;
import com.tang.trade.tang.utils.UtilOnclick;

import org.bitcoinj.core.ECKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.BrainKey;

import static com.tang.trade.tang.net.TangConstant.PATH;

public class ImportUserActivity extends BaseActivity {
    private static final int REQURE_CODE_SCAN = 100;
    private static final String DECODED_CONTENT_KEY = "codedContent";

    @BindView(R.id.improt_rg)
    RadioGroup radioGroup;
    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.iv_qr)
    ImageView iv_qr;

    @BindView(R.id.line_qr)
    LinearLayout line_qr;

    @BindView(R.id.line_qwd)
    LinearLayout line_qwd;

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.et_key_username)
    EditText et_key_username;

    @BindView(R.id.et_key_pwd)
    EditText et_key_pwd;

    @BindView(R.id.btn_pwd_comfirm)
    Button btn_pwd_comfirm;
    @BindView(R.id.btn_private_key)
    Button btn_private_key;

    @BindView(R.id.eye_key)
    ImageView eye_key;
    @BindView(R.id.eye_pwd)
    ImageView eye_pwd;

    Handler handler=new Handler();





    private ArrayList<AccountModel> data = new ArrayList<AccountModel>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_user;
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        iv_qr.setOnClickListener(this);
        btn_pwd_comfirm.setOnClickListener(this);
        btn_private_key.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.rg0){
                     line_qwd.setVisibility(View.GONE);
                     line_qr.setVisibility(View.VISIBLE);
                    radioGroup.setSelected(false);
                }else {
                    line_qwd.setVisibility(View.VISIBLE);
                    line_qr.setVisibility(View.GONE);
                    radioGroup.setSelected(true);

                }
            }
        });
        initAccount();
        eye_key.setOnClickListener(this);
        eye_pwd.setOnClickListener(this);

    }

    @Override
    public void initData() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  //打开相机权限
                    != PackageManager.PERMISSION_GRANTED ) {

                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        2);

            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.eye_key:
                if (eye_key.isSelected()) {
                    eye_key.setSelected(false);
                    et_key_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    et_key_pwd.setSelection(et_key_pwd.getText().length());
                } else {
                    eye_key.setSelected(true);
                    et_key_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    et_key_pwd.setSelection(et_key_pwd.getText().length());
                }
                break;

            case R.id.eye_pwd:
                if (eye_pwd.isSelected()) {
                    eye_pwd.setSelected(false);
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    et_password.setSelection(et_password.getText().length());
                } else {
                    eye_pwd.setSelected(true);
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    et_password.setSelection(et_password.getText().length());
                }
                break;
            case R.id.btn_private_key:
                if (UtilOnclick.isFastClick()) {
                    progressDialog.show();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final String nameKey =et_key_username.getText().toString().trim();
                                final String pwdKey =et_key_pwd.getText().toString().trim();
                                if (nameKey == null || nameKey.isEmpty()) {
                                    MyApp.showToast(getString(R.string.bds_note_null_accout));
                                    progressDialog.dismiss();
                                }else if (TextUtils.isEmpty(pwdKey)){
                                    MyApp.showToast(getString(R.string.bds_enter_private_key));
                                    progressDialog.dismiss();

                    }else if (pwdKey.length()<32){
                                    MyApp.showToast(getString(R.string.bds_private_key_err));
                                    progressDialog.dismiss();
                                }else {
                                    for (int i = 0 ; i < data.size() ; i++){
                                        if (data.get(i).getName().equals(nameKey)){
                                            MyApp.showToast(getString(R.string.bds_AccountAlreadyExists));
                                            progressDialog.dismiss();
                                            return;
                                        }
                                    }
                                    keyImport(pwdKey, nameKey);
                                }
                            }
                        },100);

                }


                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_qr:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, REQURE_CODE_SCAN);
                break;
            case R.id.btn_pwd_comfirm:
                if (UtilOnclick.isFastClick()) {
                    progressDialog.show();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final String name =et_username.getText().toString().trim();
                            final String pwd =et_password.getText().toString().trim();
                            final String myBrainKey=normailzeBrainKey(pwd);
                            if (name == null || name.isEmpty()) {
                                MyApp.showToast(getString(R.string.bds_note_null_accout));
                                progressDialog.dismiss();
                            }else if (myBrainKey == null || myBrainKey.isEmpty()) {
                                MyApp.showToast(getString(R.string.bds_note_null_pwd));
                                progressDialog.dismiss();
                            }else {
                                for (int i = 0 ; i < data.size() ; i++){
                                    if (data.get(i).getName().equals(name)){
                                        MyApp.showToast(getString(R.string.bds_AccountAlreadyExists));
                                        progressDialog.dismiss();
                                        return;
                                    }
                                }
                                oldUserUpgrade(name,myBrainKey);
                            }

                        }
                    },100);



                }

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQURE_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(DECODED_CONTENT_KEY);

                if (content.contains("borderless")){
                    String json = content.substring(10,content.length());

                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        if (json != null){
                            String name = jsonObject.optString("name");
                            String strBrainKey = jsonObject.optString("key");
                            et_key_username.setText(name);
                            et_key_pwd.setText(strBrainKey);
                            iv_qr.setImageBitmap(QRCodeUtil.getQRBitMap(strBrainKey));
                        }
                    } catch (JSONException e) {

                    }
                }else {
                    MyApp.showToast(getString(R.string.bds_no_identified));
                }


            }
        }
    }


    private void initAccount(){
        data.clear();
        try {
            String str = FileUtils.readSDFile(MyApp.get("wallet_path","")).trim();
            String str1 = str.replace("\n","");
            if (!str1.equalsIgnoreCase("")){
                JSONObject data1 = new JSONObject(str1);
                JSONArray jsonArray = data1.optJSONArray("my_accounts");
                for (int i = 0; i<jsonArray.length() ; i++){
                    AccountModel accountModel = new AccountModel();
                    JSONObject data2 = jsonArray.optJSONObject(i);
                    accountModel.setId(data2.optString("id"));
                    accountModel.setName(data2.optString("name"));
                    accountModel.setMemokey(data2.optJSONObject("options").optString("memo_key"));
                    data.add(accountModel);
                }

            }
        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**密码导入
     * */

    private void oldUserUpgrade(String username,String myBrainKey) {

        //首先查账户存在否？
        account_object account = null;
        try {
            account = BitsharesWalletWraper.getInstance().get_account_object(username);
            if (account == null){
                account = BitsharesWalletWraper.getInstance().get_account_object(username);
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
//            MyApp.showToast("check account "+oldUser +" fail");
        }
        boolean server_have_pubkey = false;
        //节点上已经有账户了
        if (account != null) {
            //获取私钥
            for (int i = 0; i < 10; ++i) {
                BrainKey brainKey = new BrainKey(myBrainKey, i);
                ECKey ecKey = brainKey.getPrivateKey();
                private_key privateKey = new private_key(ecKey.getPrivKeyBytes());
                types.private_key_type privateKeyType = new types.private_key_type(privateKey);
                types.public_key_type publicKeyType = new types.public_key_type(privateKey.get_public_key());
                //校验 account public key
                if (account.active.is_public_key_type_exist(publicKeyType) == false &&
                        account.owner.is_public_key_type_exist(publicKeyType) == false &&
                        account.options.memo_key.compare(publicKeyType) == false) {

                    continue;
                } else {
                    server_have_pubkey = true;
                    //import_brain_key 会自动save_wallet_file
                    BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + ChooseWalletActivity.CURRTEN_BIN );

                    //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                    int flag = BitsharesWalletWraper.getInstance().load_wallet_file(PATH+ ChooseWalletActivity.CURRTEN_BIN,ChooseWalletActivity.PASSWORD);

                    int nRet = -1;
                    if (flag == 0){
                        //java Android 钱包解锁
                        nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                    }else {
                        MyApp.showToast(getString(R.string.bds_wallet_load_err));
                        progressDialog.dismiss();
                        return;
                    }


                    //解锁成功
                    if (nRet == 0) {

                        if( 0 == BitsharesWalletWraper.getInstance().import_brain_key(username,ChooseWalletActivity.PASSWORD,myBrainKey) ) {
                            //分享
//                        showMyDialog();
                            MyApp.showToast(getString(R.string.bds_import_success));
                            progressDialog.dismiss();
                            finish();

                        } else {
                            MyApp.showToast(getString(R.string.bds_import_fail));

                        }
                    }else {
                        MyApp.showToast(getString(R.string.bds_import_fail));
                    }

                    break;
                }
            }

            if (!server_have_pubkey) {
                MyApp.showToast(getString(R.string.bds_invalid_password));
            }
        } else {
            MyApp.showToast(getString(R.string.bds_AccountNoExists));
        }

        progressDialog.dismiss();
    }

    /*私钥导入
    * */

    private void keyImport(String strBrainKey,String accountName){
        try {
            account_object obj = BitsharesWalletWraper.getInstance().get_account_object(accountName);
            if (obj == null) {
                obj = BitsharesWalletWraper.getInstance().get_account_object(accountName);
                if (obj == null) {
                    MyApp.showToast(getString(R.string.bds_AccountNoExists));
                    progressDialog.dismiss();
                    return;
                }

            }
            types.public_key_type publicKeyType = obj.owner.get_keys().get(0);
            String strPublicKey = publicKeyType.toString();

            try{
                types.private_key_type privateKeyType = new types.private_key_type(strBrainKey);
                if (privateKeyType == null) {
                    MyApp.showToast(getString(R.string.bds_private_key_err));
                    progressDialog.dismiss();
                    return;
                }

                public_key publicKey = privateKeyType.getPrivateKey().get_public_key();
                types.public_key_type publicKeyType2 = new types.public_key_type(publicKey);
                String strWifPubKey = publicKeyType2.toString();
                if (!strPublicKey.equals(strWifPubKey)) {

                    MyApp.showToast(getString(R.string.bds_private_key_err));
                    progressDialog.dismiss();
                    return;
                }
            }catch (Exception e){
                MyApp.showToast(getString(R.string.bds_private_key_err));
                progressDialog.dismiss();

                return;
            }

        } catch (NetworkStatusException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;

        }


        BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + ChooseWalletActivity.CURRTEN_BIN);

        //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
        int flag = BitsharesWalletWraper.getInstance().load_wallet_file(PATH+ ChooseWalletActivity.CURRTEN_BIN,ChooseWalletActivity.PASSWORD);

        int nRet = -1;
        if (flag == 0){
            //java Android 钱包解锁
            nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
        }else {
            MyApp.showToast(getString(R.string.bds_wallet_load_err));
            progressDialog.dismiss();
            return;
        }

        //解锁成功
        if (nRet == 0) {
            if (0 == BitsharesWalletWraper.getInstance().import_key(accountName, ChooseWalletActivity.PASSWORD, strBrainKey)) {
                MyApp.showToast(getString(R.string.bds_import_success));
                progressDialog.dismiss();
                finish();
            } else {
                MyApp.showToast(getString(R.string.bds_import_fail));
            }
        }else {
            MyApp.showToast(getString(R.string.bds_import_fail));
        }

        progressDialog.dismiss();
    }
    public  String normailzeBrainKey(String strBrainKey) {
        int i=0;
        int j = 0;

        strBrainKey.replace('\t',' ');
        strBrainKey.replace('\r',' ');
        strBrainKey.replace('\n',' ');
        strBrainKey.replace('\f',' ');
        //strBrainKey.replace('\v',' ');
        int n = strBrainKey.length();
        byte[] sourceData = strBrainKey.getBytes();
        byte[] destData = new byte[n];
        int c;

        while (i <n) {
            c = sourceData[i++];
            switch (c) {
                case ' ':
                    destData[j++] = ' ';
                    break;
                case 'a': c = 'A'; break;
                case 'b': c = 'B'; break;
                case 'c': c = 'C'; break;
                case 'd': c = 'D'; break;
                case 'e': c = 'E'; break;
                case 'f': c = 'F'; break;
                case 'g': c = 'G'; break;
                case 'h': c = 'H'; break;
                case 'i': c = 'I'; break;
                case 'j': c = 'J'; break;
                case 'k': c = 'K'; break;
                case 'l': c = 'L'; break;
                case 'm': c = 'M'; break;
                case 'n': c = 'N'; break;
                case 'o': c = 'O'; break;
                case 'p': c = 'P'; break;
                case 'q': c = 'Q'; break;
                case 'r': c = 'R'; break;
                case 's': c = 'S'; break;
                case 't': c = 'T'; break;
                case 'u': c = 'U'; break;
                case 'v': c = 'V'; break;
                case 'w': c = 'W'; break;
                case 'x': c = 'X'; break;
                case 'y': c = 'Y'; break;
                case 'z': c = 'Z'; break;
                default:
                    break;
            }
            destData[j++] = (byte)c;
        }

        String result = new String(destData);
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
