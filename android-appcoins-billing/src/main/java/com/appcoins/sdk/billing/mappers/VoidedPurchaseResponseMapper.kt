package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class VoidedPurchaseResponseMapper {
    fun map(response: RequestResponse): VoidedPurchaseResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Voided Purchase Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return VoidedPurchaseResponse(response.responseCode)
        }

        runCatching {
            try {
                val responseJSONObject = JSONObject(response.response)

                responseJSONObject.optJSONArray("items")?.let { itemsJsonArray ->
                    val itemJson = itemsJsonArray.getJSONObject(0)

                    val kind = itemJson.getString("kind")
                    val purchaseToken = itemJson.getString("purchaseToken")
                    val orderId = itemJson.getString("orderId")
                    val purchaseTimeMillis = itemJson.getLong("purchaseTimeMillis")
                    val voidedTimeMillis = itemJson.getLong("voidedTimeMillis")
                    val voidedSource = itemJson.getInt("voidedSource")
                    val voidedReason = itemJson.getInt("voidedReason")

                    return VoidedPurchaseResponse(
                        responseCode = response.responseCode,
                        voidedPurchase = VoidedPurchase(
                            kind = kind,
                            purchaseToken = purchaseToken,
                            orderId = orderId,
                            purchaseTimeMillis = purchaseTimeMillis,
                            voidedTimeMillis = voidedTimeMillis,
                            voidedSource = voidedSource,
                            voidedReason = voidedReason,
                        )
                    )
                }
            } catch (e: Exception) {
                logError("There was a an error mapping the Voided Purchase response: $e")
            }

            return VoidedPurchaseResponse(response.responseCode)
        }.getOrElse {
            logError("There was a an error mapping the Voided Purchase response: " + Exception(it))
            return VoidedPurchaseResponse(response.responseCode)
        }
    }
}

data class VoidedPurchaseResponse(
    val responseCode: Int,
    val voidedPurchase: VoidedPurchase? = null
)
