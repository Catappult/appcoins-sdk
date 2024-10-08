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
import com.appcoins.sdk.billing.usecases.SaveInitialAttributionTimestamp
import com.appcoins.sdk.billing.usecases.SendAttributionRetryAttempt
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_30_SECS
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.retrymechanism.exceptions.IncompleteCircularFunctionExecutionException
import com.appcoins.sdk.core.retrymechanism.retryUntilSuccess

object AttributionManager {
    private val packageName by lazy { WalletUtils.context.packageName }
    private val attributionRepository by lazy {
        AttributionRepository(BdsService(BuildConfig.MMP_BASE_HOST, TIMEOUT_30_SECS))
    }
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    fun getAttributionForUser(onSuccessfulAttribution: () -> Unit) {
        logInfo("Verifying new Attribution flow.")
        if (!attributionSharedPreferences.isAttributionComplete()) {
            logInfo("Getting Attribution for User.")
            SaveInitialAttributionTimestamp()

            val oemid = GetOemIdForPackage(packageName, WalletUtils.context)
            val guestWalletId = getWalletId()

            retryUntilSuccess(
                initialInterval = 1000,
                exponentialBackoff = true,
                maxInterval = 65000,
                runningBlock = { startAttributionRequest(oemid, guestWalletId, onSuccessfulAttribution) },
                onRetryBlock = {
                    SendAttributionRetryAttempt(
                        it,
                        attributionSharedPreferences.getInitialAttributionTimestamp()
                    )
                }
            )
        }
    }

    private fun startAttributionRequest(oemid: String?, guestWalletId: String?, onSuccessfulAttribution: () -> Unit) {
        val initialAttributionTimestamp = attributionSharedPreferences.getInitialAttributionTimestamp()
        val attributionResponse =
            attributionRepository.getAttributionForUser(packageName, oemid, guestWalletId, initialAttributionTimestamp)

        processAttributionResult(
            attributionResponse = attributionResponse,
            onSuccessfulAttribution = onSuccessfulAttribution,
            onFailedAttribution = {
                throw IncompleteCircularFunctionExecutionException("Attribution failed. Repeating request.")
            }
        )
    }

    @Suppress("complexity:CyclomaticComplexMethod")
    private fun processAttributionResult(
        attributionResponse: AttributionResponse?,
        onSuccessfulAttribution: () -> Unit,
        onFailedAttribution: () -> Unit
    ) {
        logInfo("Saving Attribution values.")
        if (attributionResponse.isSuccessfulAttributionResponse()) {
            logInfo("Completing Attribution flow.")
            attributionSharedPreferences.completeAttribution()
            attributionResponse?.apply {
                SaveAttributionResultOnPrefs(this)
            }
            updateIndicativeUserId(attributionResponse?.walletId)
            onSuccessfulAttribution()
        } else {
            logError("Attribution failed. Requesting again.")
            onFailedAttribution()
        }
    }

    private fun getWalletId(): String? {
        val walletInteract = WalletInteract(attributionSharedPreferences)

        return walletInteract.retrieveWalletId()
    }

    private fun AttributionResponse?.isSuccessfulAttributionResponse() =
        this?.responseCode?.let { isSuccess(it) } ?: false &&
            this?.packageName == this@AttributionManager.packageName &&
            !this?.walletId.isNullOrEmpty()

    private fun updateIndicativeUserId(walletId: String?) =
        walletId?.let { IndicativeAnalytics.updateInstanceId(it) }
}
