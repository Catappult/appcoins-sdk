package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

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

    class SdkQuerySkuDetailsNoSkusPresentFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_QUERY_SKU_DETAILS_NO_SKUS_PRESENT_FAILURE,
            mutableMapOf(),
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

    const val SDK_QUERY_SKU_DETAILS_REQUEST = "sdk_query_sku_details_request"
    const val SDK_QUERY_SKU_DETAILS_RESULT = "sdk_query_sku_details_result"
    const val SDK_QUERY_SKU_DETAILS_NO_SKUS_PRESENT_FAILURE = "sdk_query_sku_details_no_skus_present_failure"
    const val SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS = "sdk_query_sku_details_failure_on_parsing_skus"

    private const val QUERY_SKU_DETAILS_FLOW = "query_sku_details"
}

object SdkQuerySkuDetailsLabels {
    const val SKUS = "skus"
    const val SKU_TYPE = "sku_type"
}
