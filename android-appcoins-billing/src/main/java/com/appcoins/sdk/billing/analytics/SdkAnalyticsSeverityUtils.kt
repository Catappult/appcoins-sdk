package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.helpers.WalletUtils

class SdkAnalyticsSeverityUtils {

    fun isEventSeverityAllowed(analyticsEvent: AnalyticsEvent): Boolean {
        val savedSeverityLevel =
            if (WalletUtils.analyticsFlowSeverityLevels == null) {
                5
            } else {
                WalletUtils.analyticsFlowSeverityLevels?.firstOrNull {
                    it.flow.equals(analyticsEvent.flow, false)
                }?.severityLevel ?: 1
            }

        return savedSeverityLevel >= analyticsEvent.severityLevel
    }
}
