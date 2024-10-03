package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.mappers.Version
import com.appcoins.sdk.billing.repositories.AppVersionRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

class AppVersionManager(private val context: Context) {
    private val storeDeepLinkRepository =
        AppVersionRepository(BdsService(BuildConfig.WS75_BASE_HOST, TIMEOUT_3_SECS))

    fun getAppVersions(): List<Version>? {
        logInfo("Getting versions of the Application.")
        return storeDeepLinkRepository.getAppVersions(context.packageName)
    }
}
