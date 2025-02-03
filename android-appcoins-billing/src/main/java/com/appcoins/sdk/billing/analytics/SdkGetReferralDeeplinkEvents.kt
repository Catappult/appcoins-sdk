package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager

object SdkGetReferralDeeplinkEvents {

    class SdkGetReferralDeeplinkRequest :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_GET_REFERRAL_DEEPLINK_REQUEST,
            mutableMapOf(),
            GET_REFERRAL_DEEPLINK_FLOW,
            1
        )

    class SdkGetReferralDeeplinkResult(data: MutableMap<String, Any>) :
        AnalyticsEvent(
            AnalyticsManager.Action.IMPRESSION,
            SDK_GET_REFERRAL_DEEPLINK_RESULT,
            data,
            GET_REFERRAL_DEEPLINK_FLOW,
            1
        )

    class SdkGetReferralDeeplinkMainThreadFailure :
        AnalyticsEvent(
            AnalyticsManager.Action.ERROR,
            SDK_GET_REFERRAL_DEEPLINK_MAIN_THREAD_FAILURE,
            mutableMapOf(),
            GET_REFERRAL_DEEPLINK_FLOW,
            1
        )

    private const val SDK_GET_REFERRAL_DEEPLINK_REQUEST = "sdk_referral_deeplink_request"
    private const val SDK_GET_REFERRAL_DEEPLINK_RESULT = "sdk_referral_deeplink_result"
    private const val SDK_GET_REFERRAL_DEEPLINK_MAIN_THREAD_FAILURE = "sdk_referral_deeplink_main_thread_failure"

    private const val GET_REFERRAL_DEEPLINK_FLOW = "get_referral_deeplink"
}