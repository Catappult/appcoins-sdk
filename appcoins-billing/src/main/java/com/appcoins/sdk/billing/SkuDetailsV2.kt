package com.appcoins.sdk.billing

import org.json.JSONObject

data class PriceV2(
    val currency: String,
    val label: String,
    val symbol: String,
    val micros: Double,
    val appc: AppcV2,
    val trial: Trial?,
)

data class AppcV2(
    val label: String,
    val micros: Double,
)

data class Trial(
    val period: String,
    val endDate: String,
)

data class SkuDetailsV2(
    val sku: String,
    val title: String,
    val description: String? = null,
    val price: PriceV2,
    val period: String?,
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
            description,
            period,
            price.trial?.period,
            price.trial?.endDate,
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
            period?.let { put("period", it) }
            price.trial?.period?.let { put("trial_period", it) }
            price.trial?.endDate?.let { put("trial_period_end_date", it) }
        }.toString()
}
