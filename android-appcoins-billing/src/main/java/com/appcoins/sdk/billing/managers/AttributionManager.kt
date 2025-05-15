package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.WalletInteract
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.repositories.AttributionRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.usecases.GetAppInstalledVersion
import com.appcoins.sdk.billing.usecases.GetOemIdForPackage
import com.appcoins.sdk.billing.usecases.SaveAttributionResultOnPrefs
import com.appcoins.sdk.billing.usecases.SaveInitialAttributionTimestamp
import com.appcoins.sdk.billing.usecases.SendAttributionRetryAttempt
import com.appcoins.sdk.billing.usecases.ingameupdates.GetInstallerAppPackage
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_30_SECS
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.indicative.IndicativeAnalytics
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.network.retrymechanism.exceptions.IncompleteCircularFunctionExecutionException
import com.appcoins.sdk.core.network.retrymechanism.retryUntilSuccess

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

            SdkAnalyticsUtils.sdkAnalytics.sendAttributionRequestEvent()

            val oemid = GetOemIdForPackage(packageName, WalletUtils.context)
            val guestWalletId = getWalletId()

            try {
                startAttributionRequest(oemid, guestWalletId, onSuccessfulAttribution)
            } catch (ex: IncompleteCircularFunctionExecutionException) {
                logError("Attribution failed. Requesting again.", ex)
                onSuccessfulAttribution()
                Thread {
                    SendAttributionRetryAttempt(
                        1,
                        attributionSharedPreferences.getInitialAttributionTimestamp()
                    )
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
                }.start()
            }
        } else {
            logInfo("Attribution already complete.")
            onSuccessfulAttribution()
        }
    }

    private fun startAttributionRequest(oemid: String?, guestWalletId: String?, onSuccessfulAttribution: () -> Unit) {
        val initialAttributionTimestamp = attributionSharedPreferences.getInitialAttributionTimestamp()
        val installerAppPackage = GetInstallerAppPackage(WalletUtils.context)
        val currentVersion = GetAppInstalledVersion(WalletUtils.context.packageName, WalletUtils.context)
        val attributionResponse =
            attributionRepository.getAttributionForUser(
                packageName,
                oemid,
                guestWalletId,
                installerAppPackage,
                currentVersion,
                initialAttributionTimestamp
            )

        processAttributionResult(attributionResponse, onSuccessfulAttribution)
    }

    @Suppress("complexity:CyclomaticComplexMethod")
    private fun processAttributionResult(
        attributionResponse: AttributionResponse?,
        onSuccessfulAttribution: () -> Unit
    ) {
        logInfo("Saving Attribution values.")
        if (attributionResponse.isSuccessfulAttributionResponse()) {
            logInfo("Completing Attribution flow.")
            attributionSharedPreferences.completeAttribution()
            attributionResponse?.apply {
                SaveAttributionResultOnPrefs(this)
            }
            updateIndicativeUserId(attributionResponse?.walletId)
            SdkAnalyticsUtils.sdkAnalytics.sendAttributionResultEvent(
                attributionResponse?.oemId,
                attributionResponse?.walletId,
                attributionResponse?.utmSource,
                attributionResponse?.utmMedium,
                attributionResponse?.utmCampaign,
                attributionResponse?.utmTerm,
                attributionResponse?.utmContent
            )
            onSuccessfulAttribution()
        } else {
            SdkAnalyticsUtils.sdkAnalytics.sendAttributionRequestFailureEvent()
            throw IncompleteCircularFunctionExecutionException("Attribution failed. Repeating request.")
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
