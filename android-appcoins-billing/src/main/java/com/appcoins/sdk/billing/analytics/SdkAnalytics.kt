package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import java.io.Serializable

class SdkAnalytics(private val analyticsManager: AnalyticsManager) : Serializable {

  companion object {
    private const val EVENT_CONTEXT = "AnalyticsSDK"
  }

  fun sendPurchaseIntentEvent() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun sendOpenWalletAttemptEvent(fail: Boolean? = false) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.OPEN_INTENT_ACTION] = if (fail == true) "error" else "success"

    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_OPEN_WALLET_ATTEMPT,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun walletInstallImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_WALLET_INSTALL_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun walletInstallClick(installAction: String) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[AnalyticsLabels.INSTALL_ACTION] = installAction

    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_WALLET_INSTALL_CLICK,
      AnalyticsManager.Action.CLICK,
      EVENT_CONTEXT
    )
  }

  fun downloadWalletAptoideImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun downloadWalletFallbackImpression() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }

  fun installWalletAptoideSuccess() {
    val eventData: Map<String, Any> = emptyMap()
    analyticsManager.logEvent(
      eventData,
      AnalyticsEvents.SDK_INSTALL_WALLET_FEEDBACK,
      AnalyticsManager.Action.IMPRESSION,
      EVENT_CONTEXT
    )
  }
}

class PayAsAGuestAnalytics(private val analyticsManager: AnalyticsManager) {
  companion object {
    const val EVENT_CONTEXT = "PayAsAGuestSDK"
  }
}