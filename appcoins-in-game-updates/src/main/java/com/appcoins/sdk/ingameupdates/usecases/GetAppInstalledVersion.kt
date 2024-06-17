package com.appcoins.sdk.ingameupdates.usecases

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object GetAppInstalledVersion {
    fun invoke(packageName: String?, context: Context): Int {
        try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager
                        .getPackageInfo(packageName!!, PackageManager.PackageInfoFlags.of(0))
                } else {
                    context.packageManager.getPackageInfo(packageName!!, 0)
                }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return -1
        }
    }
}
