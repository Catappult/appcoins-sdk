package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_OPEN_DEEPLINK
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_START
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_UPDATE_CLOSE_BEHAVIOR
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.ALLOW
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.CONFIG
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.DEEPLINK
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.EXCEPTION
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.RESULT
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels.URL
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkWebPaymentFlowEvents {

    class SdkWebPaymentStart(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_START,
            data,
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentFailureToObtainUrl :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WEB_PAYMENT_FAILURE_TO_OBTAIN_URL,
            mutableMapOf(),
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentFailureToOpenDeeplink(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK,
            data,
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentErrorProcessingPurchaseResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT,
            data,
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentPurchaseResultEmpty :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WEB_PAYMENT_PURCHASE_RESULT_EMPTY,
            mutableMapOf(),
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentOpenDeeplink(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_OPEN_DEEPLINK,
            data,
            WEB_PAYMENT_FLOW,
            3
        )

    class SdkWebPaymentLaunchExternalPayment(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT,
            data,
            WEB_PAYMENT_FLOW,
            3
        )

    class SdkWebPaymentAllowExternalApps(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS,
            data,
            WEB_PAYMENT_FLOW,
            4
        )

    class SdkWebPaymentUpdateCloseBehavior(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_UPDATE_CLOSE_BEHAVIOR,
            data,
            WEB_PAYMENT_FLOW,
            4
        )

    class SdkWebPaymentExternalPaymentResult :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_EXTERNAL_PAYMENT_RESULT,
            mutableMapOf(),
            WEB_PAYMENT_FLOW,
            2
        )

    class SdkWebPaymentExecuteExternalDeeplink(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK,
            data,
            WEB_PAYMENT_FLOW,
            3
        )

    class SdkWebPaymentWalletPaymentResult :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_WALLET_PAYMENT_RESULT,
            mutableMapOf(),
            WEB_PAYMENT_FLOW,
            2
        )

    const val SDK_WEB_PAYMENT_START = "sdk_web_payment_start"
    const val SDK_WEB_PAYMENT_FAILURE_TO_OBTAIN_URL = "sdk_web_payment_failure_to_obtain_url"
    const val SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK = "sdk_web_payment_failure_to_open_deeplink"
    const val SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT = "sdk_web_payment_error_processing_purchase_result"
    const val SDK_WEB_PAYMENT_PURCHASE_RESULT_EMPTY = "sdk_web_payment_purchase_result_empty"
    const val SDK_WEB_PAYMENT_OPEN_DEEPLINK = "sdk_web_payment_open_deeplink"
    const val SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT = "sdk_web_payment_launch_external_payment"
    const val SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS = "sdk_web_payment_allow_external_apps"
    const val SDK_WEB_PAYMENT_UPDATE_CLOSE_BEHAVIOR = "sdk_web_payment_update_close_behavior"
    const val SDK_WEB_PAYMENT_EXTERNAL_PAYMENT_RESULT = "sdk_web_payment_external_payment_result"
    const val SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK = "sdk_web_payment_execute_external_deeplink"
    const val SDK_WEB_PAYMENT_WALLET_PAYMENT_RESULT = "sdk_web_payment_wallet_payment_result"

    const val WEB_PAYMENT_FLOW = "web_payment_flow"
}

object SdkWebPaymentFlowLabels {
    const val URL = "url"
    const val DEEPLINK = "deeplink"
    const val EXCEPTION = "exception"
    const val RESULT = "result"
    const val ALLOW = "allow"
    const val CONFIG = "config"
}

@Suppress("MagicNumber")
enum class SdkWebPaymentFlowProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    URL_FROM_START(URL, SDK_WEB_PAYMENT_START, 1600),
    URL_FROM_LAUNCH_EXTERNAL_PAYMENT(URL, SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT, 1601),

    DEEPLINK_FROM_OPEN_DEEPLINK(DEEPLINK, SDK_WEB_PAYMENT_OPEN_DEEPLINK, 1610),
    DEEPLINK_FROM_FAILURE_TO_OPEN_DEEPLINK(DEEPLINK, SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK, 1611),
    DEEPLINK_FROM_EXECUTE_EXTERNAL_DEEPLINK(DEEPLINK, SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK, 1612),

    EXCEPTION_FROM_FAILURE_TO_OPEN_DEEPLINK(EXCEPTION, SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK, 1620),

    RESULT_FROM_ERROR_PROCESSING_PURCHASE_RESULT(RESULT, SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT, 1630),

    ALLOW_FROM_ALLOW_EXTERNAL_APPS(ALLOW, SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS, 1640),

    CONFIG_FROM_UPDATE_CLOSE_BEHAVIOR(CONFIG, SDK_WEB_PAYMENT_UPDATE_CLOSE_BEHAVIOR, 1650),
}
