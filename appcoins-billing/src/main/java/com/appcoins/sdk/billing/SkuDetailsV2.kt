package com.appcoins.sdk.billing

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

@Serializable
data class Price(
    val currency: String,
    val value: String,
    val micros: Int,
    val appc: Appc
)

@Serializable
data class Appc(
    val value: String,
    val micros: Int,
)

@Serializable
data class SkuDetailsV2(
    val sku: String,
    val title: String,
    val description: String? = null,
    val price: Price
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJsonObject(jsonObject: JSONObject): SkuDetailsV2 =
            json.decodeFromString(jsonObject.toString())
    }
}