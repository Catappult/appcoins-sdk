package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkAppUpdateAvailableEvents {

    class SdkAppUpdateAvailableRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_APP_UPDATE_AVAILABLE_REQUEST,
            mutableMapOf(),
            APP_UPDATE_AVAILABLE_FLOW,
            1
        )

    class SdkAppUpdateAvailableResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_APP_UPDATE_AVAILABLE_RESULT,
            data,
            APP_UPDATE_AVAILABLE_FLOW,
            1
        )

    class SdkAppUpdateAvailableMainThreadFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_APP_UPDATE_AVAILABLE_MAIN_THREAD_FAILURE,
            mutableMapOf(),
            APP_UPDATE_AVAILABLE_FLOW,
            1
        )

    class SdkAppUpdateAvailableFailureToObtainResult :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_APP_UPDATE_AVAILABLE_FAILURE_TO_OBTAIN_RESULT,
            mutableMapOf(),
            APP_UPDATE_AVAILABLE_FLOW,
            1
        )

    private const val SDK_APP_UPDATE_AVAILABLE_REQUEST = "sdk_app_update_available_request"
    private const val SDK_APP_UPDATE_AVAILABLE_RESULT = "sdk_app_update_available_result"
    private const val SDK_APP_UPDATE_AVAILABLE_MAIN_THREAD_FAILURE = "sdk_app_update_available_main_thread_failure"
    private const val SDK_APP_UPDATE_AVAILABLE_FAILURE_TO_OBTAIN_RESULT =
        "sdk_app_update_available_failure_to_obtain_result"

    private const val APP_UPDATE_AVAILABLE_FLOW = "app_update_available"
}