package com.appcoins.sdk.billing.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.appcoins.sdk.billing.payflow.PayflowManager
import com.appcoins.sdk.billing.receivers.AppInstallationReceiver

class BillingLifecycleService : Service() {

    private val appInstallationReceiver by lazy { AppInstallationReceiver() }

    override fun onCreate() {
        super.onCreate()
        PayflowManager.getPayflowPriorityAsync()
        registerAppInstallationReceiver()
    }

    private fun registerAppInstallationReceiver() {
        val receiverIntentFilter = IntentFilter()
        receiverIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        receiverIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        receiverIntentFilter.addDataScheme(PACKAGE_SCHEME)
        applicationContext.registerReceiver(appInstallationReceiver, receiverIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            applicationContext.unregisterReceiver(appInstallationReceiver)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val PACKAGE_SCHEME = "package"
    }
}
