package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.WalletInteract
import com.appcoins.sdk.billing.analytics.WalletAddressProvider
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV1
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV2
import com.appcoins.sdk.billing.repositories.AttributionRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.address.OemIdExtractorService
import com.appcoins.sdk.billing.service.wallet.WalletGenerationMapper
import com.appcoins.sdk.billing.service.wallet.WalletRepository
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object AttributionManager {

    private val packageName by lazy { WalletUtils.context.packageName }
    private val attributionRepository by lazy {
        AttributionRepository(BdsService(BuildConfig.BACKEND_BASE, 30000))
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    fun getAttributionForUser() {
        val oemid = getOemIdForPackage(packageName)
        val guestWalletId = getWalletId()

        val attributionResponse =
            attributionRepository.getAttributionForUser(packageName, oemid, guestWalletId)
        saveAttributionResult(attributionResponse)
    }

    private fun saveAttributionResult(attributionResponse: AttributionResponse?) {
        attributionResponse?.oemId?.let { attributionSharedPreferences.setOemId(it) }
        attributionResponse?.walletId?.let { attributionSharedPreferences.setWalletId(it) }
    }

    private fun getOemIdForPackage(packageName: String?): String? =
        attributionSharedPreferences.getOemId() ?: OemIdExtractorService(
            OemIdExtractorV1(WalletUtils.context),
            OemIdExtractorV2(WalletUtils.context)
        ).extractOemId(packageName)

    private fun getWalletId(): String? {
        val backendService =
            BdsService(BuildConfig.BACKEND_BASE, BdsService.TIME_OUT_IN_MILLIS)
        val walletAddressProvider =
            WalletAddressProvider.provideWalletAddressProvider()
        val walletRepository =
            WalletRepository(backendService, WalletGenerationMapper(), walletAddressProvider)

        val walletInteract = WalletInteract(attributionSharedPreferences, walletRepository)

        return walletInteract.retrieveWalletId()
    }
}
