package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_RESULT
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesLabels.PURCHASES
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesLabels.SKU_TYPE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkQueryPurchasesEvents {
    class SdkQueryPurchasesRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_QUERY_PURCHASES_REQUEST,
            data,
            SDK_QUERY_PURCHASES_FLOW,
            1
        )

    class SdkQueryPurchasesTypeNotSupportedError(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR,
            data,
            SDK_QUERY_PURCHASES_FLOW,
            1
        )

    class SdkQueryPurchasesResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_QUERY_PURCHASES_RESULT,
            data,
            SDK_QUERY_PURCHASES_FLOW,
            1
        )

    const val SDK_QUERY_PURCHASES_REQUEST = "sdk_query_puchases_request"
    const val SDK_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR = "sdk_query_purchases_type_not_supported_error"
    const val SDK_QUERY_PURCHASES_RESULT = "sdk_query_purchases_result"

    const val SDK_QUERY_PURCHASES_FLOW = "query_purchases"
}

object SdkQueryPurchasesLabels {
    const val SKU_TYPE = "sku_type"
    const val PURCHASES = "purchases"
}

@Suppress("MagicNumber")
enum class SdkQueryPurchasesProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    SKU_TYPE_FROM_QUERY_PURCHASES_REQUEST(SKU_TYPE, SDK_QUERY_PURCHASES_REQUEST, 1400, true),
    SKU_TYPE_FROM_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR(
        SKU_TYPE,
        SDK_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR,
        1401,
        true
    ),

    PURCHASES_FROM_QUERY_PURCHASES_RESULT(PURCHASES, SDK_QUERY_PURCHASES_RESULT, 1410, true),
}
