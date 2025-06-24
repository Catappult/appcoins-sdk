package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedEvents.SDK_IS_FEATURE_SUPPORTED_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedEvents.SDK_IS_FEATURE_SUPPORTED_RESULT
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedLabels.FEATURE
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedLabels.RESULT
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkIsFeatureSupportedEvents {

    class SdkIsFeatureSupportedRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_IS_FEATURE_SUPPORTED_REQUEST,
            data,
            IS_FEATURE_SUPPORTED_FLOW,
            1
        )

    class SdkIsFeatureSupportedResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_IS_FEATURE_SUPPORTED_RESULT,
            data,
            IS_FEATURE_SUPPORTED_FLOW,
            1
        )

    const val SDK_IS_FEATURE_SUPPORTED_REQUEST = "sdk_is_feature_supported_request"
    const val SDK_IS_FEATURE_SUPPORTED_RESULT = "sdk_is_feature_supported_result"

    const val IS_FEATURE_SUPPORTED_FLOW = "is_feature_supported"
}

object SdkIsFeatureSupportedLabels {
    const val FEATURE = "feature"
    const val RESULT = "result"
}

@Suppress("MagicNumber")
enum class SdkIsFeatureSupportedProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    FEATURE_FROM_FEATURE_SUPPORTED_REQUEST(FEATURE, SDK_IS_FEATURE_SUPPORTED_REQUEST, 1000),

    RESULT_FROM_FEATURE_SUPPORTED_RESULT(RESULT, SDK_IS_FEATURE_SUPPORTED_RESULT, 1010),
}
