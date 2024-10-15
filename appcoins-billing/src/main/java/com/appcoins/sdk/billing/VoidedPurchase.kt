package com.appcoins.sdk.billing

/**
 * Represents an in-app voided purchase.
 * @param kind Type of voided purchase object.
 * @param purchaseToken Purchase token for the Purchase used internally on Aptoide Services.
 * @param orderId Order ID for the Purchase used internally on Aptoide Services.
 * @param purchaseTimeMillis Time in millis for when the Purchase was made.
 * @param voidedTimeMillis Time in millis for when the Purchase was voided.
 * @param voidedSource Source that Voided the Purchase.
 * Possible values are:
 * 0 - Aptoide.
 * @param voidedReason Reason of the Voiding of the Purchase.
 * Possible values are:
 * 0 - REFUNDED
 * 7 - CHARGEDBACK
 * @param voidedQuantity The voided quantity.
 */
class VoidedPurchase(
    val kind: String,
    val purchaseToken: String,
    val orderId: String,
    val purchaseTimeMillis: Long,
    val voidedTimeMillis: Long,
    val voidedSource: Int,
    val voidedReason: Int,
    val voidedQuantity: Double,
)
