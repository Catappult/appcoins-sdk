package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagEvents.SDK_MMP_PURCHASE_EVENT_RECOVERED
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagEvents.SDK_REQUEST_LIMIT_TRIGGERED
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagLabels.ORDER_ID
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagLabels.SDK_REQUEST_TYPE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkFeatureFlagEvents {

    class SdkRequestLimitTriggered(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_REQUEST_LIMIT_TRIGGERED,
            data,
            FEATURE_FLAG_FLOW,
            1
        )

    class SdkPurchaseRequestLimitTriggered :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PURCHASE_REQUEST_LIMIT_TRIGGERED,
            mutableMapOf(),
            FEATURE_FLAG_FLOW,
            1
        )

    class SdkMMPPurchaseEventRecovered(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_MMP_PURCHASE_EVENT_RECOVERED,
            data,
            FEATURE_FLAG_FLOW,
            1
        )

    const val SDK_REQUEST_LIMIT_TRIGGERED = "sdk_request_limit_triggered"
    const val SDK_PURCHASE_REQUEST_LIMIT_TRIGGERED = "sdk_purchase_request_limit_triggered"
    const val SDK_MMP_PURCHASE_EVENT_RECOVERED = "sdk_mmp_purchase_event_recovered"

    const val FEATURE_FLAG_FLOW = "feature_flag"
}

object SdkFeatureFlagLabels {
    const val SDK_REQUEST_TYPE = "sdk_request_type"
    const val ORDER_ID = "order_id"
}

@Suppress("MagicNumber")
enum class SdkFeatureFlagProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    SDK_REQUEST_TYPE_FROM_REQUEST_LIMIT_TRIGGERED(SDK_REQUEST_TYPE, SDK_REQUEST_LIMIT_TRIGGERED, 1700),
    ORDER_ID_FROM_MMP_PURCHASE_EVENT_RECOVERED(ORDER_ID, SDK_MMP_PURCHASE_EVENT_RECOVERED, 1710),
}
