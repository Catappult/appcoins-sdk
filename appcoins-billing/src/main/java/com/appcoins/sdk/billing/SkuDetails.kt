package com.appcoins.sdk.billing

import java.io.Serializable

class SkuDetails(
    val itemType: String,
    val sku: String,
    val type: String,
    val price: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val appcPrice: String,
    val appcPriceAmountMicros: Long,
    val appcPriceCurrencyCode: String,
    val fiatPrice: String,
    val fiatPriceAmountMicros: Long,
    val fiatPriceCurrencyCode: String,
    val title: String,
    val description: String?
) : Serializable {
    @Suppress("MaximumLineLength", "MaxLineLength")
    override fun toString(): String {
        return ("SkuDetails{itemType='$itemType', sku='$sku', type='$type', price='$price', priceCurrencyCode='$priceCurrencyCode', priceAmountMicros=$priceAmountMicros, appcPrice='$appcPrice', appcPriceCurrencyCode='$appcPriceCurrencyCode', appcPriceAmountMicros=$appcPriceAmountMicros, fiatPrice='$fiatPrice', fiatPriceCurrencyCode='$fiatPriceCurrencyCode', fiatPriceAmountMicros=$fiatPriceAmountMicros, title='$title', description='$description'}")
    }
}