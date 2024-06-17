package com.appcoins.sdk.ingameupdates.managers

import android.content.Context
import com.appcoins.sdk.ingameupdates.BuildConfig
import com.appcoins.sdk.ingameupdates.mappers.Version
import com.appcoins.sdk.ingameupdates.repositories.AppVersionRepository
import com.appcoins.sdk.ingameupdates.services.BdsService

class AppVersionManager(private val context: Context) {
    private val storeDeepLinkRepository =
        AppVersionRepository(BdsService(context, BuildConfig.WS75_BASE_HOST, 3000))

    fun getAppVersions(): List<Version>? =
        storeDeepLinkRepository.getAppVersions(context.packageName)
}
