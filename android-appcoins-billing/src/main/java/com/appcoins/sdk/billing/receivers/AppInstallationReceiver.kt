package com.appcoins.sdk.billing.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.payflow.PayflowManager


class AppInstallationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Received new response for package change ${intent?.action}")
        if (intent?.data != null && context != null) {
            val packageName = intent.data!!.schemeSpecificPart
            if (BILLING_APPS_PACKAGES.contains(packageName)) {
                if (Intent.ACTION_PACKAGE_ADDED == intent.action) {
                    notifyBillingAppChanged()
                    Log.i(TAG, "Package installed: $packageName")
                } else if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
                    notifyBillingAppChanged()
                    Log.i(TAG, "Package removed: $packageName")
                } else {
                    Log.i(TAG, "Package changed: $packageName -> ${intent.action}")
                }
            }
        }
    }

    private fun notifyBillingAppChanged() {
        PayflowManager.getPayflowPriorityAsync()
    }

    private companion object {
        const val TAG = "PackageChangeReceiver"
        val BILLING_APPS_PACKAGES: List<String> =
            listOf(
                BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                BuildConfig.GAMESHUB_PACKAGE_NAME
            )
    }
}