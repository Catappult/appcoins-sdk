package com.appcoins.sdk.billing.webpayment

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.SharedPreferencesRepository
import com.appcoins.sdk.billing.WalletInteract
import com.appcoins.sdk.billing.analytics.WalletAddressProvider
import com.appcoins.sdk.billing.helpers.UserCountryUtils.getUserCountry
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WalletUtils.setWebPaymentUrl
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV1
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV2
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.address.OemIdExtractorService
import com.appcoins.sdk.billing.service.wallet.WalletGenerationMapper
import com.appcoins.sdk.billing.service.wallet.WalletRepository

class WebPaymentManager(val packageName: String) {
    private val webPaymentRepository =
        WebPaymentRepository(BdsService(BuildConfig.PAYFLOW_HOST, 30000))

    fun getWebPaymentUrl(billingFlowParams: BillingFlowParams?) {
        val oemid = getOemIdForPackage(packageName)
        val guestWalletId = getGuestWalletId()

        val paymentFlowMethodList =
            webPaymentRepository.getWebPaymentUrl(
                packageName,
                getUserCountry(WalletUtils.context),
                oemid,
                guestWalletId,
                billingFlowParams
            )
        setWebPaymentUrl(paymentFlowMethodList)
    }

    private fun getOemIdForPackage(packageName: String?): String =
        OemIdExtractorService(
            OemIdExtractorV1(WalletUtils.context),
            OemIdExtractorV2(WalletUtils.context)
        ).extractOemId(packageName)

    private fun getGuestWalletId(): String {
        val backendService =
            BdsService(BuildConfig.BACKEND_BASE, BdsService.TIME_OUT_IN_MILLIS)
        val walletAddressProvider =
            WalletAddressProvider.provideWalletAddressProvider()
        val walletRepository =
            WalletRepository(backendService, WalletGenerationMapper(), walletAddressProvider)
        val sharedPreferencesRepository =
            SharedPreferencesRepository(
                WalletUtils.context,
                SharedPreferencesRepository.TTL_IN_SECONDS
            )

        val walletInteract =
            WalletInteract(sharedPreferencesRepository, walletRepository)

        return walletInteract.retrieveWalletId()
    }
}
