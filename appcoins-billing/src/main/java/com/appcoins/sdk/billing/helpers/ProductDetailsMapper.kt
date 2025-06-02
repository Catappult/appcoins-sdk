package com.appcoins.sdk.billing.helpers

import com.appcoins.sdk.billing.ProductDetails
import com.appcoins.sdk.billing.SkuDetails

class ProductDetailsMapper {
    internal fun mapSkuDetailsToProductDetails(skuDetailsList: List<SkuDetails>): List<ProductDetails> =
        skuDetailsList.map {
            val oneTimePurchaseOfferDetails = mapSkuDetailsToOneTimePurchaseOfferDetails(it)
            val subscriptionOfferDetails = mapSkuDetailsToSubscriptionOfferDetails(it)

            ProductDetails(
                it.sku,
                it.type,
                it.title,
                it.description,
                oneTimePurchaseOfferDetails,
                subscriptionOfferDetails
            )
        }

    private fun mapSkuDetailsToOneTimePurchaseOfferDetails(
        skuDetails: SkuDetails
    ): ProductDetails.OneTimePurchaseOfferDetails? {
        return if (skuDetails.type.equals("inapp", true)) {
            ProductDetails.OneTimePurchaseOfferDetails(
                skuDetails.price,
                skuDetails.priceAmountMicros,
                skuDetails.priceCurrencyCode,
                skuDetails.appcPrice,
                skuDetails.appcPriceAmountMicros,
                skuDetails.appcPriceCurrencyCode,
                skuDetails.fiatPrice,
                skuDetails.fiatPriceAmountMicros,
                skuDetails.fiatPriceCurrencyCode,
            )
        } else {
            null
        }
    }

    private fun mapSkuDetailsToSubscriptionOfferDetails(
        skuDetails: SkuDetails
    ): List<ProductDetails.SubscriptionOfferDetails>? {
        return if (skuDetails.type.equals("subs", true)) {
            listOf(
                ProductDetails.SubscriptionOfferDetails(
                    ProductDetails.PricingPhases(
                        listOf(
                            ProductDetails.PricingPhase(
                                skuDetails.period ?: "",
                                skuDetails.price,
                                skuDetails.priceAmountMicros,
                                skuDetails.priceCurrencyCode,
                                skuDetails.appcPrice,
                                skuDetails.appcPriceAmountMicros,
                                skuDetails.appcPriceCurrencyCode,
                                skuDetails.fiatPrice,
                                skuDetails.fiatPriceAmountMicros,
                                skuDetails.fiatPriceCurrencyCode,
                            ),
                        )
                    ),
                    ProductDetails.TrialDetails(
                        skuDetails.trialPeriod ?: "",
                        skuDetails.trialPeriodEndDate ?: ""
                    )
                )
            )
        } else {
            null
        }
    }
}
