package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.AppcV2
import com.appcoins.sdk.billing.PriceV2
import com.appcoins.sdk.billing.SkuDetailsV2
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class SkuDetailsResponseMapper {
    // TODO: Remove the Defaults and check docs to handle correctly the nullables or not nullables.
    fun map(response: RequestResponse): SkuDetailsResponse {

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError("Failed to obtain Sku Details Response. ResponseCode: ${response.responseCode} | Cause: ${response.exception}")
            return SkuDetailsResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val listOfItems = arrayListOf<SkuDetailsV2>()
            responseJSONObject.optJSONArray("items")?.let { listOfItemsJson ->
                for (i in 0 until listOfItemsJson.length()) {
                    val jsonObjectItem = listOfItemsJson.optJSONObject(i)

                    val sku = jsonObjectItem.optString("sku").takeIf { it.isNotEmpty() }
                    val title = jsonObjectItem.optString("title").takeIf { it.isNotEmpty() }
                    val description =
                        jsonObjectItem.optString("description").takeIf { it.isNotEmpty() } ?: ""

                    val priceJson = jsonObjectItem.optJSONObject("price")
                    val price = priceJson?.let { price ->
                        val currency = price.optString("currency").takeIf { it.isNotEmpty() }
                        val label = price.optString("label").takeIf { it.isNotEmpty() }
                        val symbol = price.optString("symbol").takeIf { it.isNotEmpty() }
                        val micros = price.optDouble("micros")

                        val appcJson = price.optJSONObject("appc")
                        val appc = appcJson?.let { appc ->
                            AppcV2(
                                label = appc.optString("label").takeIf { it.isNotEmpty() } ?: "",
                                micros = appc.optDouble("micros")
                            )
                        }

                        PriceV2(
                            currency = currency ?: "",
                            label = label ?: "",
                            symbol = symbol ?: "",
                            micros = micros,
                            appc = appc ?: AppcV2("", 0.0)
                        )
                    }

                    val skuDetails = SkuDetailsV2(
                        sku = sku ?: "",
                        title = title ?: "",
                        description = description,
                        price = price ?: PriceV2(
                            "",
                            "",
                            "",
                            0.0,
                            AppcV2("", 0.0)
                        )
                    )

                    listOfItems.add(skuDetails)
                }
            }

            return SkuDetailsResponse(
                responseCode = response.responseCode,
                items = listOfItems
            )
        }.getOrElse {
            logError("There was a an error mapping the response.", Exception(it))
            return SkuDetailsResponse(response.responseCode)
        }
    }
}

data class SkuDetailsResponse(
    val responseCode: Int,
    val items: List<SkuDetailsV2> = emptyList()
)
