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
        val completedData: Map<String, Any> = (data ?: HashMap())
        val superPropertiesAndData: Map<String, Any> =
            SdkAnalyticsUtils.superProperties + completedData

        val trackHelper = TrackHelper.track()
        addTracksToTracker(trackHelper, completedData)
        trackHelper
            .event(eventName, action.name)
            .with(tracker)
    }

    private fun addTracksToTracker(trackHelper: TrackHelper, data: Map<String, Any>) {
        val customVariables = CustomVariables()
        data.keys.forEachIndexed { index, key ->
            customVariables.put(index, key, data[key].toString())
            logInfo("Custom variable: $key = ${data[key]}")
        }
        logInfo("Custom variables: $customVariables")
        //trackHelper.visitVariables(customVariables)
    }
}
