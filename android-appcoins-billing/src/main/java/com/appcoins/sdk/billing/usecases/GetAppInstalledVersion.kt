package com.appcoins.sdk.billing.usecases

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.appcoins.sdk.core.logger.Logger.logWarning

object GetAppInstalledVersion : UseCase() {
    operator fun invoke(packageName: String?, context: Context): Int {
        super.invokeUseCase()
        if (packageName == null) return -1
        try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager
                        .getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    context.packageManager.getPackageInfo(packageName, 0)
                }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            logWarning("Failed to find installed app: $e")
            return -1
        }
    }
}
