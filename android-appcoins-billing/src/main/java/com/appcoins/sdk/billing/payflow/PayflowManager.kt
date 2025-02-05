package com.appcoins.sdk.billing.payflow

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_30_SECS
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils

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
                        payflowMethodResponse.analyticsFlowSeverityLevels
                            ?.takeIf { it.isNotEmpty() }
                            ?.let {
                                SdkAnalyticsUtils.analyticsFlowSeverityLevels = it
                            }
                    } else {
                        PayflowPriorityStream.getInstance().emit(arrayListOf())
                        SdkAnalyticsUtils.analyticsFlowSeverityLevels = null
                    }
                }
            }
        }

        payflowRepository.getPayflowPriorityAsync(payflowListener)
    }
}
