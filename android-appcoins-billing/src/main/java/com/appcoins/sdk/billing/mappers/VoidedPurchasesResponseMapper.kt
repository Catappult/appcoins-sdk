package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class VoidedPurchasesResponseMapper {
    fun map(response: RequestResponse): VoidedPurchasesResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Voided Purchases Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return VoidedPurchasesResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)

            val items = mutableListOf<VoidedPurchase>()

            responseJSONObject.optJSONArray("items")?.let { itemsJsonArray ->
                try {
                    for (i in 0 until itemsJsonArray.length()) {
                        runCatching {
                            val itemJson = itemsJsonArray.getJSONObject(i)

                            val kind = itemJson.getString("kind")
                            val purchaseToken = itemJson.getString("purchaseToken")
                            val orderId = itemJson.getString("orderId")
                            val purchaseTimeMillis = itemJson.getLong("purchaseTimeMillis")
                            val voidedTimeMillis = itemJson.getLong("voidedTimeMillis")
                            val voidedSource = itemJson.getInt("voidedSource")
                            val voidedReason = itemJson.getInt("voidedReason")
                            val voidedQuantity =
                                if (itemJson.has("voidedQuantity")) {
                                    itemJson.optDouble("voidedQuantity")
                                } else {
                                    0.0
                                }

                            items.add(
                                VoidedPurchase(
                                    kind = kind,
                                    purchaseToken = purchaseToken,
                                    orderId = orderId,
                                    purchaseTimeMillis = purchaseTimeMillis,
                                    voidedTimeMillis = voidedTimeMillis,
                                    voidedSource = voidedSource,
                                    voidedReason = voidedReason,
                                    voidedQuantity = voidedQuantity,
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    logError("There was a an error mapping the Voided Purchase response: $e")
                }
            }

            return VoidedPurchasesResponse(
                responseCode = response.responseCode,
                voidedPurchases = items
            )
        }.getOrElse {
            logError("There was a an error mapping the List of Voided Purchases response: " + Exception(it))
            return VoidedPurchasesResponse(response.responseCode)
        }
    }
}

data class VoidedPurchasesResponse(
    val responseCode: Int,
    val voidedPurchases: List<VoidedPurchase> = emptyList()
)

data class VoidedPurchase(
    val kind: String,
    val purchaseToken: String,
    val orderId: String,
    val purchaseTimeMillis: Long,
    val voidedTimeMillis: Long,
    val voidedSource: Int,
    val voidedReason: Int,
    val voidedQuantity: Double,
) {
    fun toJson(): String =
        """{"kind":"$kind","purchaseToken":"$purchaseToken","orderId":"$orderId","purchaseTimeMillis":$purchaseTimeMillis,"voidedTimeMillis":$voidedTimeMillis,"voidedSource":$voidedSource,"voidedReason":$voidedReason,"voidedQuantity":$voidedQuantity}""".trimMargin()
}
