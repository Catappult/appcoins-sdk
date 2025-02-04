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

    const val SDK_CONSUME_PURCHASE_REQUEST = "sdk_consume_purchase_request"
    const val SDK_CONSUME_PURCHASE_RESULT = "sdk_consume_purchase_result"

    private const val CONSUME_PURCHASE_FLOW = "consume_purchase"
}

object SdkConsumePurchaseLabels {
    const val PURCHASE_TOKEN = "purchase_token"
    const val RESPONSE_CODE = "response_code"
}
