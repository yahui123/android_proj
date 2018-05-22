package com.tang.trade.tang.socket;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tang.trade.tang.net.model.AllHistory;
import com.tang.trade.tang.net.model.HistoryResponseModel;
import com.tang.trade.tang.socket.chain.CallOrder;
import com.tang.trade.tang.socket.chain.asset_object;
import com.tang.trade.tang.socket.chain.block_header;
import com.tang.trade.tang.socket.chain.block_object;
import com.tang.trade.tang.socket.chain.bucket_object;
import com.tang.trade.tang.socket.chain.dynamic_global_property_object;
import com.tang.trade.tang.socket.chain.full_account;
import com.tang.trade.tang.socket.chain.global_config_object;
import com.tang.trade.tang.socket.chain.global_property_object;
import com.tang.trade.tang.socket.chain.limit_order_object;
import com.tang.trade.tang.socket.chain.object_id;
import com.tang.trade.tang.socket.chain.operation_history_object;
import com.tang.trade.tang.socket.chain.operations;
import com.tang.trade.tang.socket.chain.signed_transaction;
import com.tang.trade.tang.socket.chain.vesting_balance_object;
import com.tang.trade.tang.socket.exception.NetworkStatusException;
import com.tang.trade.tang.socket.fc.crypto.sha256_object;
import com.tang.trade.tang.socket.market.MarketTicker;
import com.tang.trade.tang.socket.market.MarketTrade;
import com.tang.trade.tang.socket.market.OrderBook;
import com.tang.trade.tang.utils.CalculateUtils;
import com.tang.trade.tang.utils.NumberUtils;
import com.tang.trade.tang.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.LimitOrder;
import de.bitsharesmunich.graphenej.Price;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.tang.trade.tang.socket.common.ErrorCode.ERROR_CONNECT_SERVER_FAILD;
import static com.tang.trade.tang.ui.loginactivity.ChooseWalletActivity.PASSWORD;


public class websocket_api extends WebSocketListener {
    private int _nDatabaseId = 2;
    private int _nHistoryId = 3;
    private int _nBroadcastId = 4;

    private OkHttpClient mOkHttpClient;
    private WebSocket mWebsocket;

    private int mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
    private int mnCliConnectStatus = WEBSOCKET_CONNECT_INVALID;
    public static int WEBSOCKET_CONNECT_INVALID = -1;
    private static int WEBSOCKET_CONNECT_SUCCESS = 0;
    private static int WEBSOCKET_ALL_READY = 0;
    private static int WEBSOCKET_CONNECT_FAIL = 1;

    private AtomicInteger mnCallId = new AtomicInteger(1);
    private HashMap<Integer, IReplyObjectProcess> mHashMapIdToProcess = new HashMap<>();
    private String gRespJson;
    private String chainid = "4a93e8abe6ab5f2b935d692e13eea73cdbfb288959fb41640b829d25b7f4bd84";


    /*
     WS_NODE_LIST: [
        {url: "wss://fake.automatic-selection.com", location: {translate: "settings.api_closest"}},
        {url: "ws://127.0.0.1:8090", location: "Locally hosted"},
        {url: "wss://bitshares.openledger.info/ws", location: "Nuremberg, Germany"},
        {url: "wss://eu.openledger.info/ws", location: "Berlin, Germany"},
        {url: "wss://bit.btsabc.org/ws", location: "Hong Kong"},
        {url: "wss://bts.transwiser.com/ws", location: "Hangzhou, China"},
        {url: "wss://bitshares.dacplay.org/ws", location:  "Hangzhou, China"},
        {url: "wss://bitshares-api.wancloud.io/ws", location:  "China"},
        {url: "wss://openledger.hk/ws", location: "Hong Kong"},
        {url: "wss://secure.freedomledger.com/ws", location: "Toronto, Canada"},
        {url: "wss://dexnode.net/ws", location: "Dallas, USA"},
        {url: "wss://altcap.io/ws", location: "Paris, France"},
        {url: "wss://bitshares.crypto.fans/ws", location: "Munich, Germany"},
        {url: "wss://node.testnet.bitshares.eu", location: "Public Testnet Server (Frankfurt, Germany)"}
         */

    private List<String> mListNode = Arrays.asList(
            "ws：//118.190.159.23:44447"
    );

    class WebsocketError {
        int code;
        String message;
        Object data;
    }

    class Call {
        int id;
        String method;
        List<Object> params;
    }

    class Reply<T> {
        String id;
        String jsonrpc;
        T result;
        WebsocketError error;
    }

    class ReplyBase {
        int id;
        String jsonrpc;
    }



    private interface IReplyObjectProcess<T> {
        void processTextToObject(String strText);
        T getReplyObject();
        String getError();
        void notifyFailure(Throwable t);
        Throwable getException();
        String getResponse();
    }

    private class ReplyObjectProcess<T> implements IReplyObjectProcess<T> {
        private String strError;
        private T mT;
        private Type mType;
        private Throwable exception;
        private String strResponse;
        public ReplyObjectProcess(Type type) {
            mType = type;
        }



        public void processTextToObject(String strText) {
            try {
                strResponse = strText;
                gRespJson = strText;
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();
                mT = gson.fromJson(strText, mType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            } catch (Exception e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            }
            synchronized (this) {
                notify();
            }
        }

        @Override
        public T getReplyObject() {
            return mT;
        }

        @Override
        public String getError() {
            return strError;
        }

        @Override
        public void notifyFailure(Throwable t) {
            exception = t;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        @Override
        public String getResponse() {
            return strResponse;
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        synchronized (mWebsocket) {
            mnConnectStatus = WEBSOCKET_CONNECT_SUCCESS;
            mWebsocket.notify();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (t instanceof IOException) {  // 出现io错误
            if (mWebsocket != null){
                synchronized (mWebsocket) {
                    mnConnectStatus = WEBSOCKET_CONNECT_FAIL;
                    mWebsocket.notify();
                }
            }

            synchronized (mHashMapIdToProcess) {
                for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                    entry.getValue().notifyFailure(t);
                }
                mHashMapIdToProcess.clear();
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        //super.onMessage(webSocket, text);

        try {
            Gson gson = new Gson();
            ReplyBase replyObjectBase = gson.fromJson(text, ReplyBase.class);

            IReplyObjectProcess iReplyObjectProcess = null;
            synchronized (mHashMapIdToProcess) {
                if (mHashMapIdToProcess.containsKey(replyObjectBase.id)) {
                    iReplyObjectProcess = mHashMapIdToProcess.get(replyObjectBase.id);
                }
            }

            if (iReplyObjectProcess != null) {
                iReplyObjectProcess.processTextToObject(text+"\t\n");
            } else {

            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

//    public static void bufferSave(String msg) {
//        try {
//            String logFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/borderless_log.txt";
//            BufferedWriter bfw = new BufferedWriter(new FileWriter(logFile, true));
//            bfw.write(msg);
//            bfw.newLine();
//            bfw.flush();
//            bfw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }



    public synchronized int connect() {
        if (mnConnectStatus == WEBSOCKET_ALL_READY) {
            return 0;
        }

        FullNodeServerSelect fullNodeServerSelect = new FullNodeServerSelect();
        String strServer = fullNodeServerSelect.getServer();
        if (TextUtils.isEmpty(strServer)) {
            return ERROR_CONNECT_SERVER_FAILD;
        }

        //for jni interface initialize
//       if (bUsedJniInterface) {
//           try {
//               mnCliConnectStatus = CliCmdExecutor.InitCli(chainid, strServer);
//           } catch (JNIException e) {
//               e.printStackTrace();
//           }
//       }

        Request request = new Request.Builder().url(strServer).build();
        mOkHttpClient = new OkHttpClient();
        mWebsocket = mOkHttpClient.newWebSocket(request, this);
        synchronized (mWebsocket) {
            if (mnConnectStatus == WEBSOCKET_CONNECT_INVALID) {
                try {
                    mWebsocket.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
                    return ERROR_CONNECT_SERVER_FAILD;
                }
            }
        }

        int nRet = 0;
        try {
            boolean bLogin = login("", "");
            if (bLogin == true) {
                _nDatabaseId = get_websocket_bitshares_api_id("database");
                _nHistoryId = get_websocket_bitshares_api_id("history");
                _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
            } else {
                nRet = ERROR_CONNECT_SERVER_FAILD;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            nRet = ERROR_CONNECT_SERVER_FAILD;
        }

//        try {
//            asset_object base = lookup_asset_symbols("BTS");
//            asset_object quote = lookup_asset_symbols("CNY");
//            subscribe_to_market(base.id, quote.id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (nRet != 0) {
            mWebsocket.close(1000, "");
            mWebsocket = null;
            mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
        } else {
            mnConnectStatus = WEBSOCKET_ALL_READY;
        }

        return nRet;
    }

    public synchronized int close() {
        synchronized (mHashMapIdToProcess) {
            for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                synchronized (entry.getValue()) {
                    entry.getValue().notify();
                }
            }
        }

        if (mWebsocket != null){
            mWebsocket.close(1000, "Close");
        }

        //free cli shared_ptr
//        if (bUsedJniInterface) {
//            try {
//                CliCmdExecutor.DistoryCli();
//            } catch (JNIException e) {
//                e.printStackTrace();
//            }
//        }
        mOkHttpClient = null;
        mWebsocket = null;
        mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
        //for cli connect status
        //mnCliConnectStatus = WEBSOCKET_CONNECT_INVALID;
        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;
        return 0;
    }



    private boolean login(String strUserName, String strPassword) throws NetworkStatusException {
        Call callObject = new Call();

        callObject.id = mnCallId.getAndIncrement();;
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add("login");

        List<Object> listLoginParams = new ArrayList<>();
        listLoginParams.add(strUserName);
        listLoginParams.add(strPassword);
        callObject.params.add(listLoginParams);

        ReplyObjectProcess<Reply<Boolean>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Boolean>>(){}.getType());
        Reply<Boolean> replyLogin = sendForReplyImpl(callObject, replyObject);
        if (replyLogin == null) {
            return false;
        } else {
            return replyLogin.result;
        }

    }

    private int get_websocket_bitshares_api_id(String strApiName) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add(strApiName);

        List<Object> listDatabaseParams = new ArrayList<>();
        callObject.params.add(listDatabaseParams);

        ReplyObjectProcess<Reply<Integer>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());
        Reply<Integer> replyApiId = sendForReplyImpl(callObject, replyObject);
        if (replyApiId == null) {
            return -1;
        } else {
            return replyApiId.result;
        }
    }

    private int get_database_api_id() throws NetworkStatusException {
        int id = get_websocket_bitshares_api_id("database");
        if (-1 != id) {
            _nDatabaseId = id;
        } else {
            close();
            connect();
            _nDatabaseId = get_websocket_bitshares_api_id("database");
        }
        return _nDatabaseId;
    }

    private int get_history_api_id() throws NetworkStatusException {
        int id = get_websocket_bitshares_api_id("history");
        if (-1 != id) {
            _nHistoryId = id;
        }else {
            close();
            connect();
            _nHistoryId = get_websocket_bitshares_api_id("history");
        }
        return _nHistoryId;
    }

    private int get_broadcast_api_id() throws NetworkStatusException {
        int id = get_websocket_bitshares_api_id("network_broadcast");
        if ( -1 != id) {
            _nBroadcastId = id;
        }else {
            close();
            connect();
            _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
        }
        return _nBroadcastId;
    }

    public sha256_object get_chain_id() throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_chain_id");

        List<Object> listDatabaseParams = new ArrayList<>();

        callObject.params.add(listDatabaseParams);

        ReplyObjectProcess<Reply<sha256_object>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<sha256_object>>(){}.getType());
        Reply<sha256_object> replyDatabase = sendForReply(callObject, replyObject);
        if (replyDatabase == null) {
            sha256_object Obj = new sha256_object();
            return Obj;
        } else {
            return replyDatabase.result;
        }
    }


    public sha256_object get_const_chain_id() {
        ReplyObjectProcess<Reply<sha256_object>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<sha256_object>>(){}.getType());

        replyObject.processTextToObject("{\"id\":65,\"jsonrpc\":\"2.0\",\"result\":\"4a93e8abe6ab5f2b935d692e13eea73cdbfb288959fb41640b829d25b7f4bd84\"}");
        Reply<sha256_object> replyObj = replyObject.getReplyObject();
        return replyObj.result;
    }


    public List<account_object> lookup_account_names(String strAccountName) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_account_names");

        List<Object> listAccountNames = new ArrayList<>();
        listAccountNames.add(strAccountName);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountNames);

        callObject.params.add(listAccountNamesParams);

        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>(){}.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);
        if (replyAccountObjectList == null) {
           return null;
        } else {
            return replyAccountObjectList.result;
        }
    }

    public List<account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_accounts");

        List<Object> listAccountIds = new ArrayList<>();
        listAccountIds.add(listAccountObjectId);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountIds);

        callObject.params.add(listAccountIds);
        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>(){}.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);
        if (replyAccountObjectList == null) {
            List<account_object> Obj = new ArrayList<account_object>();
            return Obj;
        } else {
            return replyAccountObjectList.result;
        }
    }

    public List<asset> list_account_balances(object_id<account_object> accountId) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_account_balances");

        List<Object> listAccountBalancesParam = new ArrayList<>();
        listAccountBalancesParam.add(accountId);
        listAccountBalancesParam.add(new ArrayList<Object>());
        callObject.params.add(listAccountBalancesParam);


        ReplyObjectProcess<Reply<List<asset>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset>>>(){}.getType());
        Reply<List<asset>> replyLookupAccountNames = sendForReply(callObject, replyObject);
        if (replyLookupAccountNames == null) {
            List<asset> Obj = new ArrayList<asset>();
            return Obj;
        } else {
            return replyLookupAccountNames.result;
        }
    }


    public List<operation_history_object> get_account_history(object_id<account_object> accountId, int nLimit) throws NetworkStatusException {
        _nHistoryId = get_history_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_account_history");

        List<Object> listAccountHistoryParam = new ArrayList<>();
        listAccountHistoryParam.add(accountId);
        listAccountHistoryParam.add("1.11.0");
        listAccountHistoryParam.add(nLimit);
        listAccountHistoryParam.add("1.11.0");
        callObject.params.add(listAccountHistoryParam);

        ReplyObjectProcess<Reply<List<operation_history_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<operation_history_object>>>(){}.getType());
        Reply<List<operation_history_object>> replyAccountHistory = sendForReply(callObject, replyObject);
        if (replyAccountHistory == null) {
            return null;
        } else {
            return replyAccountHistory.result;
        }
    }



    public account_object get_account(String strAccountNameOrId) throws NetworkStatusException {
        object_id<account_object> accountObjectId = object_id.create_from_string(strAccountNameOrId);

        List<account_object> listAccountObject = null;
            if (accountObjectId == null) {
                listAccountObject = lookup_account_names(strAccountNameOrId);
                if (listAccountObject == null) {
                    return null;
                }
                else
                {
                    if (listAccountObject.isEmpty()) {
                        return null;
                    }  else {
                        if (listAccountObject.size() > 0)
                            return listAccountObject.get(0);
                        else
                            return null;
                    }
                }
        } else {
            List<object_id<account_object>> listAccountObjectId = new ArrayList<>();
            listAccountObjectId.add(accountObjectId);
            listAccountObject = get_accounts(listAccountObjectId);
            if (listAccountObject == null) {
                return null;
            }
            else
            {
                if (listAccountObject.isEmpty()) {
                    return null;
                }  else {
                    if (listAccountObject.size() >0)
                         return listAccountObject.get(0);
                    else
                        return null;
                }
            }
        }

    }

    //查询转账交易列表
    public List<HistoryResponseModel.DataBean> get_transfer_history(object_id<account_object> accountId, int nLimit) throws NetworkStatusException {
        _nHistoryId = get_history_api_id();
        List<asset_object> objAssets = BitsharesWalletWraper.getInstance().list_assets_obj("", 100);
        if (objAssets == null) {
            return null;
        }
        List<HistoryResponseModel.DataBean> list = new ArrayList<HistoryResponseModel.DataBean>();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_account_history");

        List<Object> listAccountHistoryParam = new ArrayList<>();
        listAccountHistoryParam.add(accountId);
        listAccountHistoryParam.add("1.11.0");
        listAccountHistoryParam.add(nLimit);
        listAccountHistoryParam.add("1.11.0");
        callObject.params.add(listAccountHistoryParam);

        ReplyObjectProcess<Reply<List<operation_history_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<operation_history_object>>>(){}.getType());
        Reply<List<operation_history_object>> replyAccountHistory = sendForReply(callObject, replyObject);
        if (replyAccountHistory == null) {
            List<HistoryResponseModel.DataBean> Obj = new ArrayList<HistoryResponseModel.DataBean>();
            return Obj;
        } else {
                String jsonRpc = replyObject.getResponse();
                try {
                    JSONObject jsonObject = new JSONObject(jsonRpc);
                    JSONArray jsonArray = jsonObject.optJSONArray("result");
                    HistoryResponseModel.DataBean dataBean = null;
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject1 = jsonArray.optJSONObject(i);
                        JSONArray jsonArray1 = jsonObject1.optJSONArray("op");

                        if (jsonArray1.getString(0).equalsIgnoreCase("0")){
                            dataBean = new HistoryResponseModel.DataBean();
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(1);
                            String fromId = jsonObject2.optString("from");
                            String toId = jsonObject2.optString("to");
                            String amount = jsonObject2.optJSONObject("amount").optString("amount");
                            String type =  jsonObject2.optJSONObject("amount").optString("asset_id");

                            for (asset_object objAsset : objAssets){
                                if (objAsset.id.toString().equalsIgnoreCase(type)){
                                    dataBean.setSymbol(objAsset.symbol);
                                    asset_object.asset_object_legible myasset = objAsset.get_legible_asset_object(Long.parseLong(amount));
                                    dataBean.setAmount(NumberUtils.formatNumber5(myasset.count));
                                    String sFromName =  get_account(fromId).name;
                                    String sToName = get_account(toId).name;
                                    dataBean.setFrom(sFromName);
                                    dataBean.setTo(sToName);
                                    list.add(dataBean);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                  e.printStackTrace();
                  return list;
                }
                return list;
        }
    }


    public global_property_object get_global_properties() throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_global_properties");
        callObject.params.add(new ArrayList<>());

        ReplyObjectProcess<Reply<global_property_object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<global_property_object>>(){}.getType());
        Reply<global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            return null;
        } else {
            return replyObject.result;
        }
    }


    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_dynamic_global_properties");
        callObject.params.add(new ArrayList<Object>());
        ReplyObjectProcess<Reply<dynamic_global_property_object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<dynamic_global_property_object>>(){}.getType());
        Reply<dynamic_global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            return null;
        } else {
            return replyObject.result;
        }

    }

    public List<Asset> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("list_assets");

        List<Object> listAssetsParam = new ArrayList<>();
        listAssetsParam.add(strLowerBound);
        listAssetsParam.add(nLimit);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            List<Asset> Obj = new ArrayList<Asset>();
            return Obj;
        } else {
            String response =  replyObjectProcess.strResponse;
            GsonBuilder gsonBuilder = new GsonBuilder();
            Type LookupAssetSymbolsResponse = new TypeToken<WitnessResponse<List<Asset>>>(){}.getType();
            gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
            WitnessResponse<List<Asset>> witnessResponse = gsonBuilder.create().fromJson(response, LookupAssetSymbolsResponse);
            return witnessResponse.result;
        }
    }

    public List<asset_object> list_assets_obj(String strLowerBound, int nLimit) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();

        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("list_assets");

        List<Object> listAssetsParam = new ArrayList<>();
        listAssetsParam.add(strLowerBound);
        listAssetsParam.add(nLimit);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            List<asset_object> Obj = new ArrayList<asset_object>();
            return Obj;
        } else {
            return replyObject.result;
        }
    }



    public List<asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_assets");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listObjectId = new ArrayList<>();
        listObjectId.addAll(listAssetObjectId);

        listAssetsParam.add(listObjectId);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            List<asset_object> Obj = new ArrayList<asset_object>();
            return Obj;
        } else {
            return replyObject.result;
        }
    }


    public String lookup_asset_symbols_rate(String strAssetSymbol) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        String result = "";
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_asset_symbols");
        List<Object> listAssetsParam = new ArrayList<>();
        List<Object> listAssetSysmbols = new ArrayList<>();
        listAssetSysmbols.add(strAssetSymbol);
        listAssetsParam.add(listAssetSysmbols);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            return "0";
        } else {
            String jsonRpc = replyObjectProcess.getResponse();
            double nRate = 0.0;
            JSONObject jsonobj = null;
            JSONArray jarray = null;
            JSONObject job = null;
            try {
                jsonobj = new JSONObject(jsonRpc);
                jarray = jsonobj.getJSONArray("result");
                job = jarray.getJSONObject(0);
                String asset_cur_id = job.getString("id");
                int precision = job.getInt("precision");
                double base_amount = job.getJSONObject("options").getJSONObject("core_exchange_rate").getJSONObject("base").getDouble("amount");
                String base_asset_id = job.getJSONObject("options").getJSONObject("core_exchange_rate").getJSONObject("base").getString("asset_id");
                double quote_amount = job.getJSONObject("options").getJSONObject("core_exchange_rate").getJSONObject("quote").getDouble("amount");
                String quote_asset_id = job.getJSONObject("options").getJSONObject("core_exchange_rate").getJSONObject("quote").getString("asset_id");

                int targetPrec = 1;
                int bdsPrec = 1;
                //cacle other symbol precision
                if (precision <= 1){
                    return "0";
                }
                for (int i = 0; i < precision; i++) {
                    targetPrec *= 10;
                }
                //cacle bds symbol precision
                for (int n = 0; n < 5; n++) {
                    bdsPrec *= 10;
                }
                //cacle base and quote precent
                if (base_asset_id.equals("1.3.0")) {
                    //base is BDS
                    nRate = (quote_amount / targetPrec) / (base_amount / bdsPrec);
                } else if (quote_asset_id.equals("1.3.0")) {
                    //quote is BDS
                    nRate = (base_amount / targetPrec) / (quote_amount / bdsPrec);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return "0";
            }
            result = nRate + "";
            return result;
        }
    }


    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_asset_symbols");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listAssetSymbols = new ArrayList<>();
        listAssetSymbols.add(strAssetSymbol);

        listAssetsParam.add(listAssetSymbols);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            return null;
        } else {
            return replyObject.result.get(0);
        }
    }


    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_block_header");
        List<Object> listBlockNumber = new ArrayList<>();
        listBlockNumber.add(nBlockNumber);
        callObject.params.add(listBlockNumber);

        ReplyObjectProcess<Reply<block_header>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<block_header>>(){}.getType());
        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            block_header Obj = new block_header();
            return Obj;
        } else {
            return replyObject.result;
        }
    }

    //查询区块信息
    public block_object get_block(int nBlockNumber) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        block_object block = new block_object();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_block");
        List<Object> listBlockNumber = new ArrayList<>();
        listBlockNumber.add(nBlockNumber);
        callObject.params.add(listBlockNumber);

        ReplyObjectProcess<Reply<block_header>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<block_header>>(){}.getType());
        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            block_object obj = new block_object();
            return obj;
        } else {
            String sBlock = replyObjectProcess.getResponse();
            if (sBlock != null) {
                try {
                    JSONObject jsonobj = new JSONObject(sBlock);
                    JSONObject dataobj = jsonobj.getJSONObject("result");
                    block.blockNumber = nBlockNumber + "";
                    String date = TimeUtils.utc2Local(dataobj.getString("timestamp").replace("T"," "));
                    block.timeStame = date;

                    block.witnessId = dataobj.getString("witness");
                    JSONArray jarray = dataobj.getJSONArray("transactions");
                    block.transactionCount = jarray.length();

                    return block;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    public int broadcast_transaction(signed_transaction tx) throws NetworkStatusException {
        _nBroadcastId = get_broadcast_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nBroadcastId);
        callObject.params.add("broadcast_transaction");
        List<Object> listTransaction = new ArrayList<>();
        listTransaction.add(tx);
        callObject.params.add(listTransaction);

        ReplyObjectProcess<Reply<Object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());
        Reply<Object> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
             return -1;
        } else {
            if (replyObject.error != null) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public int broadcast_json_transaction(signed_transaction tx, String type) throws NetworkStatusException {
        _nBroadcastId = get_broadcast_api_id(); //没有调用
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nBroadcastId);
        callObject.params.add("broadcast_transaction");
        List<Object> listTransaction = new ArrayList<>();
        listTransaction.add(tx);
        callObject.params.add(listTransaction);

        ReplyObjectProcess<Reply<Object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());

        Reply<Object> replyObject = sendJsonForReplyImpl(callObject,replyObjectProcess);
        if (replyObject == null) {
            return -1;
        } else {
            if (replyObject.error != null) {
                return -1;
            } else {
                return 0;
            }
        }
    }


    public List<bucket_object> get_market_history(object_id<asset_object> assetObjectId1,
                                                  object_id<asset_object> assetObjectId2,
                                                  int nBucket,
                                                  Date dateStart,
                                                  Date dateEnd) throws NetworkStatusException {
        _nHistoryId = get_history_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_market_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(assetObjectId1);
        listParams.add(assetObjectId2);
        listParams.add(nBucket);
        listParams.add(dateStart);
        listParams.add(dateEnd);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<bucket_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<bucket_object>>>(){}.getType());
        Reply<List<bucket_object>> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            List<bucket_object> Obj = new ArrayList<bucket_object>();
            return Obj;
        } else {
            return replyObject.result;
        }
    }

    //获取撮合交易
    public OrderBook get_order_book(String base, String quote, int limit) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_order_book");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(limit);
        callObject.params.add(listParams);
        ReplyObjectProcess<Reply<OrderBook>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<OrderBook>>(){}.getType());
        Reply<OrderBook> replyObject = sendForReply(callObject, replyObjectProcess);
        if (replyObject == null) {
            OrderBook Obj = new OrderBook();
            return Obj;
        } else {
            return replyObject.result;
        }
    }


    public List<limit_order_object> get_limit_orders(object_id<asset_object> base,
                                                     object_id<asset_object> quote,
                                                     int limit) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_limit_orders");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(limit);
        callObject.params.add(listParams);
        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        if (replyObject == null) {
            List<limit_order_object> Obj = new ArrayList<limit_order_object>();
            return Obj;

        } else {
            return replyObject.result;
        }
    }

    public void subscribe_to_market(object_id<asset_object> base, object_id<asset_object> quote)
            throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("subscribe_to_market");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<String>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<String>>(){}.getType());
        sendForReplyImpl(callObject, replyObject);
    }


    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_ticker");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<MarketTicker>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<MarketTicker>>(){}.getType());
        Reply<MarketTicker> reply = sendForReply(callObject, replyObject);

        if (reply == null) {
            return null;
        } else {
            return reply.result;
        }
    }


    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_trade_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        String StartDate = sdf.format(start);
        listParams.add(StartDate);
        String EndDate = sdf.format(end);
        listParams.add(EndDate);
        listParams.add(limit);
        callObject.params.add(listParams);
        ReplyObjectProcess<Reply<List<MarketTrade>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<MarketTrade>>>(){}.getType());
        Reply<List<MarketTrade>> reply = sendForReply(callObject, replyObject);
        if (reply == null) {
           return null;
        } else {
            return reply.result;
        }
    }


    public full_account get_full_account(String name, boolean subscribe)
            throws NetworkStatusException, JSONException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_full_accounts");
        List<Object> listParams = new ArrayList<>();
        List<String> names = new ArrayList<>(1);
        names.add(name);
        listParams.add(names);
        listParams.add(subscribe);
        callObject.params.add(listParams);
        String rpcJson;
        full_account fullAccountObj = new full_account();
        ReplyObjectProcess<Reply<full_account_object>> replyObject =
                    new ReplyObjectProcess<>(new TypeToken<Reply<full_account_object>>() {
                    }.getType());
        try {
            Reply<full_account_object>  replay =  sendForReply(callObject, replyObject);
            if (replay == null) {
                rpcJson = replyObject.getResponse();
                if (rpcJson == null)
                   return null;
            } else {
                rpcJson = replyObject.getResponse();
            }
        } catch (JsonSyntaxException e) {
             rpcJson = replyObject.getResponse();
        } catch (Exception e) {
             rpcJson = replyObject.getResponse();
        }
        if (rpcJson == null || rpcJson.isEmpty()) {
            return null;
        }
         JSONObject jsonObject = new JSONObject(rpcJson);

        if (jsonObject != null) {
            account_object accountObj = new account_object();
            List<limit_order_object> limit_orders = new ArrayList<limit_order_object>();
            JSONArray jsonArray = jsonObject.optJSONArray("result");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray subArrayObj = jsonArray.optJSONArray(i);
                    if ( subArrayObj.length() > 0) {
                        String userName = subArrayObj.getString(0);
                        JSONObject objUser = subArrayObj.getJSONObject(1);
                        String sAccountStr = objUser.getString("account");
                        JSONArray jLimitOrdersArray = objUser.getJSONArray("limit_orders");
                        if (jLimitOrdersArray.length() > 0) {
                            List<LimitOrder> ordersList = new ArrayList<LimitOrder>();
                            for (int j = 0; j < jLimitOrdersArray.length(); j++) {
                                String sLimitOrders = jLimitOrdersArray.getString(j);
                                JSONObject limitObj = jLimitOrdersArray.getJSONObject(j);
                                String assetId = limitObj.getString("id");
                                String expiration = limitObj.getString("expiration");
                                String seller = limitObj.getString("seller");
                                long for_sale = limitObj.getLong("for_sale");
                                JSONObject sell_price = limitObj.getJSONObject("sell_price");
                                JSONObject base = sell_price.getJSONObject("base");
                                long base_amount = base.getLong("amount");
                                String base_asset_id = base.getString("asset_id");
                                JSONObject quote = sell_price.getJSONObject("quote");
                                String quote_amount = quote.getString("amount");
                                String quote_asset_id = quote.getString("asset_id");
                                String deferred_fee = limitObj.getString("deferred_fee");

                                LimitOrder limtOder = new LimitOrder(assetId);
                                UserAccount user = new UserAccount(seller);
                                limtOder.setSeller(user);
                                limtOder.setExpiration(expiration);
                                limtOder.setForSale(for_sale);
                                Price sellPrice = new Price();
                                Asset baseAsset = new Asset(base_asset_id);
                                sellPrice.base = new AssetAmount(UnsignedLong.valueOf(base_amount),baseAsset);
                                Asset quoteAsset = new Asset(quote_asset_id);
                                sellPrice.quote = new AssetAmount(UnsignedLong.valueOf(quote_amount),quoteAsset);
                                limtOder.setSellPrice(sellPrice);
                                ordersList.add(limtOder);
                            }
                            fullAccountObj.limit_orders = ordersList;
                        }

                        JSONArray jCallOrderArray = objUser.getJSONArray("call_orders");
                        if (jCallOrderArray.length() > 0) {
                            List<CallOrder> ordersList = new ArrayList<CallOrder>();
                            for (int j = 0; j < jCallOrderArray.length(); j++) {
                                CallOrder callder = new CallOrder();
                                String sLimitOrders = jCallOrderArray.getString(j);
                                JSONObject callObj = jCallOrderArray.getJSONObject(j);
                                String collateral = callObj.getString("collateral");
                                String debt = callObj.getString("debt");
                                JSONObject call_price = callObj.getJSONObject("call_price");
                                JSONObject base = call_price.getJSONObject("base");
                                String base_amount = base.getString("amount");
                                String base_asset_id = base.getString("asset_id");
                                JSONObject quote = call_price.getJSONObject("quote");
                                String quote_amount = quote.getString("amount");
                                String quote_asset_id = quote.getString("asset_id");
                                callder.setBase_amount(base_amount+"");
                                callder.setBase_asset_id(base_asset_id);
                                callder.setQuote_amount(quote_amount+"");
                                callder.setQuote_asset_id(quote_asset_id);
                                callder.setCollateral(collateral);
                                callder.setDebt(debt);
                                ordersList.add(callder);
                            }
                            fullAccountObj.call_orders = ordersList;
                        }



                    }
                }
            }
        }

        return fullAccountObj;
    }


    public List<full_account_object> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_full_accounts");

        List<Object> listParams = new ArrayList<>();
        listParams.add(names);
        listParams.add(subscribe);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<full_account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<full_account_object>>>(){}.getType());
        Reply<List<full_account_object>> reply = sendForReply(callObject, replyObject);
        if (reply == null) {
            return null;
        } else {
            return reply.result;
        }
    }

    //获取手续费
    public String get_Fee(String id, int op)  throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        String sFee = "0";
        String strResopnse = null;
        object_id<asset_object> ObjectId = object_id.create_from_string(id);
        List<object_id<asset_object>> ids = new ArrayList<>();
        ids.add(ObjectId);

        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> obj = sendForReply(callObject, replyObject);
        if (obj == null ) {
             strResopnse = replyObject.getResponse();
        } else {
             strResopnse = replyObject.getResponse();
            if (strResopnse != null && !strResopnse.isEmpty()) {

                try {
                    JSONObject jsonObject = new JSONObject(strResopnse);
                    JSONArray jArrayResult = jsonObject.getJSONArray("result");
                    if (jArrayResult != null && jArrayResult.length() > 0) {
                        JSONObject jsonObject1 = jArrayResult.getJSONObject(0);
                        JSONArray jArrayParams = jsonObject1.getJSONObject("parameters").getJSONObject("current_fees").getJSONArray("parameters");
                        if (jArrayParams != null && jArrayParams.length() > 0) {

                            if (op < jArrayParams.length()) {
                                JSONArray jArrayFees = jArrayParams.getJSONArray(op);
                                if (jArrayFees.getInt(0) == op) {
                                    JSONObject fee_obj = jArrayFees.getJSONObject(1);
                                    switch (op) {
                                        case operations.ID_TRANSER_OPERATION:
                                            long transfer_fee =  fee_obj.getLong("fee");
                                            if (transfer_fee > 0) {
                                                sFee = CalculateUtils.div(Double.parseDouble(transfer_fee+""),100000,5);
                                            }
                                            break;
                                        case operations.ID_UPGRADE_ACCOUNT_OPERATION:
                                            long liftetime_fee = fee_obj.getLong("membership_lifetime_fee");
                                            if (liftetime_fee > 0) {
                                                sFee = CalculateUtils.div(Double.parseDouble(liftetime_fee+""),100000,5);
                                            }
                                            break;
                                        case operations.ID_ACCOUNT_WHITELIST_OPERATION:
                                            long fee = fee_obj.getLong("membership_lifetime_fee");
                                            if (fee > 0) {
                                                sFee = CalculateUtils.div(Double.parseDouble(fee+""),100000,5);
                                            }
                                            break;
                                        case operations.ID_UPDATE_LMMIT_ORDER_OPERATION:
                                        case operations.ID_VESTING_WITHDRAW_OPERATION:
                                            long vest_fee =  fee_obj.getLong("fee");
                                            if (vest_fee > 0) {
                                                sFee = CalculateUtils.div(Double.parseDouble(vest_fee+""),100000,5);
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "0";
                }

            }
        }
        return sFee;
    }

    public String get_object(String id)  throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        String strResopnse = null;
        object_id ObjectId = object_id.create_from_string(id);
        List<object_id> ids = new ArrayList<>();
        ids.add(ObjectId);

        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> obj = sendForReply(callObject, replyObject);
        strResopnse = replyObject.getResponse();
        try {
            JSONObject jsonObject = new JSONObject(strResopnse);
            JSONArray jArrayResult = jsonObject.getJSONArray("result");
            return jArrayResult.get(0).toString();
        } catch (JSONException e) {
            return null;
        }

    }

    public List<limit_order_object> get_limit_orders(List<object_id<limit_order_object>> ids)
            throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        Reply<List<limit_order_object>> reply = sendForReply(callObject, replyObject);
        if (reply == null) {
           return null;

        } else {
            return reply.result;
        }
    }

    public limit_order_object get_limit_order(object_id<limit_order_object> id)
            throws NetworkStatusException {
        return get_limit_orders(Collections.singletonList(id)).get(0);
    }

    private <T> String sendForJsonReply(Call callObject,
                                        ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {


//        int id = get_websocket_bitshares_api_id("database");
//        if (-1 == id) {
//            int nRet = connect();
//            if (nRet == -1) {
//                return null;
//            }
//        }

        if (mWebsocket == null || mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
            int nRet = connect();
            if (nRet == -1) {
               // throw new NetworkStatusException("It doesn't connect to the server.");
                return null;
            }
        }

        return sendForJsonReplyImpl(callObject, replyObjectProcess);
    }

    @Nullable
    private <T> Reply<T> sendForReply(Call callObject,
                                      ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        int id = get_websocket_bitshares_api_id("database");
        if (-1 == id) {
            int nRet = connect();
            if (nRet == -1) {
                return null;
            }
        }
        if (mWebsocket == null || mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
            int nRet = connect();
            if (nRet == -1) {
                //throw new NetworkStatusException("It doesn't connect to the server.");
                return null;
            }
        }

        return sendForReplyImpl(callObject, replyObjectProcess);
    }

    private <T> Reply<T> sendJsonForReplyImpl(Call callObject,
                                          ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(callObject);
        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put(callObject.id, replyObjectProcess);
        }
        synchronized (replyObjectProcess) {
            boolean bRet = mWebsocket.send(strMessage);
            if (bRet == false) {
             // throw new NetworkStatusException("Failed to send message to server.");
                return null;
            }
            try {
                replyObjectProcess.wait(2000);
                Reply<T> replyObject = replyObjectProcess.getReplyObject();
                String jsonResp = replyObjectProcess.getResponse();
                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
                    throw new NetworkStatusException(strError);
                } else if (replyObjectProcess.getException() != null) {
                    throw new NetworkStatusException(replyObjectProcess.getException());
                } else if (replyObject == null) {
                    throw new NetworkStatusException("Reply object is null.\n" + replyObjectProcess.getResponse());
                }else if (replyObject.error != null) {
                    throw new NetworkStatusException(gson.toJson(replyObject.error));
                }
                return replyObject;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    public <T> String sendJsonForJsonReply(String strMessage, ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put( mnCallId.getAndIncrement(), replyObjectProcess);
        }
        synchronized (replyObjectProcess) {
            boolean bRet = mWebsocket.send(strMessage);
            if (bRet == false) {
                // throw new NetworkStatusException("Failed to send message to server.");
                return null;
            }
            try {
                replyObjectProcess.wait(2000);
                String jsonResp = replyObjectProcess.getResponse();
                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
                    throw new NetworkStatusException(strError);
                } else if (replyObjectProcess.getException() != null) {
                    throw new NetworkStatusException(replyObjectProcess.getException());
                }
                return jsonResp;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

    }


    private <T> String sendForJsonReplyImpl(Call callObject,
                                            ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(callObject);

        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put(callObject.id, replyObjectProcess);
        }

        synchronized (replyObjectProcess) {
            boolean bRet = mWebsocket.send(strMessage);
            if (bRet == false) {
               // throw new NetworkStatusException("Failed to send message to server.");
               return null;
            }
            try {
                replyObjectProcess.wait(2000);
                String jsonResp = replyObjectProcess.getResponse();
                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
                    throw new NetworkStatusException(strError);
                } else if (replyObjectProcess.getException() != null) {
                    throw new NetworkStatusException(replyObjectProcess.getException());
                }
                return jsonResp;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }



    private <T> Reply<T> sendForReplyImpl(Call callObject,
                                      ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(callObject);

        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put(callObject.id, replyObjectProcess);
        }

        if (mWebsocket == null) {
            connect();
            if (mWebsocket == null) {
                //TODO no network
                return null;
            }
        }

        synchronized (replyObjectProcess) {
//            if (mWebsocket == null) {
//                connect();
//                if (mWebsocket == null) {
//                    //TODO no network
//                    return null;
//                }
//            }
            boolean bRet = mWebsocket.send(strMessage);
            if (bRet == false) {
                //throw new NetworkStatusException("Failed to send message to server.");
                return null;
            }
            try {
                replyObjectProcess.wait(2000);
                Reply<T> replyObject = replyObjectProcess.getReplyObject();
                String jsonResp = replyObjectProcess.getResponse();
                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
//                    throw new NetworkStatusException(strError);
                    return null;
                } else if (replyObjectProcess.getException() != null) {

//                   throw new NetworkStatusException(replyObjectProcess.getException());
                    return null;
                } else if (replyObject == null) {
//                    throw new NetworkStatusException("Reply object is null.\n" + replyObjectProcess.getResponse());
                    return null;
                }else if (replyObject.error != null) {
//                   throw new NetworkStatusException(gson.toJson(replyObject.error));
                    return null;
                }
                return replyObject;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public boolean is_public_key_registered(String pub_key) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        boolean isRegistered = false;
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("is_public_key_registered");

        List<Object> listParams = new ArrayList<>();
        listParams.add(pub_key);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>(){}.getType());
        String strResp = null;
        try {
             strResp = sendForJsonReply(callObject, replyObject);
        } catch (Exception e) {
            strResp = replyObject.getResponse();
        }
        if (strResp == null)
            strResp = replyObject.getResponse();

        if (strResp != null && !strResp.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(strResp);
                String strResult = jsonObject.getString("result");
                if (strResult.equals("false"))
                    isRegistered = false;
                else
                    isRegistered = true;
            } catch (JSONException e) {
                e.printStackTrace();
                isRegistered = false;
            }
        }
        return isRegistered;
    }

    public List<vesting_balance_object> get_vesting_balances(String name) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();
        List<vesting_balance_object> vestingList = null;
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_vesting_balances");

        List<Object> listParams = new ArrayList<>();
        listParams.add(name);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>() {
                }.getType());
        String strResp = null;
        try {
            strResp = sendForJsonReply(callObject, replyObject);
        } catch (Exception e) {
            strResp = replyObject.getResponse();
        }
        if (strResp == null)
            strResp = replyObject.getResponse();
        if (strResp != null) {
            try {
                JSONObject jsonObject = new JSONObject(strResp);
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() > 0) {
                    vestingList = new ArrayList<vesting_balance_object>();
                    for (int i = 0 ; i< jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj != null) {
                            String id = obj.getString("id");
                            String owner = obj.getString("owner");
                            long balance_amount = obj.getJSONObject("balance").getLong("amount");
                            if (balance_amount>0) {
                                String balance_asset_id = obj.getJSONObject("balance").getString("asset_id");
                                JSONArray policyArray = obj.getJSONArray("policy");
                                if (policyArray.length() > 0) {
                                    long vesting_seconds = policyArray.getJSONObject(1).getLong("vesting_seconds");
                                    long coin_seconds_earned = policyArray.getJSONObject(1).getLong("coin_seconds_earned");
                                    String strTotal = CalculateUtils.mul(balance_amount, vesting_seconds);
                                    double nTotal = Double.parseDouble(strTotal);

                                    String strPersent = CalculateUtils.div(coin_seconds_earned, nTotal, 12);
                                    double availablePercent = Double.parseDouble(strPersent);
                                    double allowedAmount = Double.parseDouble(CalculateUtils.mul(balance_amount, availablePercent));

                                    //coin_seconds_earned/(60*60*24)
                                    double coin_days_earned = Double.parseDouble(CalculateUtils.div(coin_seconds_earned, 86400, 12));

                                    //balance_amount*vesting_seconds/(60*60*24)
                                    double coin_days_required = Double.parseDouble(CalculateUtils.div(Double.parseDouble(strTotal), 86400, 12));

                                    //vesting_seconds*(1-availablePercent)/(60*60*24)
                                    double vesting_period = Double.parseDouble(CalculateUtils.div(Double.parseDouble(CalculateUtils.mul(vesting_seconds, CalculateUtils.sub(1,availablePercent))), 86400, 2));

                                    vesting_balance_object vesting = new vesting_balance_object();
                                    vesting.vesting_seconds = vesting_seconds+"";
                                    vesting.id = id;
                                    vesting.owner = owner;
                                    vesting.TotalBalance = CalculateUtils.div(balance_amount, 100000, 5);
                                    vesting.AvailablePersent = CalculateUtils.div(availablePercent,1,4);
                                    vesting.Available_to_claim = CalculateUtils.div(allowedAmount, 100000, 5);
                                    vesting.coin_seconds_earned = coin_seconds_earned;
                                    vesting.coin_days_earned = CalculateUtils.div(coin_days_earned, 100000, 0);
                                    vesting.coin_days_required = CalculateUtils.div(coin_days_required, 100000, 0);
                                    vesting.vesting_period = vesting_period+"";
                                    vestingList.add(vesting);

                                }
                            } else {
                                vesting_balance_object vesting = new vesting_balance_object();
                                vesting.id = id;
                                vesting.owner = owner;
                                vesting.TotalBalance = "0";
                                vesting.AvailablePersent = "0";
                                vesting.Available_to_claim = "0.00000";
                                vestingList.add(vesting);
                            }
                        }
                    }
                    return vestingList;
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                return null;
            }
        }
        return vestingList;
    }


    public String get_bitasset_data(String symbol,List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        _nDatabaseId = get_database_api_id();

        asset_object asset = get_assets(listAssetObjectId).get(0);

        String feed_price = "0";
        String baseAmount = "";
        String quoteAmount = "";
        String per = "1.5";

        String result = "" ;

        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_bitasset_data");

        List<Object> listAccountIds = new ArrayList<>();
        listAccountIds.add(symbol);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountIds);

        callObject.params.add(listAccountIds);
        ReplyObjectProcess<Reply<List<asset_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>(){}.getType());
        result = sendForJsonReply(callObject, replyObject);
        if (result == null && result.equals("")) {
            return "";
        }


        try {
            JSONObject jsonObject = new JSONObject(result);
            String quote_asset_id = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("quote").optString("asset_id");
            baseAmount = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("base").optString("amount");
            quoteAmount = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("quote").optString("amount");
            per = jsonObject.optJSONObject("current_feed").optString("maintenance_collateral_ratio");
            if (quote_asset_id.equals("1.3.0")){
                feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
            }else{
                feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return feed_price+" "+per;

    }

    public String cli_get_bitasset_data(String symbol) throws NetworkStatusException {
        String feed_price = "0";
        String baseAmount = "";
        String quoteAmount = "";
        String per = "1.5";

        String result = "" ;
        if (!TextUtils.isEmpty(result)) {
            if (result.indexOf("error") != -1 || result.indexOf("exception")!= -1) {
                String strError = result;
                return "";
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                String quote_asset_id = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("quote").optString("asset_id");
                baseAmount = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("base").optString("amount");
                quoteAmount = jsonObject.optJSONObject("current_feed").optJSONObject("settlement_price").optJSONObject("quote").optString("amount");
                per = jsonObject.optJSONObject("current_feed").optString("maintenance_collateral_ratio");
                if (quote_asset_id.equals("1.3.0")){
                    feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(quoteAmount),Double.parseDouble(baseAmount),2));
                }else{
                    feed_price = NumberUtils.formatNumber2(CalculateUtils.div(Double.parseDouble(baseAmount),Double.parseDouble(quoteAmount),2));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }


        }
        else {
            return "";
        }

        return feed_price+" "+per;
    }



    public String cli_get_full_accounts(String account) throws NetworkStatusException {
        String baseAmount = "";
        String quoteAmount = "";

            String result = "";

            if (!TextUtils.isEmpty(result)) {
                if (result.indexOf("error") != -1 || result.indexOf("exception")!= -1) {
                    String strError = result;
                    return "";
                }
                try {
                    JSONObject jsonObject = new JSONObject(result);
//                    baseAmount = jsonObject.optJSONObject("current_feed").optJSONObject("core_exchange_rate").optJSONObject("base").optString("amount");
//                    quoteAmount = jsonObject.optJSONObject("current_feed").optJSONObject("core_exchange_rate").optJSONObject("quote").optString("amount");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "";
                }


            }
            else {
                return "";
            }

        return baseAmount+" "+quoteAmount;
    }


    /**
     * 借入
     * @param account
     * @return
     * @throws NetworkStatusException
     */
    public String cli_borrow_asset(String account,String amount_to_borrow, String asset_symbol, String amount_of_collateral,String privateKey) throws NetworkStatusException {

        String result = "";

        if (!TextUtils.isEmpty(result)) {
            if (result.indexOf("error") != -1 || result.indexOf("exception")!= -1) {
                String strError = result;
                return "";
            }
        }
        else {
            return "";
        }
        return result;
    }


    /**
     * 转账记录
     * @param account
     * @return
     * @throws NetworkStatusException
     */
    public HashMap<String,List<HistoryResponseModel.DataBean>> cli_transfer_record(String account,String id,String account_name ,String account_pri) throws NetworkStatusException {
        HashMap<String,List<HistoryResponseModel.DataBean>> map = new HashMap();
        List<HistoryResponseModel.DataBean> data = new ArrayList<>();
        String result = "";

            if (!TextUtils.isEmpty(result)) {
                if (result.indexOf("error") != -1 || result.indexOf("exception")!= -1) {
                    String strError = result;
                    return map;
                }
            }
            else {
                return map;
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                String op_id = "op_id";
                HistoryResponseModel.DataBean dataBean = null;
                if (jsonArray.length()>0){
                    op_id = jsonArray.optJSONObject(jsonArray.length()-1).optJSONObject("op").optString("id");
                    for (int i = 0 ; i < jsonArray.length() ; i++){
                        JSONObject object = jsonArray.optJSONObject(i);
                        String description = object.optString("description");
                        String memo = object.optString("memo");
                        if (description.contains("Transfer")){
                            String []transfers = description.split(" ");
                            dataBean = new HistoryResponseModel.DataBean();
                            dataBean.setSymbol(transfers[2]);
                            dataBean.setAmount(NumberUtils.formatNumber8(transfers[1]));
                            dataBean.setFrom(transfers[4]);
                            dataBean.setTo(transfers[6]);
                            dataBean.setFee(transfers[transfers.length-2]);
                            dataBean.setMemo(memo);
                            dataBean.setBlockNum(jsonArray.optJSONObject(i).optJSONObject("op").optString("block_num"));
                            dataBean.setId(jsonArray.optJSONObject(i).optJSONObject("op").optString("id"));
                            dataBean.setIndex(jsonArray.optJSONObject(i).optJSONObject("op").optString("op_in_trx"));
                            data.add(dataBean);
                        }

                    }
                }
                map.put(op_id,data);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        return map;
    }

    /***
     * 最新交易
     * @param nLimit
     * @return
     * @throws NetworkStatusException
     */
    public AllHistory get_all_history(String baseSymbolId, String qouteSymbolId, int nLimit) throws NetworkStatusException {
        _nHistoryId = get_history_api_id();
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_fill_order_history");

        List<Object> listAccountHistoryParam = new ArrayList<>();
        listAccountHistoryParam.add(baseSymbolId);
        listAccountHistoryParam.add(qouteSymbolId);
        listAccountHistoryParam.add(nLimit);
        callObject.params.add(listAccountHistoryParam);

        ReplyObjectProcess<Reply<AllHistory>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<AllHistory>>(){}.getType());
        Reply<AllHistory> replyObject = sendForReply(callObject, replyObjectProcess);

        Gson gson = new Gson();
        AllHistory allHistory = gson.fromJson(replyObjectProcess.getResponse(),AllHistory.class);
        return allHistory;
    }

    /***
     * cli 创建账户
     * @param  account_name 注册的账户名
     * @param  public_key   注册的账户公钥
     * @param  register     注册人名称
     * @param  register_pri 注册人私钥
     * @param  referrer     引荐人名称
     * */

    public boolean cli_register_account(String account_name,String public_key,String register,String register_pri,String referrer)
    {
        boolean bResult = false;

            //注册账户，需要注册人的私钥
            String result = "";
            if (!result.contains("Assert Exception:")) {
                if (result.contains("ref_block_num") && result.contains("operations") && result.contains("signatures")) {
                    bResult = true;
                } else {
                    bResult = false;
                }
            }else {
                bResult = false;
            }

            Log.i("resule",result);


        return bResult;

    }

    /***
     * cli 升级账户
     * @param  account_name 升级的账户名称
     * @param  account_pri  升级账户的私钥*/
    public boolean cli_upgrade_account (String account_name,String account_pri)
    {
        boolean bResult = false;

            //升级账户，需要账户的私钥
            String result = "";
            if (!result.contains("Assert Exception:")) {
                if (result.contains("ref_block_num") && result.contains("operations") && result.contains("signatures")) {
                    bResult = true;
                } else {
                    bResult = false;
                }
            }else {
                bResult = false;
            }
            Log.i("result",result);

        return bResult;
    }


    /***
     * 领取冻结资产收益
     * @param assets_id  账户id
     * @param str_amount  领取的数量
     * @param account_name 账户名称
     * @param account_pri 用户签名的账户私钥
     * @return
     *  -1 异常
     *  0  失败
     *  1  成功
     * @*/
    public int cli_withdraw_vesting(String assets_id,String str_amount,String account_name,String account_pri) {


            //升级账户，需要账户的私钥
            String result = "";

            Log.i("result",result);
            if (!result.contains("Assert Exception:")) {
                if (result.contains("ref_block_num") && result.contains("operations") && result.contains("signatures")) {
                    return 1;
                } else {
                    return 0;
                }
            }else {
                return 0;
            }

    }



    public HashMap<String,String> cli_get_block(String block_num,int index,String account_name,String account_pri) throws NetworkStatusException {
        HashMap<String,String> map = new HashMap();
        List<HistoryResponseModel.DataBean> data = new ArrayList<>();
        String result = "";


            if (!TextUtils.isEmpty(result)) {
                if (result.indexOf("error") != -1 || result.indexOf("exception")!= -1) {
                    String strError = result;
                    return map;
                }
            }
            else {
                return map;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
//                String time = jsonObject.getJSONArray("transactions").getJSONObject(index).optString("expiration");
                String time = jsonObject.optString("timestamp");
                String date = TimeUtils.utc2Local(time.replace("T"," "));
                String transaction_ids = jsonObject.getJSONArray("transaction_ids").optString(index);

                map.put("date",date);
                map.put("transaction_ids",transaction_ids);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        return map;
    }




}
