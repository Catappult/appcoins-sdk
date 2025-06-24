package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents.SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents.SDK_SERVICE_CONNECTION_EXCEPTION
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents.SDK_UNEXPECTED_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels.API_KEY
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels.DATA
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels.SIGNED_DATA
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels.STEP
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels.TYPE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

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

    const val GENERAL_FAILURE_FLOW = "general_failure"
}

object SdkGeneralFailureLabels {
    const val STEP = "step"
    const val SIGNED_DATA = "signed_data"
    const val API_KEY = "api_key"
    const val TYPE = "type"
    const val DATA = "data"
}

enum class SdkGeneralFailureStep(val type: String) {
    GET_PURCHASES("get_purchases"),
    QUERY_SKU_DETAILS("query_sku_details"),
    CONSUME("consume"),
    START_PURCHASE("start_purchase"),
    IS_FEATURE_SUPPORTED("is_feature_supported"),
}

@Suppress("MagicNumber")
enum class SdkGeneralFailureProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    STEP_FROM_SERVICE_CONNECTION_EXCEPTION(STEP, SDK_SERVICE_CONNECTION_EXCEPTION, 500, true),

    SIGNED_DATA_FROM_SIGNATURE_VERIFICATION_FAILURE(
        SIGNED_DATA,
        SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE,
        100,
        true
    ),

    API_KEY_FROM_SIGNATURE_VERIFICATION_FAILURE(API_KEY, SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE, 510, true),

    TYPE_FROM_UNEXPECTED_FAILURE(TYPE, SDK_UNEXPECTED_FAILURE, 520, true),

    DATA_FROM_UNEXPECTED_FAILURE(DATA, SDK_UNEXPECTED_FAILURE, 530, true),
}
