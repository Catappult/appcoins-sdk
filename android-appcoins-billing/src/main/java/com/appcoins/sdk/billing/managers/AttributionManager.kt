package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.WalletInteract
import com.appcoins.sdk.billing.analytics.IndicativeAnalytics
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.repositories.AttributionRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.usecases.GetOemIdForPackage
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

object AttributionManager {

    private val packageName by lazy { WalletUtils.context.packageName }
    private val attributionRepository by lazy {
        AttributionRepository(BdsService(BuildConfig.MMP_BASE_HOST, 30000))
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    fun getAttributionForUser() {
        logInfo("Verifying new Attribution flow.")
        if (!attributionSharedPreferences.isAttributionComplete()) {
            logInfo("Getting Attribution for User.")
            val oemid = GetOemIdForPackage(packageName, WalletUtils.context)
            val guestWalletId = getWalletId()

            val attributionResponse =
                attributionRepository.getAttributionForUser(packageName, oemid, guestWalletId)
            saveAttributionResult(attributionResponse)
            updateIndicativeUserId(attributionResponse?.walletId)
        }
    }

    private fun updateIndicativeUserId(walletId: String?) =
        walletId?.let { IndicativeAnalytics.updateInstanceId(it) }

    private fun saveAttributionResult(attributionResponse: AttributionResponse?) {
        logInfo("Saving Attribution values.")
        if (attributionResponse?.packageName == packageName) {
            logInfo("Completing Attribution flow.")
            attributionSharedPreferences.completeAttribution()
            attributionResponse?.apply {
                oemId?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new OEMID.")
                        logDebug("OEMID: $it")
                        attributionSharedPreferences.setOemId(it)
                    }
                }
                utmSource?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new UtmSource.")
                        logDebug("UtmSource: $it")
                        attributionSharedPreferences.setUtmSource(it)
                    }
                }
                utmMedium?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new UtmMedium.")
                        logDebug("UtmMedium: $it")
                        attributionSharedPreferences.setUtmMedium(it)
                    }
                }
                utmCampaign?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new UtmCampaign.")
                        logDebug("UtmCampaign: $it")
                        attributionSharedPreferences.setUtmCampaign(it)
                    }
                }
                utmTerm?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new UtmTerm.")
                        logDebug("UtmTerm: $it")
                        attributionSharedPreferences.setUtmTerm(it)
                    }
                }
                utmContent?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new UtmContent.")
                        logDebug("UtmContent: $it")
                        attributionSharedPreferences.setUtmContent(it)
                    }
                }
                walletId?.let {
                    if (it.isNotEmpty()) {
                        logInfo("Setting new WalletId.")
                        logDebug("WalletId: $it")
                        attributionSharedPreferences.setWalletId(it)
                    } else {
                        sendBackendGuestUidGenerationFailedEvent()
                    }
                } ?: sendBackendGuestUidGenerationFailedEvent()
            }
        } else {
            logError("Package name: ${attributionResponse?.packageName} is not the same as the current used: $packageName ")
        }
    }

    private fun getWalletId(): String? {
        val walletInteract = WalletInteract(attributionSharedPreferences)

        return walletInteract.retrieveWalletId()
    }

    private fun sendBackendGuestUidGenerationFailedEvent() {
        logError("Failure to get GuestUid for User from Attribution.")
        WalletUtils.getSdkAnalytics().sendBackendGuestUidGenerationFailedEvent()
    }
}
