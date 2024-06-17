package com.appcoins.sdk.ingameupdates.usecases

import android.content.Context
import android.os.Build
import com.appcoins.sdk.ingameupdates.managers.AppVersionManager
import com.appcoins.sdk.ingameupdates.mappers.Version

object IsUpdateAvailable {

    fun invoke(context: Context): Boolean {
        val currentVersion = GetAppInstalledVersion.invoke(context.packageName, context)

        val latestVersion =
            getLatestAppVersionForCurrentSDK(AppVersionManager(context).getAppVersions())

        return try {
            latestVersion?.let {
                currentVersion < latestVersion
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getLatestAppVersionForCurrentSDK(versions: List<Version>?): Int? =
        versions
            ?.filter { it.minSdk <= Build.VERSION.SDK_INT }
            ?.maxByOrNull { it.versionCode }?.versionCode
}