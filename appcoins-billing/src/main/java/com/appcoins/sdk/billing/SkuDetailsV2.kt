package com.appcoins.sdk.billing

import org.json.JSONObject

data class PriceV2(
    val currency: String,
    val label: String,
    val symbol: String,
    val micros: Double,
    val appc: AppcV2,
)

data class AppcV2(
    val label: String,
    val micros: Double,
)

data class SkuDetailsV2(
    val sku: String,
    val title: String,
    val description: String? = null,
    val price: PriceV2,
) {
    fun toSkuDetails(type: String = "INAPP"): SkuDetails =
        SkuDetails(
            type,
            sku,
            type,
            price.label,
            price.micros.toLong(),
            price.currency,
            price.appc.label,
            price.appc.micros.toLong(),
            "APPC",
            price.label,
            price.micros.toLong(),
            price.currency,
            title,
            description
        )

    fun toSkuDetailsResponseString(): String =
        JSONObject().apply {
            put("productId", sku)
            put("type", "INAPP")
            put("price", price.label)
            put("price_currency_code", price.currency)
            put("price_amount_micros", price.micros)
            put("appc_price", price.appc.label)
            put("appc_price_currency_code", "APPC")
            put("appc_price_amount_micros", price.appc.micros)
            put("fiat_price", price.label)
            put("fiat_price_currency_code", price.currency)
            put("fiat_price_amount_micros", price.micros)
            put("title", title)
            description?.let { put("description", it) }
        }.toString()
}
