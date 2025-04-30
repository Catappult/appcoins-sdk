package com.appcoins.sdk.core.analytics.matomo

import android.content.Context
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.manager.EventLogger
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper

object MatomoEventLogger : EventLogger {

    private var tracker : Tracker? = null

    override fun initialize(context: Context?, key: String?) {
        if (context != null && key != null) {
            tracker = TrackerBuilder.createDefault("TBD", 1).build(Matomo.getInstance(context))
        }
    }

    override fun logEvent(
        eventName: String,
        data: Map<String, Any>?,
        action: AnalyticsManager.Action,
        context: String
    ) {
        val completedData: Map<String, Any> = (data ?: HashMap())
        val superPropertiesAndData: Map<String, Any> =
            IndicativeAnalytics.superProperties + completedData
        // TODO: Add Matomo event
        // Indicative.recordEvent(eventName, IndicativeAnalytics.instanceId, superPropertiesAndData)
        TrackHelper.track().event("", "").with(tracker)
        TrackHelper.track().
        logDebug(
            "Called with: eventName = [$eventName], " +
                "superProperties = [${IndicativeAnalytics.superProperties}] " +
                "data = [$completedData], " +
                "action = [$action], " +
                "context = [$context], " +
                "instanceId = [${IndicativeAnalytics.instanceId}]"
        )
        logInfo(
            "Called with: eventName = [$eventName], " +
                "superProperties = [${IndicativeAnalytics.getLoggableSuperProperties()}], " +
                "action = [$action], " +
                "context = [$context]"
        )
    }
}
