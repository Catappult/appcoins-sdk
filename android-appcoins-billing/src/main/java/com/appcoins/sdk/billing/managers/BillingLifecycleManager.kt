package com.appcoins.sdk.billing.managers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.appcoins.sdk.billing.payflow.PayflowManager
import com.appcoins.sdk.billing.receivers.AppInstallationReceiver
import com.appcoins.sdk.billing.usecases.VerifyDontKeepActivitiesStatus
import com.appcoins.sdk.core.logger.Logger.logError

object BillingLifecycleManager {

    private const val PACKAGE_SCHEME = "package"
    private val appInstallationReceiver by lazy { AppInstallationReceiver() }

    @JvmStatic
    fun setupBillingService(context: Context) {
        Thread {
            AttributionManager.getAttributionForUser {
                PayflowManager.getPayflowPriorityAsync()
            }
            val receiverIntentFilter = IntentFilter()
            receiverIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            receiverIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            receiverIntentFilter.addDataScheme(PACKAGE_SCHEME)
            context.applicationContext.registerReceiver(
                appInstallationReceiver,
                receiverIntentFilter
            )
            VerifyDontKeepActivitiesStatus()
        }.start()
    }

    @JvmStatic
    fun finishBillingService(context: Context) {
        try {
            context.unregisterReceiver(appInstallationReceiver)
        } catch (e: Exception) {
            logError("Failed to unregister AppInstallationReceiver: $e")
        }
    }
}
