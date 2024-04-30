package com.appcoins.sdk.billing.service

import android.content.Context
import android.util.Log
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.SharedPreferencesRepository
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import java.net.InetSocketAddress

class WebPaymentCommunicationWebSocket : WebSocketServer(InetSocketAddress(PORT)) {

    private var isStarted = false
    private var context: Context? = null

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.i(TAG, "New connection established.")
        Log.d(TAG, "New connection: " + conn.remoteSocketAddress)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Log.i(TAG, "Connection closed.")
        Log.d(TAG, "Closed connection to: " + conn.remoteSocketAddress)
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Log.i(TAG, "Received new message.")
        Log.d(TAG, "Received message from " + conn.remoteSocketAddress + ": " + message)
        try {
            val jsonObject = JSONObject(message)
            saveGuestWalletId(jsonObject)
            SDKWebResponseStream.getInstance().emit(SDKWebResponse(jsonObject))
        } catch (jsonException: JSONException) {
            SDKWebResponseStream.getInstance().emit(SDKWebResponse(ResponseCode.ERROR.value))
            jsonException.printStackTrace()
        }
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        ex.printStackTrace()
    }

    override fun onStart() {
        Log.i(TAG, "WebSocket server started successfully")
    }

    @Throws(InterruptedException::class)
    override fun stop() {
        super.stop()
        context = null
    }

    fun start(context: Context?) {
        // TODO Secure WebSocket connection
        this.context = context
        if (!isStarted) {
            super.start()
            isStarted = true
        }
    }

    private fun saveGuestWalletId(jsonObject: JSONObject) {
        try {
            val guestWalletId = jsonObject.optString(GUEST_WALLET_ID_KEY)
            if (guestWalletId.isEmpty()) {
                return
            }
            val sharedPreferencesRepository = SharedPreferencesRepository(
                context,
                SharedPreferencesRepository.TTL_IN_SECONDS
            )
            sharedPreferencesRepository.walletId = guestWalletId
        } catch (exception: JSONException) {
            exception.printStackTrace()
        } catch (exception: NullPointerException) {
            exception.printStackTrace()
        }
    }

    companion object {
        private const val PORT = 8887
        private const val GUEST_WALLET_ID_KEY = "guestWalletID"
        private const val TAG = "WebPaymentSocket"
    }
}
