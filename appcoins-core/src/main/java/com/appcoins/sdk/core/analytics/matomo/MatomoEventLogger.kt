package com.appcoins.sdk.core.analytics.matomo

import android.content.Context
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.manager.EventLogger
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.CustomVariables
import org.matomo.sdk.extra.TrackHelper

object MatomoEventLogger : EventLogger {

    private var tracker: Tracker? = null

    override fun initialize(context: Context?, key: String?) {
        if (context != null && key != null) {
            logInfo("Initializing MatomoEventLogger with key: $key")
            //tracker = TrackerBuilder.createDefault("$key?api_key=123", 1)
            tracker = TrackerBuilder.createDefault(key, 1)
                .build(Matomo.getInstance(context))
        }
    }

    override fun logEvent(
        eventName: String,
        data: Map<String, Any>?,
        action: AnalyticsManager.Action,
        context: String
    ) {
        if (eventName == "sdk_launch_purchase") {
            val completedData: Map<String, Any> = (data ?: HashMap())
            val superPropertiesAndData: Map<String, Any> =
                SdkAnalyticsUtils.superProperties + completedData

            val trackHelper = TrackHelper.track()
            addVisitVariablesToTracker(trackHelper, superPropertiesAndData)
            trackHelper
                .event(eventName, action.name)
                .with(tracker)
        }
    }

    private fun addVisitVariablesToTracker(trackHelper: TrackHelper, data: Map<String, Any>) {
        val customVariables = CustomVariables()
        data.keys.forEach { key ->
            val property = Property.ofKey(key)
            if (property != null) {
                logInfo("Matomo: Adding visit variable: $key")
                customVariables.put(property.id, property.key, data[key].toString())
            }
        }
        trackHelper.visitVariables(customVariables)
    }

    private fun addDimensionsToTracker(trackHelper: TrackHelper, data: Map<String, Any>) {
        data.keys.forEach { key ->
            val property = Property.ofKey(key)
            if (property != null) {
                logInfo("Matomo: Adding dimension: $key")
                trackHelper.dimension(property.id, data[key].toString())
            }
        }
    }
}
