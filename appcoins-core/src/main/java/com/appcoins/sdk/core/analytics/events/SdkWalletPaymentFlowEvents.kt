package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

object SdkWalletPaymentFlowEvents {

    class SdkWalletPaymentStart :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_PAYMENT_START,
            mutableMapOf(),
            WALLET_PAYMENT_FLOW,
            1
        )

    class SdkWalletPaymentEmptyData :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WALLET_PAYMENT_EMPTY_DATA,
            mutableMapOf(),
            WALLET_PAYMENT_FLOW,
            1
        )

    const val SDK_WALLET_PAYMENT_START = "sdk_wallet_payment_start"
    const val SDK_WALLET_PAYMENT_EMPTY_DATA = "sdk_wallet_payment_empty_data"

    const val WALLET_PAYMENT_FLOW = "wallet_payment_flow"
}
