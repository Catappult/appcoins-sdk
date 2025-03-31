package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

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

    private const val BACKEND_REQUEST_FLOW = "backend_request"
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
