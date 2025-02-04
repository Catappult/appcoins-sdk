package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

abstract class AnalyticsEvent(
    val action: AnalyticsManager.Action,
    val name: String,
    val data: MutableMap<String, Any>,
    val flow: String,
    val severityLevel: Int
)
