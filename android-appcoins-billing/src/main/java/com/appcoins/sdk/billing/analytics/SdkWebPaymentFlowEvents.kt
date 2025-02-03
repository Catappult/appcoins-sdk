package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkWebPaymentFlowEvents {

    class SdkWebPaymentFailureToOpenDeeplink(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK,
            data,
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentErrorProcessingPurchaseResult(data: HashMap<String, String>) :
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
            hashMapOf(),
            WEB_PAYMENT_FLOW,
            1
        )

    class SdkWebPaymentOpenDeeplink(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_OPEN_DEEPLINK,
            data,
            WEB_PAYMENT_FLOW,
            3
        )

    class SdkWebPaymentLaunchExternalPayment(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT,
            data,
            WEB_PAYMENT_FLOW,
            3
        )

    class SdkWebPaymentAllowExternalApps(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS,
            data,
            WEB_PAYMENT_FLOW,
            4
        )

    class SdkWebPaymentExternalPaymentResult :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WEB_PAYMENT_EXTERNAL_PAYMENT_RESULT,
            hashMapOf(),
            WEB_PAYMENT_FLOW,
            2
        )

    class SdkWebPaymentExecuteExternalDeeplink(data: HashMap<String, String>) :
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
            hashMapOf(),
            WEB_PAYMENT_FLOW,
            2
        )

    private const val SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK = "sdk_web_payment_failure_to_open_deeplink"
    private const val SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT =
        "sdk_web_payment_error_processing_purchase_result"
    private const val SDK_WEB_PAYMENT_PURCHASE_RESULT_EMPTY = "sdk_web_payment_purchase_result_empty"
    private const val SDK_WEB_PAYMENT_OPEN_DEEPLINK = "sdk_web_payment_open_deeplink"
    private const val SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT = "sdk_web_payment_launch_external_payment"
    private const val SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS = "sdk_web_payment_allow_external_apps"
    private const val SDK_WEB_PAYMENT_EXTERNAL_PAYMENT_RESULT = "sdk_web_payment_external_payment_result"
    private const val SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK = "sdk_web_payment_execute_external_deeplink"
    private const val SDK_WEB_PAYMENT_WALLET_PAYMENT_RESULT = "sdk_web_payment_wallet_payment_result"

    private const val WEB_PAYMENT_FLOW = "web_payment_flow"
}