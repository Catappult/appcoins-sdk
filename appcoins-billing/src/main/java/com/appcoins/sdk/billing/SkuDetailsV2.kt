package com.appcoins.sdk.billing

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
}
