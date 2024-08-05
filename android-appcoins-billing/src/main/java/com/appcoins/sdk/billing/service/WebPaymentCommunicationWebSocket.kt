package com.appcoins.sdk.billing.service

import android.content.Context
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress

class WebPaymentCommunicationWebSocket(port: Int) : WebSocketServer(InetSocketAddress(port)) {

    private var isStarted = false
    private var context: Context? = null
    private var remoteSocketAddressForCurrentPayment: String? = null
    private var isNewPaymentRequest = false

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        logInfo("New connection established.")
        logDebug("New connection: " + conn.remoteSocketAddress)
        if (isNewPaymentRequest) {
            isNewPaymentRequest = false
            remoteSocketAddressForCurrentPayment = conn.remoteSocketAddress.toString()
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        logInfo("Connection closed.")
        logDebug("Closed connection to: " + conn.remoteSocketAddress + " with reason: $reason")
        if (conn.remoteSocketAddress.toString() == remoteSocketAddressForCurrentPayment) {
            remoteSocketAddressForCurrentPayment = null
            SDKWebResponseStream.getInstance()
                .emit(SDKWebResponse(ResponseCode.USER_CANCELED.value))
        }
    }

    override fun onMessage(conn: WebSocket, message: String) {
        logInfo("Received new message.")
        logDebug("Received message from " + conn.remoteSocketAddress + ": " + message)

        if (conn.remoteSocketAddress.toString() == remoteSocketAddressForCurrentPayment) {
            remoteSocketAddressForCurrentPayment = null
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        logError(ex.message.toString())
    }

    override fun onStart() {
        logInfo("WebSocket server started successfully")
    }

    override fun stop(timeout: Int, closeMessage: String?) {
        logInfo("WebSocket stopped with timeout=$timeout and closeMessage=$closeMessage.")
        context = null
        isStarted = false
        super.stop(timeout, closeMessage)
    }

    fun start(context: Context?) {
        this.context = context?.applicationContext
        if (!isStarted) {
            connectionLostTimeout = 0
            super.start()
            isStarted = true
        }
    }

    fun prepareForNewPaymentResponse() {
        remoteSocketAddressForCurrentPayment = null
        isNewPaymentRequest = true
    }
}
