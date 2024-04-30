package com.appcoins.sdk.billing.managers;

import android.content.Context;

import com.appcoins.sdk.billing.service.WebPaymentCommunicationWebSocket;

import java.net.UnknownHostException;

public class WebPaymentSocketManager {

    private final WebPaymentCommunicationWebSocket webPaymentCommunicationWebSocket;
    private static WebPaymentSocketManager instance;

    private WebPaymentSocketManager() {
        webPaymentCommunicationWebSocket = new WebPaymentCommunicationWebSocket();
    }

    public static synchronized WebPaymentSocketManager getInstance() {
        if (instance == null) {
            instance = new WebPaymentSocketManager();
        }
        return instance;
    }

    public void startServer(Context context) {
        try {
            webPaymentCommunicationWebSocket.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
