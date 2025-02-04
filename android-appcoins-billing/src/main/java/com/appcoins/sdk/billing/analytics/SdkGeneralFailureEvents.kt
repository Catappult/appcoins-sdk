package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkGeneralFailureEvents {

    class SdkServiceConnectionException(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_SERVICE_CONNECTION_EXCEPTION,
            data,
            GENERAL_FAILURE_FLOW,
            1
        )

    class SdkPurchaseSignatureVerificationFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE,
            data,
            GENERAL_FAILURE_FLOW,
            1
        )

    class SdkUnexpectedFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_UNEXPECTED_FAILURE,
            data,
            GENERAL_FAILURE_FLOW,
            1
        )

    const val SDK_SERVICE_CONNECTION_EXCEPTION = "sdk_service_connection_exception"
    const val SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE = "sdk_purchase_signature_verification_failure"
    const val SDK_UNEXPECTED_FAILURE = "sdk_unexpected_failure"

    private const val GENERAL_FAILURE_FLOW = "general_failure"
}

object SdkGeneralFailureLabels {
    const val STEP = "step"
    const val PURCHASE_TOKEN = "purchase_token"
    const val API_KEY = "api_key"
    const val TYPE = "type"
    const val DATA = "data"
}

enum class SdkGeneralFailureStep(val type: String) {
    GET_PURCHASES("get_purchases")
}
