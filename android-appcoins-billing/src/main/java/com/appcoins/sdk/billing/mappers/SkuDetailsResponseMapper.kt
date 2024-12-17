package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.AppcV2
import com.appcoins.sdk.billing.PriceV2
import com.appcoins.sdk.billing.SkuDetailsV2
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class SkuDetailsResponseMapper {
    fun map(response: RequestResponse): SkuDetailsResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Sku Details Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return SkuDetailsResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val listOfItems = arrayListOf<SkuDetailsV2>()
            responseJSONObject.optJSONArray("items")?.let { listOfItemsJson ->
                for (i in 0 until listOfItemsJson.length()) {
                    val jsonObjectItem = listOfItemsJson.optJSONObject(i)

                    val sku = jsonObjectItem.getString("sku")
                    val title = jsonObjectItem.getString("title")
                    val description =
                        jsonObjectItem.optString("description").takeIf { it.isNotEmpty() } ?: ""

                    val price =
                        jsonObjectItem.getJSONObject("price").let { price ->
                            val currency = price.getString("currency")
                            val label = price.getString("label")
                            val symbol = price.getString("symbol")
                            val micros = price.getDouble("micros")

                            val appc =
                                price.getJSONObject("appc").let { appc ->
                                    AppcV2(
                                        label = appc.getString("label"),
                                        micros = appc.getDouble("micros")
                                    )
                                }

                            PriceV2(
                                currency = currency,
                                label = label,
                                symbol = symbol,
                                micros = micros,
                                appc = appc
                            )
                        }

                    val skuDetails = SkuDetailsV2(
                        sku = sku,
                        title = title,
                        description = description,
                        price = price
                    )

                    listOfItems.add(skuDetails)
                }
            }

            return SkuDetailsResponse(
                responseCode = response.responseCode,
                items = listOfItems
            )
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            return SkuDetailsResponse(response.responseCode)
        }
    }
}

data class SkuDetailsResponse(
    val responseCode: Int,
    val items: List<SkuDetailsV2> = emptyList()
)
