package com.appcoins.sdk.core.analytics.indicative

import android.content.Context
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.manager.EventLogger
import com.indicative.client.android.Indicative

object IndicativeEventLogger : EventLogger {

    override fun initialize(context: Context?, key: String?) {
        if (context != null && key != null) {
            Indicative.launch(context, key)
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
        Indicative.recordEvent(eventName, SdkAnalyticsUtils.instanceId, superPropertiesAndData)
    }
}
