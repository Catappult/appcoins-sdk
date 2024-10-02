package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError

object GetInstallerAppPackage : UseCase() {

    operator fun invoke(context: Context): String? {
        super.invokeUseCase()
        return context.packageManager.getInstallerInfo(context.packageName)
    }

    private fun PackageManager.getInstallerInfo(packageName: String): String? {
        return try {
            val installerPackageName =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getInstallSourceInfo(packageName).installingPackageName
                } else {
                    getInstallerPackageName(packageName)
                }
            installerPackageName?.takeIf { isAppInstalled(it) }
        } catch (e: Exception) {
            logError("Failed to obtain the Installer App.", e)
            null
        }
    }

    private fun PackageManager.isAppInstalled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            logError("Installer App: $packageName not installed.", e)
            false
        }
    }
}
