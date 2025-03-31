package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

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

    private const val IS_FEATURE_SUPPORTED_FLOW = "is_feature_supported"
}

object SdkIsFeatureSupportedLabels {
    const val FEATURE = "feature"
    const val RESULT = "result"
}
