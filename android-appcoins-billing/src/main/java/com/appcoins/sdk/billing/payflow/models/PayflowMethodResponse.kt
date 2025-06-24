package com.appcoins.sdk.billing.payflow.models

import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel

data class PayflowMethodResponse(
    val responseCode: Int?,
    val paymentFlowList: ArrayList<PaymentFlowMethod>?,
    val analyticsFlowSeverityLevels: ArrayList<AnalyticsFlowSeverityLevel>?,
    val analyticsPropertiesIds: ArrayList<Int>?,
    val matomoUrl: String?,
    val matomoApiKey: String?,
)
