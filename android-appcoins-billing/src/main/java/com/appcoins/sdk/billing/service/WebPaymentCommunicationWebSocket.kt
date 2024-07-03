package com.appcoins.sdk.billing.service

import android.content.Context
import android.util.Log
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress

class WebPaymentCommunicationWebSocket(port: Int) : WebSocketServer(InetSocketAddress(port)) {

    private var isStarted = false
    private var context: Context? = null

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.i(TAG, "New connection established.")
        Log.d(TAG, "New connection: " + conn.remoteSocketAddress)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Log.i(TAG, "Connection closed.")
        Log.d(TAG, "Closed connection to: " + conn.remoteSocketAddress + " with reason: $reason")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Log.i(TAG, "Received new message.")
        Log.d(TAG, "Received message from " + conn.remoteSocketAddress + ": " + message)
        try {
            val jsonObject = JSONObject(message)
            SDKWebResponseStream.getInstance().emit(SDKWebResponse(jsonObject))
        } catch (exception: Exception) {
            SDKWebResponseStream.getInstance().emit(SDKWebResponse(ResponseCode.ERROR.value))
            exception.printStackTrace()
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Log.e(TAG, ex.message.toString())
    }

    override fun onStart() {
        Log.i(TAG, "WebSocket server started successfully")
    }

    override fun stop(timeout: Int, closeMessage: String?) {
        Log.i(TAG, "WebSocket stopped with timeout=$timeout and closeMessage=$closeMessage.")
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

    private companion object {
        const val TAG = "WebPaymentSocket"
    }
}
