package com.appcoins.sdk.billing.managers;

import android.content.Context;

import com.appcoins.sdk.billing.service.WebPaymentCommunicationWebSocket;

import java.io.IOException;
import java.net.ServerSocket;

public class WebPaymentSocketManager {

    private final WebPaymentCommunicationWebSocket webPaymentCommunicationWebSocket;
    private static WebPaymentSocketManager instance;

    private WebPaymentSocketManager(int port) {
        webPaymentCommunicationWebSocket = new WebPaymentCommunicationWebSocket(port);
    }

    public static synchronized WebPaymentSocketManager getInstance() {
        if (instance == null) {
            instance = new WebPaymentSocketManager(getPortForWebSocket());
        }
        return instance;
    }

    public int startServer(Context context) {
        try {
            webPaymentCommunicationWebSocket.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webPaymentCommunicationWebSocket.getPort();
    }

    private static int getPortForWebSocket() {
        int port = 0;
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            serverSocket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return port;
    }
}
