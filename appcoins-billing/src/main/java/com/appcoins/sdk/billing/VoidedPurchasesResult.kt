package com.appcoins.sdk.billing

/**
 * Result from the SDK when requesting the Voided Purchases.
 * @param voidedPurchases List containing the Voided Purchases.
 * @param responseCode Response code of the Voided Purchases request result. Possible values are:
 * 0 - OK - Result was successful.
 * 2 - SERVICE_UNAVAILABLE - Service is not available.
 * 3 - BILLING_UNAVAILABLE - This billing API version is not supported for the type requested.
 * 5 - DEVELOPER_ERROR - Invalid arguments provided to the API.
 * 6 - ERROR - Fatal error during the API action.
 */
data class VoidedPurchasesResult(
    var voidedPurchases: List<VoidedPurchase>,
    val responseCode: Int
)
