package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.mappers.Version
import com.appcoins.sdk.billing.repositories.AppVersionRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.core.logger.Logger.logInfo

class AppVersionManager(private val context: Context) {
    private val storeDeepLinkRepository =
        AppVersionRepository(BdsService(BuildConfig.WS75_BASE_HOST, TIMEOUT_IN_MILLIS))

    fun getAppVersions(): List<Version>? {
        logInfo("Getting versions of the Application.")
        return storeDeepLinkRepository.getAppVersions(context.packageName)
    }

    private companion object {
        const val TIMEOUT_IN_MILLIS = 3000
    }
}
