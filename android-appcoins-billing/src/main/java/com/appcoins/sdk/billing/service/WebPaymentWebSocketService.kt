package com.appcoins.sdk.billing.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.sdk.billing.managers.WebPaymentSocketManager

class WebPaymentWebSocketService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        WebPaymentSocketManager.getInstance().startServer(applicationContext)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
