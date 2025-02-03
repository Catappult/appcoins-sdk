package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

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

    private const val SDK_CALL_BACKEND_REQUEST = "sdk_call_backend_request"
    private const val SDK_CALL_BACKEND_RESPONSE = "sdk_call_backend_response"
    private const val SDK_CALL_BACKEND_MAPPING_FAILURE = "sdk_call_backend_mapping_failure"

    private const val BACKEND_REQUEST_FLOW = "backend_request"
}