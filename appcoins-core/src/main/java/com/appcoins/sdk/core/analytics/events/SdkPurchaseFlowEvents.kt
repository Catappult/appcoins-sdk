package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.SDK_LAUNCH_PURCHASE
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.SDK_PURCHASE_RESULT
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.DEVELOPER_PAYLOAD
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.FAILURE_MESSAGE
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.FREE_TRIAL
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.OBFUSCATED_ACCOUNT_ID
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.ORDER_REFERENCE
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.ORIGIN
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.PURCHASE_TOKEN
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.RESPONSE_CODE
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.SKU
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels.SKU_TYPE
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkPurchaseFlowEvents {

    class SdkLaunchPurchase(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.CLICK,
            SDK_LAUNCH_PURCHASE,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkPurchaseResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_PURCHASE_RESULT,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkLaunchPurchaseTypeNotSupportedFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE,
            data,
            PURCHASE_FLOW,
            1
        )

    class SdkLaunchPurchaseMainThreadFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE,
            mutableMapOf(),
            PURCHASE_FLOW,
            1
        )

    const val SDK_LAUNCH_PURCHASE = "sdk_launch_purchase"
    const val SDK_PURCHASE_RESULT = "sdk_purchase_result"
    const val SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE = "sdk_launch_purchase_type_not_supported_failure"
    const val SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE = "sdk_launch_purchase_main_thread_failure"

    const val PURCHASE_FLOW = "purchase_flow"
}

object SdkPurchaseFlowLabels {
    const val SKU = "sku"
    const val SKU_TYPE = "sku_type"
    const val DEVELOPER_PAYLOAD = "developer_payload"
    const val ORDER_REFERENCE = "order_reference"
    const val ORIGIN = "origin"
    const val OBFUSCATED_ACCOUNT_ID = "obfuscated_account_id"
    const val FREE_TRIAL = "free_trial"

    const val FAILURE_MESSAGE = "failure_message"

    const val RESPONSE_CODE = "response_code"
    const val PURCHASE_TOKEN = "purchase_token"
}

@Suppress("MagicNumber")
enum class SdkPurchaseFlowProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    SKU_FROM_LAUNCH_PURCHASE(SKU, SDK_LAUNCH_PURCHASE, 1300),
    SKU_FROM_PURCHASE_RESULT(SKU, SDK_PURCHASE_RESULT, 1301),

    SKU_TYPE_FROM_LAUNCH_PURCHASE(SKU_TYPE, SDK_LAUNCH_PURCHASE, 1310),
    SKU_TYPE_FROM_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE(
        SKU_TYPE,
        SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE,
        1311
    ),

    DEVELOPER_PAYLOAD_FROM_LAUNCH_PURCHASE(DEVELOPER_PAYLOAD, SDK_LAUNCH_PURCHASE, 1320),

    ORDER_REFERENCE_FROM_LAUNCH_PURCHASE(ORDER_REFERENCE, SDK_LAUNCH_PURCHASE, 1330),

    ORIGIN_FROM_LAUNCH_PURCHASE(ORIGIN, SDK_LAUNCH_PURCHASE, 1340),

    OBFUSCATED_ACCOUNT_ID_FROM_LAUNCH_PURCHASE(OBFUSCATED_ACCOUNT_ID, SDK_LAUNCH_PURCHASE, 1350),

    FREE_TRIAL_FROM_LAUNCH_PURCHASE(FREE_TRIAL, SDK_LAUNCH_PURCHASE, 1360),

    FAILURE_MESSAGE_FROM_PURCHASE_RESULT(FAILURE_MESSAGE, SDK_PURCHASE_RESULT, 1370),

    RESPONSE_CODE_FROM_PURCHASE_RESULT(RESPONSE_CODE, SDK_PURCHASE_RESULT, 1380),

    PURCHASE_TOKEN_FROM_PURCHASE_RESULT(PURCHASE_TOKEN, SDK_PURCHASE_RESULT, 1390),
}
