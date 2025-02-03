package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkConsumePurchaseEvents {

    class SdkConsumePurchaseRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_CONSUME_PURCHASE_REQUEST,
            data,
            CONSUME_PURCHASE_FLOW,
            1
        )

    class SdkConsumePurchaseResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_CONSUME_PURCHASE_RESULT,
            data,
            CONSUME_PURCHASE_FLOW,
            1
        )

    private const val SDK_CONSUME_PURCHASE_REQUEST = "sdk_consume_purchase_request"
    private const val SDK_CONSUME_PURCHASE_RESULT = "sdk_consume_purchase_result"

    private const val CONSUME_PURCHASE_FLOW = "consume_purchase"
}
