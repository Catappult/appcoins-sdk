package com.appcoins.sdk.core.analytics.events

import com.appcoins.sdk.core.analytics.manager.AnalyticsManager

object SdkSystemInformationEvents {

    class SdkDoNotKeepActivitiesActive :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_DO_NOT_KEEP_ACTIVITIES_ACTIVE,
            mutableMapOf(),
            SYSTEM_INFORMATION_FLOW,
            1
        )

    const val SDK_DO_NOT_KEEP_ACTIVITIES_ACTIVE = "sdk_do_not_keep_activities_active"

    const val SYSTEM_INFORMATION_FLOW = "system_information"
}
