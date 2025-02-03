package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkInstallWalletDialogEvents {

    class SdkInstallWalletDialog :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG_ACTION,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    class SdkInstallWalletDialogAction(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG,
            data,
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    class SdkInstallWalletDialogVanillaImpression :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG_VANILLA_IMPRESSION,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            3
        )

    class SdkInstallWalletDialogFallbackImpression :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG_FALLBACK_IMPRESSION,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            3
        )

    class SdkInstallWalletDialogSuccess :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG_SUCCESS,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    private const val SDK_WALLET_INSTALL_DIALOG = "sdk_wallet_install_dialog"
    private const val SDK_WALLET_INSTALL_DIALOG_ACTION = "sdk_wallet_install_action"
    private const val SDK_WALLET_INSTALL_DIALOG_VANILLA_IMPRESSION = "sdk_download_wallet_vanilla_impression"
    private const val SDK_WALLET_INSTALL_DIALOG_FALLBACK_IMPRESSION = "sdk_download_wallet_fallback_impression"
    private const val SDK_WALLET_INSTALL_DIALOG_SUCCESS = "sdk_wallet_install_success"

    private const val INSTALL_WALLET_DIALOG_FLOW = "install_wallet_dialog"
}
