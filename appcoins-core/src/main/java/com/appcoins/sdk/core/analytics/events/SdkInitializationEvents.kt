package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

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

    class SdkAttributionRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_REQUEST,
            mutableMapOf(),
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

    class SdkAttributionRequestFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_ATTRIBUTION_REQUEST_FAILURE,
            mutableMapOf(),
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkAttributionRetryAttempt :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_RETRY_ATTEMPT,
            mutableMapOf(),
            SDK_INITIALIZATION_FLOW,
            3
        )

    class SdkPayflowRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PAYFLOW_REQUEST,
            mutableMapOf(),
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

    private const val SDK_INITIALIZATION_FLOW = "initialization"
}

object SdkInitializationLabels {
    const val OEMID = "oemid"
    const val GUEST_ID = "guest_id"
    const val UTM_SOURCE = "utm_source"
    const val UTM_MEDIUM = "utm_medium"
    const val UTM_CAMPAIGN = "utm_campaign"
    const val UTM_TERM = "utm_term"
    const val UTM_CONTENT = "utm_content"

    const val REASON = "reason"

    const val PAYMENT_FLOW_LIST = "payment_flow_list"

    const val SERVICE = "service"
    const val METHOD = "method"
    const val METHOD_BINDING = "binding"
    const val METHOD_URI = "uri"

    const val APP_PACKAGE_NAME = "app_package_name"
    const val STATE = "state"
    const val INSTALLED = "installed"
    const val REMOVED = "removed"
}

enum class SdkInitializationService(val type: String) {
    WEB("web"),
    APTOIDE_WALLET("aptoide_wallet"),
    GAMES_HUB("games_hub"),
    APTOIDE_GAMES("aptoide_games"),
    UNAVAILABLE_SERVICE("unavailable_service"),
}
