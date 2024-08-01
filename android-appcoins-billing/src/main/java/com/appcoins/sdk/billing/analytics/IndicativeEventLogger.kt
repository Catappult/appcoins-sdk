package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import com.appcoins.sdk.billing.analytics.manager.EventLogger
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.indicative.client.android.Indicative
import java.io.Serializable
import java.util.HashMap

class IndicativeEventLogger : EventLogger, Serializable {

    override fun setup() = Unit

    override fun logEvent(
        eventName: String, data: Map<String, Any>?,
        action: AnalyticsManager.Action, context: String
    ) {
        val completedData: Map<String, Any>? = (data ?: HashMap())
        val superPropertiesAndData: Map<String, Any>?
        superPropertiesAndData = IndicativeAnalytics.superProperties + (completedData ?: HashMap())
        Indicative.recordEvent(eventName, IndicativeAnalytics.instanceId, superPropertiesAndData)
        logDebug(
            "Called with: eventName = [$eventName], superProperties = [${IndicativeAnalytics.superProperties}] data = [$completedData], action = [$action], context = [$context], instanceId = [${IndicativeAnalytics.instanceId}]"
        )
    }
}