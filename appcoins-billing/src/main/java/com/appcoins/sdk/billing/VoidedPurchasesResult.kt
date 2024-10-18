package com.appcoins.sdk.billing

/**
 * @param voidedPurchases List containing the Voided Purchases.
 * @param responseCode Response code of the Voided Purchases request result
 * Possible values are in the .
 */
data class VoidedPurchasesResult(
    var voidedPurchases: List<VoidedPurchase>,
    val responseCode: Int
)
