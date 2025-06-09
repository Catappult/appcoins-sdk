package com.appcoins.sdk.billing

class ProductDetails internal constructor(
    val productId: String,
    val productType: String,
    val title: String,
    val description: String? = null,
    val oneTimePurchaseOfferDetails: OneTimePurchaseOfferDetails? = null,
    val subscriptionOfferDetails: List<SubscriptionOfferDetails>? = null
) {
    class OneTimePurchaseOfferDetails internal constructor(
        val formattedPrice: String,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String,
        val appcFormattedPrice: String,
        val appcPriceAmountMicros: Long,
        val appcPriceCurrencyCode: String,
        val fiatFormattedPrice: String,
        val fiatPriceAmountMicros: Long,
        val fiatPriceCurrencyCode: String,
    )

    class SubscriptionOfferDetails internal constructor(
        val pricingPhases: PricingPhases,
        val trialDetails: TrialDetails? = null
    )

    class PricingPhases internal constructor(
        val pricingPhaseList: List<PricingPhase>,
    )

    class PricingPhase internal constructor(
        val billingPeriod: String,
        val formattedPrice: String,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String,
        val appcFormattedPrice: String,
        val appcPriceAmountMicros: Long,
        val appcPriceCurrencyCode: String,
        val fiatFormattedPrice: String,
        val fiatPriceAmountMicros: Long,
        val fiatPriceCurrencyCode: String,
    )

    class TrialDetails internal constructor(
        val period: String,
        val periodEndDate: String,
    )
}
