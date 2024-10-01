package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.repositories.StoreDeepLinkRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.usecases.ingameupdates.GetInstallerAppPackage
import com.appcoins.sdk.core.logger.Logger.logInfo

class StoreDeepLinkManager(private val context: Context) {
    private val storeDeepLinkRepository =
        StoreDeepLinkRepository(BdsService(BuildConfig.STORE_LINK_BASE_HOST, TIMEOUT_IN_MILLIS))

    fun getStoreDeepLink(): String? {
        logInfo("Getting Store Deeplink value.")
        val installerAppPackage = GetInstallerAppPackage(context)

        val storeDeepLink =
            storeDeepLinkRepository.getStoreDeepLink(context.packageName, installerAppPackage)

        logInfo("Store Deeplink received: $storeDeepLink")
        return storeDeepLink
    }

    private companion object {
        const val TIMEOUT_IN_MILLIS = 3000
    }
}
