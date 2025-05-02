package com.appcoins.sdk.core.analytics.matomo

import android.content.Context
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.manager.EventLogger
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper

object MatomoEventLogger : EventLogger {

    private var tracker: Tracker? = null

    override fun initialize(context: Context?, key: String?) {
        if (context != null && key != null) {
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
        addTracksToTracker(trackHelper, superPropertiesAndData)
        trackHelper
            .event(eventName, action.name)
            .with(tracker)
    }

    private fun addTracksToTracker(trackHelper: TrackHelper, data: Map<String, Any>) {
        data.keys.forEach { key ->
            val property = Property.ofKey(key)

            if (property != null) {
                trackHelper.dimension(property.id, data[key].toString())
            }
        }
    }
}
