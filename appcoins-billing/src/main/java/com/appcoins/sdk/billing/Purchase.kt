package com.appcoins.sdk.billing

/**
 * Represents an in-app billing purchase.
 */
class Purchase(
    val accountIdentifiers: AccountIdentifiers?,
    val developerPayload: String?,
    val orderId: String,
    val originalJson: String,
    val packageName: String,
    val products: List<String>,
    val purchaseState: Int,
    val purchaseTime: Long,
    val purchaseToken: String,
    val signature: String,
    val isAutoRenewing: Boolean
)
