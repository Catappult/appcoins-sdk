package com.appcoins.sdk.billing.webpayment

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.helpers.UserCountryUtils.getUserCountry
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WalletUtils.setWebPaymentUrl
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

class WebPaymentManager(val packageName: String) {
    private val webPaymentRepository =
        WebPaymentRepository(BdsService(BuildConfig.PAYFLOW_HOST, 30000))

    fun getWebPaymentUrl(billingFlowParams: BillingFlowParams?) {
        val attributionSharedPreferences = AttributionSharedPreferences(WalletUtils.context)
        val oemId = attributionSharedPreferences.getOemId()
        val walletId = attributionSharedPreferences.getWalletId()

        val webPaymentUrl =
            webPaymentRepository.getWebPaymentUrl(
                packageName,
                getUserCountry(WalletUtils.context),
                oemId,
                walletId,
                billingFlowParams
            )
        setWebPaymentUrl(webPaymentUrl)
    }
}
