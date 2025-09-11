package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_ERROR
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_MAPPING_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_RESPONSE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_DNS_MANUAL_CACHE_SUCCESS
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.BODY
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.ERROR_MESSAGE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.HEADERS
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.IP_ADDRESS
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.METHOD
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.NETWORK_SPEED
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.PATHS
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.QUERIES
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.RESPONSE_CODE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.RESPONSE_MESSAGE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.TYPE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.URL
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkBackendRequestEvents {

    class SdkCallBackendRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_CALL_BACKEND_REQUEST,
            data,
            BACKEND_REQUEST_FLOW,
            3
        )

    class SdkCallBackendResponse(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_CALL_BACKEND_RESPONSE,
            data,
            BACKEND_REQUEST_FLOW,
            2
        )

    class SdkCallBackendMappingFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_CALL_BACKEND_MAPPING_FAILURE,
            data,
            BACKEND_REQUEST_FLOW,
            1
        )

    class SdkCallBackendError(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_CALL_BACKEND_ERROR,
            data,
            BACKEND_REQUEST_FLOW,
            1
        )

    class SdkCallBackendDnsManualCacheSuccess(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_CALL_DNS_MANUAL_CACHE_SUCCESS,
            data,
            BACKEND_REQUEST_FLOW,
            1
        )

    const val SDK_CALL_BACKEND_REQUEST = "sdk_call_backend_request"
    const val SDK_CALL_BACKEND_RESPONSE = "sdk_call_backend_response"
    const val SDK_CALL_BACKEND_MAPPING_FAILURE = "sdk_call_backend_mapping_failure"
    const val SDK_CALL_BACKEND_ERROR = "sdk_call_backend_error"
    const val SDK_CALL_DNS_MANUAL_CACHE_SUCCESS = "sdk_call_dns_manual_cache_success"

    const val BACKEND_REQUEST_FLOW = "backend_request"
}

object SdkBackendRequestLabels {
    const val TYPE = "type"

    const val URL = "url"
    const val METHOD = "method"
    const val HEADERS = "headers"
    const val PATHS = "paths"
    const val QUERIES = "queries"
    const val BODY = "body"

    const val RESPONSE_CODE = "response_code"
    const val RESPONSE_MESSAGE = "response_message"
    const val ERROR_MESSAGE = "error_message"
    const val NETWORK_SPEED = "network_speed"

    const val IP_ADDRESS = "ip_address"
}

enum class SdkBackendRequestType(val type: String) {
    PAYMENT_FLOW("payment_flow"),
    ATTRIBUTION("attribution"),
    WEB_PAYMENT_URL("web_payment_url"),
    TRANSACTION("transaction"),
    INAPP_PURCHASE("inapp_purchase"),
    PURCHASES("purchases"),
    PURCHASE("purchase"),
    PURCHASE_RESULT_EVENT("purchase_result_event"),
    CONSUME_PURCHASE("consume_purchase"),
    SKU_DETAILS("sku_details"),
    STORE_DEEPLINK("store_deeplink"),
    GUEST_WALLET("guest_wallet"),
    NEW_VERSION_AVAILABLE("new_version_available"),
    SESSION_START("session_start"),
    SESSION_END("session_end"),
}

@Suppress("MagicNumber")
enum class SdkBackendRequestsProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    TYPE_FROM_BACKEND_REQUEST(TYPE, SDK_CALL_BACKEND_REQUEST, 200),
    TYPE_FROM_BACKEND_RESPONSE(TYPE, SDK_CALL_BACKEND_RESPONSE, 201),
    TYPE_FROM_BACKEND_MAPPING_FAILURE(TYPE, SDK_CALL_BACKEND_MAPPING_FAILURE, 202),
    TYPE_FROM_BACKEND_ERROR(TYPE, SDK_CALL_BACKEND_ERROR, 203),

    URL_FROM_BACKEND_REQUEST(URL, SDK_CALL_BACKEND_REQUEST, 210),
    URL_FROM_BACKEND_ERROR(URL, SDK_CALL_BACKEND_ERROR, 211),

    METHOD_FROM_BACKEND_REQUEST(METHOD, SDK_CALL_BACKEND_REQUEST, 220),

    HEADERS_FROM_BACKEND_REQUEST(HEADERS, SDK_CALL_BACKEND_REQUEST, 230),

    PATHS_FROM_BACKEND_REQUEST(PATHS, SDK_CALL_BACKEND_REQUEST, 240),

    QUERIES_FROM_BACKEND_REQUEST(QUERIES, SDK_CALL_BACKEND_REQUEST, 250),

    BODY_FROM_BACKEND_REQUEST(BODY, SDK_CALL_BACKEND_REQUEST, 260),

    RESPONSE_CODE_FROM_BACKEND_RESPONSE(RESPONSE_CODE, SDK_CALL_BACKEND_RESPONSE, 270),

    RESPONSE_MESSAGE_FROM_BACKEND_RESPONSE(RESPONSE_MESSAGE, SDK_CALL_BACKEND_REQUEST, 280),
    RESPONSE_MESSAGE_FROM_BACKEND_ERROR(RESPONSE_MESSAGE, SDK_CALL_BACKEND_ERROR, 281),
    RESPONSE_MESSAGE_FROM_BACKEND_MAPPING_FAILURE(RESPONSE_MESSAGE, SDK_CALL_BACKEND_MAPPING_FAILURE, 282),

    ERROR_MESSAGE_FROM_BACKEND_MAPPING_FAILURE(ERROR_MESSAGE, SDK_CALL_BACKEND_MAPPING_FAILURE, 290),
    ERROR_MESSAGE_FROM_BACKEND_RESPONSE(ERROR_MESSAGE, SDK_CALL_BACKEND_RESPONSE, 291),

    NETWORK_SPEED_FROM_BACKEND_ERROR(NETWORK_SPEED, SDK_CALL_BACKEND_ERROR, 300),

    URL_FROM_BACKEND_DNS_MANUAL_CACHE_SUCCESS(URL, SDK_CALL_DNS_MANUAL_CACHE_SUCCESS, 310),
    IP_ADDRESS_FROM_BACKEND_DNS_MANUAL_CACHE_SUCCESS(IP_ADDRESS, SDK_CALL_DNS_MANUAL_CACHE_SUCCESS, 311),
    RESPONSE_CODE_FROM_BACKEND_DNS_MANUAL_CACHE_SUCCESS(RESPONSE_CODE, SDK_CALL_DNS_MANUAL_CACHE_SUCCESS, 312),
}
