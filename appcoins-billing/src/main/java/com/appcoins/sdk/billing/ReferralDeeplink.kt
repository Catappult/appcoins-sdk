package com.appcoins.sdk.billing

/**
 * @param billingResult with the possible values of [BillingResponseCode].
 * @param storeDeeplink which is the Deeplink for the Application Store.
 * @param fallbackDeeplink which is the Web Link for a Fallback Store in
 * case the storeDeeplink isn't installed in the Device.
 */
class ReferralDeeplink(
    val billingResult: BillingResult,
    val storeDeeplink: String? = null,
    val fallbackDeeplink: String? = null
)
