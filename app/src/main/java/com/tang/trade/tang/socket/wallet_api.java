package com.tang.trade.tang.socket;


import android.text.TextUtils;
import android.util.Log;

import com.google.common.primitives.UnsignedInteger;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.tang.trade.app.Const;
import com.tang.trade.tang.MyApp;
import com.tang.trade.tang.net.TangConstant;
import com.tang.trade.tang.net.model.AllHistory;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.block_chain_info;
import com.tang.trade.tang.socket.chain.block_header;
import com.tang.trade.tang.socket.chain.block_object;
import com.tang.trade.tang.socket.chain.bucket_object;
import com.tang.trade.tang.socket.chain.dynamic_global_property_object;
import com.tang.trade.tang.socket.chain.fee_schedule;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.chain.global_config_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.limit_order_object;
import com.tang.trade.tang.socket.chain.memo_data;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operation_history_object;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.price;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.chain.types;
import com.tang.trade.tang.socket.chain.vesting_balance_object;
import com.tang.trade.tang.socket.common.ErrorCode;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.fc.crypto.aes;
import com.tang.trade.tang.socket.fc.crypto.sha256_object;
import com.tang.trade.tang.socket.fc.crypto.sha512_object;
import com.tang.trade.tang.socket.fc.io.base_encoder;
import com.tang.trade.tang.socket.fc.io.datastream_encoder;
import com.tang.trade.tang.socket.fc.io.datastream_size_encoder;
import com.tang.trade.tang.socket.fc.io.raw_type;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.socket.market.MarketTrade;
import com.tang.trade.tang.socket.market.OrderBook;
import com.tang.trade.tang.utils.BuildConfig;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.utils.SPUtils;

import org.bitcoinj.core.ECKey;
import org.json.JSONException;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA512Digest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.BrainKey;

import static com.tang.trade.data.remote.websocket.BorderlessDataManager.loginAccountId;

public class wallet_api {

    class wallet_object {
        sha256_object chain_id;
        List<account_object> my_accounts = new ArrayList<>();
        ByteBuffer cipher_keys;
        HashMap<object_id<account_object>, List<types.public_key_type>> extra_keys = new HashMap<>();
        String ws_server = "";
        String ws_user = "";
        String ws_password = "";

        public void update_account(account_object accountObject) {
            boolean bUpdated = false;
            for (int i = 0; i < my_accounts.size(); ++i) {
                if (my_accounts.get(i).id == accountObject.id) {
                    my_accounts.remove(i);
                    my_accounts.add(accountObject);
                    bUpdated = true;
                    break;
                }
            }

            if (bUpdated == false) {
                my_accounts.add(accountObject);
            }
        }
    }

    private websocket_api mWebsocketApi = new websocket_api();
    private wallet_object mWalletObject;
    private boolean mbLogin = false;
    private HashMap<types.public_key_type, types.private_key_type> mHashMapPub2Priv = new HashMap<>();
    private sha512_object mCheckSum = new sha512_object();

    static class plain_keys {
        Map<types.public_key_type, String> keys;
        sha512_object checksum;

        public void write_to_encoder(base_encoder encoder) {
            raw_type rawType = new raw_type();

            rawType.pack(encoder, UnsignedInteger.fromIntBits(keys.size()));
            for (Map.Entry<types.public_key_type, String> entry : keys.entrySet()) {
                encoder.write(entry.getKey().key_data);

                byte[] byteValue = entry.getValue().getBytes();
                rawType.pack(encoder, UnsignedInteger.fromIntBits(byteValue.length));
                encoder.write(byteValue);
            }
            encoder.write(checksum.hash);
        }

        public static plain_keys from_input_stream(InputStream inputStream) {
            plain_keys keysResult = new plain_keys();
            keysResult.keys = new HashMap<>();
            keysResult.checksum = new sha512_object();

            raw_type rawType = new raw_type();
            UnsignedInteger size = rawType.unpack(inputStream);
            try {
                for (int i = 0; i < size.longValue(); ++i) {
                    types.public_key_type publicKeyType = new types.public_key_type();
                    inputStream.read(publicKeyType.key_data);

                    UnsignedInteger strSize = rawType.unpack(inputStream);
                    byte[] byteBuffer = new byte[strSize.intValue()];
                    inputStream.read(byteBuffer);
                    String strPrivateKey = new String(byteBuffer);
                    keysResult.keys.put(publicKeyType, strPrivateKey);
                }
                inputStream.read(keysResult.checksum.hash);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return keysResult;
        }
    }

    public wallet_api() {
    }

    public int initialize() {
        int nRet = mWebsocketApi.connect();
        if (nRet == 0) {
            sha256_object sha256Object;
            sha256Object = mWebsocketApi.get_const_chain_id();
            if (mWalletObject == null) {
                mWalletObject = new wallet_object();
                mWalletObject.chain_id = sha256Object;
            } else if (mWalletObject.chain_id != null && mWalletObject.chain_id.equals(sha256Object) == false) {
                nRet = -1;
            }
        }
        return nRet;
    }

    public int reset() {
        //for java wallet reset
        mWebsocketApi.close();
        mWalletObject = null;
        mbLogin = false;
        mHashMapPub2Priv.clear();
        mCheckSum = new sha512_object();
        return 0;
    }

    public void close() {
        mWebsocketApi.close();
    }

    public boolean is_locked() {
        if (mWalletObject.cipher_keys.array().length > 0 &&
                mCheckSum.equals(new sha512_object())) {
            return true;
        }
        return false;
    }


    public String encrypt_password(String strPassword, String wsUrl) {

        if (mWalletObject == null)
            initialize();
        set_passwrod(strPassword);
        lock();
        mWalletObject.ws_server = wsUrl;
        return "";

    }

    public String encrypt_password_400(String strPassword, String wsUrl) {

        if (mWalletObject == null) {
            int initialize = initialize();

            if (initialize != 0) {
                return "failed";
            }
        }
        set_passwrod(strPassword);
        lock();
        mWalletObject.ws_server = wsUrl;
        return "success";

    }


    private void encrypt_keys() {
        plain_keys data = new plain_keys();
        data.keys = new HashMap<>();
        for (Map.Entry<types.public_key_type, types.private_key_type> entry : mHashMapPub2Priv.entrySet()) {
            data.keys.put(entry.getKey(), entry.getValue().toString());
        }
        data.checksum = mCheckSum;

        datastream_size_encoder sizeEncoder = new datastream_size_encoder();
        data.write_to_encoder(sizeEncoder);
        datastream_encoder encoder = new datastream_encoder(sizeEncoder.getSize());
        data.write_to_encoder(encoder);

        byte[] byteKey = new byte[32];
        System.arraycopy(mCheckSum.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(mCheckSum.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteResult = aes.encrypt(byteKey, ivBytes, encoder.getData());
        mWalletObject.cipher_keys = byteResult;

        return;

    }

    public int lock() {
        //for java wallet lock
        encrypt_keys();
        mCheckSum = new sha512_object();
        mHashMapPub2Priv.clear();
        return 0;
    }


    public int unlock(String strPassword) {
        assert (strPassword.length() > 0);

        //for java wallet unlok
        sha512_object passwordHash = sha512_object.create_from_string(strPassword);
        if (passwordHash == null || passwordHash.hash == null) {
            return -1;
        }
        byte[] byteKey = new byte[32];
        System.arraycopy(passwordHash.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(passwordHash.hash, 32, ivBytes, 0, ivBytes.length);
        if (mWalletObject == null || mWalletObject.cipher_keys == null) {
            return -1;
        }

        ByteBuffer byteDecrypt = aes.decrypt(byteKey, ivBytes, mWalletObject.cipher_keys.array());
        if (byteDecrypt == null || byteDecrypt.array().length == 0) {
            return -1;
        }

        plain_keys dataResult = plain_keys.from_input_stream(
                new ByteArrayInputStream(byteDecrypt.array())
        );

        for (Map.Entry<types.public_key_type, String> entry : dataResult.keys.entrySet()) {
            types.private_key_type privateKeyType = new types.private_key_type(entry.getValue());
            if (!TextUtils.isEmpty(privateKeyType.toString()))
                mHashMapPub2Priv.put(entry.getKey(), privateKeyType);
        }

        mCheckSum = passwordHash;
        if (passwordHash.equals(dataResult.checksum)) {
            return 0;
        } else {
            return -1;
        }
    }

    public boolean is_new() {
        if (mWalletObject == null || mWalletObject.cipher_keys == null) {
            return true;
        }

        return (mWalletObject.cipher_keys.array().length == 0 &&
                mCheckSum.equals(new sha512_object()));
    }



    //根据账户名称查询账户私钥
    public String getWifKeyWithAccount(String accountName) {
        String result = "";
        for (int i = 0; i < list_my_accounts().size(); i++) {
            account_object accountObject = list_my_accounts().get(i);
            String account_name = accountObject.name.toString();
            if (account_name.equals(accountName)) {
                types.public_key_type pubKeyType = accountObject.owner.get_keys().get(0);
                if (pubKeyType != null) {
                    types.private_key_type privateKeyType = mHashMapPub2Priv.get(pubKeyType);
                    if (privateKeyType != null) {
                        result = privateKeyType.toString();
                    }
                }
            }
        }

        return result;
    }


    //根据账户ID 查账户名
    public String getAccountNameById(String account_id) {
        String result = "";
        for (int i = 0; i < list_my_accounts().size(); i++) {
            account_object accountObject = list_my_accounts().get(i);
            String accountid = accountObject.id.toString();
            if (accountid.equals(account_id)) {
                result = accountObject.name.toString();
                break;
            }
        }

        return result;
    }



    public int load_wallet_file(String strFileName, String password) {
        if (mWalletObject != null) {
            return 0;
        }

        //web socket load wallet file
        try {
            FileInputStream fileInputStream = new FileInputStream(strFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            Gson gson = global_config_object.getInstance().getGsonBuilder().create();
            mWalletObject = gson.fromJson(inputStreamReader, wallet_object.class);
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return -1;
        } catch (JsonIOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int save_wallet_file(String strFileName) {
        int nRet = 0;

        File destDir = new File(TangConstant.PATH);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File destDir1 = new File(TangConstant.PATH_BACKUP);
        if (!destDir1.exists()) {
            destDir1.mkdirs();
        }

        File file = new File(strFileName);//创建文件
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(strFileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            Gson gson = global_config_object.getInstance().getGsonBuilder().create();
            outputStreamWriter.write(gson.toJson(mWalletObject));
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            nRet = -1;
        } catch (IOException e) {
            e.printStackTrace();
            nRet = -1;
        }

        return nRet;
    }

    public int save_wallet_file_400(String strFileName) {
        int nRet = 0;

        File destDir = new File(TangConstant.PATH);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File destDir1 = new File(TangConstant.PATH_BACKUP);
        if (!destDir1.exists()) {
            destDir1.mkdirs();
        }

        File file = new File(strFileName);//创建文件
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(strFileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            Gson gson = global_config_object.getInstance().getGsonBuilder().create();
            outputStreamWriter.write(gson.toJson(mWalletObject));
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            nRet = -1;
        } catch (IOException e) {
            e.printStackTrace();
            nRet = -1;
        }

        return nRet;
    }



    public int set_passwrod(String strPassword) {
        //for java wallet set_password
        mCheckSum = sha512_object.create_from_string(strPassword);

        return 0;
    }



    /*public synchronized int login(String strUserName, String strPassowrd) throws IOException {
        if (mbLogin) {
            return 0;
        }

        if (mWalletObject != null) {
            mbLogin = mWebsocketApi.login(
                    mWalletObject.ws_user,
                    mWalletObject.ws_password
            );
        } else {
            mWalletObject = new wallet_object();
            mbLogin = mWebsocketApi.login(
                    strUserName,
                    strPassowrd
            );
        }

        if (mbLogin) {
            mWebsocketApi.get_database_api_id();
            mWebsocketApi.get_history_api_id();
            mWebsocketApi.get_broadcast_api_id();


            mWalletObject.ws_user = strUserName;
            mWalletObject.ws_password = strPassowrd;
            sha256_object sha256Object = mWebsocketApi.get_chain_id();
            if (mWalletObject.chain_id != null &&
                    mWalletObject.chain_id.equals(sha256Object) == false) {
                return -1; // 之前的chain_id与当前的chain_id不一致
            }
            mWalletObject.chain_id = sha256Object;
        }

        return 0;
    }*/

    public List<account_object> list_my_accounts() {
        List<account_object> accountObjectList = new ArrayList<>();
        if (mWalletObject != null) {
            accountObjectList.addAll(mWalletObject.my_accounts);
        }
        return accountObjectList;
    }

    public account_object get_account(String strAccountNameOrId) throws NetworkStatusException {
        // 判定这类型
        object_id<account_object> accountObjectId = object_id.create_from_string(strAccountNameOrId);

        List<account_object> listAccountObject;
        if (accountObjectId == null) {
            listAccountObject = lookup_account_names(strAccountNameOrId);

            if (listAccountObject == null) {
                return null;
            } else {
                if (listAccountObject.isEmpty()) {
                    return null;
                }

                return listAccountObject.get(0);
            }
        } else {
            List<object_id<account_object>> listAccountObjectId = new ArrayList<>();
            listAccountObjectId.add(accountObjectId);
            listAccountObject = mWebsocketApi.get_accounts(listAccountObjectId);
            if (listAccountObject == null) {
                return null;
            } else {
                if (listAccountObject.isEmpty()) {
                    return null;
                }

                return listAccountObject.get(0);
            }
        }
    }

    public List<account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        return mWebsocketApi.get_accounts(listAccountObjectId);
    }

    public String get_Fee(String id, int op) throws NetworkStatusException {
        return mWebsocketApi.get_Fee(id, op);
    }

    public List<account_object> lookup_account_names(String strAccountName) throws NetworkStatusException {
        return mWebsocketApi.lookup_account_names(strAccountName);
    }

    public List<asset> list_account_balance(object_id<account_object> accountId) throws NetworkStatusException {
        return mWebsocketApi.list_account_balances(accountId);
    }


    public List<operation_history_object> get_account_history(object_id<account_object> accountId, int nLimit) throws NetworkStatusException {
        return mWebsocketApi.get_account_history(accountId, nLimit);
    }

    public List<HistoryResponseModel.DataBean> get_transfer_history(object_id<account_object> accountId, int nLimit) throws NetworkStatusException {
        return mWebsocketApi.get_transfer_history(accountId, nLimit);
    }

    public List<Asset> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        return mWebsocketApi.list_assets(strLowerBound, nLimit);
    }

    public List<asset_object> list_assets_obj(String strLowerBound, int nLimit) throws NetworkStatusException {
        return mWebsocketApi.list_assets_obj(strLowerBound, nLimit);
    }

    public List<asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        return mWebsocketApi.get_assets(listAssetObjectId);
    }

    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
        return mWebsocketApi.get_block_header(nBlockNumber);
    }

    public block_object get_block(int nBlockNumber) throws NetworkStatusException {
        return mWebsocketApi.get_block(nBlockNumber);
    }

    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        return mWebsocketApi.lookup_asset_symbols(strAssetSymbol);
    }

    public String lookup_asset_symbols_rate(String strAssetSymbol) throws NetworkStatusException {
        return mWebsocketApi.lookup_asset_symbols_rate(strAssetSymbol);

    }

    public int import_brain_key(String strAccountNameOrId, String strBrainKey) throws NetworkStatusException {
        account_object accountObject = get_account(strAccountNameOrId);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        Map<types.public_key_type, types.private_key_type> mapPublic2Private = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            BrainKey brainKey = new BrainKey(strBrainKey, i);
            ECKey ecKey = brainKey.getPrivateKey();
            private_key privateKey = new private_key(ecKey.getPrivKeyBytes());
            types.private_key_type privateKeyType = new types.private_key_type(privateKey);
            types.public_key_type publicKeyType = new types.public_key_type(privateKey.get_public_key());

            if (accountObject.active.is_public_key_type_exist(publicKeyType) == false &&
                    accountObject.owner.is_public_key_type_exist(publicKeyType) == false &&
                    accountObject.options.memo_key.compare(publicKeyType) == false) {
                continue;
            }
            mapPublic2Private.put(publicKeyType, privateKeyType);


        }

        if (mapPublic2Private.isEmpty() == true) {
            return ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
        }

        mWalletObject.update_account(accountObject);

        List<types.public_key_type> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.addAll(mapPublic2Private.keySet());

        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
        mHashMapPub2Priv.putAll(mapPublic2Private);

        encrypt_keys();

        return 0;
    }

    public int import_key(String account_name_or_id,
                          String wif_key) throws NetworkStatusException {
        if (!(is_locked() == false && is_new() == false)) {
            return -1;
        }

        types.private_key_type privateKeyType = new types.private_key_type(wif_key);

        if (privateKeyType == null)
            return -1;

        public_key publicKey = privateKeyType.getPrivateKey().get_public_key();
        types.public_key_type publicKeyType = new types.public_key_type(publicKey);

        account_object accountObject = get_account(account_name_or_id);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        Log.i("object", accountObject.name);

        /*List<account_object> listAccountObject = lookup_account_names(account_name_or_id);
        // 进行publicKey的比对
        if (listAccountObject.isEmpty()) {
            return -1;
        }

        account_object accountObject = listAccountObject.get(0);*/
        if (accountObject.active.is_public_key_type_exist(publicKeyType) == false &&
                accountObject.owner.is_public_key_type_exist(publicKeyType) == false &&
                accountObject.options.memo_key.compare(publicKeyType) == false) {
            return -1;
        }

        mWalletObject.update_account(accountObject);

        List<types.public_key_type> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicKeyType);

        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
        mHashMapPub2Priv.put(publicKeyType, privateKeyType);

        encrypt_keys();

        return 0;
    }

    public int import_keys(String account_name_or_id,
                           String wif_key_1,
                           String wif_key_2) throws NetworkStatusException {
        assert (is_locked() == false && is_new() == false);

        types.private_key_type privateKeyType1 = new types.private_key_type(wif_key_1);
        types.private_key_type privateKeyType2 = new types.private_key_type(wif_key_2);

        public_key publicKey1 = privateKeyType1.getPrivateKey().get_public_key();
        public_key publicKey2 = privateKeyType1.getPrivateKey().get_public_key();
        types.public_key_type publicKeyType1 = new types.public_key_type(publicKey1);
        types.public_key_type publicKeyType2 = new types.public_key_type(publicKey2);

        account_object accountObject = get_account(account_name_or_id);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        /*List<account_object> listAccountObject = lookup_account_names(account_name_or_id);
        // 进行publicKey的比对
        if (listAccountObject.isEmpty()) {
            return -1;
        }

        account_object accountObject = listAccountObject.get(0);*/
        if (accountObject.active.is_public_key_type_exist(publicKeyType1) == false &&
                accountObject.owner.is_public_key_type_exist(publicKeyType1) == false &&
                accountObject.options.memo_key.compare(publicKeyType1) == false) {
            return -1;
        }

        if (accountObject.active.is_public_key_type_exist(publicKeyType2) == false &&
                accountObject.owner.is_public_key_type_exist(publicKeyType2) == false &&
                accountObject.options.memo_key.compare(publicKeyType2) == false) {
            return -1;
        }


        mWalletObject.update_account(accountObject);

        List<types.public_key_type> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicKeyType1);
        listPublicKeyType.add(publicKeyType2);

        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
        mHashMapPub2Priv.put(publicKeyType1, privateKeyType1);
        mHashMapPub2Priv.put(publicKeyType2, privateKeyType2);

        encrypt_keys();

        // 保存至文件
        return 0;
    }

    public int import_account_password(String strAccountName,
                                       String strPassword) throws NetworkStatusException {
        private_key privateActiveKey = private_key.from_seed(strAccountName + "active" + strPassword);
        private_key privateOwnerKey = private_key.from_seed(strAccountName + "owner" + strPassword);

        types.public_key_type publicActiveKeyType = new types.public_key_type(privateActiveKey.get_public_key());
        types.public_key_type publicOwnerKeyType = new types.public_key_type(privateOwnerKey.get_public_key());

        account_object accountObject = get_account(strAccountName);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        if (accountObject.active.is_public_key_type_exist(publicActiveKeyType) == false &&
                accountObject.owner.is_public_key_type_exist(publicOwnerKeyType) == false) {
            return ErrorCode.ERROR_PASSWORD_INVALID;
        }

        List<types.public_key_type> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicActiveKeyType);
        listPublicKeyType.add(publicOwnerKeyType);
        mWalletObject.update_account(accountObject);
        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
        mHashMapPub2Priv.put(publicActiveKeyType, new types.private_key_type(privateActiveKey));
        mHashMapPub2Priv.put(publicOwnerKeyType, new types.private_key_type(privateOwnerKey));

        encrypt_keys();

        // 保存至文件

        return 0;
    }

    public asset transfer_calculate_fee(String strAmount,
                                        String strAssetSymbol,
                                        String strMemo) throws NetworkStatusException {
        object_id<asset_object> assetObjectId = object_id.create_from_string(strAssetSymbol);
        asset_object assetObject;
        if (assetObjectId == null) {
            assetObject = lookup_asset_symbols(strAssetSymbol);
        } else {
            List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
            listAssetObjectId.add(assetObjectId);
            assetObject = get_assets(listAssetObjectId).get(0);
        }

        if (assetObject == null) {
            return null;
        }

        operations.transfer_operation transferOperation = new operations.transfer_operation();
        transferOperation.from = new object_id<account_object>(0, account_object.class);//accountObjectFrom.id;
        transferOperation.to = new object_id<account_object>(0, account_object.class);
        transferOperation.amount = assetObject.amount_from_string(strAmount);
        transferOperation.extensions = new HashSet<>();
        /*if (TextUtils.isEmpty(strMemo) == false) {

        }*/

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = 0;
        operationType.operationContent = transferOperation;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);
        tx.extensions = new HashSet<>();
        set_operation_fees(tx, get_global_properties().parameters.current_fees);

        return transferOperation.fee;
    }

    //add param amount_to_fee: 手续费
    //add param symbol_to_fee: 手续费货币符号
    public signed_transaction transfer(String strFrom,
                                       String strTo,
                                       String strAmount,
                                       String strAssetSymbol,
                                       String strMemo,
                                       String amount_to_fee,
                                       String symbol_to_fee) throws NetworkStatusException {

        object_id<asset_object> assetObjectId = object_id.create_from_string(strAssetSymbol);
        asset_object assetObject;
        if (assetObjectId == null) {
            assetObject = lookup_asset_symbols(strAssetSymbol);
        } else {
            List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
            listAssetObjectId.add(assetObjectId);
            assetObject = get_assets(listAssetObjectId).get(0);
        }

        if (assetObject == null) {
            return null;
        }
        account_object accountObjectFrom = get_account(strFrom);
        account_object accountObjectTo = get_account(strTo);
        if (accountObjectTo == null) {
            return null;
        }

        operations.transfer_operation transferOperation = new operations.transfer_operation();
        transferOperation.from = accountObjectFrom.id;
        transferOperation.to = accountObjectTo.id;
        transferOperation.amount = assetObject.amount_from_string(strAmount);

        transferOperation.extensions = new HashSet<>();
        if (TextUtils.isEmpty(strMemo) == false) {
            transferOperation.memo = new memo_data();
            transferOperation.memo.from = accountObjectFrom.options.memo_key;
            transferOperation.memo.to = accountObjectTo.options.memo_key;

            types.private_key_type privateKeyType = mHashMapPub2Priv.get(accountObjectFrom.options.memo_key);
            if (privateKeyType == null || privateKeyType.getPrivateKey() == null) {
                // // TODO: 07/09/2017 获取失败的问题
                return null;
            }
            transferOperation.memo.set_message(
                    privateKeyType.getPrivateKey(),
                    accountObjectTo.options.memo_key.getPublicKey(),
                    strMemo,
                    0
            );
            transferOperation.memo.get_message(
                    privateKeyType.getPrivateKey(),
                    accountObjectTo.options.memo_key.getPublicKey()
            );
        }

        if (!TextUtils.isEmpty(symbol_to_fee)) {
            object_id<asset_object> feeAssetObjectId = object_id.create_from_string(symbol_to_fee);
            List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
            listAssetObjectId.add(feeAssetObjectId);
            asset_object feeAssetObject = get_assets(listAssetObjectId).get(0);
            transferOperation.fee = feeAssetObject.amount_from_string(amount_to_fee);
        }

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_TRANSER_OPERATION;
        operationType.operationContent = transferOperation;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);
        tx.extensions = new HashSet<>();

        if (transferOperation.fee == null) {

            set_operation_fees(tx, get_global_properties().parameters.current_fees);
        }


        //// TODO: 07/09/2017 tx.validate();
        return sign_transaction(tx);
    }

    public asset calculate_sell_asset_fee(String amountToSell, asset_object assetToSell,
                                          String minToReceive, asset_object assetToReceive,
                                          global_property_object globalPropertyObject) {
        operations.limit_order_create_operation op = new operations.limit_order_create_operation();

        op.amount_to_sell = assetToSell.amount_from_string(amountToSell);
        op.min_to_receive = assetToReceive.amount_from_string(minToReceive);

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_CREATE_LIMIT_ORDER_OPERATION;
        operationType.operationContent = op;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);

        tx.extensions = new HashSet<>();
        set_operation_fees(tx, globalPropertyObject.parameters.current_fees);

        return op.fee;
    }

    public asset calculate_sell_fee(asset_object assetToSell, asset_object assetToReceive,
                                    double rate, double amount,
                                    global_property_object globalPropertyObject) {

        String strAmount = Double.toString(amount);
        String strMinToReceive = Double.toString(rate * amount);
        if (strAmount == null) {
            return null;
        }
        if (strMinToReceive == null) {
            return null;
        }

        return calculate_sell_asset_fee(strAmount, assetToSell,
                strMinToReceive, assetToReceive, globalPropertyObject);
    }

    public asset calculate_buy_fee(asset_object assetToReceive, asset_object assetToSell,
                                   double rate, double amount,
                                   global_property_object globalPropertyObject) {

        String strAmount = Double.toString(rate * amount);
        if (strAmount == null) {
            return null;
        }
        String strMinToReceive = Double.toString(amount);
        if (strMinToReceive == null) {
            return null;
        }
        return calculate_sell_asset_fee(strAmount, assetToSell,
                strMinToReceive, assetToReceive, globalPropertyObject);
    }

    public signed_transaction sell_asset(String amountToSell,
                                         String symbolToSell,
                                         String minToReceive,
                                         String symbolToReceive,
                                         int timeoutSecs,
                                         boolean fillOrKill,
                                         String amount_to_fee,
                                         String symbol_to_fee,
                                         int index) throws NetworkStatusException {
        // 这是用于出售的帐号
        operations.limit_order_create_operation op = new operations.limit_order_create_operation();
        account_object accountObject;
        if (index < list_my_accounts().size()) {
            accountObject = list_my_accounts().get(index);
            op.seller = accountObject.id;
        } else {
            if (loginAccountId != null) {
                op.seller = loginAccountId;
            } else {
                accountObject = get_account(SPUtils.getString(Const.USERNAME, ""));
                if (accountObject != null) {
                    op.seller = accountObject.id;
                }
            }
        }

        // 填充数据 && 防止科学计数法出现在字符串中
        BigDecimal db = new BigDecimal(minToReceive);
        String minToReceiveStr = db.toPlainString();
        BigDecimal db2 = new BigDecimal(amountToSell);
        String amountToSellStr = db2.toPlainString();

        asset_object assetToSell = lookup_asset_symbols(symbolToSell);
        asset_object assetToReceive = lookup_asset_symbols(symbolToReceive);
        if (assetToSell == null || assetToReceive == null) {
            return null;
        }

        op.amount_to_sell = assetToSell.amount_from_string(amountToSellStr);
        op.min_to_receive = assetToReceive.amount_from_string(minToReceiveStr);
        if (timeoutSecs > 0) {
            op.expiration = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSecs));
        } else {
            op.expiration = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(360));
        }
        op.fill_or_kill = fillOrKill;
        op.extensions = new HashSet<>();

        if (!TextUtils.isEmpty(symbol_to_fee)) {
            object_id<asset_object> feeAssetObjectId = object_id.create_from_string(symbol_to_fee);
            List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
            listAssetObjectId.add(feeAssetObjectId);
            asset_object feeAssetObject = get_assets(listAssetObjectId).get(0);
            op.fee = feeAssetObject.amount_from_string(amount_to_fee);
        }

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_CREATE_LIMIT_ORDER_OPERATION;
        operationType.operationContent = op;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);

        tx.extensions = new HashSet<>();
        if (op.fee == null) {
            if (null != get_global_properties().parameters) {
                set_operation_fees(tx, get_global_properties().parameters.current_fees);
            } else {
                return null;
            }
        }

        return sign_transaction(tx);
    }

    /**
     * @param symbolToSell    卖出的货币符号
     * @param symbolToReceive 买入的货币符号
     * @param minToReceive
     * @param amount          要卖出多少个<t>symbolToSell</t>
     * @throws NetworkStatusException
     */
    public signed_transaction sell(String symbolToSell, String symbolToReceive, double minToReceive,
                                   double amount, String amount_to_fee,
                                   String symbol_to_fee, int index) throws NetworkStatusException {


//        String minToReceive =  Double.toString(rate * amount);
//        if (minToReceive.contains(".")) {
//            if (symbolToReceive.equalsIgnoreCase("BTC") || symbolToReceive.equalsIgnoreCase("LTC") || symbolToReceive.equalsIgnoreCase("ETH") ) {
//
//                minToReceive = NumberUtils.formatNumber8((Double.toString(rate * amount) ) + "");
//            } else {
//                minToReceive = NumberUtils.formatNumber((Double.toString(rate * amount)) + "");
//            }
//        }

        return sell_asset(Double.toString(amount), symbolToSell, minToReceive + "",
                symbolToReceive, 0, false, amount_to_fee, symbol_to_fee, index);

        // return sell_asset(Double.toString(amount), symbolToSell, Double.toString(rate * amount),
        //        symbolToReceive, 0, false, amount_to_fee,  symbol_to_fee,index);
    }

    public signed_transaction sell(String symbolToSell, String symbolToReceive, double rate,
                                   double amount, int timeoutSecs, String amount_to_fee,
                                   String symbol_to_fee, int index) throws NetworkStatusException {

        String minToReceive = Double.toString(rate * amount);
        if (minToReceive.contains(".")) {
            if (symbolToReceive.equalsIgnoreCase("BTC") || symbolToReceive.equalsIgnoreCase("LTC") || symbolToReceive.equalsIgnoreCase("ETH")) {

                minToReceive = NumberUtils.formatNumber8((Double.toString(rate * amount)) + "");
            } else {
                minToReceive = NumberUtils.formatNumber5((Double.toString(rate * amount)) + "");
            }
        }

        return sell_asset(Double.toString(amount), symbolToSell, minToReceive,
                symbolToReceive, timeoutSecs, false, amount_to_fee, symbol_to_fee, index);

        // return sell_asset(Double.toString(amount), symbolToSell, Double.toString(rate * amount),
        //         symbolToReceive, timeoutSecs, false,amount_to_fee,symbol_to_fee,index);
    }

    /**
     * @param symbolToReceive 买入的货币符号
     * @param symbolToSell    卖出的货币符号
     * @param amountToSell
     * @param amount          要买入多少个<t>symbolToReceive</t>
     * @throws NetworkStatusException
     */
    public signed_transaction buy(String symbolToReceive, String symbolToSell, double amountToSell,
                                  double amount, String amount_to_fee,
                                  String symbol_to_fee, int index) throws NetworkStatusException {

//        String amountToSell = Double.toString(rate * amount);
//        if (amountToSell.contains(".")) {
//            if (symbolToSell.equalsIgnoreCase("BTC") || symbolToSell.equalsIgnoreCase("LTC") ||symbolToSell.equalsIgnoreCase("ETH")) {
//
//                amountToSell = NumberUtils.formatNumber8((Double.toString(rate * amount) ) + "");
//            } else {
//                amountToSell = NumberUtils.formatNumber((Double.toString(rate * amount)) + "");
//            }
//        }
        return sell_asset(amountToSell + "", symbolToSell, Double.toString(amount),
                symbolToReceive, 0, false, amount_to_fee, symbol_to_fee, index);

        // return sell_asset(Double.toString(rate * amount), symbolToSell, Double.toString(amount),
        //         symbolToReceive, 0, false,amount_to_fee,symbol_to_fee,index);
    }

    public signed_transaction buy(String symbolToReceive, String symbolToSell, double rate,
                                  double amount, int timeoutSecs, String amount_to_fee,
                                  String symbol_to_fee, int index) throws NetworkStatusException {

        String amountToSell = Double.toString(rate * amount);
        if (amountToSell.contains(".")) {
            if (symbolToSell.equalsIgnoreCase("BTC") || symbolToSell.equalsIgnoreCase("LTC") || symbolToSell.equalsIgnoreCase("ETH")) {

                amountToSell = NumberUtils.formatNumber8((Double.toString(rate * amount)) + "");
            } else {
                amountToSell = NumberUtils.formatNumber5((Double.toString(rate * amount)) + "");
            }
        }
        return sell_asset(amountToSell, symbolToSell, Double.toString(amount),
                symbolToReceive, 0, false, amount_to_fee, symbol_to_fee, index);

        // return sell_asset(Double.toString(rate * amount), symbolToSell, Double.toString(amount),
        //        symbolToReceive, timeoutSecs, false, amount_to_fee,symbol_to_fee,index);
    }

    public signed_transaction cancel_order(object_id<limit_order_object> id)
            throws NetworkStatusException {
        operations.limit_order_cancel_operation op = new operations.limit_order_cancel_operation();
        op.fee_paying_account = mWebsocketApi.get_limit_order(id).seller;
        op.order = id;
        op.extensions = new HashSet<>();

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_CANCEL_LMMIT_ORDER_OPERATION;
        operationType.operationContent = op;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);

        tx.extensions = new HashSet<>();
        set_operation_fees(tx, get_global_properties().parameters.current_fees);

        return sign_transaction(tx);
    }

    public global_property_object get_global_properties() throws NetworkStatusException {
        return mWebsocketApi.get_global_properties();
    }

    public block_chain_info get_info() throws NetworkStatusException {
        block_chain_info bchain = new block_chain_info();

        global_property_object global_props = get_global_properties();
        if (global_props == null) {
            return null;
        }
        dynamic_global_property_object dynamic_props = get_dynamic_global_properties();
        if (dynamic_props == null) {
            return null;
        }

        Date currentDateTime;
        if (dynamic_props != null) {
            bchain.head_block_num = Integer.toString(dynamic_props.head_block_number);
            bchain.head_block_id = dynamic_props.head_block_id.toString();
            //计算时间差
            currentDateTime = new Date();
            long diff = currentDateTime.getTime() - dynamic_props.time.getTime();
            if (diff < 0) {
                diff = dynamic_props.time.getTime() - currentDateTime.getTime();
            }
            long sec = diff / 1000;
            bchain.head_block_age = sec + " second old";

            Date maintenanceDateTime = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                String dateStr = dynamic_props.next_maintenance_time.replace("T", " ");
                maintenanceDateTime = sdf.parse(dateStr);
                long diff_main = currentDateTime.getTime() - maintenanceDateTime.getTime();
                if (diff_main < 0) {
                    diff_main = maintenanceDateTime.getTime() - currentDateTime.getTime();
                }
                long hour = diff_main / (1000 * 60 * 60);
                bchain.next_maintenance_time = hour + " hours in the future";
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

        if (global_props != null) {

            bchain.active_committee_memberts_count = global_props.active_committee_members.size();
            bchain.active_witnesses_count = global_props.active_witnesses.size();

        }

        return bchain;
    }


    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
        return mWebsocketApi.get_dynamic_global_properties();
    }


    public signed_transaction upgrade_account(String name, String fee) throws NetworkStatusException {

        account_object object = mWebsocketApi.get_account(name);
        operations.account_upgrade_operation operation = new operations.account_upgrade_operation();
        operation.account_to_upgrade = object.id;
        operation.upgrade_to_lifetime_member = true;
        operation.active = object.owner;

        asset_object assetBDS = lookup_asset_symbols("BDS");
        if (assetBDS == null) {
            return null;
        }
        object_id<asset_object> feeAssetObjectId = assetBDS.id;
        List<object_id<asset_object>> listAssetObjectId = new ArrayList<>();
        listAssetObjectId.add(feeAssetObjectId);
        asset_object feeAssetObject = get_assets(listAssetObjectId).get(0);
        operation.fee = feeAssetObject.amount_from_string(fee);

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_UPGRADE_ACCOUNT_OPERATION;
        operationType.operationContent = operation;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);

        tx.extensions = new HashSet<>();
        //set_operation_fees(tx, get_global_properties().parameters.current_fees);
        return sign_transaction(tx);
    }

    public signed_transaction create_account_with_private_key(private_key privateOwnerKey,
                                                              String strAccountName,
                                                              String strPassword,
                                                              String strRegistar,
                                                              String strReferrer) throws NetworkStatusException {
        int nActiveKeyIndex = find_first_unused_derived_key_index(privateOwnerKey);

        String strWifKey = new types.private_key_type(privateOwnerKey).toString();
        private_key privateActiveKey = derive_private_key(strWifKey, nActiveKeyIndex);

        strWifKey = new types.private_key_type(privateActiveKey).toString();
        int nMemoKeyIndex = find_first_unused_derived_key_index(privateActiveKey);
        private_key privateMemoKey = derive_private_key(strWifKey, nMemoKeyIndex);

        types.public_key_type publicOwnerKey = new types.public_key_type(privateOwnerKey.get_public_key());
        types.public_key_type publicActiveKey = new types.public_key_type(privateActiveKey.get_public_key());
        types.public_key_type publicMemoKey = new types.public_key_type(privateMemoKey.get_public_key());

        operations.account_create_operation operation = new operations.account_create_operation();
        operation.name = strAccountName;

        if (operation.options == null)
            operation.options = new types.account_options();
        if (publicMemoKey != null)
            operation.options.memo_key = publicOwnerKey;

        operation.active = new authority(1, publicOwnerKey, 1);
        operation.owner = new authority(1, publicOwnerKey, 1);

        account_object accountRegistrar = get_account(strRegistar);
        account_object accountReferr = get_account(strReferrer);

        operation.referrer = accountReferr.id;
        operation.registrar = accountRegistrar.id;
        operation.referrer_percent = accountReferr.referrer_rewards_percentage;

        operations.operation_type operationType = new operations.operation_type();
        operationType.nOperationType = operations.ID_CREATE_ACCOUNT_OPERATION;
        operationType.operationContent = operation;

        signed_transaction tx = new signed_transaction();
        tx.operations = new ArrayList<>();
        tx.operations.add(operationType);

        tx.extensions = new HashSet<>();
        set_operation_fees(tx, get_global_properties().parameters.current_fees);

        return sign_create_account_transaction(tx);


        // global_property_object globalPropertyObject = mWebsocketApi.get_global_properties();
        // dynamic_global_property_object dynamicGlobalPropertyObject = mWebsocketApi.get_dynamic_global_properties();

    }


    private signed_transaction sign_create_account_transaction(signed_transaction tx) throws NetworkStatusException {
        // // TODO: 07/09/2017 这里的set应出问题
        signed_transaction.required_authorities requiresAuthorities = tx.get_required_authorities();

        Set<object_id<account_object>> req_active_approvals = new HashSet<>();
        req_active_approvals.addAll(requiresAuthorities.active);

        Set<object_id<account_object>> req_owner_approvals = new HashSet<>();
        req_owner_approvals.addAll(requiresAuthorities.owner);


        for (authority authorityObject : requiresAuthorities.other) {
            for (object_id<account_object> accountObjectId : authorityObject.account_auths.keySet()) {
                req_active_approvals.add(accountObjectId);
            }
        }

        Set<object_id<account_object>> accountObjectAll = new HashSet<>();
        accountObjectAll.addAll(req_active_approvals);
        accountObjectAll.addAll(req_owner_approvals);


        List<object_id<account_object>> listAccountObjectId = new ArrayList<>();
        listAccountObjectId.addAll(accountObjectAll);

        List<account_object> listAccountObject = get_accounts(listAccountObjectId);
        HashMap<object_id<account_object>, account_object> hashMapIdToObject = new HashMap<>();
        for (account_object accountObject : listAccountObject) {
            hashMapIdToObject.put(accountObject.id, accountObject);
        }

        HashSet<types.public_key_type> approving_key_set = new HashSet<>();
        for (object_id<account_object> accountObjectId : req_active_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.active.get_keys());
        }

        for (object_id<account_object> accountObjectId : req_owner_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.owner.get_keys());
        }

        for (authority authorityObject : requiresAuthorities.other) {
            for (types.public_key_type publicKeyType : authorityObject.get_keys()) {
                approving_key_set.add(publicKeyType);
            }
        }

        // // TODO: 07/09/2017 被简化了
        dynamic_global_property_object dynamicGlobalPropertyObject = get_dynamic_global_properties();
        tx.set_reference_block(dynamicGlobalPropertyObject.head_block_id);

        Date dateObject = dynamicGlobalPropertyObject.time;
        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 30);

        dateObject = calender.getTime();

        tx.set_expiration(dateObject);

        for (types.public_key_type pulicKeyType : approving_key_set) {
            types.private_key_type privateKey = mHashMapPub2Priv.get(pulicKeyType);
            if (privateKey != null) {
                tx.sign(privateKey, mWalletObject.chain_id);
            }
        }

        // 发出tx，进行广播，这里也涉及到序列化
        int nRet = mWebsocketApi.broadcast_json_transaction(tx, "");
        if (nRet == 0) {
            return tx;
        } else {
            return null;
        }
    }

    private signed_transaction sign_transaction(signed_transaction tx) throws NetworkStatusException {
        // // TODO: 07/09/2017 这里的set应出问题
        signed_transaction.required_authorities requiresAuthorities = tx.get_required_authorities();

        Set<object_id<account_object>> req_active_approvals = new HashSet<>();
        req_active_approvals.addAll(requiresAuthorities.active);

        Set<object_id<account_object>> req_owner_approvals = new HashSet<>();
        req_owner_approvals.addAll(requiresAuthorities.owner);


        for (authority authorityObject : requiresAuthorities.other) {
            for (object_id<account_object> accountObjectId : authorityObject.account_auths.keySet()) {
                req_active_approvals.add(accountObjectId);
            }
        }

        Set<object_id<account_object>> accountObjectAll = new HashSet<>();
        accountObjectAll.addAll(req_active_approvals);
        accountObjectAll.addAll(req_owner_approvals);


        List<object_id<account_object>> listAccountObjectId = new ArrayList<>();
        listAccountObjectId.addAll(accountObjectAll);

        List<account_object> listAccountObject = get_accounts(listAccountObjectId);
        HashMap<object_id<account_object>, account_object> hashMapIdToObject = new HashMap<>();
        for (account_object accountObject : listAccountObject) {
            hashMapIdToObject.put(accountObject.id, accountObject);
        }

        HashSet<types.public_key_type> approving_key_set = new HashSet<>();
        for (object_id<account_object> accountObjectId : req_active_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.active.get_keys());
        }

        for (object_id<account_object> accountObjectId : req_owner_approvals) {
            account_object accountObject = hashMapIdToObject.get(accountObjectId);
            approving_key_set.addAll(accountObject.owner.get_keys());
        }

        for (authority authorityObject : requiresAuthorities.other) {
            for (types.public_key_type publicKeyType : authorityObject.get_keys()) {
                approving_key_set.add(publicKeyType);
            }
        }

        // // TODO: 07/09/2017 被简化了
        dynamic_global_property_object dynamicGlobalPropertyObject = get_dynamic_global_properties();
        tx.set_reference_block(dynamicGlobalPropertyObject.head_block_id);

        Date dateObject = dynamicGlobalPropertyObject.time;
        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 12 * 3600);

        dateObject = calender.getTime();

        tx.set_expiration(dateObject);

        for (types.public_key_type pulicKeyType : approving_key_set) {
            types.private_key_type privateKey = mHashMapPub2Priv.get(pulicKeyType);
            if (privateKey != null) {
                tx.sign(privateKey, mWalletObject.chain_id);
            }
        }

        // 发出tx，进行广播，这里也涉及到序列化
        int nRet = mWebsocketApi.broadcast_transaction(tx);
        if (nRet == 0) {
            return tx;
        } else {
            return null;
        }
    }

    public List<bucket_object> get_market_history(object_id<asset_object> assetObjectId1,
                                                  object_id<asset_object> assetObjectId2,
                                                  int nBucket,
                                                  Date dateStart,
                                                  Date dateEnd) throws NetworkStatusException {
        return mWebsocketApi.get_market_history(
                assetObjectId1,
                assetObjectId2,
                nBucket,
                dateStart,
                dateEnd
        );
    }

    private void set_operation_fees(signed_transaction tx, fee_schedule feeSchedule) {
        for (operations.operation_type operationType : tx.operations) {
            feeSchedule.set_fee(operationType, price.unit_price(new object_id<asset_object>(0, asset_object.class)));
        }
    }


    private private_key derive_private_key(String strWifKey, int nSeqNumber) {
        String strData = strWifKey + " " + nSeqNumber;
        byte[] bytesBuffer = strData.getBytes();
        SHA512Digest digest = new SHA512Digest();
        digest.update(bytesBuffer, 0, bytesBuffer.length);

        byte[] out = new byte[64];
        digest.doFinal(out, 0);

        SHA256Digest digest256 = new SHA256Digest();
        byte[] outSeed = new byte[32];
        byte[] outSeed2 = new byte[64];
        digest256.update(out, 0, out.length);
        digest.doFinal(outSeed2, 0);
        System.arraycopy(outSeed2, 0, outSeed, 0, 32);

        return new private_key(outSeed);
    }

    private int find_first_unused_derived_key_index(private_key privateKey) {
        int first_unused_index = 0;
        int number_of_consecutive_unused_keys = 0;

        String strWifKey = new types.private_key_type(privateKey).toString();
        for (int key_index = 0; ; ++key_index) {
            private_key derivedPrivateKey = derive_private_key(strWifKey, key_index);
            types.public_key_type publicKeyType = new types.public_key_type(derivedPrivateKey.get_public_key());

            if (mHashMapPub2Priv.containsKey(publicKeyType) == false) {
                if (number_of_consecutive_unused_keys != 0) {
                    ++number_of_consecutive_unused_keys;
                    if (number_of_consecutive_unused_keys > 5)
                        return first_unused_index;
                } else {
                    first_unused_index = key_index;
                    number_of_consecutive_unused_keys = 1;
                }
            } else {
                first_unused_index = 0;
                number_of_consecutive_unused_keys = 0;
            }
        }
    }

    public String decrypt_memo_message(memo_data memoData) {
        assert (is_locked() == false);
        String strMessage = null;

        types.private_key_type privateKeyType = mHashMapPub2Priv.get(memoData.to);
        if (privateKeyType != null) {
            strMessage = memoData.get_message(privateKeyType.getPrivateKey(), memoData.from.getPublicKey());
        } else {
            privateKeyType = mHashMapPub2Priv.get(memoData.from);
            if (privateKeyType != null) {
                strMessage = memoData.get_message(privateKeyType.getPrivateKey(), memoData.to.getPublicKey());
            }
        }

        return strMessage;
    }

    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        return mWebsocketApi.get_ticker(base, quote);
    }

    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        return mWebsocketApi.get_trade_history(base, quote, start, end, limit);
    }

    public List<limit_order_object> get_limit_orders(object_id<asset_object> base,
                                                     object_id<asset_object> quote,
                                                     int limit) throws NetworkStatusException {
        return mWebsocketApi.get_limit_orders(base, quote, limit);
    }

    public full_account get_full_account(String name, boolean subscribe)
            throws NetworkStatusException, JSONException {
        return mWebsocketApi.get_full_account(name, subscribe);
    }

    public List<full_account_object> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        return mWebsocketApi.get_full_accounts(names, subscribe);
    }

    //获取撮合记录
    public OrderBook get_order_book(String base, String quote, int limit) throws NetworkStatusException {
        return mWebsocketApi.get_order_book(base, quote, limit);
    }

    public HashMap<types.public_key_type, types.private_key_type> get_wallet_hash() {
        return mHashMapPub2Priv;
    }

    public boolean is_public_key_registered(String pub_key) throws NetworkStatusException {
        return mWebsocketApi.is_public_key_registered(pub_key);
    }

    //查询矿工工资
    public List<vesting_balance_object> get_vesting_balances(String name) throws NetworkStatusException {
        return mWebsocketApi.get_vesting_balances(name);
    }


    public void withdraw_vesting() throws NetworkStatusException {

    }


    //生成公私钥对
    public Map<private_key, public_key> generateKeyFromPassword(String account_name, String password) {
        Map<private_key, public_key> keys = new HashMap<private_key, public_key>();
        if (account_name != null && password != null) {
            String seed = account_name + "owner" + password;
            private_key privKey = private_key.from_seed(seed);
            public_key pubKey = privKey.get_public_key();
            keys.put(privKey, pubKey);
        }
        return keys;
    }



    //喂价
    public String cli_get_bitasset_data(String symbol) throws NetworkStatusException {
        return mWebsocketApi.cli_get_bitasset_data(symbol);
    }

    //喂价
    public String get_bitasset_data(String symbol) throws NetworkStatusException {
        return mWebsocketApi.get_bitasset_data(symbol);
    }

    //强平触发价
    public String cli_get_full_accounts(String account) throws NetworkStatusException {
        return mWebsocketApi.cli_get_full_accounts(account);
    }

    //cli 资产借入
    public String cli_borrow_asset(String account, String amount_to_borrow, String asset_symbol, String amount_of_collateral) throws NetworkStatusException {
        //TODO: 配合cli的使用，需要获取注册人私钥.
        String strWifKey = getWifKeyWithAccount(account);
        //TODO: 检查注册账户私钥是否存在.
        // 如果不存在请检查钱包文件是否有该账户名称，并且看代码是否执行过load_wallet_file（）函数
        if (strWifKey.isEmpty())
            return "";
        return mWebsocketApi.cli_borrow_asset(account, amount_to_borrow, asset_symbol, amount_of_collateral, strWifKey);
    }

    //cli 转账记录
    public HashMap<String, List<HistoryResponseModel.DataBean>> cli_transfer_record(String account, String id) throws NetworkStatusException {
        String name = SPUtils.getString(Const.USERNAME, "");
        String strWifKey = getWifKeyWithAccount(name);
        //TODO: 检查注册账户私钥是否存在.
        // 如果不存在请检查钱包文件是否有该账户名称，并且看代码是否执行过load_wallet_file（）函数
        if (strWifKey.isEmpty())
            return null;
        return mWebsocketApi.cli_transfer_record(account, id, name, strWifKey);
    }


    public AllHistory get_all_history(String baseSymbolId, String qouteSymbolId, int nLimit) throws NetworkStatusException {
        return mWebsocketApi.get_all_history(baseSymbolId, qouteSymbolId, nLimit);
    }

    /***
     * cli 注册账户
     * @param account_name  账户名称
     * @param public_key    账户公钥
     * @param register      注册人
     * @param referrer      引荐人
     * @return
     *  -1 账户本地私钥不存在
     *  0 升级失败
     *  1 升级成功
     * */
    public int cli_register_account(String account_name, String public_key, String register, String referrer) {

        //TODO: 配合cli的使用，需要获取注册人私钥.
        String regWifKey = getWifKeyWithAccount(register);
        //TODO: 检查注册账户私钥是否存在.
        // 如果不存在请检查钱包文件是否有该注册人，并且看代码是否执行过load_wallet_file（）函数
        if (regWifKey.isEmpty())
            return -1;
        if (mWebsocketApi.cli_register_account(account_name, public_key, register, regWifKey, referrer))
            return 1;
        else
            return 0;
    }

    /***
     * cli 升级账户
     * @param account_name  账户名称
     * @return
     *  -1 账户本地私钥不存在
     *  0 升级失败
     *  1 升级成功
     * */
    public int cli_upgrade_account(String account_name) {
        //TODO:配合cli的使用，需要获取升级账户的私钥
        String accWifKey = getWifKeyWithAccount(account_name);
        //TODO: 检查账户私钥是否存在.
        // 如果不存在请检查钱包文件是否有该账户名称，并且看代码是否执行过load_wallet_file（）函数
        if (accWifKey.isEmpty())
            return -1;
        if (mWebsocketApi.cli_upgrade_account(account_name, accWifKey)) {
            return 1;
        } else {
            return 0;
        }

    }


    /***
     * cli 领取冻结资产
     * @param str_amount 领取金额
     * @return
     * -1 本地内存账户不存在
     * -2 本地内存账户私钥不存在
     * 0  领取资产失败
     * 1  领取资产成功*/
    public int cli_withdraw_vesting(String assets_id, String str_amount) {

        //TODO: 根据账户名查询账户私钥
        // 如果不存在请检查钱包文件是否有该账户名称，并且看代码是否执行过load_wallet_file（）函数
        String accWifKey = getWifKeyWithAccount(SPUtils.getString(Const.USERNAME, ""));
        if (accWifKey.isEmpty()) {
            return -2;
        }

        return mWebsocketApi.cli_withdraw_vesting(assets_id, str_amount, SPUtils.getString(Const.USERNAME, ""), accWifKey);
    }



    public HashMap<String, String> cli_get_block(String block_num, int index) throws NetworkStatusException {
        String name = SPUtils.getString(Const.USERNAME, "");
        String strWifKey = getWifKeyWithAccount(name);
        //TODO: 检查注册账户私钥是否存在.
        // 如果不存在请检查钱包文件是否有该账户名称，并且看代码是否执行过load_wallet_file（）函数
        if (strWifKey.isEmpty())
            return null;
        return mWebsocketApi.cli_get_block(block_num, index, name, strWifKey);
    }

}