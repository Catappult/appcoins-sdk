package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkWalletPaymentFlowEvents {

    class SdkWalletPaymentEmptyData(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_WALLET_PAYMENT_EMPTY_DATA,
            data,
            WALLET_PAYMENT_FLOW,
            1
        )

    private const val SDK_WALLET_PAYMENT_EMPTY_DATA = "sdk_wallet_payment_empty_data"

    private const val WALLET_PAYMENT_FLOW = "wallet_payment_flow"
}