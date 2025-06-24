package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents.SDK_CONSUME_PURCHASE_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents.SDK_CONSUME_PURCHASE_RESULT
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseLabels.PURCHASE_TOKEN
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseLabels.RESPONSE_CODE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

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

    const val CONSUME_PURCHASE_FLOW = "consume_purchase"
}

object SdkConsumePurchaseLabels {
    const val PURCHASE_TOKEN = "purchase_token"
    const val RESPONSE_CODE = "response_code"
}

@Suppress("MagicNumber")
enum class SdkConsumePurchaseProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    RESPONSE_CODE_FROM_CONSUME_PURCHASE_RESULT(RESPONSE_CODE, SDK_CONSUME_PURCHASE_RESULT, 400, true),

    PURCHASE_TOKEN_FROM_CONSUME_PURCHASE_REQUEST(PURCHASE_TOKEN, SDK_CONSUME_PURCHASE_REQUEST, 410, true),
    PURCHASE_TOKEN_FROM_CONSUME_PURCHASE_RESULT(PURCHASE_TOKEN, SDK_CONSUME_PURCHASE_RESULT, 411, true),
}
