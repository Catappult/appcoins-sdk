package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager.Action
import java.io.Serializable

@Suppress("complexity:TooManyFunctions")
class SdkAnalytics(private val analyticsManager: AnalyticsManager) : Serializable {

    companion object {
        private const val EVENT_CONTEXT = "AnalyticsSDK"
    }

    fun sendStartConnetionEvent() {
        logEvent(
            eventName = SdkAnalyticsEvents.SDK_START_CONNECTION,
            action = Action.IMPRESSION
        )
    }

    fun sendPurchaseIntentEvent(skuDetails: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.SKU_NAME] = skuDetails

        logEvent(
            eventData,
            SdkAnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START,
            Action.CLICK
        )
    }

    fun sendPurchaseViaWebEvent(skuDetails: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.SKU_NAME] = skuDetails

        logEvent(
            eventData,
            SdkAnalyticsEvents.SDK_WEB_PAYMENT_IMPRESSION,
            Action.IMPRESSION
        )
    }

    fun sendCallBackendPayflowEvent(
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.BACKEND_ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BACKEND_PAYFLOW,
            Action.IMPRESSION
        )
    }

    fun sendCallBackendWebPaymentUrlEvent(
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.BACKEND_ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BACKEND_WEB_PAYMENT_URL,
            Action.IMPRESSION
        )
    }

    fun sendCallBackendAttributionEvent(
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.BACKEND_ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BACKEND_ATTRIBUTION,
            Action.IMPRESSION
        )
    }

    fun sendCallBackendAppVersionEvent(
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.BACKEND_ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BACKEND_APP_VERSION,
            Action.IMPRESSION
        )
    }

    fun sendCallBackendStoreLinkEvent(
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.BACKEND_ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BACKEND_STORE_LINK,
            Action.IMPRESSION
        )
    }

    fun sendCallBindServiceAttemptEvent(payflowMethod: String, priority: Int) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BIND_SERVICE_METHOD] = payflowMethod
        eventData[AnalyticsLabels.BIND_SERVICE_PRIORITY] = priority

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_ATTEMPT,
            Action.IMPRESSION
        )
    }

    fun sendCallBindServiceFailEvent(payflowMethod: String, priority: Int) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.BIND_SERVICE_METHOD] = payflowMethod
        eventData[AnalyticsLabels.BIND_SERVICE_PRIORITY] = priority

        logEvent(
            eventData,
            SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_FAIL,
            Action.IMPRESSION
        )
    }

    fun walletInstallImpression() {
        logEvent(
            eventName = SdkInstallFlowEvents.SDK_WALLET_INSTALL_IMPRESSION,
            action = Action.IMPRESSION
        )
    }

    fun walletInstallClick(installAction: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.INSTALL_ACTION] = installAction

        logEvent(
            eventData,
            SdkInstallFlowEvents.SDK_WALLET_INSTALL_CLICK,
            Action.CLICK
        )
    }

    fun downloadWalletAptoideImpression() {
        logEvent(
            eventName = SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION,
            action = Action.IMPRESSION
        )
    }

    fun downloadWalletFallbackImpression(storeType: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.STORE_TYPE] = storeType

        logEvent(
            eventData,
            SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION,
            Action.IMPRESSION
        )
    }

    fun installWalletAptoideSuccess() {
        logEvent(
            eventName = SdkInstallFlowEvents.SDK_INSTALL_WALLET_FEEDBACK,
            action = Action.IMPRESSION
        )
    }

    fun appUpdateDeeplinkImpression(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.APP_UPDATE_DEEPLINK] = deeplink

        logEvent(
            eventName = SdkUpdateFlowEvents.SDK_APP_UPDATE_DEEPLINK_IMPRESSION,
            action = Action.IMPRESSION
        )
    }

    fun appUpdateImpression() {
        logEvent(
            eventName = SdkUpdateFlowEvents.SDK_APP_UPDATE_IMPRESSION,
            action = Action.IMPRESSION
        )
    }

    fun appUpdateClick(updateAction: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.APP_UPDATE_ACTION] = updateAction

        logEvent(
            eventData,
            SdkUpdateFlowEvents.SDK_APP_UPDATE_CLICK,
            Action.CLICK
        )
    }

    fun sendPurchaseStatusEvent(paymentStatus: String, responseMessage: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.PAYMENT_STATUS] = paymentStatus
        eventData[AnalyticsLabels.PAYMENT_STATUS_MESSAGE] = responseMessage

        logEvent(
            eventData,
            SdkAnalyticsEvents.SDK_IAP_PAYMENT_STATUS_FEEDBACK,
            Action.CLICK
        )
    }

    fun sendAttributionRetryAttemptEvent(failureMessage: String) =
        sendEmptyUnexpectedFailureEvent(
            SdkAnalyticsFailureLabels.ATTRIBUTION_RETRY_ATTEMPT,
            failureMessage
        )

    fun sendUnsuccessfulWebViewResultEvent(failureMessage: String) =
        sendEmptyUnexpectedFailureEvent(
            SdkAnalyticsFailureLabels.SDK_WEB_VIEW_RESULT_FAILED,
            failureMessage
        )

    fun sendWebPaymentUrlNotGeneratedEvent() =
        sendEmptyUnexpectedFailureEvent(SdkAnalyticsFailureLabels.SDK_WEB_PAYMENT_URL_GENERATION_FAILED)

    fun sendBackendGuestUidGenerationFailedEvent() =
        sendEmptyUnexpectedFailureEvent(SdkAnalyticsFailureLabels.SDK_BACKEND_GUEST_UID_GENERATION_FAILED)

    fun sendUnsuccessfulBackendRequestEvent(endpoint: String, failureMessage: String) {
        val message = "Failed to make request to the endpoint - $endpoint. Cause - $failureMessage"
        sendEmptyUnexpectedFailureEvent(
            SdkAnalyticsFailureLabels.SDK_BACKEND_REQUEST_FAILED,
            message
        )
    }

    private fun sendEmptyUnexpectedFailureEvent(
        failureType: String,
        failureMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.FAILURE_TYPE] = failureType
        failureMessage?.let { eventData[AnalyticsLabels.FAILURE_MESSAGE] = it }

        logEvent(
            eventData,
            SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
            Action.ERROR
        )
    }

    private fun logEvent(
        eventData: Map<String, Any> = emptyMap(),
        eventName: String,
        action: Action
    ) = analyticsManager.logEvent(eventData, eventName, action, EVENT_CONTEXT)
}
