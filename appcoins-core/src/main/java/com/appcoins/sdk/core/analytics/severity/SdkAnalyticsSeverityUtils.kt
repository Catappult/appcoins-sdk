package com.appcoins.sdk.core.analytics.severity

import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.AnalyticsEvent

class SdkAnalyticsSeverityUtils {

    fun isEventSeverityAllowed(analyticsEvent: AnalyticsEvent): Boolean {
        val savedSeverityLevel =
            if (SdkAnalyticsUtils.analyticsFlowSeverityLevels == null) {
                5
            } else {
                SdkAnalyticsUtils.analyticsFlowSeverityLevels?.firstOrNull {
                    it.flow.equals(analyticsEvent.flow, false)
                }?.severityLevel ?: 1
            }

        return savedSeverityLevel >= analyticsEvent.severityLevel
    }
}
