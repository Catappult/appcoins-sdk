package com.appcoins.sdk.billing.analytics

import android.util.Log
import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import com.appcoins.sdk.billing.analytics.manager.EventLogger
import com.indicative.client.android.Indicative
import java.io.Serializable
import java.util.HashMap

class IndicativeEventLogger constructor(
  private val indicativeAnalytics: IndicativeAnalytics
) : EventLogger, Serializable {

  companion object {
    private const val TAG = "IndicativeEventLogger"
  }

  override fun setup() = Unit

  override fun logEvent(
    eventName: String, data: Map<String, Any>?,
    action: AnalyticsManager.Action, context: String
  ) {
    val completedData: Map<String, Any>? = (data ?: HashMap())
    val superPropertiesAndData: Map<String, Any>?
    superPropertiesAndData = indicativeAnalytics.superProperties + (completedData ?: HashMap())
    Indicative.recordEvent(eventName, indicativeAnalytics.instanceId, superPropertiesAndData)
    Log.d(
      TAG,
      "logEvent() called with: eventName = [$eventName], superProperties = [${indicativeAnalytics.superProperties}] data = [$completedData], action = [$action], context = [$context], instanceId = [${indicativeAnalytics.instanceId}]"
    )
  }
}