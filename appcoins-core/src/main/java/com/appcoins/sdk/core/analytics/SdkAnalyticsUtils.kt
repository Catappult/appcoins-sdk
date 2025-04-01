package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents.CONSUME_PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents.INSTALL_WALLET_DIALOG_FLOW
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents.WALLET_PAYMENT_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.WEB_PAYMENT_FLOW
import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel
import com.appcoins.sdk.core.logger.Logger.logInfo

object SdkAnalyticsUtils {
    var analyticsFlowSeverityLevels: List<AnalyticsFlowSeverityLevel>? = null
        set(value) {
            field = value
            logInfo(value.toString())
        }
    val defaultAnalyticsFlowSeverityLevels =
        listOf(
            AnalyticsFlowSeverityLevel(flow = CONSUME_PURCHASE_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = PURCHASE_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = WALLET_PAYMENT_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = WEB_PAYMENT_FLOW, 2),
            AnalyticsFlowSeverityLevel(flow = INSTALL_WALLET_DIALOG_FLOW, 1),
        )
    var isAnalyticsSetupFromPayflowFinalized: Boolean = false
        set(value) {
            field = value
            if (value) {
                sdkAnalytics.sendEventsOnQueue()
            }
        }
    val sdkAnalytics: SdkAnalytics by lazy { SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager()) }
    var isIndicativeEventLoggerInitialized = false
}
