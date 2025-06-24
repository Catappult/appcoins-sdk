package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_RESULT
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableLabels.RESULT
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

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

    const val SDK_APP_UPDATE_AVAILABLE_REQUEST = "sdk_app_update_available_request"
    const val SDK_APP_UPDATE_AVAILABLE_RESULT = "sdk_app_update_available_result"
    const val SDK_APP_UPDATE_AVAILABLE_MAIN_THREAD_FAILURE = "sdk_app_update_available_main_thread_failure"
    const val SDK_APP_UPDATE_AVAILABLE_FAILURE_TO_OBTAIN_RESULT =
        "sdk_app_update_available_failure_to_obtain_result"

    const val APP_UPDATE_AVAILABLE_FLOW = "app_update_available"
}

object SdkAppUpdateAvailableLabels {
    const val RESULT = "result"
}

@Suppress("MagicNumber")
enum class SdkAppUpdateAvailableProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    RESULT_FROM_APP_UPDATE_AVAILABLE(RESULT, SDK_APP_UPDATE_AVAILABLE_RESULT, 100, true),
}
