package com.appcoins.sdk.core.analytics.matomo

import android.content.Context
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableProperties
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestsProperties
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseProperties
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
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper

object MatomoEventLogger : EventLogger {

    private var tracker: Tracker? = null
    private val allProperties: List<Property> by lazy {
        val mutableList = mutableListOf<Property>()
        mutableList.addAll(SdkGeneralProperties.values())
        mutableList.addAll(SdkAppUpdateAvailableProperties.values())
        mutableList.addAll(SdkBackendRequestsProperties.values())
        mutableList.addAll(SdkConsumePurchaseProperties.values())
        mutableList.addAll(SdkGeneralFailureProperties.values())
        mutableList.addAll(SdkGetReferralDeeplinkProperties.values())
        mutableList.addAll(SdkInitializationProperties.values())
        mutableList.addAll(SdkInstallWalletDialogProperties.values())
        mutableList.addAll(SdkIsFeatureSupportedProperties.values())
        mutableList.addAll(SdkLaunchAppUpdateDialogProperties.values())
        mutableList.addAll(SdkLaunchAppUpdateProperties.values())
        mutableList.addAll(SdkPurchaseFlowProperties.values())
        mutableList.addAll(SdkQueryPurchasesProperties.values())
        mutableList.addAll(SdkQuerySkuDetailsProperties.values())
        mutableList.addAll(SdkWebPaymentFlowProperties.values())
        mutableList.filter { !it.skip }
    }
    private const val GENERAL_PROPERTIES_EVENT_NAME = "general_properties"

    override fun initialize(context: Context?, key: String?, domain: String?) {
        if (context != null && key != null) {
            tracker = TrackerBuilder
                .createDefault("$domain?api_key=$key", 1)
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

        tracker?.setUserId(SdkAnalyticsUtils.instanceId)

        val trackHelper = TrackHelper.track()
        addDimensionsToTracker(trackHelper, eventName, superPropertiesAndData)
        trackHelper
            .event(eventName, action.name)
            .with(tracker)
    }

    private fun addDimensionsToTracker(trackHelper: TrackHelper, eventName: String, data: Map<String, Any>) {
        data.keys.forEach { key ->
            val property = allProperties.findPropertyId(eventName, key)
            if (property != null) {
                trackHelper.dimension(property.id, data[key].toString())
            }
        }
    }

    private fun List<Property>.findPropertyId(eventName: String, key: String) =
        firstOrNull { it.eventName == GENERAL_PROPERTIES_EVENT_NAME || (it.eventName == eventName && it.key == key) }
}
