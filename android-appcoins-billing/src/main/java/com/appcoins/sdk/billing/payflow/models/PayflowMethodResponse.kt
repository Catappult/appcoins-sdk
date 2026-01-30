package com.appcoins.sdk.billing.payflow.models

import com.appcoins.sdk.billing.payflow.models.featureflags.FeatureFlag
import com.appcoins.sdk.core.analytics.matomo.models.MatomoDetails
import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel

data class PayflowMethodResponse(
    val responseCode: Int?,
    val paymentFlowList: ArrayList<PaymentFlowMethod>?,
    val analyticsFlowSeverityLevels: ArrayList<AnalyticsFlowSeverityLevel>?,
    val matomoDetails: MatomoDetails?,
    val featureFlags: ArrayList<FeatureFlag>?,
)
