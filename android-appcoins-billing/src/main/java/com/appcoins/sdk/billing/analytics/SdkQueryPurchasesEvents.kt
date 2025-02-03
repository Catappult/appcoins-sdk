package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

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