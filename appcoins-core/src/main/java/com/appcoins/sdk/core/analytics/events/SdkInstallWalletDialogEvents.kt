package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

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

    class SdkInstallWalletDialogFallbackImpression(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_WALLET_INSTALL_DIALOG_FALLBACK_IMPRESSION,
            data,
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

    const val SDK_WALLET_INSTALL_DIALOG = "sdk_wallet_install_dialog"
    const val SDK_WALLET_INSTALL_DIALOG_ACTION = "sdk_wallet_install_action"
    const val SDK_WALLET_INSTALL_DIALOG_VANILLA_IMPRESSION = "sdk_download_wallet_vanilla_impression"
    const val SDK_WALLET_INSTALL_DIALOG_FALLBACK_IMPRESSION = "sdk_download_wallet_fallback_impression"
    const val SDK_WALLET_INSTALL_DIALOG_SUCCESS = "sdk_wallet_install_success"

    private const val INSTALL_WALLET_DIALOG_FLOW = "install_wallet_dialog"
}

object SdkInstallWalletDialogLabels {
    const val WALLET_INSTALL_ACTION = "wallet_install_action"
    const val BACK_BUTTON = "back_button"
    const val CANCEL = "cancel"
    const val INSTALL = "install"
    const val SOURCE = "source"
}
