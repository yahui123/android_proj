package com.tang.trade.tang.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.tang.trade.app.Const;
import com.tang.trade.data.remote.http.DataError;
import com.tang.trade.data.remote.websocket.AsyncObserver;
import com.tang.trade.data.remote.websocket.BorderlessDataManager;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.R;
import com.tang.trade.tang.net.AcceptorApi;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.callback.JsonCallBack;
import com.tang.trade.tang.net.model.HttpResponseModel;
import com.tang.trade.tang.socket.BitsharesWalletWraper;
import com.tang.trade.tang.socket.account_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.socket.public_key;
import com.tang.trade.tang.ui.base.BaseActivity;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.MyTextTuils;
import com.tang.trade.utils.SPUtils;

import org.bitcoinj.core.ECKey;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import io.reactivex.disposables.Disposable;

import static com.tang.trade.tang.net.TangConstant.PATH;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.CURRTEN_BIN;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;


public class CreateAccountActivity extends BaseActivity {

    private final String WALLET_BRAINKEY = "HOLLY WOOD BOTH PEACH WOOD JACK SONN LAURA ZINGER ARE BIG ARTISTS ";

    private private_key accountPrivateKey;
    private String suggestPublicKey;
    private String suggestPrivateKey;


    @BindView(R.id.et_user)
    EditText et_user;
    @BindView(R.id.btn_ok)
    Button btn_ok;
    @BindView(R.id.iv_back)
    ImageView iv_back;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    if (!BitsharesWalletWraper.getInstance().is_public_key_registered(suggestPublicKey)) {
                        break;
                    } else {
                        Date currentTime3 = new Date();
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH:mm:ss");
                        String strSpeed3 = sdf3.format(currentTime3);
                        mBrainKey = new BrainKey(WALLET_BRAINKEY + usrName.toUpperCase() + strSpeed3.toUpperCase(), BrainKey.DEFAULT_SEQUENCE_NUMBER);
                    }

                } else {
                    Date currentTime2 = new Date();
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH:mm:ss");
                    String strSpeed2 = sdf2.format(currentTime2);
                    //this wallet is already have this private_key
                    mBrainKey = new BrainKey(WALLET_BRAINKEY + usrName.toUpperCase() + strSpeed2.toUpperCase(), BrainKey.DEFAULT_SEQUENCE_NUMBER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void createAccount(String account_name) {

        final String loginUser = SPUtils.getString(Const.USERNAME, "");

        BorderlessDataManager.getInstance().registerForWebSocket(suggestPublicKey, account_name, loginUser, loginUser, 0, new AsyncObserver() {
            @Override
            public void onError(DataError error) {
                MyApp.showToast(getString(R.string.network));
                progressDialog.dismiss();
            }

            @Override
            public void onSubscribe(Disposable d) {
                progressDialog.show();
            }

            @Override
            public void onNext(Object o) {
                BitsharesWalletWraper.getInstance().set_wallet_file_path(TangConstant.PATH + CURRTEN_BIN);
                int flag = BitsharesWalletWraper.getInstance().load_wallet_file(PATH + CURRTEN_BIN, PASSWORD);
                int nRet;
                if (flag != -1) {
                    //java Android 钱包解锁
                    nRet = BitsharesWalletWraper.getInstance().unlock(PASSWORD);
                } else {
                    MyApp.showToast(getString(R.string.bds_wallet_load_err));
                    return;
                }

                //解锁成功
                if (nRet != 0) {
                    MyApp.showToast(getString(R.string.registerfail));

                }
                if (0 == BitsharesWalletWraper.getInstance().import_key(et_user.getText().toString(), PASSWORD, suggestPrivateKey)) {
                    MyApp.showToast(getString(R.string.registersuccess));
                    finish();
                } else {
                    MyApp.showToast(getString(R.string.registerfail));
                }
                progressDialog.dismiss();
            }

            @Override
            public void onComplete() {

            }
        });

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_account;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_ok:
                final String userName = et_user.getText().toString();
                if (userName.isEmpty()) {
                    MyApp.showToast(getString(R.string.bds_note_null_accout));
                } else {
                    suggestPrivateKey = null;
                    while (suggestPrivateKey == null) {
                        suggest_brain_key(userName);
                    }

                    if (userName.matches("^[^\\u4e00-\\u9fa5]{0,}$") == true) {

                        if (MyTextTuils.containsEmoji(userName)) {
                            MyApp.showToast(getString(R.string.bds_no_enjoy));
                            return;
                        }

                        createAccount(userName);

                    } else {
                        MyApp.showToast(getString(R.string.bds_contain_chinese));
                    }


                }
                break;
        }
    }

    @Override
    public void initView() {
        iv_back.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }


    //分享单张图片
    public void shareSingleBin() {
        String imagePath = TangConstant.PATH + CURRTEN_BIN;
        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Log.d("share", "uri:" + imageUri);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("*/*");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.bds_share)));
    }


    /**
     * 分享
     */
    private void showMyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.setCancelable(false);


        builder.setTitle(R.string.bds_safety_alert);
        builder.setMessage(R.string.bds_safety_alert_message);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shareSingleBin();
                alertDialog.dismiss();
                finish();

            }
        });
        builder.show();
    }



}
