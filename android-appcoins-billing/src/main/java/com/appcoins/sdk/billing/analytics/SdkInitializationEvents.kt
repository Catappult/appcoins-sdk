package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkInitializationEvents {
    class SdkStartConnection :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_START_CONNECTION,
            mutableMapOf(),
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkFinishConnection(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_FINISH_CONNECTION,
            data,
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkServiceConnected(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_SERVICE_CONNECTED,
            data,
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkServiceConnectionFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_SERVICE_CONNECTION_FAILED,
            data,
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkAttributionRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_REQUEST,
            data,
            SDK_INITIALIZATION_FLOW,
            3
        )

    class SdkAttributionResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_RESULT,
            data,
            SDK_INITIALIZATION_FLOW,
            2
        )

    class SdkAttributionRequestFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_ATTRIBUTION_REQUEST_FAILURE,
            data,
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkAttributionRetryAttempt(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_RETRY_ATTEMPT,
            data,
            SDK_INITIALIZATION_FLOW,
            3
        )

    class SdkPayflowRequest(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PAYFLOW_REQUEST,
            data,
            SDK_INITIALIZATION_FLOW,
            3
        )

    class SdkPayflowResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PAYFLOW_RESULT,
            data,
            SDK_INITIALIZATION_FLOW,
            2
        )

    class SdkAppInstallationTrigger(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_APP_INSTALLATION_TRIGGER,
            data,
            SDK_INITIALIZATION_FLOW,
            3
        )

    const val SDK_START_CONNECTION = "sdk_start_connection"
    const val SDK_FINISH_CONNECTION = "sdk_finish_connection"
    const val SDK_ATTRIBUTION_REQUEST = "sdk_attribution_request"
    const val SDK_ATTRIBUTION_RESULT = "sdk_attribution_result"
    const val SDK_ATTRIBUTION_REQUEST_FAILURE = "sdk_attribution_request_failure"
    const val SDK_ATTRIBUTION_RETRY_ATTEMPT = "sdk_attribution_retry_attempt"
    const val SDK_PAYFLOW_REQUEST = "sdk_payflow_request"
    const val SDK_PAYFLOW_RESULT = "sdk_payflow_result"
    const val SDK_SERVICE_CONNECTED = "sdk_service_connected"
    const val SDK_SERVICE_CONNECTION_FAILED = "sdk_service_connection_failed"
    const val SDK_APP_INSTALLATION_TRIGGER = "sdk_app_installation_trigger"

    const val SDK_INITIALIZATION_FLOW = "initialization"
}
