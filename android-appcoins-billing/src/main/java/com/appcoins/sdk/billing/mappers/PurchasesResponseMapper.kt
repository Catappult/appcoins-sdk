package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class PurchaseResponseMapper {
    fun map(response: RequestResponse): PurchasesResponse {

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError("Failed to obtain Purchase Response. ResponseCode: ${response.responseCode} | Cause: ${response.exception}")
            return PurchasesResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)

            val items = mutableListOf<Purchase>()

            responseJSONObject.optJSONArray("items")?.let { itemsJsonArray ->
                try {
                    for (i in 0 until itemsJsonArray.length()) {
                        runCatching {
                            val itemJson = itemsJsonArray.getJSONObject(i)

                            val uid = itemJson.optString("uid")
                            val sku = itemJson.optString("sku")
                            val state = itemJson.optString("state")
                            val orderUid =
                                itemJson.optString("order_uid")
                            val payload = itemJson.optString("payload").takeIf { it.isNotEmpty() }
                            val created = itemJson.optString("created")

                            val verificationJson = itemJson.getJSONObject("verification")
                            val verification =
                                Verification(
                                    type = verificationJson.optString("type"),
                                    data = verificationJson.optString("data"),
                                    signature = verificationJson.optString("signature"),
                                )

                            items.add(
                                Purchase(
                                    uid = uid,
                                    sku = sku,
                                    state = state,
                                    orderUid = orderUid,
                                    payload = payload,
                                    created = created,
                                    verification = verification
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    logError("There was a an error mapping the Purchase response: $e")
                }
            }

            return PurchasesResponse(
                responseCode = response.responseCode,
                purchases = items
            )
        }.getOrElse {
            logError("There was a an error mapping the List of Purchases response: " + Exception(it))
            return PurchasesResponse(response.responseCode)
        }
    }
}

data class PurchasesResponse(
    val responseCode: Int,
    val purchases: List<Purchase> = emptyList()
)

data class Purchase(
    val uid: String,
    val sku: String,
    val state: String,
    val orderUid: String,
    val payload: String?,
    val created: String,
    val verification: Verification
)

data class Verification(
    val type: String,
    val data: String,
    val signature: String
)
