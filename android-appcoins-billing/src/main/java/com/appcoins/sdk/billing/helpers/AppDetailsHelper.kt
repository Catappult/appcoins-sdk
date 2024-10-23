package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

class AppDetailsHelper {

    fun getAppName(context: Context): String? {
        logInfo("Getting name of the App.")
        return try {
            val (packageManager, appInfo) = getPackageManagerAndAppInfo(context)

            val appLabel = packageManager.getApplicationLabel(appInfo)
            appLabel.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            logError("Failed to find App Name: $e")
            null
        }
    }

    fun getAppLauncherIcon(context: Context): Drawable? {
        logInfo("Getting Launcher Icon of the App.")
        return try {
            val (packageManager, appInfo) = getPackageManagerAndAppInfo(context)

            packageManager.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            logError("Failed to find App Launcher Icon: $e")
            null
        }
    }

    private fun getPackageManagerAndAppInfo(context: Context): Pair<PackageManager, ApplicationInfo> {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0)
            )
        } else {
            context.packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
        }
        return Pair(packageManager, appInfo)
    }
}
