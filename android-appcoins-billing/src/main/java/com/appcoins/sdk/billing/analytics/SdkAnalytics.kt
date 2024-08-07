package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import java.io.Serializable

class SdkAnalytics(private val analyticsManager: AnalyticsManager) : Serializable {

  companion object {
    private const val EVENT_CONTEXT = "AnalyticsSDK"
  }

  fun sendStartConnetionEvent() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      SdkAnalyticsEvents.SDK_START_CONNECTION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendPurchaseIntentEvent(skuDetails: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.SKU_NAME] = skuDetails

    analyticsManager.logEvent(
      eventData,
      SdkAnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun sendPurchaseViaWebEvent(skuDetails: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.SKU_NAME] = skuDetails

    analyticsManager.logEvent(
      eventData,
      SdkAnalyticsEvents.SDK_WEB_PAYMENT_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBackendPayflowEvent(responseCode: Int?, responseMessage: String?) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
    eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BACKEND_PAYFLOW,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBackendWebPaymentUrlEvent(responseCode: Int?, responseMessage: String?) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
    eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BACKEND_WEB_PAYMENT_URL,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBackendAttributionEvent(responseCode: Int?, responseMessage: String?) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
    eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BACKEND_ATTRIBUTION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBackendAppVersionEvent(responseCode: Int?, responseMessage: String?) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
    eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BACKEND_APP_VERSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBackendStoreLinkEvent(responseCode: Int?, responseMessage: String?) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BACKEND_RESPONSE_CODE] = responseCode?.toString() ?: ""
    eventData[AnalyticsLabels.BACKEND_RESPONSE_MESSAGE] = responseMessage ?: ""

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BACKEND_STORE_LINK,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBindServiceAttemptEvent(payflowMethod: String, priority: Int) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BIND_SERVICE_METHOD] = payflowMethod
    eventData[AnalyticsLabels.BIND_SERVICE_PRIORITY] = priority

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_ATTEMPT,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun sendCallBindServiceFailEvent(payflowMethod: String, priority: Int) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.BIND_SERVICE_METHOD] = payflowMethod
    eventData[AnalyticsLabels.BIND_SERVICE_PRIORITY] = priority

    analyticsManager.logEvent(
      eventData,
      SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_FAIL,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun walletInstallImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      SdkInstallFlowEvents.SDK_WALLET_INSTALL_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun walletInstallClick(installAction: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.INSTALL_ACTION] = installAction

    analyticsManager.logEvent(
      eventData,
      SdkInstallFlowEvents.SDK_WALLET_INSTALL_CLICK,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun downloadWalletAptoideImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun downloadWalletFallbackImpression(storeType: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.STORE_TYPE] = storeType

    analyticsManager.logEvent(
      eventData,
      SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun installWalletAptoideSuccess() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      SdkInstallFlowEvents.SDK_INSTALL_WALLET_FEEDBACK,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun appUpdateDeeplinkImpression(deeplink: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.APP_UPDATE_DEEPLINK] = deeplink

    analyticsManager.logEvent(
      eventData,
      SdkUpdateFlowEvents.SDK_APP_UPDATE_DEEPLINK_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun appUpdateImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      SdkUpdateFlowEvents.SDK_APP_UPDATE_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun appUpdateClick(updateAction: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.APP_UPDATE_ACTION] = updateAction

    analyticsManager.logEvent(
      eventData,
      SdkUpdateFlowEvents.SDK_APP_UPDATE_CLICK,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun sendPurchaseStatusEvent(paymentStatus: String, responseMessage: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.PAYMENT_STATUS] = paymentStatus
    eventData[AnalyticsLabels.PAYMENT_STATUS_MESSAGE] = responseMessage

    analyticsManager.logEvent(
      eventData,
      SdkAnalyticsEvents.SDK_IAP_PAYMENT_STATUS_FEEDBACK,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun sendWebPaymentUrlNotGeneratedEvent() =
    sendUnexpectedFailureEvent(SdkAnalyticsFailureLabels.SDK_WEB_PAYMENT_URL_GENERATION_FAILED)

  fun sendBackendGuestUidGenerationFailedEvent() =
    sendUnexpectedFailureEvent(SdkAnalyticsFailureLabels.SDK_BACKEND_GUEST_UID_GENERATION_FAILED)

  private fun sendUnexpectedFailureEvent(failureType: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.FAILURE_TYPE] = failureType

    analyticsManager.logEvent(
      eventData,
      SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
      AnalyticsManager.Action.ERROR,
      EVENT_CONTEXT
    )
  }
}

class PayAsAGuestAnalytics(private val analyticsManager: AnalyticsManager) {
  companion object {
    const val EVENT_CONTEXT = "PayAsAGuestSDK"
  }
}