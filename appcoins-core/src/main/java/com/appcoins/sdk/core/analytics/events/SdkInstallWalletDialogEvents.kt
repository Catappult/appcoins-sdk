package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_ACTION
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_FALLBACK
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogLabels.SOURCE
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogLabels.WALLET_INSTALL_ACTION
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkInstallWalletDialogEvents {

    class SdkInstallWalletDialog :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_INSTALL_WALLET_DIALOG,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    class SdkInstallWalletDialogAction(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_INSTALL_WALLET_DIALOG_ACTION,
            data,
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    class SdkInstallWalletDialogDownloadWalletVanilla :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_VANILLA,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            3
        )

    class SdkInstallWalletDialogDownloadWalletFallback(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_FALLBACK,
            data,
            INSTALL_WALLET_DIALOG_FLOW,
            3
        )

    class SdkInstallWalletDialogSuccess :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_INSTALL_WALLET_DIALOG_SUCCESS,
            mutableMapOf(),
            INSTALL_WALLET_DIALOG_FLOW,
            1
        )

    const val SDK_INSTALL_WALLET_DIALOG = "sdk_install_wallet_dialog"
    const val SDK_INSTALL_WALLET_DIALOG_ACTION = "sdk_install_wallet_dialog_action"
    const val SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_VANILLA = "sdk_install_wallet_dialog_download_wallet_vanilla"
    const val SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_FALLBACK = "sdk_install_wallet_dialog_download_wallet_fallback"
    const val SDK_INSTALL_WALLET_DIALOG_SUCCESS = "sdk_install_wallet_dialog_success"

    const val INSTALL_WALLET_DIALOG_FLOW = "install_wallet_dialog"
}

object SdkInstallWalletDialogLabels {
    const val WALLET_INSTALL_ACTION = "wallet_install_action"
    const val BACK_BUTTON = "back_button"
    const val CANCEL = "cancel"
    const val INSTALL = "install"
    const val SOURCE = "source"
}

@Suppress("MagicNumber")
enum class SdkInstallWalletDialogProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    WALLET_INSTALL_ACTION_FROM_DIALOG_ACTION(WALLET_INSTALL_ACTION, SDK_INSTALL_WALLET_DIALOG_ACTION, 900),

    SOURCE_FROM_DIALOG_DOWNLOAD_WALLET_FALLBACK(SOURCE, SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_FALLBACK, 910),
}
