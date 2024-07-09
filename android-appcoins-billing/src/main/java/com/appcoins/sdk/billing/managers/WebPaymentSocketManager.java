package com.appcoins.sdk.billing.managers;

import android.content.Context;
import android.content.Intent;

import com.appcoins.sdk.billing.service.WebPaymentCommunicationWebSocket;
import com.appcoins.sdk.billing.service.WebPaymentWebSocketService;

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

    public int startServiceForPayment(Context context) {
        try {
            Intent intent = new Intent(context.getApplicationContext(), WebPaymentWebSocketService.class);
            context.getApplicationContext().startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        webPaymentCommunicationWebSocket.prepareForNewPaymentResponse();
        return webPaymentCommunicationWebSocket.getPort();
    }

    public void startServer(Context context) {
        try {
            webPaymentCommunicationWebSocket.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
