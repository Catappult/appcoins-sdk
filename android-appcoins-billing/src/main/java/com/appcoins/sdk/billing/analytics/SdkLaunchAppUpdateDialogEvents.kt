package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

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
    const val SDK_LAUNCH_APP_UPDATE_DIALOG_DEEPLINK_FAILURE = "sdk_launch_app_update_dialog_deeplink_failure"

    private const val LAUNCH_APP_UPDATE_DIALOG_FLOW = "launch_app_update_dialog"
}

object SdkLaunchAppUpdateDialogLabels {
    const val ACTION = "action"
    const val BACK_BUTTON = "back_button"
    const val CLOSE = "close"
    const val UPDATE = "update"
}
