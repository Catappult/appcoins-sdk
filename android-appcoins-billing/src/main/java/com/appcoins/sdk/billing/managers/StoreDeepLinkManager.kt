package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.repositories.StoreDeepLinkRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.usecases.GetOemIdForPackage
import com.appcoins.sdk.billing.usecases.ingameupdates.GetInstallerAppPackage
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

class StoreDeepLinkManager(private val context: Context) {
    private val storeDeepLinkRepository =
        StoreDeepLinkRepository(BdsService(BuildConfig.STORE_LINK_BASE_HOST, TIMEOUT_3_SECS))

    fun getStoreDeepLink(): String? {
        logInfo("Getting Store Deeplink value.")
        val oemid = GetOemIdForPackage(WalletUtils.context.packageName, WalletUtils.context)
        val installerAppPackage = GetInstallerAppPackage(context)

        val storeDeepLink =
            storeDeepLinkRepository.getStoreDeepLink(context.packageName, installerAppPackage, oemid)

        logInfo("Store Deeplink received: $storeDeepLink")
        return storeDeepLink
    }
}
