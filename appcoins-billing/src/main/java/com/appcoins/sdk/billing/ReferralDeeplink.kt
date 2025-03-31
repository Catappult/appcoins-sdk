package com.appcoins.sdk.billing

/**
 * @param responseCode with the possible values of [ResponseCode].
 * @param storeDeeplink which is the Deeplink for the Application Store.
 * @param fallbackDeeplink which is the Web Link for a Fallback Store in
 * case the storeDeeplink isn't installed in the Device.
 */
class ReferralDeeplink(
    val responseCode: ResponseCode,
    val storeDeeplink: String? = null,
    val fallbackDeeplink: String? = null
)
