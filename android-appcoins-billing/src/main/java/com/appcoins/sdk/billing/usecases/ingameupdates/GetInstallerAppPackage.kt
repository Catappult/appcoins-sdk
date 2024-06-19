package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object GetInstallerAppPackage {

    fun invoke(context: Context): String? =
        context.packageManager.getInstallerInfo(context.packageName)

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
            e.printStackTrace()
            null
        }
    }

    private fun PackageManager.isAppInstalled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}