package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import android.os.Build
import com.appcoins.sdk.billing.managers.AppVersionManager
import com.appcoins.sdk.billing.mappers.Version
import com.appcoins.sdk.billing.usecases.GetAppInstalledVersion
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError

object IsUpdateAvailable : UseCase() {

    operator fun invoke(context: Context): Boolean {
        super.invokeUseCase()
        val currentVersion = GetAppInstalledVersion(context.packageName, context)

        val latestVersion =
            getLatestAppVersionForCurrentSDK(AppVersionManager(context).getAppVersions())

        return try {
            latestVersion?.let {
                currentVersion < latestVersion
            } ?: false
        } catch (e: Exception) {
            logError("Failed to verify if update available: $e")
            false
        }
    }

    private fun getLatestAppVersionForCurrentSDK(versions: List<Version>?): Int? =
        versions
            ?.filter { it.minSdk <= Build.VERSION.SDK_INT }
            ?.maxByOrNull { it.versionCode }?.versionCode
}
