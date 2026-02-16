package com.appcoins.sdk.core.analytics.matomo

import android.content.Context
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableProperties
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestsProperties
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseProperties
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagProperties
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureProperties
import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkProperties
import com.appcoins.sdk.core.analytics.events.SdkInitializationProperties
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogProperties
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedProperties
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogProperties
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateProperties
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowProperties
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesProperties
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsProperties
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowProperties
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.manager.EventLogger
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper

object MatomoEventLogger : EventLogger {

    private var tracker: Tracker? = null
    private var allProperties: List<Property> = emptyList()
    private const val GENERAL_PROPERTIES_EVENT_NAME = "general_properties"

    override fun initialize(context: Context?, key: String?, domain: String?) {
        if (context != null && key != null) {
            tracker = TrackerBuilder
                .createDefault("$domain?api_key=$key", 1)
                .build(Matomo.getInstance(context))
            setupProperties()
        }
    }

    override fun logEvent(
        eventName: String,
        data: Map<String, Any>?,
        action: AnalyticsManager.Action,
        context: String
    ) {
        try {
            val completedData: Map<String, Any> = (data ?: HashMap())
            val superPropertiesAndData: Map<String, Any> =
                SdkAnalyticsUtils.superProperties + completedData

            tracker?.setUserId(SdkAnalyticsUtils.instanceId)

            val trackHelper = TrackHelper.track()
            addDimensionsToTracker(trackHelper, eventName, superPropertiesAndData)
            val jsonObjectData = createJsonObjectFromData(superPropertiesAndData)
            trackHelper
                .event(eventName, action.name)
                .apply {
                    jsonObjectData?.let {
                        name(jsonObjectData)
                    }
                }
                .with(tracker)
        } catch (ex: Exception) {
            logError("There was a failure when sending the Matomo Event.", ex)
        }
    }

    private fun addDimensionsToTracker(trackHelper: TrackHelper, eventName: String, data: Map<String, Any>) {
        data.keys.forEach { key ->
            val property = allProperties.findPropertyId(eventName, key)
            if (property != null) {
                val matomoId = getMatomoId(property.id)
                if (matomoId != null) {
                    trackHelper.dimension(matomoId, data[key].toString())
                } else {
                    logError("Matomo id not found for property id: ${property.id}")
                }
            }
        }
    }

    private fun List<Property>.findPropertyId(eventName: String, key: String) =
        firstOrNull {
            if (it.eventName == GENERAL_PROPERTIES_EVENT_NAME) {
                it.key == key
            } else {
                it.eventName == eventName && it.key == key
            }
        }

    private fun setupProperties() {
        val propertiesIds =
            SdkAnalyticsUtils.matomoCustomProperties ?: SdkAnalyticsUtils.defaultMatomoCustomProperties
        val properties = mutableListOf<Property>()
        properties.addAll(SdkGeneralProperties.entries)
        properties.addAll(SdkAppUpdateAvailableProperties.entries)
        properties.addAll(SdkBackendRequestsProperties.entries)
        properties.addAll(SdkConsumePurchaseProperties.entries)
        properties.addAll(SdkFeatureFlagProperties.entries)
        properties.addAll(SdkGeneralFailureProperties.entries)
        properties.addAll(SdkGetReferralDeeplinkProperties.entries)
        properties.addAll(SdkInitializationProperties.entries)
        properties.addAll(SdkInstallWalletDialogProperties.entries)
        properties.addAll(SdkIsFeatureSupportedProperties.entries)
        properties.addAll(SdkLaunchAppUpdateDialogProperties.entries)
        properties.addAll(SdkLaunchAppUpdateProperties.entries)
        properties.addAll(SdkPurchaseFlowProperties.entries)
        properties.addAll(SdkQueryPurchasesProperties.entries)
        properties.addAll(SdkQuerySkuDetailsProperties.entries)
        properties.addAll(SdkWebPaymentFlowProperties.entries)
        allProperties = properties.filter { propertiesIds.map { propertiesIds -> propertiesIds.sdkId }.contains(it.id) }
    }

    private fun getMatomoId(sdkId: Int): Int? =
        (
            SdkAnalyticsUtils.matomoCustomProperties
                ?: SdkAnalyticsUtils.defaultMatomoCustomProperties
            ).find { it.sdkId == sdkId }?.matomoId

    private fun createJsonObjectFromData(data: Map<String, Any>): String? =
        runCatching {
            val jsonObject = JSONObject()
            data.forEach { (key, value) ->
                jsonObject.put(key, value.toString())
            }
            jsonObject.toString()
        }.getOrElse {
            logError("There was an error mapping the event Data.", Exception(it))
            null
        }
}
