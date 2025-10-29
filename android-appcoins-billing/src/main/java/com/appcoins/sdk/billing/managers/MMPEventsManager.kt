package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.repositories.MMPEventsRepository
import com.appcoins.sdk.billing.service.BdsRetryService
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_30_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

object MMPEventsManager {
    private val packageName by lazy { WalletUtils.context.packageName }
    private val mmpEventsRepository by lazy {
        MMPEventsRepository(
            BdsRetryService(
                BdsService(BuildConfig.MMP_BASE_HOST, TIMEOUT_30_SECS),
                backendRequestsSharedPreferences
            )
        )
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }
    private val backendRequestsSharedPreferences by lazy {
        BackendRequestsSharedPreferences(WalletUtils.context)
    }

    fun sendSuccessfulPurchaseResultEvent(
        purchase: Purchase,
        orderId: String,
        purchaseValue: String,
        paymentMethod: String
    ) {
        logInfo("Sending Successful Purchase Result Event to MMP.")
        val walletId = attributionSharedPreferences.getWalletId() ?: return
        val appVersionCode = WalletUtils.appVersionCode
        mmpEventsRepository.sendSuccessfulPurchaseResultEvent(
            packageName,
            attributionSharedPreferences.getOemId(),
            walletId,
            purchase.sku,
            orderId,
            purchaseValue,
            paymentMethod,
            appVersionCode,
            attributionSharedPreferences.getUtmSource(),
            attributionSharedPreferences.getUtmMedium(),
            attributionSharedPreferences.getUtmCampaign(),
            attributionSharedPreferences.getUtmTerm(),
            attributionSharedPreferences.getUtmContent(),
        )
    }

    fun sendUserSessionEvent(
        sessionId: String,
        sessionStartTimestamp: Long,
        duration: Long,
    ) {
        logInfo("Sending User Session Event to MMP.")
        val walletId = attributionSharedPreferences.getWalletId() ?: return
        val appVersionCode = WalletUtils.appVersionCode
        mmpEventsRepository.sendUserSessionEvent(
            sessionId,
            sessionStartTimestamp,
            duration,
            packageName,
            attributionSharedPreferences.getOemId(),
            walletId,
            appVersionCode,
            attributionSharedPreferences.getUtmSource(),
            attributionSharedPreferences.getUtmMedium(),
            attributionSharedPreferences.getUtmCampaign(),
            attributionSharedPreferences.getUtmTerm(),
            attributionSharedPreferences.getUtmContent(),
        )
    }
}
