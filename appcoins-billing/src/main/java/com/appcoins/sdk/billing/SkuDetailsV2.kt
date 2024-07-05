package com.appcoins.sdk.billing

data class Price(
    val currency: String,
    val label: String,
    val micros: Int,
    val appc: Appc,
)

data class Appc(
    val label: String,
    val micros: Int,
)

data class SkuDetailsV2(
    val sku: String,
    val title: String,
    val description: String? = null,
    val price: Price,
)
