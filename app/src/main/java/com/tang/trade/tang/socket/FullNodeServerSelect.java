package com.tang.trade.tang.socket;

import com.tang.trade.tang.BuildConfig;
import com.tang.trade.tang.MyApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class FullNodeServerSelect {
    private List<String> mListNode = Arrays.asList(
//           "ws://118.190.159.23:22227"
            "ws://118.190.159.23:44447"
    );

    public String
    getServer() {
        if (BuildConfig.DEBUG) {
//            return "ws://139.199.124.245:12357";
            return MyApp.CURRENT_NODE;
        } else {
            return MyApp.CURRENT_NODE;
        }
    }

    private String getAutoSelectServer() {
        List<WebSocket> listWebsocket = new ArrayList<>();
        final Object objectSync = new Object();

        final int nTotalCount = mListNode.size();
        final List<String> listSelectedServer = new ArrayList<>();
        for (final String strServer : mListNode) {
            Request request = new Request.Builder().url(strServer).build();
            OkHttpClient okHttpClient = new OkHttpClient();
            WebSocket webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(""); // 失败，则填空

                        if (listSelectedServer.size() == nTotalCount) {
                            objectSync.notify();
                        }
                    }
                }

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(strServer);
                        objectSync.notify();
                    }
                }
            });
            listWebsocket.add(webSocket);
        }

        String strResultServer = "";
        synchronized (objectSync) {
            if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                for (String strServer : listSelectedServer) {
                    if (strServer.isEmpty() == false) {
                        strResultServer = strServer;
                        break;
                    }
                }
            }

            if (strResultServer.isEmpty()) {
                try {
                    objectSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (listSelectedServer.isEmpty() == false && listSelectedServer.size() < nTotalCount ) {
                    for (String strServer : listSelectedServer) {
                        if (strServer.isEmpty() == false) {
                            strResultServer = strServer;
                            break;
                        }
                    }
                }
            }
        }

        for (WebSocket webSocket : listWebsocket) {
            webSocket.close(1000, "close");
        }

        return strResultServer;
    }
}
