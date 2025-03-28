package com.appcoins.sdk.core.analytics.severity

import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.AnalyticsEvent

class SdkAnalyticsSeverityUtils {

    fun isEventSeverityAllowed(analyticsEvent: AnalyticsEvent): Boolean {
        val savedSeverityLevel =
            (
                SdkAnalyticsUtils.analyticsFlowSeverityLevels
                    ?: SdkAnalyticsUtils.defaultAnalyticsFlowSeverityLevels
                ).firstOrNull { it.flow.equals(analyticsEvent.flow, false) }?.severityLevel ?: 0

        return savedSeverityLevel >= analyticsEvent.severityLevel
    }
}
