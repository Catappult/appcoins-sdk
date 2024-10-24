package com.appcoins.sdk.billing

/**
 * Represents the Purchase Data of a Voided Purchase.
 * @param sku Sku of the Voided Purchase.
 * @param skuType Sku Type of the Voided Purchase (inapp or subs).
 * @param developerPayload Developer Payload of the Voided Purchase.
 */
data class VoidedPurchaseData(
    val sku: String?,
    val skuType: String?,
    val developerPayload: String?,
)
