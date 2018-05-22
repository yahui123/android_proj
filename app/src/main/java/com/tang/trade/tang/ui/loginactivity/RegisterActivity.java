package com.tang.trade.tang.ui.loginactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.TangApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.ResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.MyTextTuils;
import com.tang.trade.tang.utils.UtilOnclick;

import org.bitcoinj.core.ECKey;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;

import static com.tang.trade.tang.net.TangConstant.PATH;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_referrer)
    EditText et_referrer;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    private final String WALLET_BRAINKEY = "HOLLY WOOD BOTH PEACH WOOD JACK SONN STALLONA LAURA SCHWAR ZINGER ARE BIG ARTISTS ";

    private String suggestPublicKey;
    private String suggestPrivateKey;
    private private_key accountPrivateKey;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what ==1){
                startActivity(new Intent(RegisterActivity.this,RegisterSuccessfullyActivity.class).putExtra("name",et_username.getText().toString()).putExtra("suggestPrivateKey",suggestPrivateKey));
                finish();
            }
        }
    };

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            final String userName = et_username.getText().toString();

            if (TextUtils.isEmpty(userName.trim())||!userName.matches("^([a-z]{1}[0-9a-z]{5,19})$")){
                //MyApp.showToast(getString(R.string.bds_account_note));
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, getString(R.string.bds_account_note), Toast.LENGTH_LONG).show();
                btn_confirm.setClickable(true);


            }else {
                createAccount();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public void onClick(View v) {
        if (UtilOnclick.isFastClick()) {
            if (v.getId() == R.id.btn_confirm) {
                progressDialog.show();
                final String userName = et_username.getText().toString();
                btn_confirm.setClickable(false);
                handler.postDelayed(runnable,100);


            } else if (v.getId() == R.id.iv_back) {
                finish();
            }

        }
    }

    @Override
    public void initView() {
        btn_confirm.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        MyTextTuils.setEditTextInhibitInputSpeChat(et_username);
        MyTextTuils.setEditTextInhibitInputSpeChat(et_referrer);

    }

    @Override
    public void initData() {

    }

    private void suggest_brain_key(String usrName) {

        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        String strSpeed = sdf.format(currentTime);
        BrainKey mBrainKey = new BrainKey(WALLET_BRAINKEY + usrName.toUpperCase() + strSpeed.toUpperCase(), BrainKey.DEFAULT_SEQUENCE_NUMBER);
        while (true) {
            try {
                Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
                ECKey ecKey = mBrainKey.getPrivateKey();
                accountPrivateKey = new private_key(ecKey.getPrivKeyBytes());
                suggestPublicKey = address.toString();
                suggestPrivateKey = mBrainKey.getWalletImportFormat();
                types.public_key_type pubKeyType = new types.public_key_type(accountPrivateKey.get_public_key());
                types.private_key_type privateKeyType = BitsharesWalletWraper.getInstance().get_wallet_hash().get(pubKeyType);

                if (privateKeyType == null) {
                    //check block chain have this key ?
                    if ( ! BitsharesWalletWraper.getInstance().is_public_key_registered(suggestPublicKey) ) {
                        break;
                    } else {
                        Date currentTime3 = new Date();
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH:mm:ss");
                        String strSpeed3 = sdf3.format(currentTime3);
                        mBrainKey = new BrainKey(WALLET_BRAINKEY + usrName.toUpperCase() + strSpeed3.toUpperCase(), BrainKey.DEFAULT_SEQUENCE_NUMBER);
                    }
                }
                else {
                    //this wallet is already have this private_key
                    Date currentTime2 = new Date();
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH:mm:ss");
                    String strSpeed2 = sdf2.format(currentTime2);
                    mBrainKey = new BrainKey(WALLET_BRAINKEY + usrName.toUpperCase() + strSpeed2.toUpperCase(), BrainKey.DEFAULT_SEQUENCE_NUMBER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createAccount() {
        final String userName = et_username.getText().toString();
        String referName = et_referrer.getText().toString();
        if (TextUtils.isEmpty(referName))
            referName="";
        if (userName.isEmpty()) {
        }
        else
        {
            if (userName.matches("^[^\\u4e00-\\u9fa5]{0,}$") && referName.matches("^[^\\u4e00-\\u9fa5]{0,}$") ) {

                if (MyTextTuils.containsEmoji(userName) || MyTextTuils.containsEmoji(referName)) {
                    MyApp.showToast(getString(R.string.bds_no_enjoy));
                    btn_confirm.setClickable(true);
                    progressDialog.dismiss();
                    return;
                }
                suggest_brain_key(userName);
                TangApi.creatAccount(userName, suggestPublicKey, referName, "register_account", new JsonCallBack<ResponseModel>(ResponseModel.class) {
                    @Override
                    public void onSuccess(final Response<ResponseModel> response) {
//                    Log.e("eeeee","注册223"+response.body().getStatus());
                        if (response.body().getStatus().equalsIgnoreCase("success")) {

                            //handler.postDelayed(thread1,8000);
                            int s = 1;

                            BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + ChooseWalletActivity.CURRTEN_BIN);

                            //java Android 钱包通过java方式加载到内存，转换成mWalletObject对象
                            int flag = BitsharesWalletWraper.getInstance().load_wallet_file(PATH+ ChooseWalletActivity.CURRTEN_BIN,ChooseWalletActivity.PASSWORD);

                            int nRet = -1;
                            if (flag == 0){
                                //java Android 钱包解锁
                                nRet = BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                            }else {
                                MyApp.showToast(getString(R.string.bds_wallet_load_err));
                                return;
                            }
                            //解锁成功
                            if (nRet == 0) {
                                while (s != 0) {
                                    BitsharesWalletWraper.getInstance().unlock(ChooseWalletActivity.PASSWORD);
                                    s = BitsharesWalletWraper.getInstance().import_key(et_username.getText().toString(), ChooseWalletActivity.PASSWORD, suggestPrivateKey);
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                MyApp.showToast(getString(R.string.bds_regist_successful));
                                btn_confirm.setClickable(true);
                                progressDialog.dismiss();
                                startActivity(new Intent(RegisterActivity.this, RegisterSuccessfullyActivity.class).putExtra("name", et_username.getText().toString()).putExtra("suggestPrivateKey", suggestPrivateKey).putExtra("suggestPublicKey", suggestPublicKey).putExtra("index",-1));
                                finish();
                            }else {
                                MyApp.showToast(getString(R.string.bds_register_fail));
                            }

                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.body().getMsg().equals("error")||TextUtils.isEmpty(response.body().getMsg())){
                                        MyApp.showToast(getString(R.string.bds_register_fail));
                                    }else {
                                        MyApp.showToast(response.body().getMsg());
                                    }
                                    btn_confirm.setClickable(true);
//                                Log.e("eeeee","注册22"+response.body().getMsg());
                                }
                            });
                            btn_confirm.setClickable(true);
                            progressDialog.dismiss();
                        }

                    }

                    @Override
                    public void onError(Response<ResponseModel> response) {
                        super.onError(response);
                        MyApp.showToast(getString(R.string.network));
                        progressDialog.dismiss();
                        btn_confirm.setClickable(true);
                    }

                    @Override
                    public void onStart(Request<ResponseModel, ? extends Request> request) {
                        super.onStart(request);
                    }
                });
            }else {
                MyApp.showToast(getString(R.string.bds_contain_chinese));
                btn_confirm.setClickable(true);
                progressDialog.dismiss();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
        handler.removeCallbacksAndMessages(null);
    }


}
