package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_APP_INSTALLATION_TRIGGER
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_ATTRIBUTION_RESULT
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_ATTRIBUTION_RETRY_ATTEMPT
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_PAYFLOW_RESULT
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_SERVICE_CONNECTED
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents.SDK_SERVICE_CONNECTION_FAILED
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.APP_PACKAGE_NAME
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.GUEST_ID
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.MESSAGE
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.METHOD
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.OEMID
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.PAYMENT_FLOW_LIST
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.SERVICE
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.STATE
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.UTM_CAMPAIGN
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.UTM_CONTENT
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.UTM_MEDIUM
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.UTM_SOURCE
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels.UTM_TERM
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkInitializationEvents {
    class SdkStartConnection :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_START_CONNECTION,
            mutableMapOf(),
            SDK_INITIALIZATION_FLOW,
            1
        )

    class SdkFinishConnection :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_FINISH_CONNECTION,
            mutableMapOf(),
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

    class SdkAttributionRetryAttempt(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_ATTRIBUTION_RETRY_ATTEMPT,
            data,
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

    const val SDK_INITIALIZATION_FLOW = "initialization"
}

object SdkInitializationLabels {
    const val OEMID = "oemid"
    const val GUEST_ID = "guest_id"
    const val UTM_SOURCE = "utm_source"
    const val UTM_MEDIUM = "utm_medium"
    const val UTM_CAMPAIGN = "utm_campaign"
    const val UTM_TERM = "utm_term"
    const val UTM_CONTENT = "utm_content"

    const val PAYMENT_FLOW_LIST = "payment_flow_list"

    const val SERVICE = "service"
    const val SERVICE_INSTALL_WALLET_DIALOG = "install_wallet_dialog"
    const val METHOD = "method"
    const val METHOD_BINDING = "binding"
    const val METHOD_URI = "uri"

    const val APP_PACKAGE_NAME = "app_package_name"
    const val STATE = "state"
    const val INSTALLED = "installed"
    const val REMOVED = "removed"

    const val MESSAGE = "message"
}

@Suppress("MagicNumber")
enum class SdkInitializationProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    OEMID_FROM_ATTRIBUTION_RESULT(OEMID, SDK_ATTRIBUTION_RESULT, 700),

    GUEST_ID_FROM_ATTRIBUTION_RESULT(GUEST_ID, SDK_ATTRIBUTION_RESULT, 710),

    UTM_SOURCE_FROM_ATTRIBUTION_RESULT(UTM_SOURCE, SDK_ATTRIBUTION_RESULT, 720),

    UTM_MEDIUM_FROM_ATTRIBUTION_RESULT(UTM_MEDIUM, SDK_ATTRIBUTION_RESULT, 730),

    UTM_CAMPAIGN_FROM_ATTRIBUTION_RESULT(UTM_CAMPAIGN, SDK_ATTRIBUTION_RESULT, 740),

    UTM_TERM_FROM_ATTRIBUTION_RESULT(UTM_TERM, SDK_ATTRIBUTION_RESULT, 750),

    UTM_CONTENT_FROM_ATTRIBUTION_RESULT(UTM_CONTENT, SDK_ATTRIBUTION_RESULT, 760),

    PAYMENT_FLOW_LIST_FROM_PAYFLOW_RESULT(PAYMENT_FLOW_LIST, SDK_PAYFLOW_RESULT, 770),

    SERVICE_FROM_SERVICE_CONNECTED(SERVICE, SDK_SERVICE_CONNECTED, 780),
    SERVICE_FROM_SERVICE_CONNECTION_FAILED(SERVICE, SDK_SERVICE_CONNECTION_FAILED, 780),

    METHOD_FROM_SERVICE_CONNECTED(METHOD, SDK_SERVICE_CONNECTED, 790),
    METHOD_FROM_SERVICE_CONNECTION_FAILED(METHOD, SDK_SERVICE_CONNECTION_FAILED, 790),

    APP_PACKAGE_NAME_FROM_APP_INSTALLATION_TRIGGER(APP_PACKAGE_NAME, SDK_APP_INSTALLATION_TRIGGER, 800),

    STATE_FROM_APP_INSTALLATION_TRIGGER(STATE, SDK_APP_INSTALLATION_TRIGGER, 810),

    MESSAGE_FROM_ATTRIBUTION_RETRY_ATTEMPT(MESSAGE, SDK_ATTRIBUTION_RETRY_ATTEMPT, 820),
}
