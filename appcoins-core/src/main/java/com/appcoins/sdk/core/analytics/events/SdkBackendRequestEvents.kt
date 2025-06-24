package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_ERROR
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_MAPPING_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_REQUEST
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents.SDK_CALL_BACKEND_RESPONSE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.BODY
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.ERROR_MESSAGE
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels.HEADERS
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

    const val SDK_CALL_BACKEND_REQUEST = "sdk_call_backend_request"
    const val SDK_CALL_BACKEND_RESPONSE = "sdk_call_backend_response"
    const val SDK_CALL_BACKEND_MAPPING_FAILURE = "sdk_call_backend_mapping_failure"
    const val SDK_CALL_BACKEND_ERROR = "sdk_call_backend_error"

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
    NEW_VERSION_AVAILABLE("new_version_available")
}

@Suppress("MagicNumber")
enum class SdkBackendRequestsProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    TYPE_FROM_BACKEND_REQUEST(TYPE, SDK_CALL_BACKEND_REQUEST, 200, true),
    TYPE_FROM_BACKEND_RESPONSE(TYPE, SDK_CALL_BACKEND_RESPONSE, 201, true),
    TYPE_FROM_BACKEND_MAPPING_FAILURE(TYPE, SDK_CALL_BACKEND_MAPPING_FAILURE, 202, true),
    TYPE_FROM_BACKEND_ERROR(TYPE, SDK_CALL_BACKEND_ERROR, 203, true),

    URL_FROM_BACKEND_REQUEST(URL, SDK_CALL_BACKEND_REQUEST, 210, true),
    URL_FROM_BACKEND_ERROR(URL, SDK_CALL_BACKEND_ERROR, 211, true),

    METHOD_FROM_BACKEND_REQUEST(METHOD, SDK_CALL_BACKEND_REQUEST, 220, true),

    HEADERS_FROM_BACKEND_REQUEST(HEADERS, SDK_CALL_BACKEND_REQUEST, 230, true),

    PATHS_FROM_BACKEND_REQUEST(PATHS, SDK_CALL_BACKEND_REQUEST, 240, true),

    QUERIES_FROM_BACKEND_REQUEST(QUERIES, SDK_CALL_BACKEND_REQUEST, 250, true),

    BODY_FROM_BACKEND_REQUEST(BODY, SDK_CALL_BACKEND_REQUEST, 260, true),

    RESPONSE_CODE_FROM_BACKEND_RESPONSE(RESPONSE_CODE, SDK_CALL_BACKEND_RESPONSE, 270, true),

    RESPONSE_MESSAGE_FROM_BACKEND_RESPONSE(RESPONSE_MESSAGE, SDK_CALL_BACKEND_REQUEST, 280, true),
    RESPONSE_MESSAGE_FROM_BACKEND_ERROR(RESPONSE_MESSAGE, SDK_CALL_BACKEND_ERROR, 281, true),
    RESPONSE_MESSAGE_FROM_BACKEND_MAPPING_FAILURE(RESPONSE_MESSAGE, SDK_CALL_BACKEND_MAPPING_FAILURE, 282, true),

    ERROR_MESSAGE_FROM_BACKEND_MAPPING_FAILURE(ERROR_MESSAGE, SDK_CALL_BACKEND_MAPPING_FAILURE, 290, true),
    ERROR_MESSAGE_FROM_BACKEND_RESPONSE(ERROR_MESSAGE, SDK_CALL_BACKEND_RESPONSE, 291, true),

    NETWORK_SPEED_FROM_BACKEND_ERROR(NETWORK_SPEED, SDK_CALL_BACKEND_ERROR, 300, true),
}
