package com.tang.trade.tang.socket.exception;


public class NetworkStatusException extends Exception{
    public NetworkStatusException(String strMessage) {
        super(strMessage);
    }

    public NetworkStatusException(Throwable throwable) {
        super(throwable);
    }
}
