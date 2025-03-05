package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel

object SdkAnalyticsUtils {
    var analyticsFlowSeverityLevels: List<AnalyticsFlowSeverityLevel>? = null
    val sdkAnalytics: SdkAnalytics by lazy { SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager()) }
    var isIndicativeEventLoggerInitialized = false
}
