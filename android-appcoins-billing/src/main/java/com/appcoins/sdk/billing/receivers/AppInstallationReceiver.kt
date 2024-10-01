package com.appcoins.sdk.billing.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.payflow.PayflowManager
import com.appcoins.sdk.core.logger.Logger.logInfo

class AppInstallationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        logInfo("Received new response for package change ${intent?.action}")
        if (intent?.data != null && context != null) {
            val packageName = intent.data!!.schemeSpecificPart
            if (BILLING_APPS_PACKAGES.contains(packageName)) {
                if (Intent.ACTION_PACKAGE_ADDED == intent.action) {
                    notifyBillingAppChanged()
                    logInfo("Package installed: $packageName")
                } else if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
                    notifyBillingAppChanged()
                    logInfo("Package removed: $packageName")
                } else {
                    logInfo("Package changed: $packageName -> ${intent.action}")
                }
            }
        }
    }

    private fun notifyBillingAppChanged() =
        Thread { PayflowManager.getPayflowPriorityAsync() }.start()

    private companion object {
        val BILLING_APPS_PACKAGES: List<String> =
            listOf(
                BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                BuildConfig.GAMESHUB_PACKAGE_NAME,
                BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE,
                BuildConfig.APTOIDE_GAMES_PACKAGE_NAME,
            )
    }
}