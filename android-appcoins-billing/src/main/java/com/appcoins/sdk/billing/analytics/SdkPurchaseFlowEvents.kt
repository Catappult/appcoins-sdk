package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkPurchaseFlowEvents {

    class SdkLaunchPurchaseRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.CLICK,
            SDK_LAUNCH_PURCHASE_REQUEST,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkLaunchPurchaseResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_PURCHASE_RESULT,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkLaunchPurchaseTypeNotSupportedFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkLaunchPurchaseMainThreadFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE,
            mutableMapOf(),
            PURCHASE_FLOW,
            1
        )

    private const val SDK_LAUNCH_PURCHASE_REQUEST = "sdk_launch_purchase_request"
    private const val SDK_LAUNCH_PURCHASE_RESULT = "sdk_launch_purchase_result"
    private const val SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE = "sdk_launch_purchase_type_not_supported_failure"
    private const val SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE = "sdk_launch_purchase_main_thread_failure"

    private const val PURCHASE_FLOW = "purchase_flow"
}
