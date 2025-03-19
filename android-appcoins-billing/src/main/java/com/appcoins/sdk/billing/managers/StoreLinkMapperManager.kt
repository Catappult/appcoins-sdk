package com.appcoins.sdk.billing.managers

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.mappers.NewVersionAvailableResponse
import com.appcoins.sdk.billing.mappers.ReferralDeeplinkResponse
import com.appcoins.sdk.billing.mappers.StoreLinkResponse
import com.appcoins.sdk.billing.repositories.StoreLinkMapperRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.usecases.GetAppInstalledVersion
import com.appcoins.sdk.billing.usecases.GetOemIdForPackage
import com.appcoins.sdk.billing.usecases.ingameupdates.GetInstallerAppPackage
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

class StoreLinkMapperManager(private val context: Context) {
    private val storeLinkMapperRepository =
        StoreLinkMapperRepository(BdsService(BuildConfig.STORE_LINK_BASE_HOST, TIMEOUT_3_SECS))

    fun getStoreDeepLink(): StoreLinkResponse? {
        logInfo("Getting Store Deeplink value.")
        val oemid = GetOemIdForPackage(WalletUtils.context.packageName, WalletUtils.context)
        val installerAppPackage = GetInstallerAppPackage(context)

        val storeDeepLink =
            storeLinkMapperRepository.getStoreDeeplink(context.packageName, installerAppPackage, oemid)

        logInfo("Store Deeplink received: $storeDeepLink")
        return storeDeepLink
    }

    fun getReferralDeeplink(): ReferralDeeplinkResponse {
        logInfo("Getting Referral Deeplink value.")
        val oemid = GetOemIdForPackage(WalletUtils.context.packageName, WalletUtils.context)
        val installerAppPackage = GetInstallerAppPackage(context)

        val referralDeeplink =
            storeLinkMapperRepository.getReferralDeeplink(context.packageName, installerAppPackage, oemid)

        logInfo("Referral Deeplink received: $referralDeeplink")
        return referralDeeplink
    }

    fun getNewVersionAvailability(): NewVersionAvailableResponse {
        logInfo("Getting New Version Availability.")
        val oemid = GetOemIdForPackage(WalletUtils.context.packageName, WalletUtils.context)
        val installerAppPackage = GetInstallerAppPackage(context)
        val currentVersion = GetAppInstalledVersion(context.packageName, context)

        val newVersionAvailableResponse =
            storeLinkMapperRepository.getNewVersionAvailability(
                context.packageName,
                installerAppPackage,
                oemid,
                currentVersion
            )

        logInfo("New Version Availability received: $newVersionAvailableResponse")
        return newVersionAvailableResponse
    }
}
