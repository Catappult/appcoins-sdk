package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

object SdkLaunchAppUpdateStoreEvents {

    class SdkLaunchAppUpdateStoreRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_APP_UPDATE_STORE_REQUEST,
            mutableMapOf(),
            LAUNCH_APP_UPDATE_STORE_FLOW,
            1
        )

    const val SDK_LAUNCH_APP_UPDATE_STORE_REQUEST = "sdk_launch_app_update_store_request"

    private const val LAUNCH_APP_UPDATE_STORE_FLOW = "launch_app_update_store"
}
