package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_RESULT
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsLabels.SKUS
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsLabels.SKU_TYPE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

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
    const val SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS = "sdk_query_sku_details_failure_on_parsing_skus"

    const val QUERY_SKU_DETAILS_FLOW = "query_sku_details"
}

object SdkQuerySkuDetailsLabels {
    const val SKUS = "skus"
    const val SKU_TYPE = "sku_type"
}

@Suppress("MagicNumber")
enum class SdkQuerySkuDetailsProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    SKUS_FROM_SKU_DETAILS_REQUEST(SKUS, SDK_QUERY_SKU_DETAILS_REQUEST, 1500, true),
    SKUS_FROM_SKU_DETAILS_RESULT(SKUS, SDK_QUERY_SKU_DETAILS_RESULT, 1501, true),
    SKUS_FROM_SKU_DETAILS_FAILURE_PARSING_SKUS(SKUS, SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS, 1502, true),

    SKU_TYPE_FROM_SKU_DETAILS_REQUEST(SKU_TYPE, SDK_QUERY_SKU_DETAILS_REQUEST, 1510, true),
    SKU_TYPE_FROM_SKU_DETAILS_FAILURE_PARSING_SKUS(SKU_TYPE, SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS, 1511, true),
}
