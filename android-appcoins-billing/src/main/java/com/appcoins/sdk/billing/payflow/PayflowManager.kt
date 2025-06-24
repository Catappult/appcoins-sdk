package com.appcoins.sdk.billing.payflow

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream
import com.appcoins.sdk.billing.payflow.models.PayflowMethodResponse
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_30_SECS
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.matomo.MatomoEventLogger

object PayflowManager {

    private val payflowRepository by lazy {
        PayflowRepository(BdsService(BuildConfig.PAYFLOW_HOST, TIMEOUT_30_SECS))
    }

    @JvmStatic
    fun getPayflowPriorityAsync() {
        SdkAnalyticsUtils.sdkAnalytics.sendPayflowRequestEvent()
        val payflowListener = object : PayflowListener {
            override fun onResponse(payflowMethodResponse: PayflowMethodResponse) {
                payflowMethodResponse.responseCode?.let { responseCode ->
                    SdkAnalyticsUtils.sdkAnalytics
                        .sendPayflowResultEvent(payflowMethodResponse.paymentFlowList?.map { it.name })
                    if (ServiceUtils.isSuccess(responseCode)) {
                        val sortedMethods = payflowMethodResponse.paymentFlowList
                        sortedMethods?.sortBy { it.priority }
                        PayflowPriorityStream.getInstance().emit(sortedMethods)
                        payflowMethodResponse.analyticsFlowSeverityLevels?.let {
                            SdkAnalyticsUtils.analyticsFlowSeverityLevels = it
                        }
                        payflowMethodResponse.analyticsPropertiesIds?.let {
                            SdkAnalyticsUtils.analyticsPropertiesIds = it
                        }
                        setupMatomo(payflowMethodResponse)
                    } else {
                        PayflowPriorityStream.getInstance().emit(arrayListOf())
                        SdkAnalyticsUtils.analyticsFlowSeverityLevels = null
                        SdkAnalyticsUtils.analyticsPropertiesIds = null
                    }
                    SdkAnalyticsUtils.isAnalyticsSetupFromPayflowFinalized = true
                }
            }
        }

        payflowRepository.getPayflowPriorityAsync(payflowListener)
    }

    private fun setupMatomo(payflowMethodResponse: PayflowMethodResponse) {
        payflowMethodResponse.matomoUrl?.takeIf { it.isNotEmpty() }?.let { matomoUrl ->
            payflowMethodResponse.matomoApiKey?.takeIf { it.isNotEmpty() }?.let { matomoApiKey ->
                MatomoEventLogger.initialize(WalletUtils.context, matomoApiKey, matomoUrl)
            }
        }
    }
}
