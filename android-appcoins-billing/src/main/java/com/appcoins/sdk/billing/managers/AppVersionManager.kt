package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.mappers.Version
import com.appcoins.sdk.billing.repositories.AppVersionRepository
import com.appcoins.sdk.billing.service.BdsService

class AppVersionManager(private val context: Context) {
    private val storeDeepLinkRepository =
        AppVersionRepository(BdsService(BuildConfig.WS75_BASE_HOST, 3000))

    fun getAppVersions(): List<Version>? =
        storeDeepLinkRepository.getAppVersions(context.packageName)
}
