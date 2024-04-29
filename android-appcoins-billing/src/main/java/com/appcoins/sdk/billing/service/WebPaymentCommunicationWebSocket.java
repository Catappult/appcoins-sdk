package com.appcoins.sdk.billing.service;

import android.content.Context;
import android.util.Log;

import com.appcoins.sdk.billing.SharedPreferencesRepository;
import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class WebPaymentCommunicationWebSocket extends WebSocketServer {

    private static final int port = 8887;

    private boolean isStarted = false;

    private Context context = null;

    public WebPaymentCommunicationWebSocket() throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.i("WebPaymentSocket", "New connection established.");
        Log.d("WebPaymentSocket", "New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.i("WebPaymentSocket", "Connection closed.");
        Log.d("WebPaymentSocket", "Closed connection to: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.i("WebPaymentSocket", "Received new message.");
        Log.d("WebPaymentSocket", "Received message from " + conn.getRemoteSocketAddress() + ": " + message);

        try {
            JSONObject jsonObject = new JSONObject(message);

            saveGuestWalletId(jsonObject);
            SDKWebResponseStream.getInstance().emit(new SDKWebResponse(jsonObject));
        } catch (JSONException jsonException) {

        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.i("WebPaymentSocket", "WebSocket server started successfully");
    }

    @Override
    public void stop() throws InterruptedException {
        super.stop();
        this.context = null;
    }


    public void start(Context context) {
        /*char[] keystorePassword = "password".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = WebPaymentCommunicationWebSocket.class.getResourceAsStream("/keystore.jks");
        keyStore.load(inputStream, keystorePassword);

        // Initialize key manager factory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword);

        // Initialize SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        webPaymentCommunicationWebSocket.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));*/
        this.context = context;
        if (!isStarted) {
            super.start();
            isStarted = true;
        }
    }

    private void saveGuestWalletId(JSONObject uri) {
        String guestWalletId = uri.optString(GUEST_WALLET_ID_KEY);
        if (guestWalletId == null || guestWalletId.isEmpty()) {
            return;
        }

        SharedPreferencesRepository sharedPreferencesRepository =
                new SharedPreferencesRepository(
                        context,
                        SharedPreferencesRepository.TTL_IN_SECONDS
                );
        sharedPreferencesRepository.setWalletId(guestWalletId);
    }

    private static final String GUEST_WALLET_ID_KEY = "guestWalletID";
}
