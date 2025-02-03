package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkLaunchAppUpdateStoreEvents {

    class SdkLaunchAppUpdateStoreRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_APP_UPDATE_STORE_REQUEST,
            hashMapOf(),
            LAUNCH_APP_UPDATE_STORE_FLOW,
            1
        )

    class SdkLaunchAppUpdateStoreResult(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_APP_UPDATE_STORE_RESULT,
            data,
            LAUNCH_APP_UPDATE_STORE_FLOW,
            1
        )

    class SdkLaunchAppUpdateStoreDeeplinkFailure(data: HashMap<String, String>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_APP_UPDATE_STORE_DEEPLINK_FAILURE,
            data,
            LAUNCH_APP_UPDATE_STORE_FLOW,
            1
        )

    private const val SDK_LAUNCH_APP_UPDATE_STORE_REQUEST = "sdk_launch_app_update_store_request"
    private const val SDK_LAUNCH_APP_UPDATE_STORE_RESULT = "sdk_launch_app_update_store_result"
    private const val SDK_LAUNCH_APP_UPDATE_STORE_DEEPLINK_FAILURE = "sdk_launch_app_update_store_deeplink_failure"

    private const val LAUNCH_APP_UPDATE_STORE_FLOW = "launch_app_update_store"
}