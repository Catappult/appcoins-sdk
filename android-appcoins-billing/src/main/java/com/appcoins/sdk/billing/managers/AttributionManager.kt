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
import com.appcoins.sdk.billing.usecases.SaveAttributionResultOnPrefs
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

object AttributionManager {
    private const val TIMEOUT_IN_MILLIS = 30000

    private val packageName by lazy { WalletUtils.context.packageName }
    private val attributionRepository by lazy {
        AttributionRepository(BdsService(BuildConfig.MMP_BASE_HOST, TIMEOUT_IN_MILLIS))
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
            processAttributionResult(attributionResponse)
            updateIndicativeUserId(attributionResponse?.walletId)
        }
    }

    private fun updateIndicativeUserId(walletId: String?) =
        walletId?.let { IndicativeAnalytics.updateInstanceId(it) }

    @Suppress("complexity:CyclomaticComplexMethod")
    private fun processAttributionResult(attributionResponse: AttributionResponse?) {
        logInfo("Saving Attribution values.")
        if (attributionResponse?.packageName == packageName) {
            logInfo("Completing Attribution flow.")
            attributionSharedPreferences.completeAttribution()
            attributionResponse?.apply {
                SaveAttributionResultOnPrefs(this)
            }
        } else {
            logError(
                "Package name: ${attributionResponse?.packageName} " +
                    "is not the same as the current used: $packageName "
            )
        }
    }

    private fun getWalletId(): String? {
        val walletInteract = WalletInteract(attributionSharedPreferences)

        return walletInteract.retrieveWalletId()
    }
}
