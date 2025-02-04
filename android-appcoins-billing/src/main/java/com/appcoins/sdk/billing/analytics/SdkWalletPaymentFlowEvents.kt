package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkWalletPaymentFlowEvents {

    class SdkWalletPaymentStart(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_PAYMENT_START,
            data,
            WALLET_PAYMENT_FLOW,
            1
        )

    class SdkWalletPaymentEmptyData(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WALLET_PAYMENT_EMPTY_DATA,
            data,
            WALLET_PAYMENT_FLOW,
            1
        )

    const val SDK_WALLET_PAYMENT_START = "sdk_wallet_payment_start"
    const val SDK_WALLET_PAYMENT_EMPTY_DATA = "sdk_wallet_payment_empty_data"

    private const val WALLET_PAYMENT_FLOW = "wallet_payment_flow"
}
