package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkLabels.DEEPLINK
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateEvents.SDK_LAUNCH_APP_UPDATE_DEEPLINK_FAILURE
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateEvents.SDK_LAUNCH_APP_UPDATE_RESULT
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.matomo.Property

object SdkLaunchAppUpdateEvents {

    class SdkLaunchAppUpdateResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_LAUNCH_APP_UPDATE_RESULT,
            data,
            LAUNCH_APP_UPDATE_FLOW,
            1
        )

    class SdkLaunchAppUpdateDeeplinkFailure(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_LAUNCH_APP_UPDATE_DEEPLINK_FAILURE,
            data,
            LAUNCH_APP_UPDATE_FLOW,
            1
        )

    const val SDK_LAUNCH_APP_UPDATE_RESULT = "sdk_launch_app_update_result"
    const val SDK_LAUNCH_APP_UPDATE_DEEPLINK_FAILURE = "sdk_launch_app_update_deeplink_failure"

    const val LAUNCH_APP_UPDATE_FLOW = "launch_app_update"
}

object SdkLaunchAppUpdateLabels {
    const val DEEPLINK = "deeplink"
}

@Suppress("MagicNumber")
enum class SdkLaunchAppUpdateProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    DEEPLINK_FROM_APP_UPDATE_RESULT(DEEPLINK, SDK_LAUNCH_APP_UPDATE_RESULT, 1200, true),
    DEEPLINK_FROM_APP_UPDATE_DEEPLINK_FAILURE(DEEPLINK, SDK_LAUNCH_APP_UPDATE_DEEPLINK_FAILURE, 1201, true),
}
