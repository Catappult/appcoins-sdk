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

object AttributionManager {

    private val packageName by lazy { WalletUtils.context.packageName }
    private val attributionRepository by lazy {
        AttributionRepository(BdsService(BuildConfig.MMP_BASE_HOST, 30000))
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    fun getAttributionForUser() {
        if (!attributionSharedPreferences.isAttributionComplete()) {
            val oemid = GetOemIdForPackage.invoke(packageName, WalletUtils.context)
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
        if (attributionResponse?.packageName == packageName) {
            attributionSharedPreferences.completeAttribution()
            attributionResponse?.apply {
                oemId?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setOemId(it)
                }
                utmSource?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setUtmSource(it)
                }
                utmMedium?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setUtmMedium(it)
                }
                utmCampaign?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setUtmCampaign(it)
                }
                utmTerm?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setUtmTerm(it)
                }
                utmContent?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setUtmContent(it)
                }
                walletId?.let {
                    if (it.isNotEmpty()) attributionSharedPreferences.setWalletId(it)
                } ?: WalletUtils.getSdkAnalytics().sendBackendGuestUidGenerationFailedEvent()
            }
        }
    }

    private fun getWalletId(): String? {
        val walletInteract = WalletInteract(attributionSharedPreferences)

        return walletInteract.retrieveWalletId()
    }
}
