package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.repositories.MMPEventsRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object MMPEventsManager {

    private val packageName by lazy { WalletUtils.context.packageName }
    private val mmpEventsRepository by lazy {
        MMPEventsRepository(BdsService(BuildConfig.MMP_BASE_HOST, 30000))
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    fun sendSuccessfulPurchaseResultEvent(
        purchase: Purchase,
        orderId: String?,
        purchaseValue: String?
    ) {
        mmpEventsRepository.sendSuccessfulPurchaseResultEvent(
            packageName,
            attributionSharedPreferences.getOemId(),
            attributionSharedPreferences.getWalletId(),
            purchase.sku,
            orderId,
            purchaseValue,
            attributionSharedPreferences.getUtmSource(),
            attributionSharedPreferences.getUtmMedium(),
            attributionSharedPreferences.getUtmCampaign(),
            attributionSharedPreferences.getUtmTerm(),
            attributionSharedPreferences.getUtmContent()
        )
    }
}
