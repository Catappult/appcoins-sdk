package com.appcoins.sdk.ingameupdates.managers

import android.content.Context
import com.appcoins.sdk.ingameupdates.BuildConfig
import com.appcoins.sdk.ingameupdates.repositories.StoreDeepLinkRepository
import com.appcoins.sdk.ingameupdates.services.BdsService
import com.appcoins.sdk.ingameupdates.usecases.GetInstallerAppPackage

class StoreDeepLinkManager(private val context: Context) {
    private val storeDeepLinkRepository =
        StoreDeepLinkRepository(BdsService(context, BuildConfig.STORE_LINK_BASE_HOST, 3000))

    fun getStoreDeepLink(): String? {
        val installerAppPackage =
            GetInstallerAppPackage.invoke(context)?.let { installerAppPackage ->
                val storeDeepLink =
                    storeDeepLinkRepository.getStoreDeepLink(
                        context.packageName,
                        installerAppPackage
                    )

                storeDeepLink
            }

        return installerAppPackage
    }

}
