package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.repositories.StoreDeepLinkRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.usecases.ingameupdates.GetInstallerAppPackage

class StoreDeepLinkManager(private val context: Context) {
    private val storeDeepLinkRepository =
        StoreDeepLinkRepository(BdsService(BuildConfig.STORE_LINK_BASE_HOST, 3000))

    fun getStoreDeepLink(): String? {
        val installerAppPackage = GetInstallerAppPackage.invoke(context)

        val storeDeepLink =
            storeDeepLinkRepository.getStoreDeepLink(context.packageName, installerAppPackage)

        return storeDeepLink
    }

}
