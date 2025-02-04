package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkPurchaseFlowEvents {

    class SdkLaunchPurchase(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.CLICK,
            SDK_LAUNCH_PURCHASE,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkPurchaseResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PURCHASE_RESULT,
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

    const val SDK_LAUNCH_PURCHASE = "sdk_launch_purchase"
    const val SDK_PURCHASE_RESULT = "sdk_purchase_result"
    const val SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE = "sdk_launch_purchase_type_not_supported_failure"
    const val SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE = "sdk_launch_purchase_main_thread_failure"

    private const val PURCHASE_FLOW = "purchase_flow"
}

object SdkPurchaseFlowLabels {
    const val SKU = "sku"
    const val SKU_TYPE = "sku_type"
    const val DEVELOPER_PAYLOAD = "developer_payload"
    const val ORDER_REFERENCE = "order_reference"
    const val ORIGIN = "origin"

    const val RESPONSE_CODE = "response_code"
    const val PURCHASE_TOKEN = "purchase_token"
}
