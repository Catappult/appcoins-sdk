package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkQuerySkuDetailsEvents {

    class SdkQuerySkuDetailsRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_QUERY_SKU_DETAILS_REQUEST,
            data,
            QUERY_SKU_DETAILS_FLOW,
            1
        )

    class SdkQuerySkuDetailsResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_QUERY_SKU_DETAILS_RESULT,
            data,
            QUERY_SKU_DETAILS_FLOW,
            1
        )

    class SdkQuerySkuDetailsNoSkusPresentFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_QUERY_SKU_DETAILS_NO_SKUS_PRESENT_FAILURE,
            data,
            QUERY_SKU_DETAILS_FLOW,
            1
        )

    class SdkQuerySkuDetailsFailureParsingSkus(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS,
            data,
            QUERY_SKU_DETAILS_FLOW,
            1
        )

    private const val SDK_QUERY_SKU_DETAILS_REQUEST = "sdk_query_sku_details_request"
    private const val SDK_QUERY_SKU_DETAILS_RESULT = "sdk_query_sku_details_result"
    private const val SDK_QUERY_SKU_DETAILS_NO_SKUS_PRESENT_FAILURE = "sdk_query_sku_details_no_skus_present_failure"
    private const val SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS = "sdk_query_sku_details_failure_on_parsing_skus"

    private const val QUERY_SKU_DETAILS_FLOW = "query_sku_details"
}
