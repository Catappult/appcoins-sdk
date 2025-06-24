package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_RESULT
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogLabels.ACTION
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkLaunchAppUpdateDialogEvents {

    class SdkLaunchAppUpdateDialogRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_APP_UPDATE_DIALOG_REQUEST,
            mutableMapOf(),
            LAUNCH_APP_UPDATE_DIALOG_FLOW,
            1
        )

    class SdkLaunchAppUpdateDialogAction(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.CLICK,
            SDK_LAUNCH_APP_UPDATE_DIALOG_ACTION,
            data,
            LAUNCH_APP_UPDATE_DIALOG_FLOW,
            1
        )

    const val SDK_LAUNCH_APP_UPDATE_DIALOG_REQUEST = "sdk_launch_app_update_dialog_request"
    const val SDK_LAUNCH_APP_UPDATE_DIALOG_ACTION = "sdk_launch_app_update_dialog_action"

    const val LAUNCH_APP_UPDATE_DIALOG_FLOW = "launch_app_update_dialog"
}

object SdkLaunchAppUpdateDialogLabels {
    const val ACTION = "action"
    const val BACK_BUTTON = "back_button"
    const val CLOSE = "close"
    const val UPDATE = "update"
}

@Suppress("MagicNumber")
enum class SdkLaunchAppUpdateDialogProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
) : Property {
    ACTION_FROM_APP_UPDATE_AVAILABLE(ACTION, SDK_APP_UPDATE_AVAILABLE_RESULT, 1100),
}
